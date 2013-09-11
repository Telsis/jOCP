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

/**
 * Send this message to tell the remote platform to continue processing a call
 * that is suspended at a detection point.
 * <p/>
 * This message has no parameters.
 * <p/>
 * <b>Notes</b>
 * <p/>
 * Detection points for the key stages of a call (for example, call start, call
 * cleardown) are armed on a call-handling platform in the INAP network. When a
 * detection point is encountered in a call, the service switching point stops
 * processing that call until it is instructed to resume by the fastSCP (via the
 * fastTC).
 * <p/>
 * This message is sent where the subsequent control of the call is to be
 * handled by the network's call-handling platform rather than the fastSCP.
 * <p/>
 * After this message is sent, the task on the fastSCP stops running and no more
 * detection points are triggered on the call-handling platform.
 *
 * @author Telsis
 */
public class INAPContinue extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.INAP_CONTINUE;

    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 0;

    /**
     * Decode the buffer into an INAP Continue message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public INAPContinue(final ByteBuffer buffer) throws OCPException {
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
    }

    /**
     * Instantiates a new INAP Continue message.
     */
    public INAPContinue() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
    }

    @Override
    public final String toString() {
        return "INAP continue";
    }

}
