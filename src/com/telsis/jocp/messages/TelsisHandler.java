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
import com.telsis.jocp.messages.telsishandler.TelsisHandlerPayload;

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
 * <td>sendData</td>
 * <td>64</td>
 * <td>Custom data</td>
 * </tr>
 * </table>
 *
 * @see TelsisHandlerResult
 * @author Telsis
 */
public class TelsisHandler extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.TELSIS_HANDLER;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 68;

    /** The length of the sendData field. */
    private static final int            SEND_DATA_LENGTH = 64;

    /** An empty payload used in the encode method. */
    private static final byte[]         EMPTY_PAYLOAD = new byte[SEND_DATA_LENGTH];

    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The handler number to call. */
    private TelsisHandlerNumber         handlerNumber;
    /** The payload. */
    private TelsisHandlerPayload        payload = null;


    /**
     * Decode the buffer into a TelsisHandler message.
     *
     * @param buffer
     *            the message to decode
     * @param minLength
     *            the minimum length of the message
     * @param maxLength
     *            the maximum length of the message
     * @param decode
     *            true if the payload should be decoded; false to skip decoding
     *            the payload. For use by subclasses that need to decode fields
     *            in a different order
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    protected TelsisHandler(final ByteBuffer buffer, final int minLength,
            final int maxLength, final boolean decode) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() < minLength
                || buffer.limit() > maxLength) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    this.getCommandCode(),
                    CallCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        if (!decode) {
            return;
        }

        buffer.get(); // for word alignment
        zipNumber = buffer.get();
        short number = buffer.getShort();
        try {
            handlerNumber = TelsisHandlerNumber.fromShort(number);
        } catch (Exception e) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    this.getCommandCode(),
                    CallCommandUnsupported.REASON_INVALID_FIELD_VALUE,
                    number);
        }
        decodePayload(buffer);
    }

    /**
     * Decode the buffer into a Telsis Handler message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public TelsisHandler(final ByteBuffer buffer) throws OCPException {
        this(buffer, EXPECTED_LENGTH, EXPECTED_LENGTH, true);
    }

    /**
     * Calls {@link CallControlMessage#CallControlMessage(short)} directly with
     * the specified message type. For use by subclasses.
     *
     * @param commandCode
     *            the command code
     */
    protected TelsisHandler(final short commandCode) {
        super(commandCode);
    }

    /**
     * Instantiates a new Telsis Handler message.
     */
    public TelsisHandler() {
        this(TYPE.getCommandCode());
    }

    /**
     * Decodes the payload of the message.
     * @param buffer
     *            the ByteBuffer with position set at the beginning
     *            of the payload
     * @throws OCPException if decoding failed
     */
    protected final void decodePayload(final ByteBuffer buffer)
            throws OCPException {

        if (handlerNumber.getPayloadClass() == null) {
            // No payload to decode
            return;
        }

        try {
            TelsisHandlerPayload tmpPayload = handlerNumber.getPayloadClass().newInstance();
            tmpPayload.decode(buffer);
            payload = tmpPayload;
        } catch (Exception e) {
            // Threw an exception decoding the buffer
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    this.getCommandCode(),
                    CallCommandUnsupported.REASON_CUSTOM_DATA_INVALID);
        }
    }

    /**
     * Encode the header and payload into the buffer. This modifies the buffer
     * in-place and sets the buffer's position to the end of the payload.
     *
     * @param buffer
     *            The buffer to insert the message into
     * @param encode
     *            true if the payload should be encoded; false to skip encoding
     *            the payload. For use by subclasses that need to encode fields
     *            in a different order
     */
    protected void encode(final ByteBuffer buffer, // CSIGNORE: DesignForExtension
            final boolean encode) {
        super.encode(buffer);

        if (!encode) {
            return;
        }

        buffer.put((byte) 0);
        buffer.put(zipNumber);
        buffer.putShort(handlerNumber.getNumber());
        if (payload != null) {
            payload.encode(buffer);
            buffer.put(new byte[SEND_DATA_LENGTH - payload.getLength()]);
        } else {
            buffer.put(EMPTY_PAYLOAD);
        }
    }

    @Override // CSIGNORE: DesignForExtension
    protected void encode(final ByteBuffer buffer) {
        encode(buffer, true);
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
     * Gets the handler number.
     *
     * @return the handler number
     */
    public final TelsisHandlerNumber getHandlerNumber() {
        return handlerNumber;
    }

    /**
     * Sets the handler number.
     *
     * @param newHandlerNumber the new handler number
     */
    public final void setHandlerNumber(final TelsisHandlerNumber newHandlerNumber) {
        this.handlerNumber = newHandlerNumber;
    }

    /**
     * Gets the payload.
     *
     * @return the payload
     */
    public final TelsisHandlerPayload getPayload() {
        return payload;
    }

    /**
     * Sets the payload.
     *
     * @param newPayload the new payload
     */
    public final void setPayload(final TelsisHandlerPayload newPayload) {
        if (newPayload.getLength() > getMaximumPayloadLength()) {
            throw new IllegalArgumentException("The maximum length for this "
                    + "parameter is " + getMaximumPayloadLength());
        }
        this.payload = newPayload;
    }

    /**
     * Gets the maximum payload length.
     *
     * @return the maximum payload length
     */
    protected int getMaximumPayloadLength() { // CSIGNORE: DesignForExtension
        return SEND_DATA_LENGTH;
    }

    @Override // CSIGNORE: DesignForExtension
    public String toString() {
        return "Telsis Handler";
    }

}
