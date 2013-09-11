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
 * Send this message in response to a {@link TaskActive Task Active} message.
 * The message content indicates whether or not the specified caller task is
 * active.
 * <p/>
 * The task that is being checked for activity or inactivity does not send a
 * reply to the fastSCP (because if the task is inactive, it cannot receive or
 * send messages). As with the Task Active message, this message is sent to the
 * receiving platform with a 0xFFFFFFFF forward task ID in the header
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>taskID</td>
 * <td>4</td>
 * <td>Task ID on unit receiving message associated with task that's been
 * checked.</td>
 * </tr>
 * <tr>
 * <td>result</td>
 * <td>2</td>
 * <td>0 for Task not active<br/>
 * 1 for Task active.</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * On an Ocean platform, the Task Active Result message is sent by a management
 * task.
 *
 * @see TaskActive
 * @author Telsis
 */
public class TaskActiveResult extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.TASK_ACTIVE_RESULT;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 6;

    /** The task id. */
    private int                         taskID;
    /** The result. */
    private short                       result;

    /**
     * Decode the buffer into a Task Active Result message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public TaskActiveResult(final ByteBuffer buffer) throws OCPException {
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

        this.taskID = buffer.getInt();
        this.result = buffer.getShort();
    }

    /**
     * Instantiates a new Task Active Result.
     */
    public TaskActiveResult() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putInt(taskID);
        buffer.putShort(result);
    }

    /**
     * Gets the task ID.
     *
     * @return the task ID
     */
    public final int getTaskID() {
        return taskID;
    }

    /**
     * Sets the task ID.
     *
     * @param newTaskID the new task ID
     */
    public final void setTaskID(final int newTaskID) {
        this.taskID = newTaskID;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public final short getResult() {
        return result;
    }

    /**
     * Sets the result.
     *
     * @param newResult the new result
     */
    public final void setResult(final short newResult) {
        this.result = newResult;
    }

    @Override
    public final String toString() {
        return "Task active response";
    }
}
