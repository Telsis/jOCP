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
package com.telsis.jocp.messages;

import java.nio.ByteBuffer;

import com.telsis.jocp.CallMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message in response to the {@link SetCDRExtendedFieldData} message.
 * This message tells the fastSCP whether or not the set CDR operation
 * succeeded.
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
 * <td>result</td>
 * <td>2</td>
 * <td>A value of 1 indicates success</td>
 * </tr>
 * </table>
 *
 * @see SetCDRExtendedFieldData
 * @author Telsis
 */
public class SetCDRExtendedFieldDataResult extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.SET_CDR_EXTENDED_FIELD_DATA_RESULT;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 4;

    /** The index into the zip table on the SCP for this result. */
    private byte zipNumber;
    /** A value of 1 indicates success. */
    private short result;

    /**
     * Decode the buffer into an Set CDR Extended Field Data Result message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public SetCDRExtendedFieldDataResult(final ByteBuffer buffer) throws OCPException {
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
        result = buffer.getShort();
    }

    /**
     * Instantiates a new Set CDR Extended Field Data Result message.
     */
    public SetCDRExtendedFieldDataResult() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        buffer.put((byte) 0);
        buffer.put(zipNumber);
        buffer.putShort(result);
    }

    /**
     * Gets the index into the zip table on the SCP for this result.
     *
     * @return the index into the zip table on the SCP for this result
     */
    public final byte getZipNumber() {
        return zipNumber;
    }

    /**
     * Sets the index into the zip table on the SCP for this result.
     *
     * @param newZipNumber
     *            the new index into the zip table on the SCP for this result
     */
    public final void setZipNumber(final byte newZipNumber) {
        this.zipNumber = newZipNumber;
    }

    /**
     * Gets the result. A value of 1 indicates success.
     *
     * @return the result
     */
    public final short getResult() {
        return result;
    }

    /**
     * Sets the result. A value of 1 indicates success.
     *
     * @param newResult
     *            the new result
     */
    public final void setResult(final short newResult) {
        this.result = newResult;
    }

    @Override
    public final String toString() {
        return "Set CDR Extended Field Data Result";
    }
}
