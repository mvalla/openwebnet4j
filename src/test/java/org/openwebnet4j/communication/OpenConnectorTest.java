
/**
 * Copyright (c) 2026 Contributors to the openwebnet4j project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.openwebnet4j.communication;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.openwebnet4j.message.FrameException;

class OpenConnectorTest {

    /**
     * Test implementation of OpenConnector.
     * It simulates CMD connection and command sending without using sockets.
     */
    private static class TestConnector extends OpenConnector {

        private final AtomicInteger openCmdCalls = new AtomicInteger();
        private final AtomicInteger sendCalls = new AtomicInteger();

        private volatile boolean failReconnect;
        private volatile boolean failNextSendAndReconnect;

        private CountDownLatch reconnectEntered;
        private CountDownLatch allowReconnect;

        @Override
        public void openCmdConn() throws OWNException {
            openCmdCalls.incrementAndGet();

            if (reconnectEntered != null) {
                reconnectEntered.countDown();

                try {
                    if (!allowReconnect.await(2, TimeUnit.SECONDS)) {
                        throw new OWNException("Reconnect test timeout");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OWNException("Reconnect interrupted", e);
                }
            }

            if (failReconnect) {
                throw new OWNException("Simulated CMD reconnect failure");
            }

            // Set connected only after successful connection.
            isCmdConnected = true;
        }

        @Override
        public void openMonConn() throws OWNException {
            isMonConnected = true;
        }

        @Override
        protected Response sendCommandSynchInternal(String frame) throws IOException, FrameException {

            sendCalls.incrementAndGet();

            if (failNextSendAndReconnect) {
                failNextSendAndReconnect = false;

                // Simulate BUSConnector behaviour:
                // the current CMD connection fails.
                isCmdConnected = false;

                // BUSConnector immediately attempts an internal reconnect.
                try {
                    openCmdConn();
                } catch (OWNException e) {
                    throw new IOException("Cannot create NEW CMD connection", e);
                }
            }

            return null;
        }

        @Override
        protected void processFrame(String newFrame) {
            // Not needed for these tests.
        }
    }

    @Test
    void shouldSendCommandWithoutReconnectWhenCmdIsConnected() throws Exception {

        TestConnector connector = new TestConnector();
        connector.isCmdConnected = true;

        assertNull(connector.sendCommandSynch("*1*1*1##"));

        assertEquals(0, connector.openCmdCalls.get());
        assertEquals(1, connector.sendCalls.get());
        assertTrue(connector.isCmdConnected());
    }

    @Test
    void shouldReconnectBeforeSendingWhenCmdIsDisconnected() throws Exception {

        TestConnector connector = new TestConnector();

        assertFalse(connector.isCmdConnected());

        assertNull(connector.sendCommandSynch("*1*1*1##"));

        assertEquals(1, connector.openCmdCalls.get());
        assertEquals(1, connector.sendCalls.get());
        assertTrue(connector.isCmdConnected());
    }

    @Test
    void shouldThrowOwnExceptionWhenReconnectFails() {

        TestConnector connector = new TestConnector();
        connector.failReconnect = true;

        assertThrows(OWNException.class, () -> connector.sendCommandSynch("*1*1*1##"));

        assertEquals(1, connector.openCmdCalls.get());
        assertEquals(0, connector.sendCalls.get());
        assertFalse(connector.isCmdConnected());
    }

    @Test
    void shouldReconnectOnNextCommandAfterSendAndReconnectFailure() throws Exception {

        TestConnector connector = new TestConnector();

        connector.isCmdConnected = true;
        connector.isMonConnected = true;
        connector.failNextSendAndReconnect = true;
        connector.failReconnect = true;

        // First command:
        // - sending fails;
        // - CMD is marked disconnected;
        // - BUSConnector-like internal reconnect fails;
        // - OWNException is propagated.
        assertThrows(OWNException.class, () -> connector.sendCommandSynch("*1*1*1##"));

        assertFalse(connector.isCmdConnected());
        assertTrue(connector.isMonConnected());

        assertEquals(1, connector.sendCalls.get());
        assertEquals(1, connector.openCmdCalls.get());

        // Allow the next reconnect attempt to succeed.
        connector.failReconnect = false;

        // Second command must trigger a new connection attempt.
        assertNull(connector.sendCommandSynch("*1*1*2##"));

        assertTrue(connector.isCmdConnected());
        assertTrue(connector.isMonConnected());

        assertEquals(2, connector.openCmdCalls.get());
        assertEquals(2, connector.sendCalls.get());
    }

    @Test
    void shouldNotOpenMultipleCmdConnectionsForConcurrentCommands() throws Exception {

        TestConnector connector = new TestConnector();

        connector.reconnectEntered = new CountDownLatch(1);
        connector.allowReconnect = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Response> first = executor.submit(() -> connector.sendCommandSynch("*1*1*1##"));

            // Wait until the first command enters openCmdConn().
            assertTrue(connector.reconnectEntered.await(2, TimeUnit.SECONDS));

            // Submit a second command while the first reconnect is blocked.
            Future<Response> second = executor.submit(() -> connector.sendCommandSynch("*1*1*2##"));

            // Allow the first reconnect to complete.
            connector.allowReconnect.countDown();

            assertNull(first.get(2, TimeUnit.SECONDS));
            assertNull(second.get(2, TimeUnit.SECONDS));

            // Only the first command should have opened the connection.
            assertEquals(1, connector.openCmdCalls.get());

            // Both commands should have been sent.
            assertEquals(2, connector.sendCalls.get());

            assertTrue(connector.isCmdConnected());

        } finally {
            connector.allowReconnect.countDown();
            executor.shutdownNow();
        }
    }
}
