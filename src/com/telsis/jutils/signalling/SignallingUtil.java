/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2011-2013.
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
package com.telsis.jutils.signalling;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Utility class for operations involving legacy signalling. This class contains
 * several utility methods for converting between SIP and legacy signalling
 * schemes, plus methods for conversions involving legacy Ocean equipment.
 *
 * @author Telsis
 */
public final class SignallingUtil {
    /** Q.763 Odd/even indicator mask. */
    public static final short Q763_ODD_EVEN_MASK = (short) 0x8000;
    /** Q.763 Nature of address indicator mask. */
    public static final short Q763_NATURE_OF_ADDRESS_MASK = (short) 0x7F00;
    /** Q.763 Number incomplete indicator mask. */
    public static final short Q763_NUMBER_INCOMPLETE_MASK = (short) 0x0080;
    /** Q.763 Numbering plan indicator mask. */
    public static final short Q763_NUMBERING_PLAN_MASK = (short) 0x0070;
    /** Q.763 Address presentation restricted indicator mask. */
    public static final short Q763_PRESENTATION_MASK = (short) 0x000C;
    /** Q.763 Screening indicator mask. */
    public static final short Q763_SCREENING_MASK = (short) 0x0003;

    /** Q.763 Odd/even indicator: even number of digits. */
    public static final short Q763_LENGTH_EVEN = (short) 0x0000;
    /** Q.763 Odd/even indicator: odd number of digits. */
    public static final short Q763_LENGTH_ODD = (short) 0x8000;

    /** Q.763 Nature of address indicator: subscriber number (national use). */
    public static final short Q763_NAI_SUBSCRIBER_NUMBER = (short) 0x0100;
    /** Q.763 Nature of address indicator: unknown (national use). */
    public static final short Q763_NAI_UNKNOWN = (short) 0x0200;
    /** Q.763 Nature of address indicator: national (significant) number). */
    public static final short Q763_NAI_NATIONAL = (short) 0x0300;
    /** Q.763 Nature of address indicator: international number. */
    public static final short Q763_NAI_INTERNATIONAL = (short) 0x0400;
    /** Q.763 Nature of address indicator: PISN specific number (national use). */
    public static final short Q763_NAI_PISN_SPECIFIC = (short) 0x0500;

    /** Q.763 Numbering complete indicator: number complete. */
    public static final short Q763_NUMBER_COMPLETE = (short) 0x0000;
    /** Q.763 Numbering complete indicator: number incomplete. */
    public static final short Q763_NUMBER_INCOMPLETE = (short) 0x0080;

    /** Q.763 Numbering plan indicator: unknown (national use). */
    public static final short Q763_NUM_PLAN_UNKNOWN = (short) 0x0000;
    /** Q.763 Numbering plan indicator: E.164. */
    public static final short Q763_NUM_PLAN_E164 = (short) 0x0010;
    /** Q.763 Numbering plan indicator: X.121 (national use). */
    public static final short Q763_NUM_PLAN_X121 = (short) 0x0030;
    /** Q.763 Numbering plan indicator: F.69 (national use). */
    public static final short Q763_NUM_PLAN_F69 = (short) 0x0040;
    /** Q.763 Numbering plan indicator: private (national use). */
    public static final short Q763_NUM_PLAN_PRIVATE = (short) 0x0050;
    /** Q.763 Numbering plan indicator: reserved for national use. */
    public static final short Q763_NUM_PLAN_NATIONAL = (short) 0x0060;

    /** Q.763 Address presentation restricted indicator: presentation allowed. */
    public static final short Q763_PRESENTATION_ALLOWED = (short) 0x0000;
    /** Q.763 Address presentation restricted indicator: presentation restricted. */
    public static final short Q763_PRESENTATION_RESTRICTED = (short) 0x0004;
    /** Q.763 Address presentation restricted indicator: address not available. */
    public static final short Q763_PRESENTATION_ADDRESS_UNAVAILABLE = (short) 0x0008;

    /** Q.763 Screening indicator: user provided, not verified. */
    public static final short Q763_SCREENING_USER_NOT_VERIFIED = (short) 0x0000;
    /** Q.763 Screening indicator: user provided, verified and passed. */
    public static final short Q763_SCREENING_USER_VERIFIED_PASSED = (short) 0x0001;
    /** Q.763 Screening indicator: user provided, verified and failed. */
    public static final short Q763_SCREENING_USER_VERIFIED_FAILED = (short) 0x0002;
    /** Q.763 Screening indicator: network provided. */
    public static final short Q763_SCREENING_NETWORK = (short) 0x0003;

