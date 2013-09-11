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
 * Send this message when the active link between the sending and remote
 * platforms is down or if the sending platform needs to swap links to recover
 * from errors (for example, errors caused by corrupt data packets). In an Ocean
 * installation, the message tells the remote platform to activate the link to
 * the fastSSP's other signalling card. On receipt of the message, the remote
 * platform activates the specified link and sends subsequent messages to the
 * fastSSP down that link.
 * <p/>
 * The expected reply to this message is {@link ChangeActiveLinkAck Change
 * Active Link Acknowledge}.
 * <p/>
 * This message has the following parameter:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>thisLink</td>
 * <td>2</td>
 * <td>1 = make this link active<br>
 * 0 = make other link active</td>
 * </tr>
 * </table>
 *
 * @see ChangeActiveLinkAck
 * @author Telsis
 */
public class ChangeActiveLink extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.CHANGE_ACTIVE_LINK;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 2;

    /** The this link field. */
    private short                       thisLink;

    /**
     * Decode the buffer into a Change Active Link message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public ChangeActiveLink(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.thisLink = buffer.getShort();
    }

    /**
     * Instantiates a new Change Active Link message.
     */
    public ChangeActiveLink() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(this.thisLink);
    }

    /**
     * Gets the this link.
     *
     * @return the this link
     */
    public final short getThisLink() {
        return thisLink;
    }

    /**
     * Sets the this link.
     *
     * @param newThisLink the new this link
     */
    public final void setThisLink(final short newThisLink) {
        this.thisLink = newThisLink;
    }

    @Override
    public final String toString() {
        return "Change Active Link";
    }

}
