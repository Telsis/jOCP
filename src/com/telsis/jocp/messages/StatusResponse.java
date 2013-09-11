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
import java.nio.charset.Charset;
import java.util.Arrays;

import com.telsis.jocp.LinkMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message in response to a {@link StatusRequest Status Request}
 * message to indicate the number of calls in progress on the platform.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>unitName</td>
 * <td>32</td>
 * <td>NULL if no name is being sent</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>2</td>
 * <td>Spare</td>
 * </tr>
 * <tr>
 * <td>activeCalls</td>
 * <td>2</td>
 * <td>Count of calls in progress which have been presented to the SCP</td>
 * </tr>
 * </table>
 *
 * @see StatusRequest
 * @author Telsis
 */
public class StatusResponse extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.STATUS_RESPONSE;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 36;

    /** The length of the unitName field. */
    private static final int            UNIT_NAME_LENGTH = 32;

    /** The unit name. */
    private String                      unitName;
    /** The flags. */
    private short                       flags;
    /** The count of calls in progress which have been presented to the SCP. */
    private short                       activeCalls;

    /**
     * Decode the buffer into a Status Response message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public StatusResponse(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        byte[] unitNameByte = new byte[UNIT_NAME_LENGTH];
        buffer.get(unitNameByte);
        if (unitNameByte[0] == 0x00) {
            this.unitName = "";
        } else {
            for (int i = 1; i < unitNameByte.length; i++) {
                if (unitNameByte[i] == 0x00) {
                    unitNameByte = Arrays.copyOf(unitNameByte, i);
                }
            }
            this.unitName = new String(unitNameByte, Charset.forName("US-ASCII"));
        }

        this.flags = buffer.getShort();
        this.activeCalls = buffer.getShort();
    }

    /**
     * Instantiates a new Status Response message.
     */
    public StatusResponse() {
        super(TYPE.getCommandCode());
        this.unitName = "";
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        // Write the unit name (32 bytes, null-terminated)
        byte[] nameBytes = unitName.getBytes(Charset.forName("US-ASCII"));
        int i = 0;
        for (; i < (UNIT_NAME_LENGTH - 1) && i < nameBytes.length; i++) {
            buffer.put(nameBytes[i]);
        }
        for (; i < UNIT_NAME_LENGTH; i++) {
            buffer.put((byte) 0);
        }

        buffer.putShort(flags); // unused field
        buffer.putShort(activeCalls);
    }

    /**
     * Gets the unit name.
     *
     * @return the unit name
     */
    public final String getUnitName() {
        return unitName;
    }

    /**
     * Sets the unit name.
     *
     * @param newUnitName the new unit name
     */
    public final void setUnitName(final String newUnitName) {
        this.unitName = newUnitName;
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

    /**
     * Gets the number of active calls.
     *
     * @return the number of active calls
     */
    public final short getActiveCalls() {
        return activeCalls;
    }

    /**
     * Sets the number of active calls.
     *
     * @param newActiveCalls the new number of active calls
     */
    public final void setActiveCalls(final short newActiveCalls) {
        this.activeCalls = newActiveCalls;
    }


    @Override
    public final String toString() {
        return "Status response";
    }

}