    /** Q.850 Location: user (U). */
    public static final byte Q850_LOCATION_U = 0x00;
    /** Q.850 Location: private network serving the local user (LPN). */
    public static final byte Q850_LOCATION_LPN = 0x01;
    /** Q.850 Location: public network serving the local user (LN). */
    public static final byte Q850_LOCATION_LN = 0x02;
    /** Q.850 Location: transit network (TN). */
    public static final byte Q850_LOCATION_TN = 0x03;
    /** Q.850 Location: public network serving the remote user (RLN). */
    public static final byte Q850_LOCATION_RLN = 0x04;
    /** Q.850 Location: private network serving the remote user (RPN). */
    public static final byte Q850_LOCATION_RPN = 0x05;
    /** Q.850 Location: international network (INTL). */
    public static final byte Q850_LOCATION_INTL = 0x06;
    /** Q.850 Location: network beyond interworking point (BI). */
    public static final byte Q850_LOCATION_BI = 0x09;

    /** Q.850 Cause: unallocated (unassigned) number. */
    public static final byte Q850_CAUSE_UNALLOCATED_NUMBER = 1;
    /** Q.850 Cause: no route to specified transit network. */
    public static final byte Q850_CAUSE_NO_ROUTE_TO_SPECIFIED_TRANSIT_NETWORK = 2;
    /** Q.850 Cause: no route to destination. */
    public static final byte Q850_CAUSE_NO_ROUTE_TO_DESTINATION = 3;
    /** Q.850 Cause: send special information tone. */
    public static final byte Q850_CAUSE_SEND_SPECIAL_INFORMATION_TONE = 4;
    /** Q.850 Cause: misdialled trunk prefix. */
    public static final byte Q850_CAUSE_MISDIALLED_TRUNK_PREFIX = 5;
    /** Q.850 Cause: channel unacceptable. */
    public static final byte Q850_CAUSE_CHANNEL_UNACCEPTABLE = 6;
    /** Q.850 Cause: call awarded and being delivered in an established channel. */
    public static final byte Q850_CAUSE_CALL_AWARDED_AND_DELIVERED_IN_ESTABLISHED_CHANNEL = 7;
    /** Q.850 Cause: preemption. */
    public static final byte Q850_CAUSE_PREEMPTION = 8;
    /** Q.850 Cause: preemption - circuit reserved for reuse. */
    public static final byte Q850_CAUSE_PREEMPTION_CIRCUIT_RESERVED_FOR_REUSE = 9;
    /** Q.850 Cause: QoR: ported number. */
    public static final byte Q850_CAUSE_QOR_PORTED_NUMBER = 14;
    /** Q.850 Cause: normal call clearing. */
    public static final byte Q850_CAUSE_NORMAL_CALL_CLEARING = 16;
    /** Q.850 Cause: user busy. */
    public static final byte Q850_CAUSE_USER_BUSY = 17;
    /** Q.850 Cause: no user responding. */
    public static final byte Q850_CAUSE_NO_USER_RESPONDING = 18;
    /** Q.850 Cause: no answer from user (user alerted). */
    public static final byte Q850_CAUSE_NO_ANSWER_FROM_USER_USER_ALERTED = 19;
    /** Q.850 Cause: subscriber absent. */
    public static final byte Q850_CAUSE_SUBSCRIBER_ABSENT = 20;
    /** Q.850 Cause: call rejected. */
    public static final byte Q850_CAUSE_CALL_REJECTED = 21;
    /** Q.850 Cause: number changed. */
    public static final byte Q850_CAUSE_NUMBER_CHANGED = 22;
    /** Q.850 Cause: redirection to new destination. */
    public static final byte Q850_CAUSE_REDIRECTION_TO_NEW_DESTINATION = 23;
    /** Q.850 Cause: call rejected due to feature at the destination. */
    public static final byte Q850_CAUSE_CALL_REJECTED_DUE_TO_FEATURE_AT_DESTINATION = 24;
    /** Q.850 Cause: exchange routing error. */
    public static final byte Q850_CAUSE_EXCHANGE_ROUTING_ERROR = 25;
    /** Q.850 Cause: non-selected user clearing. */
    public static final byte Q850_CAUSE_NONSELECTED_USER_CLEARING = 26;
    /** Q.850 Cause: destination out of order. */
    public static final byte Q850_CAUSE_DESTINATION_OUT_OF_ORDER = 27;
    /** Q.850 Cause: invalid number format (address incomplete). */
    public static final byte Q850_CAUSE_INVALID_NUMBER_FORMAT_ADDRESS_INCOMPLETE = 28;
    /** Q.850 Cause: facility rejected. */
    public static final byte Q850_CAUSE_FACILITY_REJECTED = 29;
    /** Q.850 Cause: response to STATUS ENQUIRY. */
    public static final byte Q850_CAUSE_RESPONSE_TO_STATUS_ENQUIRY = 30;
    /** Q.850 Cause: normal unspecified. */
    public static final byte Q850_CAUSE_NORMAL_UNSPECIFIED = 31;
    /** Q.850 Cause: no circuit/channel available. */
    public static final byte Q850_CAUSE_NO_CIRCUIT_CHANNEL_AVAILABLE = 34;
    /** Q.850 Cause: network out of order. */
    public static final byte Q850_CAUSE_NETWORK_OUT_OF_ORDER = 38;
    /** Q.850 Cause: permanent frame mode connection out of service. */
    public static final byte Q850_CAUSE_PERMANENT_FRAME_MODE_CONNECTION_OUT_OF_SERVICE = 39;
    /** Q.850 Cause: permanent frame mode connection operational. */
    public static final byte Q850_CAUSE_PERMANENT_FRAME_MODE_CONNECTION_OPERATIONAL = 40;
    /** Q.850 Cause: temporary failure. */
    public static final byte Q850_CAUSE_TEMPORARY_FAILURE = 41;
    /** Q.850 Cause: switching equipment congestion. */
    public static final byte Q850_CAUSE_SWITCHING_EQUIPMENT_CONGESTION = 42;
    /** Q.850 Cause: access information discarded. */
    public static final byte Q850_CAUSE_ACCESS_INFORMATION_DISCARDED = 43;
    /** Q.850 Cause: requested circuit/channel not available. */
    public static final byte Q850_CAUSE_REQUESTED_CIRCUIT_CHANNEL_NOT_AVAILABLE = 44;
    /** Q.850 Cause: precedence call blocked. */
    public static final byte Q850_CAUSE_PRECEDENCE_CALL_BLOCKED = 46;
    /** Q.850 Cause: resource unavailable unspecified. */
    public static final byte Q850_CAUSE_RESOURCE_UNAVAILABLE_UNSPECIFIED = 47;
    /** Q.850 Cause: quality of service not available. */
    public static final byte Q850_CAUSE_QUALITY_OF_SERVICE_NOT_AVAILABLE = 49;
    /** Q.850 Cause: requested facility not subscribed. */
    public static final byte Q850_CAUSE_REQUESTED_FACILITY_NOT_SUBSCRIBED = 50;
    /** Q.850 Cause: outgoing calls barred within CUG. */
    public static final byte Q850_CAUSE_OUTGOING_CALLS_BARRED_WITHIN_CUG = 53;
    /** Q.850 Cause: incoming calls barred within CUG. */
    public static final byte Q850_CAUSE_INCOMING_CALLS_BARRED_WITHIN_CUG = 55;
    /** Q.850 Cause: bearer capability not authorized. */
    public static final byte Q850_CAUSE_BEARER_CAPABILITY_NOT_AUTHORIZED = 57;
    /** Q.850 Cause: bearer capability not presently available. */
    public static final byte Q850_CAUSE_BEARER_CAPABILITY_NOT_PRESENTLY_AVAILABLE = 58;
    /**
     * Q.850 Cause: inconsistency in designated outgoing access information and
     * subscriber class.
     */
    public static final byte Q850_CAUSE_INCONSISTENCY_IN_OUTGOING_INFORMATION_AND_CLASS = 62;
    /** Q.850 Cause: service or option not available unspecified. */
    public static final byte Q850_CAUSE_SERVICE_OR_OPTION_NOT_AVAILABLE_UNSPECIFIED = 63;
    /** Q.850 Cause: bearer capability not implemented. */
    public static final byte Q850_CAUSE_BEARER_CAPABILITY_NOT_IMPLEMENTED = 65;
    /** Q.850 Cause: channel type not implemented. */
    public static final byte Q850_CAUSE_CHANNEL_TYPE_NOT_IMPLEMENTED = 66;
    /** Q.850 Cause: requested facility not implemented. */
    public static final byte Q850_CAUSE_REQUESTED_FACILITY_NOT_IMPLEMENTED = 69;
    /** Q.850 Cause: only restricted digital information bearer capability is available. */
    public static final byte Q850_CAUSE_ONLY_RESTRICTED_BEARER_CAPABILITY_AVAILABLE = 70;
    /** Q.850 Cause: service or option not implemented unspecified. */
    public static final byte Q850_CAUSE_SERVICE_OR_OPTION_NOT_IMPLEMENTED_UNSPECIFIED = 79;
    /** Q.850 Cause: invalid call reference value. */
    public static final byte Q850_CAUSE_INVALID_CALL_REFERENCE_VALUE = 81;
    /** Q.850 Cause: identified channel does not exist. */
    public static final byte Q850_CAUSE_IDENTIFIED_CHANNEL_DOES_NOT_EXIST = 82;
    /** Q.850 Cause: a suspended call exists but this call identity does not. */
    public static final byte Q850_CAUSE_SUSPENDED_CALL_EXISTS_BUT_THIS_IDENTITY_DOES_NOT = 83;
    /** Q.850 Cause: call identity in use. */
    public static final byte Q850_CAUSE_CALL_IDENTITY_IN_USE = 84;
    /** Q.850 Cause: no call suspended. */
    public static final byte Q850_CAUSE_NO_CALL_SUSPENDED = 85;
    /** Q.850 Cause: call having the requested call identity has been cleared. */
    public static final byte Q850_CAUSE_THE_REQUESTED_CALL_IDENTITY_HAS_BEEN_CLEARED = 86;
    /** Q.850 Cause: user not member of CUG. */
    public static final byte Q850_CAUSE_USER_NOT_MEMBER_OF_CUG = 87;
    /** Q.850 Cause: incompatible destination. */
    public static final byte Q850_CAUSE_INCOMPATIBLE_DESTINATION = 88;
    /** Q.850 Cause: non-existent CUG. */
    public static final byte Q850_CAUSE_NON_EXISTENT_CUG = 90;
    /** Q.850 Cause: invalid transit network selection. */
    public static final byte Q850_CAUSE_INVALID_TRANSIT_NETWORK_SELECTION = 91;
    /** Q.850 Cause: invalid message unspecified. */
    public static final byte Q850_CAUSE_INVALID_MESSAGE_UNSPECIFIED = 95;
    /** Q.850 Cause: mandatory information element is missing. */
    public static final byte Q850_CAUSE_MANDATORY_INFORMATION_ELEMENT_IS_MISSING = 96;
    /** Q.850 Cause: message type non-existent or not implemented. */
    public static final byte Q850_CAUSE_MESSAGE_TYPE_NON_EXISTENT_OR_NOT_IMPLEMENTED = 97;
    /**
     * Q.850 Cause: message not compatible with call state or message type
     * non-existent or not implemented.
     */
    public static final byte Q850_CAUSE_MESSAGE_INCOMPATIBLE_OR_NONEXISTENT_OR_UNIMPLEMENTED = 98;
    /** Q.850 Cause: information element /parameter non-existent or not implemented . */
    public static final byte Q850_CAUSE_INFORMATION_ELEMENT_NON_EXISTENT_OR_NOT_IMPLEMENTED = 99;
    /** Q.850 Cause: invalid information element contents. */
    public static final byte Q850_CAUSE_INVALID_INFORMATION_ELEMENT_CONTENTS = 100;
    /** Q.850 Cause: message not compatible with call state. */
    public static final byte Q850_CAUSE_MESSAGE_NOT_COMPATIBLE_WITH_CALL_STATE = 101;
    /** Q.850 Cause: recovery on timer expiry. */
    public static final byte Q850_CAUSE_RECOVERY_ON_TIMER_EXPIRY = 102;
    /** Q.850 Cause: parameter non-existent or not implemented passed on. */
    public static final byte Q850_CAUSE_PARAMETER_NON_EXISTENT_OR_NOT_IMPLEMENTED_PASSED_ON = 103;
    /** Q.850 Cause: message with unrecognized parameter discarded. */
    public static final byte Q850_CAUSE_MESSAGE_WITH_UNRECOGNIZED_PARAMETER_DISCARDED = 110;
    /** Q.850 Cause: protocol error unspecified. */
    public static final byte Q850_CAUSE_PROTOCOL_ERROR_UNSPECIFIED = 111;
    /** Q.850 Cause: interworking unspecified. */
    public static final byte Q850_CAUSE_INTERWORKING_UNSPECIFIED = 127;

