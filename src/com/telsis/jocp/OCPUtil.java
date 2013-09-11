/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2010-2013.
 *
 * This Program is free software: you can copy, redistribute and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License or (at your option) any later version.
 *
 * If you modify this Program you must mark it as changed by you and give a relevant date.
 *
 * This Program is published in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * receive a copy of the GNU General Public License along with this program. If not,
 * see <http//www.gnu.org/licenses/>.
 *
 * In making commercial use of this Program you indemnify Telsis Limited and all of its related
 * Companies for any contractual assumptions of liability that may be imposed on Telsis Limited
 * or any of its related Companies.
 *
 */
package com.telsis.jocp;

import com.telsis.jutils.signalling.GenericTelno;
import com.telsis.jutils.signalling.SignallingUtil;
import com.telsis.jutils.signalling.TelnoType;

/**
 * Utility class for the OCP Call Control application. This class contains
 * several utility methods for converting between generic and OCP data types.
 *
 * @author Telsis
 */
public final class OCPUtil {
    /**
     * Creating this class is not currently supported, so this constructor
     * throws an UnsupportedOperationException when called.
     */
    private OCPUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a string of digits into an {@link OCPTelno}. This converts the
     * characters 0-9 as-is, the characters '*#ABCDEF' (case-insensitively)
     * according to the value of <code>permitStarHash</code>, and ignores the
     * separators '-.()'. The presence of any other characters will abort the
     * conversion and result in an empty telno of type UNKNOWN being returned.
     *
     * @param digits
     *            The digits to convert
     * @param typePlan
     *            The type/plan to use if the conversion is successful
     * @param permitStarHash
     *            True if the characters '*#ABCD' should be converted to the
     *            digits 'ABCDEF' and the characters 'EF' rejected; false if the
     *            characters 'ABCDEF' should be converted to the digits 'ABCDEF'
     *            and the characters '*#' rejected
     * @return The OCP Telno
     * @throws IllegalArgumentException
     *             if the string contains more than
     *             {@value com.telsis.jocp.OCPTelno#MAX_TELNO_LENGTH} digits
     */
    public static OCPTelno convertStringToOCPTelno(final String digits,
            final TelnoType typePlan, final boolean permitStarHash)
            throws IllegalArgumentException { // CSIGNORE: RedundantThrows
        return convertGenericTelnoToOCPTelno(SignallingUtil
                .convertStringToGenericTelno(digits, typePlan, permitStarHash));
    }

    /**
     * Converts a {@link GenericTelno} into an {@link OCPTelno}.
     *
     * @param telno
     *            The generic telno to convert
     * @return The OCP Telno
     */
    public static OCPTelno convertGenericTelnoToOCPTelno(
            final GenericTelno telno) {
        byte[] packedTelno = new byte[OCPTelno.TELNO_BYTES_LENGTH];
        byte[] unpackedTelno = telno.getDigits();

        if (unpackedTelno.length > OCPTelno.MAX_TELNO_LENGTH) {
            return new OCPTelno(
                    SignallingUtil.convertTelnoTypeToQ931(TelnoType.UNKNOWN),
                    (byte) 0, packedTelno);
        }
        int outIndex = 0;
        for (int i = 0; i < unpackedTelno.length; i++) {
            packedTelno[outIndex] |= unpackedTelno[i];
            if (i % 2 != 0) {
                outIndex++;
            } else {
                packedTelno[outIndex] <<= 4;    // CSIGNORE: MagicNumber
            }
        }

        return new OCPTelno(
                SignallingUtil.convertTelnoTypeToQ931(telno.getType()),
                (byte) unpackedTelno.length, packedTelno);
    }

    /**
     * Converts an OCP telno into a String of digits. The hex digits A-F will be
     * converted according to the <code>permitStarHash</code> setting.
     *
     * @param ocpTelno
     *            The telno to convert into a string of digits
     * @param permitStarHash
     *            True if the telno digits 'ABCDEF' should be converted to the
     *            characters '*#ABCD'; false if the digits should be converted
     *            to the characters 'ABCDEF'
     * @return A String containing the digits in the telno
     */
    public static String convertOCPTelnoToString(final OCPTelno ocpTelno,
            final boolean permitStarHash) {
        return SignallingUtil.convertGenericTelnoToString(
                convertOCPTelnoToGenericTelno(ocpTelno), permitStarHash);
    }

    /**
     * Converts an OCP telno into a Generic Telno.
     *
     * @param ocpTelno
     *            The telno to convert
     * @return the corresponding generic telno
     */
    public static GenericTelno convertOCPTelnoToGenericTelno(
            final OCPTelno ocpTelno) {
        GenericTelno telno = new GenericTelno();
        telno.setDigits(ocpTelno.getUnpackedDigits());
        telno.setType(SignallingUtil.convertQ931ToTelnoType(
                ocpTelno.getTypePlan()));
        return telno;
    }
}
