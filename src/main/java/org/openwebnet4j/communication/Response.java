/**
 * Copyright (c) 2020-2021 Contributors to the openwebnet4j project
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

import java.util.ArrayList;
import java.util.List;
import org.openwebnet4j.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a response to a OpenWebNet request sent to the gateway. Can contain
 * multiple messages (frames), last message should be an ACK/NACK.
 *
 * @author M. Valla - Initial contribution
 */
public class Response {
    private OpenMessage requestMessage;
    private OpenMessage finalResponse = null;
    private final List<OpenMessage> responses = new ArrayList<>();
    boolean isSuccess;

    private final Logger logger = LoggerFactory.getLogger(Response.class);

    /**
     * Creates a {@link Response} object associated to the request message
     *
     * @param request the {@link OpenMessage} request message
     */
    public Response(OpenMessage request) {
        this.requestMessage = request;
    }

    /**
     * Returns the initial request message
     *
     * @return the initial request message
     */
    public OpenMessage getRequest() {
        return requestMessage;
    }

    /**
     * Returns true if the request was successful (the last OpenMessages in the response is an ACK)
     *
     * @return true if the request was successful
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Returns a list of {@link OpenMessage} received as response
     *
     * @return a list of {@link OpenMessage}
     */
    public List<OpenMessage> getResponseMessages() {
        return responses;
    }

    /**
     * Returns the last OpenMessage that has finalised this response
     *
     * @return the last {@link OpenMessage}
     */
    public OpenMessage getFinalResponse() {
        return finalResponse;
    }

    @Override
    public String toString() {
        return "{REQ=" + requestMessage.toString() + "|RESP=" + responses.toString() + "}";
    }

    /**
     * Add a new message received as response
     *
     * @param msg the new message to add
     */
    protected synchronized void addResponse(OpenMessage msg) {
        responses.add(msg);
        logger.debug("{}   <<add   {}", requestMessage, msg);
        logger.debug("now: {}   <<==    {}", requestMessage, getResponseMessages());
        if (msg.isACK() || msg.isNACK()) { // ACK/NACK -> the response is final
            finalResponse = msg;
            if (msg.isACK()) {
                isSuccess = true;
            }
        }
    }

    /**
     * Returns true if an ACK/NACK has been received
     *
     * @return true if an ACK/NACK has been received
     */
    protected boolean hasFinalResponse() {
        return finalResponse != null;
    }

    protected synchronized void waitResponse() {
        if (finalResponse == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalMonitorStateException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug(
                    "REQ={} has already a final response set (={}) -> no need to wait",
                    requestMessage.toString(),
                    finalResponse);
        }
    }

    protected synchronized void responseReady() {
        notify();
    }
}
