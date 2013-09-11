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

import com.telsis.jocp.LinkMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message to indicate which load-sharing fastSCP should take
 * exclusive control of calls.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>2</td>
 * <td>Bit 0- cleared = cancel, set = setting preferred unit status</td>
 * </tr>
 * <tr>
 * <td>preferredSCP</td>
 * <td>4</td>
 * <td>IP address of the preferred SCP</td>
 * </tr>
 * <tr>
 * <td>secondarySCP</td>
 * <td>4</td>
 * <td>IP address of the Secondary SCP</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * This message applies only to load-sharing fastSCPs.
 * <p/>
 * If a link in a load-sharing fastSCP cluster fails, this message is sent to
 * all call-handling platforms to which the load-shared fastSCPs are connected.
 * The calling-handling platforms then decide which fastSCP to use depending on
 * the state of links between the calling-handling platforms and fastSCPs.
 *
 * @author Telsis
 */
public class PreferredUnit extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.PREFERRED_UNIT;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 10;

    /** The length of the preferredSCP field. */
    private static final int            PREFERRED_SCP_LENGTH = 4;
    /** The length of the secondarySCP field. */
    private static final int            SECONDARY_SCP_LENGTH = 4;

    /** The flags. */
    private short                       flags;
    /** The IP address of the preferred SCP. */
    private byte[]                      preferredSCP = new byte[PREFERRED_SCP_LENGTH];
    /** The IP address of the Secondary SCP. */
    private byte[]                      secondarySCP = new byte[SECONDARY_SCP_LENGTH];

    /**
     * Decode the buffer into a Preferred Unit message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public PreferredUnit(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.flags = buffer.getShort();
        buffer.get(this.preferredSCP);
        buffer.get(this.secondarySCP);
    }

    /**
     * Instantiates a new Preferred Unit message.
     */
    public PreferredUnit() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        buffer.putShort(this.flags);
        buffer.put(preferredSCP);
        buffer.put(secondarySCP);
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
     * Gets the preferred SCP.
     *
     * @return the preferred SCP
     */
    public final byte[] getPreferredSCP() {
        return Arrays.copyOf(preferredSCP, preferredSCP.length);
    }

    /**
     * Sets the preferred SCP.
     *
     * @param newPreferredSCP the new preferred SCP
     */
    public final void setPreferredSCP(final byte[] newPreferredSCP) {
        this.preferredSCP = Arrays.copyOf(newPreferredSCP, newPreferredSCP.length);
    }

    /**
     * Gets the secondary SCP.
     *
     * @return the secondary SCP
     */
    public final byte[] getSecondarySCP() {
        return Arrays.copyOf(secondarySCP, secondarySCP.length);
    }

    /**
     * Sets the secondary SCP.
     *
     * @param newSecondarySCP the new secondary SCP
     */
    public final void setSecondarySCP(final byte[] newSecondarySCP) {
        this.secondarySCP = Arrays.copyOf(newSecondarySCP, newSecondarySCP.length);
    }

    @Override
    public final String toString() {
        return "Preferred unit";
    }

}
