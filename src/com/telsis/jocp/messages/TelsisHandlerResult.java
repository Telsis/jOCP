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
import java.util.Arrays;

import com.telsis.jocp.CallMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;
import com.telsis.jocp.messages.telsishandler.TelsisHandlerPayload;

/**
 * Send this message in response to a {@link TelsisHandler Telsis Handler} or
 * {@link TelsisHandlerWithParty Telsis Handler With Party} message.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
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
 * <td>returnData</td>
 * <td>64</td>
 * <td>Custom data</td>
 * </tr>
 * </table>
 *
 * @see TelsisHandler
 * @see TelsisHandlerWithParty
 * @author Telsis
 */
public class TelsisHandlerResult extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.TELSIS_HANDLER_RESULT;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 66;

    /** The length of the returnData field. */
    private static final int            RETURN_DATA_LENGTH = 64;

    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The return data. */
    private final byte[]                returnData = new byte[RETURN_DATA_LENGTH];

    /**
     * Decode the buffer into a Telsis Handler Result message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public TelsisHandlerResult(final ByteBuffer buffer) throws OCPException {
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

        buffer.get(); // for word alignment
        zipNumber = buffer.get();
        buffer.get(returnData);
    }

    /**
     * Instantiates a new Telsis Handler Result message.
     */
    public TelsisHandlerResult() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.put((byte) 0); // for word alignment
        buffer.put(zipNumber);
        buffer.put(returnData);
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
     * Gets the return data decoded as a TelsisHandlerPayload instance.
     *
     * @param <T>
     *            Type parameter for the payload class
     * @param payloadClass
     *            The class of the payload to decode
     * @return the decoded payload
     */
    public final <T extends TelsisHandlerPayload> T getPayload(
            final Class<T> payloadClass) {
        try {
            T payload = payloadClass.newInstance();
            payload.decode(ByteBuffer.wrap(returnData));
            return payload;
        } catch (Exception e) {
            // Threw an exception decoding the buffer
            throw new IllegalArgumentException(
                    "Failed to decode data as " + payloadClass.getSimpleName());
        }
    }

    /**
     * Set the return data to the given payload. It encodes the payload
     * into the returnData instead of storing a reference to the payload
     * object.
     *
     * @param payload
     *            the payload to encode in the return data
     */
    public final void setPayload(final TelsisHandlerPayload payload) {
        Arrays.fill(returnData, (byte) 0);
        payload.encode(ByteBuffer.wrap(returnData));
    }

    @Override
    public final String toString() {
        return "Telsis Handler result";
    }

}
