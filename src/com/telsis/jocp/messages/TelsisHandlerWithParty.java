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
import com.telsis.jocp.messages.telsishandler.TelsisHandlerNumber;

/**
 * Send this message to run a Telsis handler on the remote platform. The
 * expected reply to this message is {@link TelsisHandlerResult Telsis Handler
 * Result}.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>fwdLegID</td>
 * <td>2</td>
 * <td>The party the handler is destined for or 0xFFFF means no party.</td>
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
 * <td>handlerNumber</td>
 * <td>2</td>
 * <td>Service number to call</td>
 * </tr>
 * <tr>
 * <td>dataLength</td>
 * <td>2</td>
 * <td>Length of the following data field</td>
 * </tr>
 * <tr>
 * <td>data</td>
 * <td>0 - 448</td>
 * <td>Up to 448 bytes of Custom data. Note that other units have smaller
 * limits:
 * <ul>
 * <li>The fastSSP is limited to an overall message size of 255 bytes.</li>
 * <li>The fastIP only supports up to 64 bytes of custom data.</li>
 * <li>The fastSSP (OCP Caller) only supports up to 128 bytes of custom data.</li>
 * <li>The fastTC (INAP Interface) supports 233 bytes of custom data.</li>
 * <li>The fastSCP supports up to 448 bytes of custom data.</li>
 * <li>The jOCP library supports up to 448 bytes of custom data</li>
 * </ul>
 * </td>
 * </tr>
 * </table>
 *
 * @see TelsisHandlerResult
 * @author Telsis
 */
public class TelsisHandlerWithParty extends TelsisHandler {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.TELSIS_HANDLER_WITH_PARTY;

    /** The minimum expected length of this message. */
    private static final int            EXPECTED_LENGTH_MIN = 8;
    /** The maximum expected length of this message. */
    private static final int            EXPECTED_LENGTH_MAX = 456;

    /** The maximum length of the payload data. */
    private static final int            MAX_DATA_LENGTH     = 448;

    /** The party the handler is destined for. */
    private short                       fwdLegID;

    /**
     * Decode the buffer into a Telsis Handler With Party message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public TelsisHandlerWithParty(final ByteBuffer buffer) throws OCPException {
        super(buffer, EXPECTED_LENGTH_MIN, EXPECTED_LENGTH_MAX, false); // Need custom decoding

        int dataLength;

        fwdLegID = buffer.getShort();
        buffer.get(); // for word alignment
        setZipNumber(buffer.get());
        short number = buffer.getShort();
        try {
            setHandlerNumber(TelsisHandlerNumber.fromShort(number));
        } catch (Exception e) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    this.getCommandCode(),
                    CallCommandUnsupported.REASON_INVALID_FIELD_VALUE,
                    number);
        }
        dataLength = buffer.getShort();
        // The rest of the message is the data rounded to even length.
        if (dataLength + dataLength % 2 != buffer.remaining()) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    TYPE.getCommandCode(),
                    CallCommandUnsupported.REASON_INVALID_FIELD_VALUE,
                    (short) buffer.limit());
        }
        decodePayload(buffer);
    }

    /**
     * Instantiates a new Telsis Handler With Party message.
     */
    public TelsisHandlerWithParty() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer, false); // Need custom encoding

        buffer.putShort(fwdLegID);
        buffer.put((byte) 0);
        buffer.put(getZipNumber());
        buffer.putShort(getHandlerNumber().getNumber());
        if (getPayload() != null) {
            buffer.putShort(getPayload().getLength());
            getPayload().encode(buffer);
        } else {
            buffer.putShort((short) 0);
        }
    }

    /**
     * Gets the forward leg ID.
     *
     * @return the forward leg ID
     */
    public final short getFwdLegID() {
        return fwdLegID;
    }

    /**
     * Sets the forward leg ID.
     *
     * @param newFwdLegID the new forward leg ID
     */
    public final void setFwdLegID(final short newFwdLegID) {
        this.fwdLegID = newFwdLegID;
    }

    @Override
    protected final int getMaximumPayloadLength() {
        return MAX_DATA_LENGTH;
    }

    @Override
    public final String toString() {
        return "Telsis Handler with party";
    }

}
