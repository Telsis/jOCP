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
 * Send this message to check the status of the link between the sending and
 * remote platforms.
 * <p/>
 * Heartbeat is useful for trapping the situation where there is no
 * inter-platform communication. Ocean platforms send this message every 10
 * seconds and also after 1 second of silence (that is, inactivity on a link).
 * <p/>
 * This message has no parameters.
 *
 * @author Telsis
 */
public class Heartbeat extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.HEARTBEAT;

    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 0;

    /**
     * Decode the buffer into a Heartbeat message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public Heartbeat(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }
    }

    /**
     * Instantiates a new Heartbeat message.
     */
    public Heartbeat() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
    }

    @Override
    public final String toString() {
        return "Hearthbeat";
    }
}
