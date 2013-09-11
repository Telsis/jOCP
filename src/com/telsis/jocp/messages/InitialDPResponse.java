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
 * Send this message (if required) in response to See {@link InitialDP Initial
 * DP} or {@link InitialDPServiceKey Initial DP Service Key}.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>origLegID</td>
 * <td>2</td>
 * <td>SCP's Leg ID for A party</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>2</td>
 * <td>Bit 0 = Cleardown notification required</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * The fastSCP only sends this message if it requires the call-handling platform
 * to send it cleardown messages for each leg of the current call. The
 * call-handling platform should therefore not send cleardown messages unless it
 * receives this message with bit 0 set.
 *
 * @see InitialDP
 * @see InitialDPServiceKey
 * @author Telsis
 */
public class InitialDPResponse extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.INITIAL_DP_RESPONSE;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 4;

    /** Indicates that cleardown notification is required. */
    public static final short           FLAG_CLEARDOWN_NOTIFICATION = 1;

    /** The SCP's Leg ID for A party. */
    private short                       origLegID;
    /** The flags. */
    private short                       flags;

    /**
     * Decode the buffer into an Initial DP Response message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public InitialDPResponse(final ByteBuffer buffer) throws OCPException {
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

        origLegID = buffer.getShort();
        flags = buffer.getShort();
    }

    /**
     * Instantiates a new Initial DP Response message.
     */
    public InitialDPResponse() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(origLegID);
        buffer.putShort(flags);
    }

    /**
     * Gets the orig leg ID.
     *
     * @return the orig leg ID
     */
    public final short getOrigLegID() {
        return origLegID;
    }

    /**
     * Sets the orig leg ID.
     *
     * @param newOrigLegID the new orig leg ID
     */
    public final void setOrigLegID(final short newOrigLegID) {
        this.origLegID = newOrigLegID;
    }

    /**
     * Gets the flags.
     *
     * @return the flags
     */
    public final short getFlags() {
        return flags;
    }

    /**
     * Sets the flags.
     *
     * @param newFlags the new flags
     */
    public final void setFlags(final short newFlags) {
        this.flags = newFlags;
    }

    @Override
    public final String toString() {
        return "Initial DP response";
    }
}
