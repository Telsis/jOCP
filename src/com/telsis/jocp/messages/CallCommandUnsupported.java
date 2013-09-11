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
package com.telsis.jocp.messages;

import java.nio.ByteBuffer;

import com.telsis.jocp.CallMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message in response to an OCP service layer message that is
 * unrecognised, is incorrectly formatted or has been sent at an invalid time.
 * <p/>
 * For example, in Ocean architecture, if a fastSCP sends a Prompt and Collect
 * DTMF message to a fastSSP in which the message payload's Number of Digits
 * field contains a value outside the range 1 to 32, the fastSSP responds by
 * returning a Call Command Error message. The Reason field contains the value 5
 * and the invalid out-of-range value is sent in the Value field.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>commandCode</td>
 * <td>2</td>
 * <td>The Command Code of command with error.</td>
 * </tr>
 * <tr>
 * <td>reason</td>
 * <td>2</td>
 * <td>0 = command code unsupported<br>
 * 1 = length unsupported (length returned in value field)<br>
 * 2 = illegal command use<br>
 * 3 = unsupported command mode (mode field returned in value field)<br>
 * 4 = unsupported command option (option field returned in value field)<br>
 * 5 = invalid field value (invalid field value returned in value field)<br>
 * 6 = unexpected command received<br>
 * 7 = Custom Data Invalid<br>
 * 8 = Field in Custom Data Out of Range</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>4</td>
 * <td>Additional information returned for some errors (see Reason values above)
 * </td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * This message may be sent with a NULL forward task ID (since it may be sent in
 * response to {@link TaskActive Task Active} or {@link TaskActiveResult Task
 * Active Result}, both of which may have NULL forward and backward task IDs).
 *
 * @author Telsis
 */
public class CallCommandUnsupported extends CallControlMessage {
    /** The mesage type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.CALL_COMMAND_UNSUPPORTED;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 8;

    /** Command code unsupported. */
    public static final short           REASON_COMMAND_CODE_UNSUPPORTED       = 0;
    /** Length unsupported (length returned in value field). */
    public static final short           REASON_LENGTH_UNSUPPORTED             = 1;
    /** Illegal command use. */
    public static final short           REASON_ILLEGAL_COMMAND_USE            = 2;
    /** Unsupported command mode (mode field returned in value field). */
    public static final short           REASON_UNSUPPORTED_COMMAND_MODE       = 3;
    /** Unsupported command option (option field returned in value field). */
    public static final short           REASON_UNSUPPORTED_COMMAND_OPERATION  = 4;
    /** Invalid field value (invalid field value returned in value field). */
    public static final short           REASON_INVALID_FIELD_VALUE            = 5;
    /** Unexpected command received. */
    public static final short           REASON_UNEXPECTED_COMMAND_RECEIVED    = 6;
    /** Custom Data Invalid. */
    public static final short           REASON_CUSTOM_DATA_INVALID            = 7;
    /** Field in Custom Data Out of Range. */
    public static final short           REASON_CUSTOM_DATA_FIELD_OUT_OF_RANGE = 8;

    /** The Command Code of command with error. */
    private short                       nestedCommandCode;
    /** The reason. */
    private short                       reason;
    /** The additional information returned for some errors. */
    private int                         value;

    /**
     * Decode the buffer into a Call Command Unsupported message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public CallCommandUnsupported(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    TYPE.getCommandCode(),
                    CallCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.nestedCommandCode = buffer.getShort();
        this.reason = buffer.getShort();
        this.value = buffer.getInt();
    }

    /**
     * Instantiates a new Call Command Unsupported message.
     */
    public CallCommandUnsupported() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        buffer.putShort(nestedCommandCode);
        buffer.putShort(reason);
        buffer.putInt(value);
    }

    /**
     * Gets the reason.
     *
     * @return the reason
     */
    public final short getReason() {
        return reason;
    }

    /**
     * Sets the reason.
     *
     * @param newReason the new reason
     */
    public final void setReason(final short newReason) {
        this.reason = newReason;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public final int getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param newValue the new value
     */
    public final void setValue(final int newValue) {
        this.value = newValue;
    }

    /**
     * Gets the nested command code.
     *
     * @return the nested command code
     */
    public final short getNestedCommandCode() {
        return nestedCommandCode;
    }

    /**
     * Sets the nested command code.
     *
     * @param newNestedCommandCode the new nested command code
     */
    public final void setNestedCommandCode(final short newNestedCommandCode) {
        this.nestedCommandCode = newNestedCommandCode;
    }

    @Override
    public final String toString() {
        return "Call Command Unsupported";
    }

}
