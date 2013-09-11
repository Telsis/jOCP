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
 * Send this message to force the remote master/slave platform to run as master.
 * If the platform is already running as master, it will ignore this message.
 * <p/>
 * This message has no parameters.
 * <p/>
 * <b>Notes:</b>
 * <p/>
 * In an Ocean installation, active telephone calls may or may not be lost when
 * a master fastSCP fails and a slave fastSCP is promoted to master. This
 * depends on the following:
 * <ul>
 * <li>If the former master fastSCP was processing a call when the former slave
 * fastSCP received this message, then that call is lost and the call is
 * disconnected. A "live" call cannot be transferred between fastSCPs. However,
 * if the former master had already instructed the call-handling platform how to
 * handle the call, the call is not lost and the caller is unaffected.
 * Persistent data is not lost because all of the persistent parameters are
 * copied to the new master fastSCP before the swap is completed (provided the
 * link between the two fastSCPs remains intact).</li>
 * <li>If the former master fastSCP was removed from service due to a serious
 * failure, for example, the local power or the LAN failed, persistent data may
 * have been lost. This is because persistent parameters are copied between
 * fastSCPs only as a background process. However, to overcome this problem to
 * some extent, the newly promoted master fastSCP (on receipt of this message)
 * notifies the service running on it that a swap has occurred and that some
 * data may be corrupt. Therefore, the service can decide whether or not action
 * must be taken.</li>
 * </ul>
 *
 * @author Telsis
 */
public class BecomeMaster extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.BECOME_MASTER;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 0;

    /**
     * Decode the buffer into a Become Master message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public BecomeMaster(final ByteBuffer buffer) throws OCPException {
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
     * Instantiates a new Become Master message.
     */
    public BecomeMaster() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
    }

    @Override
    public final String toString() {
        return "Become Master";
    }
}
