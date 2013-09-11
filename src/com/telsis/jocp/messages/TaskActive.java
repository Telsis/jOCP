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
 * Send this message to determine whether a particular task on the remote
 * platform is currently active (that is, running).
 * <p/>
 * This message is not addressed to the task it is querying (because if the task
 * isn't active, it cannot receive the message and reply). Instead, the forward
 * task ID is set to 0xFFFFFFFF.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>taskIDToCheck</td>
 * <td>4</td>
 * <td>The Task ID to be checked (ID on unit which receives request).</td>
 * </tr>
 * <tr>
 * <td>associatedTaskID</td>
 * <td>4</td>
 * <td>The Task ID associated with taskIDToCheck on unit sending request.</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * On Ocean call-handling platforms, the Task Active message is not directed to
 * caller tasks, but processed by a management task.
 *
 * @see TaskActiveResult
 * @author Telsis
 */
public class TaskActive extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.TASK_ACTIVE;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 8;

    /** The task id to check. */
    private int                         taskIDToCheck;
    /** The associated task id. */
    private int                         associatedTaskID;

    /**
     * Decode the buffer into a Task Active message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public TaskActive(final ByteBuffer buffer) throws OCPException {
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

        this.taskIDToCheck = buffer.getInt();
        this.associatedTaskID = buffer.getInt();
    }

    /**
     * Instantiates a new Task Active message.
     */
    public TaskActive() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putInt(taskIDToCheck);
        buffer.putInt(associatedTaskID);
    }

    /**
     * Gets the task ID to check.
     *
     * @return the task ID to check
     */
    public final int getTaskIDToCheck() {
        return taskIDToCheck;
    }

    /**
     * Sets the task ID to check.
     *
     * @param newTaskIDToCheck the new task ID to check
     */
    public final void setTaskIDToCheck(final int newTaskIDToCheck) {
        this.taskIDToCheck = newTaskIDToCheck;
    }

    /**
     * Gets the associated task ID.
     *
     * @return the associated task ID
     */
    public final int getAssociatedTaskID() {
        return associatedTaskID;
    }

    /**
     * Sets the associated task ID.
     *
     * @param newAssociatedTaskID the new associated task ID
     */
    public final void setAssociatedTaskID(final int newAssociatedTaskID) {
        this.associatedTaskID = newAssociatedTaskID;
    }

    @Override
    public final String toString() {
        return "Task active";
    }
}
