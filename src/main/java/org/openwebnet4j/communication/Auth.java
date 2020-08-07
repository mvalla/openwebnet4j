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
package org.openwebnet4j.communication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains helper methods for authenticating to a BUS OpenWebNet gateway using a numeric (OPEN) or alphanumeric
 * (HMAC) password.
 * Encoding algorithm for OPEN numeric password can be found at this link:
 * https://rosettacode.org/wiki/OpenWebNet_Password#JavaScript
 * HMAC authentication algorithm can be found at this Legrand/BTicino link:
 * https://developer.legrand.com/documentation/open-web-net-for-myhome/
 *
 * @author M. Valla - Initial contribution
 *
 */
public class Auth {

    private final Logger logger = LoggerFactory.getLogger(Auth.class);

    /**
     * Convert [0-9] digits string to hex string
     */
    protected static String digitToHex(String digits) {
        String out = "";
        char[] chars = digits.toCharArray();
        for (int i = 0; i < digits.length(); i = i + 4) {
            out = out
                    + Integer.toHexString(
                            Character.getNumericValue(chars[i]) * 10 + Character.getNumericValue(chars[i + 1]))
                    + Integer.toHexString(
                            Character.getNumericValue(chars[i + 2]) * 10 + Character.getNumericValue(chars[i + 3]));
        }
        return out;
    }

    /**
     * Convert hex string to [0-9] digits string
     */
    protected static String hexToDigit(String hexString) {
        String out = "";
        for (char c : hexString.toCharArray()) {
            out = out + Integer.valueOf("" + c, 16) / 10 + Integer.valueOf("" + c, 16) % 10;
        }
        return out;
    }

    /**
     * Convert bytes array to hex string
     */
    protected static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Generate Rb HMAC random hex string from: key=timeMSEC_SINCE_EPOCH using SHA-256
     */
    protected static String calcHmacRb() {
        return calcSHA256("time" + System.currentTimeMillis());
    }

    /**
     * Return SHA-256 hash of the input string
     */
    protected static String calcSHA256(String message) {
        String response = null;
        try {
            MessageDigest md;
            byte[] encodedhash;
            md = MessageDigest.getInstance("SHA-256");
            encodedhash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            response = bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Encodes a numeric OPEN password
     *
     * @param pass numeric password to encode
     * @param nonce received from the gateway
     * @return the encoded password
     */
    protected static String calcOpenPass(final String pass, final String nonce) {
        boolean flag = true;
        int num1 = 0x0;
        int num2 = 0x0;
        int password = Integer.parseInt(pass, 10);

        for (int x = 0; x < nonce.length(); x++) {
            char c = nonce.charAt(x);
            if (c != '0') {
                if (flag) {
                    num2 = password;
                }
                flag = false;
            }
            switch (c) {
                case '1':
                    num1 = num2 & 0xFFFFFF80;
                    num1 = num1 >>> 7;
                    num2 = num2 << 25;
                    num1 = num1 + num2;
                    break;
                case '2':
                    num1 = num2 & 0xFFFFFFF0;
                    num1 = num1 >>> 4;
                    num2 = num2 << 28;
                    num1 = num1 + num2;
                    break;
                case '3':
                    num1 = num2 & 0xFFFFFFF8;
                    num1 = num1 >>> 3;
                    num2 = num2 << 29;
                    num1 = num1 + num2;
                    break;
                case '4':
                    num1 = num2 << 1;
                    num2 = num2 >>> 31;
                    num1 = num1 + num2;
                    break;
                case '5':
                    num1 = num2 << 5;
                    num2 = num2 >>> 27;
                    num1 = num1 + num2;
                    break;
                case '6':
                    num1 = num2 << 12;
                    num2 = num2 >>> 20;
                    num1 = num1 + num2;
                    break;
                case '7':
                    num1 = num2 & 0x0000FF00;
                    num1 = num1 + ((num2 & 0x000000FF) << 24);
                    num1 = num1 + ((num2 & 0x00FF0000) >>> 16);
                    num2 = (num2 & 0xFF000000) >>> 8;
                    num1 = num1 + num2;
                    break;
                case '8':
                    num1 = num2 & 0x0000FFFF;
                    num1 = num1 << 16;
                    num1 = num1 + (num2 >>> 24);
                    num2 = num2 & 0x00FF0000;
                    num2 = num2 >>> 8;
                    num1 = num1 + num2;
                    break;
                case '9':
                    num1 = ~num2;
                    break;
                case '0':
                    num1 = num2;
                    break;
            }
            num2 = num1;
        }
        return Integer.toUnsignedString(num1 >>> 0);
    }
}
