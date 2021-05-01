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
package org.openwebnet4j.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.EnergyManagement;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;

/**
 * Tests for {@link BaseOpenMessage} and subclasses.
 *
 * @author M. Valla - Initial contribution
 * @author Andrea Conte - Energy Management contribution
 * @author G. Cocchi - Thermoregulation contribution
 */
public class MessageTest {

    @Test
    public void testLightingOn() {

        Lighting lm = Lighting.requestTurnOn("789309801#9");
        assertNotNull(lm.getWhere());
        assertEquals("789309801#9", lm.getWhere().value());

        Lighting lightMsg = Lighting.requestTurnOn("0311#4#01");

        assertNotNull(lightMsg);
        assertEquals(Who.LIGHTING, lightMsg.getWho());
        assertNull(lightMsg.getDim());
        assertEquals(Lighting.WHAT.ON, lightMsg.getWhat());
        assertNotNull(lightMsg.getWhere());
        assertTrue(lightMsg.isCommand());
        assertEquals("0311#4#01", lightMsg.getWhere().value());
        assertTrue(lightMsg.isOn());
        assertFalse(lightMsg.isOff());
    }

    @Test
    public void testLightingCommandTranslationAndParams() {
        Lighting lightMsg;
        try {
            lightMsg = (Lighting) BaseOpenMessage.parse("*1*1000#1#1#2#3*0311#4#01##");
            assertNotNull(lightMsg);
            assertEquals(Who.LIGHTING, lightMsg.getWho());
            assertTrue(lightMsg.isCommand());
            assertTrue(lightMsg.isCommandTranslation());
            assertNull(lightMsg.getDim());
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
            assertEquals(Who.AUTOMATION, automMsg.getWho());
            assertTrue(automMsg.isCommand());
            assertTrue(automMsg.isCommandTranslation());
            assertEquals("55", automMsg.getWhere().value());
            assertNull(automMsg.getDim());
            assertEquals(Automation.WHAT.STOP, automMsg.getWhat());
            assertTrue(automMsg.isStop());
            assertFalse(automMsg.isUp());
            System.out.println(automMsg.toStringVerbose());
            // advanced motor actuator
            automMsg = (Automation) BaseOpenMessage.parse("*#2*55*10*10*100*0*0##");
            assertNotNull(automMsg);
            assertEquals(Who.AUTOMATION, automMsg.getWho());
            assertFalse(automMsg.isCommand());
            assertFalse(automMsg.isCommandTranslation());
            assertEquals("55", automMsg.getWhere().value());
            assertEquals(Automation.DIM.SHUTTER_STATUS, automMsg.getDim());
            assertNotNull(automMsg.getDimValues());
            assertEquals(4, automMsg.getDimValues().length);
            assertEquals("10", automMsg.getDimValues()[0]);
            assertEquals("100", automMsg.getDimValues()[1]);
            assertEquals("0", automMsg.getDimValues()[2]);
            assertEquals("0", automMsg.getDimValues()[3]);
            System.out.println(automMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testThermoregulation() {
        Thermoregulation thermoMsg;
        try {
            thermoMsg = (Thermoregulation) BaseOpenMessage.parse("*#4*6*12*1048*3##");
            assertNotNull(thermoMsg);
            assertEquals(Who.THERMOREGULATION, thermoMsg.getWho());
            assertFalse(thermoMsg.isCommand());
            assertEquals("6", thermoMsg.getWhere().value());
            assertEquals(Thermoregulation.DIM.COMPLETE_PROBE_STATUS, thermoMsg.getDim());
            assertNotNull(thermoMsg.getDimValues());
            assertEquals("1048", thermoMsg.getDimValues()[0]);
            // encoding tests
            assertEquals(-4.8, Thermoregulation.parseTemperature(thermoMsg));
            System.out.println(
                    "Temperature: " + Thermoregulation.parseTemperature(thermoMsg) + "°C");
            assertEquals("1214", Thermoregulation.encodeTemperature(-21.4));
            System.out.println(thermoMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    // TODO thestWhereThermo

    @Test
    public void testZigBeeLightingWhere() {
        Lighting lightMsg;
        try {
            lightMsg = (Lighting) BaseOpenMessage.parse("*1*1*702053501#9##");
            assertNotNull(lightMsg);
            assertEquals(Who.LIGHTING, lightMsg.getWho());
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
    public void testMalformedCmdAndDimFrames() {
        String[] wrongFrames = {"*1*a*123##", "**12", "4##", "*1##", "*1*##", "*1**##"};
        for (String frame : wrongFrames) {
            try {
                BaseOpenMessage.parse(frame);
                // if we can parse this message, this test fails
                Assertions.fail("MalformedFrameException not detected: " + frame);
            } catch (FrameException e) {
                System.out.println(
                        "correctly got FrameException for frame: " + frame + ": " + e.getMessage());
                assertTrue(e instanceof MalformedFrameException);
            }
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
        try {
            msg = BaseOpenMessage.parse("*#5*0##");
        } catch (FrameException e) {
            assertTrue(e instanceof UnsupportedFrameException);
        }
        assertNull(msg);
    }

    @Test
    public void testUnsupportedWhat() {
        OpenMessage msg = null;
        try {
            msg = BaseOpenMessage.parse("*2*4*11##");
            BaseOpenMessage bmsg = (BaseOpenMessage) msg;
            System.out.println(bmsg.toStringVerbose());
        } catch (FrameException e) {
            assertTrue(e instanceof UnsupportedFrameException);
        }
    }

    @Test
    public void testDimParams() {
        GatewayMgmt gwMsg;
        try {
            gwMsg = (GatewayMgmt) BaseOpenMessage.parse("*#13**16*1*2*3##");
            assertNotNull(gwMsg);
            assertEquals(Who.GATEWAY_MANAGEMENT, gwMsg.getWho());
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
            assertEquals(Who.GATEWAY_MANAGEMENT, gwMsg.getWho());
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

    @Test
    public void testGatewayMgmt() {
        GatewayMgmt gwMsg = GatewayMgmt.requestMACAddress();
        assertNotNull(gwMsg);
        assertEquals(Who.GATEWAY_MANAGEMENT, gwMsg.getWho());
        assertFalse(gwMsg.isCommand());
        assertEquals(GatewayMgmt.DIM.MAC_ADDRESS, gwMsg.getDim());
        assertNull(gwMsg.getWhere());
        assertNull(gwMsg.getWhat());
    }

    @Test
    public void testEnergyManagerUnit() {
        EnergyManagement energyMsg;
        try {
            energyMsg = (EnergyManagement) BaseOpenMessage.parse("*#18*51*113##");
            assertNotNull(energyMsg);
            assertEquals(Who.ENERGY_MANAGEMENT, energyMsg.getWho());
            assertFalse(energyMsg.isCommand());
            assertEquals("51", energyMsg.getWhere().value());
            assertEquals(EnergyManagement.DIM.ACTIVE_POWER, energyMsg.getDim());
            assertNotNull(energyMsg.getDimValues());
        } catch (FrameException e) {
            System.out.println(e.getMessage());
            Assertions.fail();
        }
    }
}
