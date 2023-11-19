/**
 * Copyright (c) 2020-2023 Contributors to the openwebnet4j project
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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openwebnet4j.message.Alarm;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.CENPlusScenario;
import org.openwebnet4j.message.CENPlusScenario.CENPlusPressure;
import org.openwebnet4j.message.CENPlusScenario.WhatCENPlus;
import org.openwebnet4j.message.CENScenario;
import org.openwebnet4j.message.CENScenario.CENPressure;
import org.openwebnet4j.message.CENScenario.WhatCEN;
import org.openwebnet4j.message.EnergyManagement;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Scenario;
import org.openwebnet4j.message.Thermoregulation;
import org.openwebnet4j.message.Thermoregulation.Function;
import org.openwebnet4j.message.Thermoregulation.OperationMode;
import org.openwebnet4j.message.Thermoregulation.WhatThermo;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;

/**
 * Tests for {@link BaseOpenMessage} and subclasses.
 *
 * @author M. Valla - Initial contribution. Alarm and Scenario contributions. WhereLightAutom tests.
 * @author Andrea Conte - Energy Management contribution
 * @author G. Cocchi - Thermoregulation contribution
 * @author G. Fabiani - Auxiliary contribution
 */
public class MessageTest {

    @Test
    public void testWhereLightAutom() {
        assertThrows(IllegalArgumentException.class, () -> new WhereLightAutom("a"));

        Lighting lightMsg;
        try {
            lightMsg = (Lighting) BaseOpenMessage.parse("*#1*#25#4#01##");
            assertNotNull(lightMsg);
            assertTrue(lightMsg.getWhere() instanceof WhereLightAutom);
            WhereLightAutom wl = (WhereLightAutom) (lightMsg.getWhere());
            assertFalse(wl.isAPL());
            assertEquals(25, wl.getGroup());
            assertEquals("#4#01", wl.getBUSIfc());

            wl = new WhereLightAutom("0"); // GEN
            assertFalse(wl.isAPL());
            assertTrue(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(-1, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("#25"); // GR 25
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertTrue(wl.isGroup());
            assertEquals(-1, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(25, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("00"); // A 0
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertTrue(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(0, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("7"); // A 7
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertTrue(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(7, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("100"); // A 10
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertTrue(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(10, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("0003"); // A 0, PL 3
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(0, wl.getArea());
            assertEquals(3, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("0013"); // A 0, PL 13
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(0, wl.getArea());
            assertEquals(13, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("13"); // A 1, PL 3
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(1, wl.getArea());
            assertEquals(3, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("0113"); // A 1, PL 13
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(1, wl.getArea());
            assertEquals(13, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("1003"); // A 10, PL 3
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(10, wl.getArea());
            assertEquals(3, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("1013"); // A 10, PL 13
            assertTrue(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(10, wl.getArea());
            assertEquals(13, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertNull(wl.getBUSIfc());

            wl = new WhereLightAutom("0#3"); // GEN , local BUS 3
            assertFalse(wl.isAPL());
            assertTrue(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(-1, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertEquals("#3", wl.getBUSIfc());

            wl = new WhereLightAutom("#2#3"); // GR 2, local BUS 3
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertFalse(wl.isArea());
            assertTrue(wl.isGroup());
            assertEquals(-1, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(2, wl.getGroup());
            assertEquals("#3", wl.getBUSIfc());

            wl = new WhereLightAutom("100#3"); // A 10, local BUS 3
            assertFalse(wl.isAPL());
            assertFalse(wl.isGeneral());
            assertTrue(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(10, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertEquals("#3", wl.getBUSIfc());

            wl = new WhereLightAutom("0#4#02"); // GEN , local BUS #4#02
            assertFalse(wl.isAPL());
            assertTrue(wl.isGeneral());
            assertFalse(wl.isArea());
            assertFalse(wl.isGroup());
            assertEquals(-1, wl.getArea());
            assertEquals(-1, wl.getPL());
            assertEquals(-1, wl.getGroup());
            assertEquals("#4#02", wl.getBUSIfc());

            try {
                wl = new WhereLightAutom("10");
                // if we can parse this where, this test fails
                Assertions.fail("IllegalArgumentException not detected: " + wl);
            } catch (Exception e) {
                System.out.println("correctly got IllegalArgumentException for WhereLightAutom 10: " + e.getMessage());
                assertTrue(e instanceof IllegalArgumentException);
            }

            try {
                wl = new WhereLightAutom("101");
                // if we can parse this where, this test fails
                Assertions.fail("IllegalArgumentException not detected: " + wl);
            } catch (Exception e) {
                System.out.println("correctly got IllegalArgumentException for WhereLightAutom 101: " + e.getMessage());
                assertTrue(e instanceof IllegalArgumentException);
            }

        } catch (FrameException e) {
            Assertions.fail();
        }

    }

    @Test
    public void testLightingOn() {

        Lighting lm = Lighting.requestTurnOn("789309801#9");
        assertNotNull(lm.getWhere());
        assertEquals("789309801#9", lm.getWhere().value());

        Lighting lightMsg = Lighting.requestTurnOn("0311#4#01");

        assertNotNull(lightMsg);
        assertEquals(Who.LIGHTING, lightMsg.getWho());
        assertNull(lightMsg.getDim());
        assertEquals(Lighting.WhatLighting.ON, lightMsg.getWhat());
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
            lightMsg = (Lighting) BaseOpenMessage.parse("*1*1000#1#01#2#3*0311#4#01##");
            assertNotNull(lightMsg);
            assertEquals(Who.LIGHTING, lightMsg.getWho());
            assertTrue(lightMsg.isCommand());
            assertTrue(lightMsg.isCommandTranslation());
            assertNull(lightMsg.getDim());
            assertEquals("0311#4#01", lightMsg.getWhere().value());
            assertEquals(Lighting.WhatLighting.ON, lightMsg.getWhat());
            assertTrue(lightMsg.isOn());
            assertFalse(lightMsg.isOff());
            assertNotNull(lightMsg.getWhatParams());
            assertEquals(3, lightMsg.getWhatParams().length);
            assertEquals("01", lightMsg.getWhatParams()[0]);
            assertEquals("2", lightMsg.getWhatParams()[1]);
            assertEquals("3", lightMsg.getWhatParams()[2]);
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
            assertEquals(Automation.WhatAutomation.STOP, automMsg.getWhat());
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
            assertEquals(Automation.DimAutomation.SHUTTER_STATUS, automMsg.getDim());
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
    public void testAuxiliary() {
        Auxiliary auxiliaryMsg;
        try {
            auxiliaryMsg = (Auxiliary) BaseOpenMessage.parse("*9*1*1##");
            assertNotNull(auxiliaryMsg);
            assertEquals(Who.AUX, auxiliaryMsg.getWho());
            assertEquals("1", auxiliaryMsg.getWhere().value());
            assertEquals(Auxiliary.WhatAuxiliary.ON, auxiliaryMsg.getWhat());
            assertTrue(auxiliaryMsg.isCommand());
            assertFalse(auxiliaryMsg.isCommandTranslation());
            System.out.println(auxiliaryMsg.toStringVerbose());

            auxiliaryMsg = Auxiliary.requestTurnOn("2");
            assertNotNull(auxiliaryMsg);
            assertEquals(Who.AUX, auxiliaryMsg.getWho());
            assertEquals("2", auxiliaryMsg.getWhere().value());
            assertEquals(Auxiliary.WhatAuxiliary.ON, auxiliaryMsg.getWhat());
            assertTrue(auxiliaryMsg.isCommand());
            assertTrue(auxiliaryMsg.isOn());
            System.out.println(auxiliaryMsg.toStringVerbose());
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
            assertEquals(Thermoregulation.DimThermo.COMPLETE_PROBE_STATUS, thermoMsg.getDim());
            assertNotNull(thermoMsg.getDimValues());
            assertEquals("1048", thermoMsg.getDimValues()[0]);
            // temperature encoding tests
            assertEquals(-4.8, Thermoregulation.parseTemperature(thermoMsg));
            System.out.println("Temperature: " + Thermoregulation.parseTemperature(thermoMsg) + "°C");
            assertEquals("1214", Thermoregulation.encodeTemperature(-21.4));
            System.out.println(thermoMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testWhereThermo() {
        Thermoregulation thermoMsg;
        try {
            thermoMsg = (Thermoregulation) BaseOpenMessage.parse("*#4*1#1*20*0##");
            assertNotNull(thermoMsg);
            assertTrue(thermoMsg.getWhere() instanceof WhereThermo);
            WhereThermo wt = (WhereThermo) (thermoMsg.getWhere());
            assertEquals(1, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertEquals(1, wt.getActuator());

            thermoMsg = (Thermoregulation) BaseOpenMessage.parse("*4*21*#0#1##");
            assertNotNull(thermoMsg);
            assertTrue(thermoMsg.getWhere() instanceof WhereThermo);
            wt = (WhereThermo) (thermoMsg.getWhere());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertEquals(1, wt.getActuator());

            wt = new WhereThermo("2");
            assertFalse(wt.isCentralUnit());
            assertEquals(2, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("002");
            assertFalse(wt.isCentralUnit());
            assertEquals(2, wt.getZone());
            assertEquals(0, wt.getProbe());
            assertTrue(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("500");
            assertFalse(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(5, wt.getProbe());
            assertTrue(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("202");
            assertFalse(wt.isCentralUnit());
            assertEquals(2, wt.getZone());
            assertEquals(2, wt.getProbe());
            assertTrue(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("0");
            assertFalse(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("#0");
            assertTrue(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("#0#1");
            assertTrue(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(1, wt.getActuator());

            wt = new WhereThermo("#0#2");
            assertTrue(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(2, wt.getActuator());

            wt = new WhereThermo("#1");
            assertFalse(wt.isCentralUnit());
            assertEquals(1, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("#34");
            assertFalse(wt.isCentralUnit());
            assertEquals(34, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(-1, wt.getActuator());

            wt = new WhereThermo("5#8");
            assertFalse(wt.isCentralUnit());
            assertEquals(5, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(8, wt.getActuator());

            wt = new WhereThermo("99#0");
            assertFalse(wt.isCentralUnit());
            assertEquals(99, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(0, wt.getActuator());

            wt = new WhereThermo("0#0");
            assertFalse(wt.isCentralUnit());
            assertEquals(0, wt.getZone());
            assertEquals(-1, wt.getProbe());
            assertFalse(wt.isProbe());
            assertEquals(0, wt.getActuator());

            try {
                wt = new WhereThermo("1#12");
                // if we can parse this where, this test fails
                Assertions.fail("IllegalArgumentException not detected: " + wt);
            } catch (Exception e) {
                System.out.println("correctly got IllegalArgumentException for WhereThermo 1#12: " + e.getMessage());
                assertTrue(e instanceof IllegalArgumentException);
            }

        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testWhatThermo() {
        Thermoregulation thermoMsg;
        try {
            thermoMsg = (Thermoregulation) BaseOpenMessage.parse("*4*2215*#0##");
            WhatThermo wt = (WhatThermo) thermoMsg.getWhat();
            assertEquals(2215, wt.value());
            assertEquals(OperationMode.SCENARIO_15, wt.getMode());
            assertEquals(Function.COOLING, wt.getFunction());

            // basic functions 0,1
            wt = WhatThermo.fromValue(0);
            assertEquals(Function.COOLING, wt.getFunction());
            assertEquals(0, wt.value());
            wt = WhatThermo.fromValue(1);
            assertEquals(Function.HEATING, wt.getFunction());
            assertEquals(1, wt.value());

            // PROTECTION
            wt = WhatThermo.fromValue(302);
            assertEquals(Function.GENERIC, wt.getFunction());
            assertEquals(OperationMode.PROTECTION, wt.getMode());
            assertEquals(302, wt.value());
            assertFalse(WhatThermo.isComplex(wt.getMode().mode()));
            // OFF
            wt = WhatThermo.fromValue(203);
            assertEquals(Function.COOLING, wt.getFunction());
            assertEquals(OperationMode.OFF, wt.getMode());
            assertEquals(203, wt.value());
            assertFalse(WhatThermo.isComplex(wt.getMode().mode()));
            // MANUAL
            wt = WhatThermo.fromValue(110);
            assertEquals(Function.HEATING, wt.getFunction());
            assertEquals(OperationMode.MANUAL, wt.getMode());
            assertEquals(110, wt.value());
            assertFalse(WhatThermo.isComplex(wt.getMode().mode()));
            // SCENARIO
            wt = WhatThermo.fromValue(1202);
            assertEquals(Function.HEATING, wt.getFunction());
            assertEquals(OperationMode.SCENARIO_2, wt.getMode());
            assertEquals("SCENARIO", wt.getMode().mode());
            assertEquals(1202, wt.value());
            assertTrue(WhatThermo.isComplex(wt.getMode().mode()));
            assertEquals(2, wt.getMode().programNumber());
            // WEEKLY
            wt = WhatThermo.fromValue(2102);
            assertEquals(Function.COOLING, wt.getFunction());
            assertEquals(OperationMode.WEEKLY_2, wt.getMode());
            assertEquals("WEEKLY", wt.getMode().mode());
            assertEquals(2102, wt.value());
            assertTrue(WhatThermo.isComplex(wt.getMode().mode()));
            assertEquals(2, wt.getMode().programNumber());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testCEN() {
        CENScenario cenMsg;
        try {
            cenMsg = (CENScenario) BaseOpenMessage.parse("*15*01#3*0001##");
            assertNotNull(cenMsg);
            assertEquals(Who.CEN_SCENARIO_SCHEDULER, cenMsg.getWho());
            assertTrue(cenMsg.isCommand());
            assertFalse(cenMsg.isCommandTranslation());
            assertEquals("0001", cenMsg.getWhere().value());
            assertNull(cenMsg.getDim());
            assertEquals(WhatCEN.BUTTON_01, cenMsg.getWhat());
            assertEquals(1, cenMsg.getButtonNumber());
            assertEquals(CENPressure.EXTENDED_PRESSURE, cenMsg.getButtonPressure());
            System.out.println(cenMsg.toStringVerbose());
            cenMsg = (CENScenario) BaseOpenMessage.parse("*15*02*22##");
            assertNotNull(cenMsg);
            assertEquals(Who.CEN_SCENARIO_SCHEDULER, cenMsg.getWho());
            assertTrue(cenMsg.isCommand());
            assertFalse(cenMsg.isCommandTranslation());
            assertEquals("22", cenMsg.getWhere().value());
            assertNull(cenMsg.getDim());
            assertEquals(WhatCEN.BUTTON_02, cenMsg.getWhat());
            assertEquals(2, cenMsg.getButtonNumber());
            assertEquals(CENPressure.START_PRESSURE, cenMsg.getButtonPressure());
            System.out.println(cenMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testCENPlus() {
        CENPlusScenario cenPlusMsg;
        try {
            cenPlusMsg = (CENPlusScenario) BaseOpenMessage.parse("*25*22#2*22047##");
            assertNotNull(cenPlusMsg);
            assertEquals(Who.CEN_PLUS_SCENARIO_SCHEDULER, cenPlusMsg.getWho());
            assertTrue(cenPlusMsg.isCommand());
            assertFalse(cenPlusMsg.isCommandTranslation());
            assertEquals("22047", cenPlusMsg.getWhere().value());
            assertNull(cenPlusMsg.getDim());
            assertEquals(WhatCENPlus.START_EXT_PRESSURE, cenPlusMsg.getWhat());
            assertEquals(2, cenPlusMsg.getButtonNumber());
            assertEquals(CENPlusPressure.START_EXTENDED_PRESSURE, cenPlusMsg.getButtonPressure());
            System.out.println(cenPlusMsg.toStringVerbose());
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
        String[] wrongFrames = { "*1*a*123##", "**12", "4##", "*1##", "*1*##", "*1**##" };
        for (String frame : wrongFrames) {
            try {
                BaseOpenMessage.parse(frame);
                // if we can parse this message, this test fails
                Assertions.fail("MalformedFrameException not detected: " + frame);
            } catch (FrameException e) {
                System.out.println("correctly got FrameException for frame: " + frame + ": " + e.getMessage());
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
            assertEquals(GatewayMgmt.DimGatewayMgmt.FIRMWARE_VERSION, gwMsg.getDim());
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
            assertEquals(GatewayMgmt.DimGatewayMgmt.FIRMWARE_VERSION, gwMsg.getDim());
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
        assertEquals(GatewayMgmt.DimGatewayMgmt.MAC_ADDRESS, gwMsg.getDim());
        assertNull(gwMsg.getWhere());
        assertNull(gwMsg.getWhat());

        // date time decoding tests
        try {
            gwMsg = (GatewayMgmt) BaseOpenMessage.parse("*#13**22*09*37*30*102*03*01*05*2019##");
            assertNotNull(gwMsg);
            assertEquals(Who.GATEWAY_MANAGEMENT, gwMsg.getWho());
            assertFalse(gwMsg.isCommand());
            assertEquals(GatewayMgmt.DimGatewayMgmt.DATETIME, gwMsg.getDim());
            assertNotNull(gwMsg.getDimValues());
            assertEquals("09", gwMsg.getDimValues()[0]);
            assertEquals(ZonedDateTime.parse("2019-05-01T09:37:30-02:00"), GatewayMgmt.parseDateTime(gwMsg));
            // encode and then parse current date time
            ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            System.out.println("Now: " + now);
            String nowEncoded = GatewayMgmt.toOWNDateTime(now);
            System.out.println("Now OWN encoded: " + nowEncoded);
            gwMsg = (GatewayMgmt) BaseOpenMessage.parse("*#13**22*" + nowEncoded + "##");
            ZonedDateTime nowParsed = GatewayMgmt.parseDateTime(gwMsg);
            System.out.println("Now parsed: " + nowParsed);
            assertTrue(nowParsed.isEqual(now));
            System.out.println(gwMsg.toStringVerbose());
        } catch (FrameException e) {
            Assertions.fail();
        }
    }

    @Test
    public void testEnergyManagerUnit() {
        EnergyManagement energyMsg;
        EnergyManagement currentMonthTotalizerMsg;
        EnergyManagement currentDayTotalizerMsg;
        try {
            energyMsg = (EnergyManagement) BaseOpenMessage.parse("*#18*51*113##");
            assertNotNull(energyMsg);
            assertEquals(Who.ENERGY_MANAGEMENT, energyMsg.getWho());
            assertFalse(energyMsg.isCommand());
            assertEquals("51", energyMsg.getWhere().value());
            assertEquals(EnergyManagement.DimEnergyMgmt.ACTIVE_POWER, energyMsg.getDim());
            assertNotNull(energyMsg.getDimValues());

            currentMonthTotalizerMsg = (EnergyManagement) BaseOpenMessage.parse("*#18*51*53##");
            assertFalse(currentMonthTotalizerMsg.isCommand());
            assertEquals(EnergyManagement.DimEnergyMgmt.PARTIAL_TOTALIZER_CURRENT_MONTH,
                    currentMonthTotalizerMsg.getDim());
            assertNotNull(currentMonthTotalizerMsg.getDimValues());

            currentDayTotalizerMsg = (EnergyManagement) BaseOpenMessage.parse("*#18*51*54##");
            assertFalse(currentDayTotalizerMsg.isCommand());
            assertEquals(EnergyManagement.DimEnergyMgmt.PARTIAL_TOTALIZER_CURRENT_DAY, currentDayTotalizerMsg.getDim());
            assertNotNull(currentDayTotalizerMsg.getDimValues());
        } catch (FrameException e) {
            System.out.println(e.getMessage());
            Assertions.fail();
        }
    }

    @Test
    public void testAlarm() {
        Alarm alarmMsg;
        try {
            alarmMsg = (Alarm) BaseOpenMessage.parse("*#5##");
            assertNotNull(alarmMsg);
            assertEquals(Who.BURGLAR_ALARM, alarmMsg.getWho());
            assertFalse(alarmMsg.isCommand());
            assertNull(alarmMsg.getWhere());

            alarmMsg = (Alarm) BaseOpenMessage.parse("*5*1*##");
            assertNotNull(alarmMsg);
            assertTrue(alarmMsg.isCommand());
            assertNull(alarmMsg.getWhere());
            assertEquals(Alarm.WhatAlarm.SYSTEM_ACTIVE, alarmMsg.getWhat());

            alarmMsg = (Alarm) BaseOpenMessage.parse("*5*11*#2##");
            assertNotNull(alarmMsg);
            assertTrue(alarmMsg.isCommand());
            assertNotNull(alarmMsg.getWhere());
            assertEquals("#2", alarmMsg.getWhere().value());
            assertEquals(2, ((WhereAlarm) alarmMsg.getWhere()).getZone());
            assertEquals(Alarm.WhatAlarm.ZONE_ENGAGED, alarmMsg.getWhat());
        } catch (FrameException e) {
            System.out.println(e.getMessage());
            Assertions.fail();
        }
    }

    @Test
    public void testScenario() {
        Scenario scenarioMsg;
        try {
            scenarioMsg = (Scenario) BaseOpenMessage.parse("*0*14*95##");
            assertNotNull(scenarioMsg);
            assertEquals(Who.SCENARIO, scenarioMsg.getWho());
            assertTrue(scenarioMsg.isCommand());
            assertNotNull(scenarioMsg.getWhere());
            assertEquals("95", scenarioMsg.getWhere().value());
            assertEquals(Scenario.WhatScenario.SCENARIO_14, scenarioMsg.getWhat());

            scenarioMsg = (Scenario) BaseOpenMessage.parse("*0*2*05##");
            assertNotNull(scenarioMsg);
            assertEquals(Who.SCENARIO, scenarioMsg.getWho());
            assertTrue(scenarioMsg.isCommand());
            assertNotNull(scenarioMsg.getWhere());
            assertEquals("05", scenarioMsg.getWhere().value());
            assertEquals(Scenario.WhatScenario.SCENARIO_02, scenarioMsg.getWhat());

            scenarioMsg = (Scenario) BaseOpenMessage.parse("*0*40#5*06##");
            assertNotNull(scenarioMsg);
            assertEquals(Who.SCENARIO, scenarioMsg.getWho());
            assertTrue(scenarioMsg.isCommand());
            assertNotNull(scenarioMsg.getWhere());
            assertEquals("06", scenarioMsg.getWhere().value());

        } catch (FrameException e) {
            System.out.println(e.getMessage());
            Assertions.fail();
        }
    }
}