    /** Q.931 Type of number mask. */
    public static final byte Q931_NUM_TYPE_MASK = 0x70;
    /** Q.931 Numbering plan identification mask. */
    public static final byte Q931_NUM_PLAN_MASK = 0x0F;
    /** Q.931 Presentation indicator mask. */
    public static final byte Q931_CLI_PRESENTATION_MASK = 0x60;
    /** Q.931 Screening indicator mask. */
    public static final byte Q931_CLI_SCREENING_MASK = 0x03;

    /** Q.931 Type of number: unknown. */
    public static final byte Q931_NUM_TYPE_UNKNOWN = 0x00;
    /** Q.931 Type of number: international number. */
    public static final byte Q931_NUM_TYPE_INTERNATIONAL = 0x10;
    /** Q.931 Type of number: national number. */
    public static final byte Q931_NUM_TYPE_NATIONAL = 0x20;
    /** Q.931 Type of number: network specific number. */
    public static final byte Q931_NUM_TYPE_NETWORK_SPECIFIC = 0x30;
    /** Q.931 Type of number: subscriber number. */
    public static final byte Q931_NUM_TYPE_SUBSCRIBER = 0x40;
    /** Q.931 Type of number: abbreviated number. */
    public static final byte Q931_NUM_TYPE_ABBREVIATED = 0x60;

