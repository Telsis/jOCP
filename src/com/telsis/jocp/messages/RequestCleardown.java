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
 * Send this message to instruct the call-handling platform to clear a call.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>destLegID</td>
 * <td>2</td>
 * <td>Leg ID on call handling unit to act upon.</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>1</td>
 * <td>Bit 0 = reason is raw signalling scheme code, not Q850.</td>
 * </tr>
 * <tr>
 * <td>reason</td>
 * <td>1</td>
 * <td>Q850 or raw reason code.</td>
 * </tr>
 * </table>
 *
 * @see CallCleardown
 * @author Telsis
 */
public class RequestCleardown extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.REQUEST_CLEARDOWN;
    /** The expected length of this messge. */
    private static final int            EXPECTED_LENGTH = 4;

    /**
     * Indicates that the reason field contains the raw signalling scheme code.
     */
    public static final byte            FLAG_NATIVE_REASON = 1;
    /** Set destLegID to this to indicate that all legs are to be cleared. */
    public static final short           LEG_ID_CLEAR_ALL   = (short) 0xFFFF;

    /** The leg ID on call handling unit to act upon. */
    private short                       destLegID;
    /** The flags. */
    private byte                        flags;
    /** The reason. */
    private byte                        reason;

    /**
     * Decode the buffer into a Request Cleardown message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public RequestCleardown(final ByteBuffer buffer) throws OCPException {
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

        this.destLegID = buffer.getShort();
        this.flags = buffer.get();
        this.reason = buffer.get();
    }

    /**
     * Instantiates a new Request Cleardown message.
     */
    public RequestCleardown() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(destLegID);
        buffer.put(flags);
        buffer.put(reason);
    }

    /**
     * Gets the dest leg ID.
     *
     * @return the dest leg ID
     */
    public final short getDestLegID() {
        return destLegID;
    }

    /**
     * Sets the dest leg ID.
     *
     * @param newDestLegID the new dest leg ID
     */
    public final void setDestLegID(final short newDestLegID) {
        this.destLegID = newDestLegID;
    }

    /**
     * Gets the flags.
     *
     * @return the flags
     */
    public final byte getFlags() {
        return flags;
    }

    /**
     * Sets the flags.
     *
     * @param newFlags the new flags
     */
    public final void setFlags(final byte newFlags) {
        this.flags = newFlags;
    }

    /**
     * Gets the reason.
     *
     * @return the reason
     */
    public final byte getReason() {
        return reason;
    }

    /**
     * Sets the reason.
     *
     * @param newReason the new reason
     */
    public final void setReason(final byte newReason) {
        this.reason = newReason;
    }

    @Override
    public final String toString() {
        return "Request Cleardown";
    }

}
