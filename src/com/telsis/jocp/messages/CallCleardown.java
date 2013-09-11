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
 * Send this message to tell an SCP that a call has cleared down.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>destLegID</td>
 * <td>2</td>
 * <td>SCP's Leg ID for cleared party</td>
 * </tr>
 * <tr>
 * <td>rawCause</td>
 * <td>1</td>
 * <td>Signalling scheme's cause of cleardown, if available</td>
 * </tr>
 * <tr>
 * <td>cause</td>
 * <td>1</td>
 * <td>Q850 reason for cleardown</td>
 * </tr>
 * <tr>
 * <td>answerTime</td>
 * <td>4</td>
 * <td>Time from A party arriving when this Leg answered (10ths second)</td>
 * </tr>
 * <tr>
 * <td>clearTime</td>
 * <td>4</td>
 * <td>Time from A party arriving when this Leg cleared (10ths second)</td>
 * </tr>
 * </table>
 *
 * @see RequestCleardown
 * @author Telsis
 */
public class CallCleardown extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.CALL_CLEARDOWN;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 12;

    /** The SCP's Leg ID for cleared party. */
    private short                       destLegID;
    /** The signalling scheme's cause of cleardown, if available. */
    private byte                        rawCause;
    /** The Q850 reason for cleardown. */
    private byte                        cause;
    /** The time from A party arriving when this Leg answered (10ths second). */
    private int                         answerTime;
    /** The time from A party arriving when this Leg cleared (10ths second). */
    private int                         clearTime;

    /**
     * Decode the buffer into a Call Cleardown message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public CallCleardown(final ByteBuffer buffer) throws OCPException {
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

        destLegID = buffer.getShort();
        rawCause = buffer.get();
        cause = buffer.get();
        answerTime = buffer.getInt();
        clearTime = buffer.getInt();
    }

    /**
     * Instantiates a new Call Cleardown message.
     */
    public CallCleardown() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(destLegID);
        buffer.put(rawCause);
        buffer.put(cause);
        buffer.putInt(answerTime);
        buffer.putInt(clearTime);
    }

    /**
     * Gets the dest leg ID.
     *
     * @return the dest leg ID
     */
    public final short getDestLegID() {
        return destLegID;
    }

    /**
     * Sets the dest leg ID.
     *
     * @param newDdestLegID the new dest leg ID
     */
    public final void setDestLegID(final short newDdestLegID) {
        this.destLegID = newDdestLegID;
    }

    /**
     * Gets the raw cause.
     *
     * @return the raw cause
     */
    public final byte getRawCause() {
        return rawCause;
    }

    /**
     * Sets the raw cause.
     *
     * @param newRawCause the new raw cause
     */
    public final void setRawCause(final byte newRawCause) {
        this.rawCause = newRawCause;
    }

    /**
     * Gets the cause.
     *
     * @return the cause
     */
    public final byte getCause() {
        return cause;
    }

    /**
     * Sets the cause.
     *
     * @param newCause the new cause
     */
    public final void setCause(final byte newCause) {
        this.cause = newCause;
    }

    /**
     * Gets the answer time.
     *
     * @return the answer time
     */
    public final int getAnswerTime() {
        return answerTime;
    }

    /**
     * Sets the answer time.
     *
     * @param newAnswerTime the new answer time
     */
    public final void setAnswerTime(final int newAnswerTime) {
        this.answerTime = newAnswerTime;
    }

    /**
     * Gets the clear time.
     *
     * @return the clear time
     */
    public final int getClearTime() {
        return clearTime;
    }

    /**
     * Sets the clear time.
     *
     * @param newClearTime the new clear time
     */
    public final void setClearTime(final int newClearTime) {
        this.clearTime = newClearTime;
    }

    @Override
    public final String toString() {
        return "Call Cleardown";
    }

}
