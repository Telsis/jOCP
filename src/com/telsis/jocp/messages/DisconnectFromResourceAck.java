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
 * Send this message in response to the {@link DisconnectFromResource Disconnect
 * From Resource} message. This message tells the fastSCP that the call-handling
 * platform is attempting to disconnect from the external resource.
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
 * <td>This is the leg ID that is being disconnected</td>
 * </tr>
 * <tr>
 * <td>spare</td>
 * <td>1</td>
 * <td><i>For word alignment</i></td>
 * </tr>
 * <tr>
 * <td>zipNumber</td>
 * <td>1</td>
 * <td>The index into the zip table on the SCP for this result</td>
 * </tr>
 * <tr>
 * <td>result</td>
 * <td>1</td>
 * <td>&gt;0 indicates error</td>
 * </tr>
 * <tr>
 * <td>reason</td>
 * <td>1</td>
 * <td>If result indicates failure, then this contains the reason</td>
 * </tr>
 * </table>
 *
 * @see DisconnectFromResource
 * @author Telsis
 */
public class DisconnectFromResourceAck extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.DISCONNECT_FROM_RESOURCE_ACK;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 6;

    /** The leg ID that is being connected. */
    private short                       destLegID;
    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The result. */
    private byte                        result;
    /** The reason. */
    private byte                        reason;

    /**
     * Decode the buffer into a Disconnect From Resource Ack message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public DisconnectFromResourceAck(final ByteBuffer buffer) throws OCPException {
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

        destLegID = buffer.getShort();
        buffer.get(); // for word alignment
        zipNumber = buffer.get();
        result = buffer.get();
        reason = buffer.get();
    }

    /**
     * Instantiates a new Disconnect From Resource Ack message.
     */
    public DisconnectFromResourceAck() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(destLegID);
        buffer.put((byte) 0); // for word alignment
        buffer.put(zipNumber);
        buffer.put(result);
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
     * Gets the zip number.
     *
     * @return the zip number
     */
    public final byte getZipNumber() {
        return zipNumber;
    }

    /**
     * Sets the zip number.
     *
     * @param newZipNumber the new zip number
     */
    public final void setZipNumber(final byte newZipNumber) {
        this.zipNumber = newZipNumber;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public final byte getResult() {
        return result;
    }

    /**
     * Sets the result.
     *
     * @param newResult the new result
     */
    public final void setResult(final byte newResult) {
        this.result = newResult;
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
        return "Disconnect from resource ack";
    }

}
