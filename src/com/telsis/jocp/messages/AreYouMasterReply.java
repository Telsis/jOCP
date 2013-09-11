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
 * Send this message in response to an {@link AreYouMaster Are You Master}
 * message to indicate the platform's master/slave status.
 * <p/>
 * If both platforms report themselves as master, the platform receiving this
 * message instructs the platform with the highest Platform ID to be master.
 * This ensures that all receiving platforms connected to a dual-redundant pair
 * of platforms nominate the same platform to be master.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>scpID</td>
 * <td>4</td>
 * <td>fastSCP ID number</td>
 * </tr>
 * <tr>
 * <td>masterFlag</td>
 * <td>2</td>
 * <td>0 = Slave<br>
 * 1 = Master</td>
 * </tr>
 * </table>
 *
 * @see AreYouMaster
 * @author Telsis
 */
public class AreYouMasterReply extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.ARE_YOU_MASTER_REPLY;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 6;

    /** The fastSCP ID number. */
    private int                         scpID;
    /** The master flag. */
    private short                       masterFlag;

    /**
     * Decode the buffer into an Are You Master Reply message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public AreYouMasterReply(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    CallCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.scpID = buffer.getInt();
        this.masterFlag = buffer.getShort();
    }

    /**
     * Instantiates a new Are You Master Reply message.
     */
    public AreYouMasterReply() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putInt(this.scpID);
        buffer.putShort(this.masterFlag);
    }

    /**
     * Gets the SCP ID.
     *
     * @return the SCP ID
     */
    public final int getScpID() {
        return scpID;
    }

    /**
     * Sets the SCP ID.
     *
     * @param newScpID the new SCP ID
     */
    public final void setScpID(final int newScpID) {
        this.scpID = newScpID;
    }

    /**
     * Gets the master flag.
     *
     * @return the master flag
     */
    public final short getMasterFlag() {
        return masterFlag;
    }

    /**
     * Sets the master flag.
     *
     * @param newMasterFlag the new master flag
     */
    public final void setMasterFlag(final short newMasterFlag) {
        this.masterFlag = newMasterFlag;
    }

    @Override
    public final String toString() {
        return "Are you Master reply";
    }

}
