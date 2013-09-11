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
import java.util.Arrays;

import com.telsis.jocp.CallMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message if you want to include custom Call Detail Record (CDR)
 * information to a particular call leg on a remote call-handling platform. The
 * expected reply to this message is {@link SetCDRExtendedFieldDataResult}
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
 * <td>The ID of the leg of the call for which the CDR information is to be set.
 * This is either the calling party or the called party.</td>
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
 * <td>dataLength</td>
 * <td>2</td>
 * <td>The length of the data</td>
 * </tr>
 * <tr>
 * <td>data</td>
 * <td>40</td>
 * <td>The data to be written to the CDR</td>
 * </tr>
 * </table>
 *
 * @see SetCDRExtendedFieldDataResult
 * @author Telsis
 */
public class SetCDRExtendedFieldData extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.SET_CDR_EXTENDED_FIELD_DATA;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 46;

    /** The length of the data field. */
    public static final int            DATA_LENGTH = 40;

    /** The ID of the leg of the call for which the CDR information is to be set. */
    private short destLegID;
    /** The index into the zip table on the SCP for this result. */
    private byte zipNumber;
    /** The length of the data. */
    private short dataLength;
    /** The data to be written to the CDR. */
    private byte[] data;

    /**
     * Decode the buffer into an Set CDR Extended Field Data message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public SetCDRExtendedFieldData(final ByteBuffer buffer) throws OCPException {
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
        dataLength = buffer.getShort();
        if (dataLength > DATA_LENGTH) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    TYPE.getCommandCode(),
                    CallCommandUnsupported.REASON_INVALID_FIELD_VALUE,
                    dataLength);
        }
        data = new byte[DATA_LENGTH];
        buffer.get(data);
    }

    /**
     * Instantiates a new Set CDR Extended Field Data message.
     */
    public SetCDRExtendedFieldData() {
        super(TYPE.getCommandCode());
        data = new byte[DATA_LENGTH];
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        buffer.putShort(destLegID);
        buffer.put((byte) 0);
        buffer.put(zipNumber);
        buffer.putShort(dataLength);
        buffer.put(data);
    }

    /**
     * Gets the ID of the leg of the call for which the CDR information is to be
     * set.
     *
     * @return the ID of the leg of the call for which the CDR information is to
     *         be set
     */
    public final short getDestLegID() {
        return destLegID;
    }

    /**
     * Sets the ID of the leg of the call for which the CDR information is to be
     * set.
     *
     * @param newDestLegID
     *            the new ID of the leg of the call for which the CDR
     *            information is to be set
     */
    public final void setDestLegID(final short newDestLegID) {
        this.destLegID = newDestLegID;
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
     * Gets the length of the data.
     *
     * @return the length of the data
     */
    public final short getRawDataLength() {
        return dataLength;
    }

    /**
     * Sets the length of the data. This must be no more than
     * {@value #DATA_LENGTH}.
     *
     * @param newDataLength
     *            the new length of the data
     */
    public final void setRawDataLength(final short newDataLength) {
        if (newDataLength > DATA_LENGTH) {
            throw new IllegalArgumentException(
                    "The maximum value for this parameter is " + DATA_LENGTH);
        }
        this.dataLength = newDataLength;
    }

    /**
     * Gets the data to be written to the CDR.
     *
     * @return the data to be written to the CDR
     */
    public final byte[] getRawData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Sets the data to be written to the CDR. This must be exactly
     * {@value #DATA_LENGTH} bytes.
     *
     * @param newData
     *            the new data to be written to the CDR
     */
    public final void setRawData(final byte[] newData) {
        if (newData.length != DATA_LENGTH) {
            throw new IllegalArgumentException("This parameter must be "
                    + DATA_LENGTH + " bytes exactly");
        }
        this.data = Arrays.copyOf(newData, newData.length);
    }

    /**
     * Gets the data to be written to the CDR.
     *
     * @return the data to be written to the CDR
     */
    public final byte[] getData() {
        return Arrays.copyOf(data, dataLength);
    }

    /**
     * Sets the data to be written to the CDR. This must be no more than
     * {@value #DATA_LENGTH} bytes long.
     *
     * @param newData
     *            the new data to be written to the CDR
     */
    public final void setData(final byte[] newData) {
        if (newData.length > DATA_LENGTH) {
            throw new IllegalArgumentException("This parameter must be no more than "
                    + DATA_LENGTH + " bytes long");
        }
        this.data = Arrays.copyOf(newData, DATA_LENGTH);
        this.dataLength = (short) newData.length;
    }

    @Override
    public final String toString() {
        return "Set CDR Extended Field Data";
    }
}
