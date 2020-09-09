/**
 * Copyright (c) 2020 Contributors to the openwebnet4j project
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
package org.openwebnet4j.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.openwebnet4j.message.WhereZigBee;

/**
 * Tests for {@link BaseOpenMessage} and subclasses.
 *
 * @author M. Valla - Initial contribution
 */

public class MessageTest {

    @Test
    public void testLightingCommandTranslationAndParams() {
        Lighting lightMsg;
        try {
            lightMsg = (Lighting) BaseOpenMessage.parse("*1*1000#1#1#2#3*0311#4#01##");
            assertNotNull(lightMsg);
            assertTrue(lightMsg.isCommand());
            assertTrue(lightMsg.isCommandTranslation());
            assertEquals("0311#4#01", lightMsg.getWhere().value());
            assertEquals(Lighting.WHAT.ON, lightMsg.getWhat());
            assertTrue(lightMsg.isOn());
            assertFalse(lightMsg.isOff());
            assertNotNull(lightMsg.getCommandParams());
            assertEquals(3, lightMsg.getCommandParams().length);
            assertEquals(1, lightMsg.getCommandParams()[0]);
            assertEquals(2, lightMsg.getCommandParams()[1]);
            assertEquals(3, lightMsg.getCommandParams()[2]);
            System.out.println(lightMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testAutomation() {
        Automation automMsg;
        try {
            automMsg = (Automation) BaseOpenMessage.parse("*2*1000#0*55##");
            assertNotNull(automMsg);
            assertTrue(automMsg.isCommand());
            assertTrue(automMsg.isCommandTranslation());
            assertEquals("55", automMsg.getWhere().value());
            assertEquals(Automation.WHAT.STOP, automMsg.getWhat());
            assertTrue(automMsg.isStop());
            assertFalse(automMsg.isUp());
            System.out.println(automMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testZigBeeLightingWhere() {
        Lighting lightMsg;
        try {
            lightMsg = (Lighting) BaseOpenMessage.parse("*1*1*702053501#9##");
            assertNotNull(lightMsg);
            WhereZigBee wz = (WhereZigBee) (lightMsg.getWhere());
            assertEquals("702053501#9", wz.value());
            assertEquals(WhereZigBee.UNIT_01, wz.getUnit());
            assertEquals("7020535", wz.getAddr());
            System.out.println(lightMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }

    }

    @Test
    public void testUnknownUnsupportedWho() {
        OpenMessage msg = null;
        try {
            msg = BaseOpenMessage.parse("*19*1*123##");
        } catch (FrameException e) {
            assertTrue(e instanceof MalformedFrameException);
        }
        try {
            msg = BaseOpenMessage.parse("*3*1*123##");
        } catch (FrameException e) {
            assertTrue(e instanceof UnsupportedFrameException);
        }
        assertNull(msg);
    }

    @Test
    public void testDimParams() {
        GatewayMgmt gwMsg;
        try {
            gwMsg = (GatewayMgmt) BaseOpenMessage.parse("*#13**16*1*2*3##");
            assertNotNull(gwMsg);
            assertFalse(gwMsg.isCommand());
            assertEquals(GatewayMgmt.DIM.FIRMWARE_VERSION, gwMsg.getDim());
            assertNotNull(gwMsg.getDimValues());
            assertEquals(3, gwMsg.getDimValues().length);
            assertEquals("1", gwMsg.getDimValues()[0]);
            assertEquals("2", gwMsg.getDimValues()[1]);
            assertEquals("3", gwMsg.getDimValues()[2]);
            System.out.println(gwMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testDimWritingParamsValues() {
        GatewayMgmt gwMsg;
        try {
            gwMsg = (GatewayMgmt) BaseOpenMessage.parse("*#13**#16#5#4*255*3##");
            // TODO change test frame with an existing one from Energy mgmt
            assertNotNull(gwMsg);
            assertFalse(gwMsg.isCommand());
            assertEquals(GatewayMgmt.DIM.FIRMWARE_VERSION, gwMsg.getDim());
            assertNotNull(gwMsg.getDimParams());
            assertEquals(2, gwMsg.getDimParams().length);
            assertEquals(5, gwMsg.getDimParams()[0]);
            assertEquals(4, gwMsg.getDimParams()[1]);
            assertNotNull(gwMsg.getDimValues());
            assertEquals(2, gwMsg.getDimValues().length);
            assertEquals("255", gwMsg.getDimValues()[0]);
            assertEquals("3", gwMsg.getDimValues()[1]);
            System.out.println(gwMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

}