    /** Q.931 Numbering plan identification: unknown. */
    public static final byte Q931_NUM_PLAN_UNKNOWN = 0x00;
    /** Q.931 Numbering plan identification: ISDN/telephony numbering plan (E.164). */
    public static final byte Q931_NUM_PLAN_E164 = 0x01;
    /** Q.931 Numbering plan identification: data numbering plan (X.121). */
    public static final byte Q931_NUM_PLAN_DATA = 0x03;
    /** Q.931 Numbering plan identification: telex numbering plan (F.69). */
    public static final byte Q931_NUM_PLAN_TELEX = 0x04;
    /** Q.931 Numbering plan identification: national standard numbering plan. */
    public static final byte Q931_NUM_PLAN_NAT_STD = 0x08;
    /** Q.931 Numbering plan identification: private numbering plan. */
    public static final byte Q931_NUM_PLAN_PRIVATE = 0x09;

    /** Q.931 Presentation indicator: presentation allowed. */
    public static final byte Q931_CLI_PRESENTATION_ALLOWED = 0x00;
    /** Q.931 Presentation indicator: presentation restricted. */
    public static final byte Q931_CLI_PRESENTATION_RESTRICTED = 0x20;
    /** Q.931 Presentation indicator: number not available due to interworking. */
    public static final byte Q931_CLI_NOT_AVAILABLE = 0x40;
    /** The Constant Q931_CLI_PRESENTATION_UNAVAIL_NETWORK. */
    public static final byte Q931_CLI_PRESENTATION_UNAVAIL_NETWORK = 0x60;

