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
 * Send this message to a remote platform if the task to which a message from
 * that platform was directed is no longer running or if the leg of the call to
 * which the message was directed is no longer present.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>invalidLegID</td>
 * <td>2</td>
 * <td>The leg ID that isn't valid, or 0xFFFF if it's a Task ID that isn't
 * valid.</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * On an Ocean platform, the Abort message is sent by a management task if the
 * caller task to which the incoming message was directed is no longer running;
 * it is sent by a caller task if the leg of the call to which the incoming
 * message was directed is no longer present.
 * <p/>
 * To avoid recurring loops, a platform must not send an Abort message in
 * response to an Abort message that is addressed to an inactive task.
 *
 * @author Telsis
 */
public class Abort extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.ABORT;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 2;

    /** Set invalidLegID to this if it's a Task ID that isn't valid. */
    public static final short           TASK_NOT_RUNNING = (short) 0xFFFF;

    /**
     * The leg ID that isn't valid, or TASK_NOT_RUNNING if it's a Task ID that
     * isn't valid.
     */
    private short                       invalidLegID;

    /**
     * Decode the buffer into an Abort message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public Abort(final ByteBuffer buffer) throws OCPException {
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

        this.invalidLegID = buffer.getShort();
    }

    /**
     * Instantiates a new Abort message.
     */
    public Abort() {
        super(TYPE.getCommandCode());
    }

    /**
     * Instantiates a new Abort message for the specified leg.
     *
     * @param newInvalidLegID the invalid leg ID
     */
    public Abort(final short newInvalidLegID) {
        this();
        this.invalidLegID = newInvalidLegID;
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(invalidLegID);
    }

    /**
     * Sets the invalid leg ID.
     *
     * @param newInvalidLegID the new invalid leg ID
     */
    public final void setInvalidLegID(final short newInvalidLegID) {
        this.invalidLegID = newInvalidLegID;
    }

    /**
     * Gets the invalid leg ID.
     *
     * @return the invalid leg ID
     */
    public final short getInvalidLegID() {
        return invalidLegID;
    }

    @Override
    public final String toString() {
        return "Abort";
    }

}
