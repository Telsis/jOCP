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

import com.telsis.jocp.LinkMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message when the platform receives a link management message it is
 * unable to interpret.
 * <p/>
 * For example, if a fastSCP sends a {@link StatusRequest Status Request}
 * message to a fastIP in which the message length is 30 bytes in length (rather
 * than 28), the fastIP should respond by returning a Link Command Error
 * message. The Reason field in this case would be 1 and the payload's Value
 * field would be 30.
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
 * <td>The command code which isn't supported</td>
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
 * 6 = unexpected command received, i.e. if a L-S message is received for a M/S
 * unit.</td>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>4</td>
 * <td>The unsupported value - not used if reason indicates unsupported command
 * code</td>
 * </tr>
 * </table>
 *
 * @author Telsis
 */
public class LinkCommandUnsupported extends LinkMessage {
    /** The mesage type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.LINK_COMMAND_UNSUPPORTED;
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

    /** The command code which isn't supported. */
    private short                       nestedCommandCode;
    /** The reason. */
    private short                       reason;
    /** The unsupported value. */
    private int                         value;

    /**
     * Decode the buffer into a Link Command Unsupported message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public LinkCommandUnsupported(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.nestedCommandCode = buffer.getShort();
        this.reason = buffer.getShort();
        this.value = buffer.getInt();
    }

    /**
     * Instantiates a new Link Command Unsupported message.
     */
    public LinkCommandUnsupported() {
        super(TYPE.getCommandCode());
    }

    /**
     * Instantiates a new Link Command Unsupported message with the specified
     * values.
     *
     * @param newNestedCommandCode
     *            the command code which isn't supported
     * @param newReason
     *            the reason
     * @param newValue
     *            the unsupported value
     */
    public LinkCommandUnsupported(final short newNestedCommandCode,
            final short newReason, final int newValue) {
        super(TYPE.getCommandCode());

        this.nestedCommandCode = newNestedCommandCode;
        this.reason = newReason;
        this.value = newValue;
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
     * Gets the unsupported value.
     *
     * @return the unsupported value
     */
    public final int getValue() {
        return value;
    }

    /**
     * Sets the unsupported value.
     *
     * @param newValue the new unsupported value
     */
    public final void setValue(final int newValue) {
        this.value = newValue;
    }

    /**
     * Gets the command code which isn't supported.
     *
     * @return the command code which isn't supported
     */
    public final short getNestedCommandCode() {
        return nestedCommandCode;
    }

    /**
     * Sets the command code which isn't supported.
     *
     * @param newNestedCommandCode the new command code which isn't supported
     */
    public final void setNestedCommandCode(final short newNestedCommandCode) {
        this.nestedCommandCode = newNestedCommandCode;
    }

    @Override
    public final String toString() {
        return "Link command unsupported";
    }

}