    /** Q.931 Screening indicator: user-provided, not screened. */
    public static final byte Q931_CLI_USER_UNSCREENED = 0x00;
    /** Q.931 Screening indicator: user-provided, verified and passed. */
    public static final byte Q931_CLI_USER_VERIFIED_PASS = 0x01;
    /** Q.931 Screening indicator: user-provided, verified and failed. */
    public static final byte Q931_CLI_USER_VERIFIED_FAIL = 0x02;
    /** Q.931 Screening indicator: network provided. */
    public static final byte Q931_CLI_NETWORK = 0x03;

    /** The number of seconds between 1970 and 1980. */
    public static final int NO_SECONDS_1970_1980 = 315532800;

    /** Regex pattern to accept only digits. */
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    /**
     * Creating this class is not currently supported, so this constructor
     * throws an UnsupportedOperationException when called.
     */
    private SignallingUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a string of digits into a {@link GenericTelno}. This converts
     * the characters 0-9 as-is, the characters '*#ABCDEF' (case-insensitively)
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
     * @return The telephone number
     */
    public static GenericTelno convertStringToGenericTelno(final String digits,
            final TelnoType typePlan, final boolean permitStarHash) {
        final GenericTelno telno = new GenericTelno();
        byte[] unpackedDigits = new byte[digits.length()];
        int validDigits = 0;
        boolean valid = true;

        for (int inIndex = 0; inIndex < digits.length() && valid; inIndex++) {
            // CSOFF: MagicNumber
            switch (digits.charAt(inIndex)) {
            case '0':
                unpackedDigits[validDigits++] = 0;
                break;
            case '1':
                unpackedDigits[validDigits++] = 1;
                break;
            case '2':
                unpackedDigits[validDigits++] = 2;
                break;
            case '3':
                unpackedDigits[validDigits++] = 3;
                break;
            case '4':
                unpackedDigits[validDigits++] = 4;
                break;
            case '5':
                unpackedDigits[validDigits++] = 5;
                break;
            case '6':
                unpackedDigits[validDigits++] = 6;
                break;
            case '7':
                unpackedDigits[validDigits++] = 7;
                break;
            case '8':
                unpackedDigits[validDigits++] = 8;
                break;
            case '9':
                unpackedDigits[validDigits++] = 9;
                break;
            case 'a':
            case 'A':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xC;
                } else {
                    unpackedDigits[validDigits++] = 0xA;
                }
                break;
            case 'b':
            case 'B':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xD;
                } else {
                    unpackedDigits[validDigits++] = 0xB;
                }
                break;
            case 'c':
            case 'C':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xE;
                } else {
                    unpackedDigits[validDigits++] = 0xC;
                }
                break;
            case 'd':
            case 'D':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xF;
                } else {
                    unpackedDigits[validDigits++] = 0xD;
                }
                break;
            case 'e':
            case 'E':
                if (permitStarHash) {
                    valid = false;
                } else {
                    unpackedDigits[validDigits++] = 0xE;
                }
                break;
            case 'f':
            case 'F':
                if (permitStarHash) {
                    valid = false;
                } else {
                    unpackedDigits[validDigits++] = 0xF;
                }
                break;
            case '*':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xA;
                } else {
                    valid = false;
                }
                break;
            case '#':
                if (permitStarHash) {
                    unpackedDigits[validDigits++] = 0xB;
                } else {
                    valid = false;
                }
                break;
            case '-':
            case '.':
            case '(':
            case ')':
                // Separators
                break;
            default:
                valid = false;
                break;
            }
            // CSON: MagicNumber
        }

        if (valid) {
            telno.setType(typePlan);
            telno.setDigits(Arrays.copyOf(unpackedDigits, validDigits));
        } else {
            telno.setType(TelnoType.UNKNOWN);
            telno.setDigits(new byte[]{});
        }
        return telno;
    }

    /**
     * Converts a string of digits into the telephone-subscriber part of a tel:
     * or sip: URI. The telno will be decoded as follows:
     * <ul>
     * <li>If the type/plan is International E.164 and the telno contains only
     * the digits 0-9, then it will be returned as a global-number.</li>
     * <li>Otherwise if the telno contains the {@code phoneContextSeparator}
     * digit, then the digits before this will be used as the phone-context
     * descriptor and the digits after this will be used as the local-number.</li>
     * <li>If the phone-context is not a valid global-number or was not found,
     * then it will be replaced with the {@code defaultPhoneContext} (unless
     * that parameter is null, in which case URI generation will fail).</li>
     * </ul>
     *
     * @param digits
     *            The digits to convert
     * @param typePlan
     *            The type/plan to use
     * @param phoneContextParameter
     *            The name of the context parameter (e.g. "phone-context")
     * @param phoneContextSeparator
     *            The character that separates the context and the number
     * @param defaultPhoneContext
     *            The default context. If this is <code>null</code> and the
     *            digits form a local-number without a context then this
     *            function will return null
     * @return The telephone-subscriber part of a tel: URI
     */
    public static String convertStringToTelephoneSubscriber(final String digits,
            final TelnoType typePlan, final String phoneContextParameter,
            final char phoneContextSeparator, final String defaultPhoneContext) {
        return convertStringToTelephoneSubscriber(digits, typePlan, phoneContextParameter,
                phoneContextSeparator, defaultPhoneContext, false);
    }

    /**
     * Converts a string of digits into the telephone-subscriber part of a tel:
     * or sip: URI. The telno will be decoded as follows:
     * <ul>
     * <li>If the type/plan is International E.164 and the telno contains valid
     * digits according to the <code>permitHexDigits</code> parameter, then it
     * will be returned as a global-number.</li>
     * <li>Otherwise if the telno contains the {@code phoneContextSeparator}
     * digit, then the digits before this will be used as the phone-context
     * descriptor and the digits after this will be used as the local-number.</li>
     * <li>If the phone-context is not a valid global-number or was not found,
     * then it will be replaced with the {@code defaultPhoneContext} (unless
     * that parameter is null, in which case URI generation will fail).</li>
     * </ul>
     * Note that if <code>permitHexDigits</code> is <code>true</code>, the
     * type/plan is not International E.164, and the
     * <code>phoneContextSeparator</code> is present in the digits, then this
     * function will fail to extract the correct phone-context and local-number.
     *
     * @param digits
     *            The digits to convert
     * @param typePlan
     *            The type/plan to use
     * @param phoneContextParameter
     *            The name of the context parameter (e.g. "phone-context")
     * @param phoneContextSeparator
     *            The character that separates the context and the number
     * @param defaultPhoneContext
     *            The default context. If this is <code>null</code> and the
     *            digits form a local-number without a context then this
     *            function will return null
     * @param permitHexDigits
     *            Whether or not hex digits are permitted in a global-number
     * @return The telephone-subscriber part of a tel: URI
     */
    public static String convertStringToTelephoneSubscriber(final String digits,
            final TelnoType typePlan, final String phoneContextParameter,
            final char phoneContextSeparator, final String defaultPhoneContext,
            final boolean permitHexDigits) {
        StringBuilder sb = new StringBuilder();

        if (typePlan == TelnoType.INTERNATIONAL
                && (permitHexDigits || DIGITS.matcher(digits).matches())) {
            // global-number
            sb.append('+').append(digits);
            return sb.toString();
        } else {
            // local-number
            String phoneContext = "";
            int phoneContextIndex = digits.indexOf(phoneContextSeparator);
            if (phoneContextIndex != -1) {
                // The telno has an embedded phone-context
                phoneContext = digits.substring(0, phoneContextIndex);
                sb.append(digits.substring(phoneContextIndex + 1));
                if (phoneContextIndex > 0
                        && (permitHexDigits || DIGITS.matcher(phoneContext).matches())) {
                    // The embedded phone-context is a valid global-number
                    sb.append(';').append(phoneContextParameter).append('=');
                    sb.append('+').append(phoneContext);
                    return sb.toString();
                }
            } else {
                // The telno does not have an embedded phone-context
                sb.append(digits);
            }

            // Missing or invalid embedded phone-context
            if (defaultPhoneContext == null) {
                // No default phone-context so can't generate a valid URI
                return null;
            } else if (!defaultPhoneContext.isEmpty()) {
                // Use the default phone-context
                sb.append(';').append(phoneContextParameter).append('=');
                sb.append(defaultPhoneContext);
            }
            // Strictly speaking a local-number without a phone-context is
            // invalid, but we allow it if we've been explicitly given an empty
            // default phone-context
            return sb.toString();
        }
    }

    /**
     * Converts a generic telno into a String of digits. The hex digits A-F will
     * be converted according to the <code>permitStarHash</code> setting.
     *
     * @param telno
     *            The telno to convert into a string of digits
     * @param permitStarHash
     *            True if the telno digits 'ABCDEF' should be converted to the
     *            characters '*#ABCD'; false if the digits should be converted
     *            to the characters 'ABCDEF'
     * @return A String containing the digits in the telno
     */
    public static String convertGenericTelnoToString(final GenericTelno telno,
            final boolean permitStarHash) {
        // CSOFF: MagicNumber

        StringBuilder digits = new StringBuilder(telno.getDigits().length);

        for (byte digit : telno.getDigits()) {
            digit &= 0x0F;

            switch (digit) {
            case 0x0:
                digits.append('0');
                break;
            case 0x1:
                digits.append('1');
                break;
            case 0x2:
                digits.append('2');
                break;
            case 0x3:
                digits.append('3');
                break;
            case 0x4:
                digits.append('4');
                break;
            case 0x5:
                digits.append('5');
                break;
            case 0x6:
                digits.append('6');
                break;
            case 0x7:
                digits.append('7');
                break;
            case 0x8:
                digits.append('8');
                break;
            case 0x9:
                digits.append('9');
                break;
            case 0xA:
                if (permitStarHash) {
                    digits.append('*');
                } else {
                    digits.append('A');
                }
                break;
            case 0xB:
                if (permitStarHash) {
                    digits.append('#');
                } else {
                    digits.append('B');
                }
                break;
            case 0xC:
                if (permitStarHash) {
                    digits.append('A');
                } else {
                    digits.append('C');
                }
                break;
            case 0xD:
                if (permitStarHash) {
                    digits.append('B');
                } else {
                    digits.append('D');
                }
                break;
            case 0xE:
                if (permitStarHash) {
                    digits.append('C');
                } else {
                    digits.append('E');
                }
                break;
            case 0xF:
                if (permitStarHash) {
                    digits.append('D');
                } else {
                    digits.append('F');
                }
                break;
            default:
                //Ignore any other digit
                break;
            }
        }

        return digits.toString();
        // CSON: MagicNumber
    }

    /**
     * Converts a generic telno into the telephone-subscriber part of a tel: or
     * sip: URI. The telno will be decoded as follows:
     * <ul>
     * <li>If the type/plan is International E.164 and the telno contains only
     * the digits 0-9, then it will be returned as a global-number.</li>
     * <li>Otherwise if the telno contains the {@code phoneContextSeparator}
     * digit, then the digits before this will be used as the phone-context
     * descriptor and the digits after this will be used as the local-number.</li>
     * <li>If the phone-context is not a valid global-number or was not found,
     * then it will be replaced with the {@code defaultPhoneContext}.</li>
     * </ul>
     *
     * @param telno
     *            The telno to convert
     * @param permitStarHash
     *            True if the telno digits 'ABCDEF' should be converted to the
     *            characters '*#ABCD'; false if the digits should be converted
     *            to the characters 'ABCDEF'
     * @param phoneContextParameter
     *            The name of the context parameter (e.g. "phone-context")
     * @param phoneContextSeparator
     *            The character that separates the context and the number
     * @param defaultPhoneContext
     *            The default context
     * @return The telephone-subscriber part of a tel: URI
     */
    public static String convertGenericTelnoToTelephoneSubscriber(final GenericTelno telno,
            final boolean permitStarHash, final String phoneContextParameter,
            final char phoneContextSeparator, final String defaultPhoneContext) {
        return convertStringToTelephoneSubscriber(
                convertGenericTelnoToString(telno, permitStarHash),
                telno.getType(),
                phoneContextParameter,
                phoneContextSeparator,
                defaultPhoneContext,
                false);
    }

    /**
     * Converts a generic telno into the telephone-subscriber part of a tel: or
     * sip: URI. The telno will be decoded as follows:
     * <ul>
     * <li>If the type/plan is International E.164 and the telno contains valid
     * digits according to the <code>permitHexDigits</code> parameter, then it
     * will be returned as a global-number.</li>
     * <li>Otherwise if the telno contains the {@code phoneContextSeparator}
     * digit, then the digits before this will be used as the phone-context
     * descriptor and the digits after this will be used as the local-number.</li>
     * <li>If the phone-context is not a valid global-number or was not found,
     * then it will be replaced with the {@code defaultPhoneContext} (unless
     * that parameter is null, in which case URI generation will fail).</li>
     * </ul>
     * Note that if <code>permitHexDigits</code> is <code>true</code>, the
     * type/plan is not International E.164, and the
     * <code>phoneContextSeparator</code> is present in the digits, then this
     * function will fail to extract the correct phone-context and local-number.
     *
     * @param telno
     *            The telno to convert
     * @param permitStarHash
     *            True if the telno digits 'ABCDEF' should be converted to the
     *            characters '*#ABCD'; false if the digits should be converted
     *            to the characters 'ABCDEF'
     * @param phoneContextParameter
     *            The name of the context parameter (e.g. "phone-context")
     * @param phoneContextSeparator
     *            The character that separates the context and the number
     * @param defaultPhoneContext
     *            The default context
     * @param permitHexDigits
     *            Whether or not hex digits are permitted in a global-number
     * @return The telephone-subscriber part of a tel: URI
     */
    public static String convertGenericTelnoToTelephoneSubscriber(final GenericTelno telno,
            final boolean permitStarHash, final String phoneContextParameter,
            final char phoneContextSeparator, final String defaultPhoneContext,
            final boolean permitHexDigits) {
        return convertStringToTelephoneSubscriber(
                convertGenericTelnoToString(telno, permitStarHash),
                telno.getType(),
                phoneContextParameter,
                phoneContextSeparator,
                defaultPhoneContext,
                permitHexDigits);
    }

    /**
     * Convert a generic telephone number type into the corresponding Q.931 type
     * and plan. This is the number format used by OCP.
     *
     * @param type the generic telephone number type
     * @return the corresponding Q.931 type and plan
     */
    public static byte convertTelnoTypeToQ931(final TelnoType type) {
        switch (type) {
        case UNKNOWN:
        default:
            return Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_UNKNOWN;

        case INTERNATIONAL:
            return Q931_NUM_TYPE_INTERNATIONAL | Q931_NUM_PLAN_E164;

        case PRIVATE:
            return Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_PRIVATE;

        case UNKNOWN_TELEPHONY:
            return Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_E164;
        }
    }

    /**
     * Convert a generic telephone number type into the corresponding Q.763 type
     * and plan.
     *
     * @param type the generic telephone number type
     * @return the corresponding Q.763 type and plan
     */
    public static short convertTelnoTypeToQ763(final TelnoType type) {
        switch (type) {
        case UNKNOWN:
        default:
            return Q763_NAI_UNKNOWN | Q763_NUM_PLAN_UNKNOWN;

        case INTERNATIONAL:
            return Q763_NAI_INTERNATIONAL | Q763_NUM_PLAN_E164;

        case PRIVATE:
            return Q763_NAI_UNKNOWN | Q763_NUM_PLAN_PRIVATE;

        case UNKNOWN_TELEPHONY:
            return Q763_NAI_UNKNOWN | Q763_NUM_PLAN_E164;
        }
    }

    /**
     * Convert a Q.931 type and plan into the corresponding generic telephone
     * number type. This is the number format used by OCP.
     *
     * @param type
     *            the Q.931 type and plan
     * @return the corresponding generic telephone number type
     */
    public static TelnoType convertQ931ToTelnoType(final short type) {
        switch (type & (Q931_NUM_TYPE_MASK | Q931_NUM_PLAN_MASK)) {
            case Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_UNKNOWN:
            default:
                return TelnoType.UNKNOWN;

            case Q931_NUM_TYPE_INTERNATIONAL | Q931_NUM_PLAN_E164:
                return TelnoType.INTERNATIONAL;

            case Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_PRIVATE:
                return TelnoType.PRIVATE;

            case Q931_NUM_TYPE_UNKNOWN | Q931_NUM_PLAN_E164:
                return TelnoType.UNKNOWN_TELEPHONY;
        }
    }

    /**
     * Convert a Q.763 type and plan into the corresponding generic telephone
     * number type.
     *
     * @param type
     *            the Q.763 type and plan
     * @return the corresponding generic telephone number type
     */
    public static TelnoType convertQ763ToTelnoType(final short type) {
        switch (type & (Q763_NATURE_OF_ADDRESS_MASK | Q763_NUMBERING_PLAN_MASK)) {
            case Q763_NAI_UNKNOWN | Q763_NUM_PLAN_UNKNOWN:
            default:
                return TelnoType.UNKNOWN;

            case Q763_NAI_INTERNATIONAL | Q763_NUM_PLAN_E164:
                return TelnoType.INTERNATIONAL;

            case Q763_NAI_UNKNOWN | Q763_NUM_PLAN_PRIVATE:
                return TelnoType.PRIVATE;

            case Q763_NAI_UNKNOWN | Q763_NUM_PLAN_E164:
                return TelnoType.UNKNOWN_TELEPHONY;
        }
    }

    /**
     * Convert the current time to an Ocean time.
     *
     * @return The current time as the number of seconds since 1980.
     */
    public static int getOceanTime() {
        return getOceanTime(new Date());
    }

    /**
     * Convert the specified date to an Ocean time.
     *
     * @param date
     *            the Date to convert.
     * @return The specified date as the number of seconds since 1980.
     */
    public static int getOceanTime(final Date date) {
        return (int) TimeUnit.SECONDS.convert(date.getTime(),
                TimeUnit.MILLISECONDS)
                - NO_SECONDS_1970_1980;
    }

    /**
     * Convert the specified Ocean time to a Java {@link Date}.
     *
     * @param oceanTime
     *            The specified date as the number of seconds since 1980.
     * @return The specified date as a Java {@link Date}.
     */
    public static Date getDate(final int oceanTime) {
        return new Date(TimeUnit.MILLISECONDS.convert(((long) oceanTime)
                + NO_SECONDS_1970_1980, TimeUnit.SECONDS));
    }
}
