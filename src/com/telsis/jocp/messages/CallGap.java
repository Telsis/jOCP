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
 * Send this message to tell the remote platform to gap calls in accordance with
 * the specified criteria.
 * <p/>
 * Call-gapping is an overload protection mechanism designed to reduce the risk
 * of the fastSCP running out of resources when controlling calls. In an Ocean
 * installation, when a fastSCP detects that it is starting to become
 * overloaded, it sends this message to the call-handling platform(s) indicating
 * that its resources are low. The remote call-handling platform(s) then stop
 * sending information on new calls to the fastSCP, until it can resume
 * controlling calls. Call gapping does not mean a loss of service to current
 * callers, only that new calls are not accepted by the overloaded fastSCP.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>duration</td>
 * <td>2</td>
 * <td>0 = cancel gapping<br>
 * -1 = infinite<br>
 * <i>-2 = network specific [for future use]</i></td>
 * </tr>
 * <tr>
 * <td>interval</td>
 * <td>2</td>
 * <td>0 = reject all calls not meeting criteria<br>
 * -1 = reject all calls meeting criteria</td>
 * </tr>
 * <tr>
 * <td>messageID</td>
 * <td>4</td>
 * <td>File number to play, -1 = no file</td>
 * </tr>
 * <tr>
 * <td>releaseCause</td>
 * <td>2</td>
 * <td>If high byte = 1 then raw reason in low byte, if 0 then Q850 reason</td>
 * </tr>
 * <tr>
 * <td>controlType</td>
 * <td>1</td>
 * <td>0 = call gapping automatically activated<br>
 * +1 = call gapping manually activated</td>
 * </tr>
 * <tr>
 * <td>criteriaType</td>
 * <td>1</td>
 * <td>0 - No criteria (All calls meet criteria)<br>
 * <i>1 - Criteria is a called address [for future use]</i><br>
 * <i>2 - Criteria is a service key [for future use]</i><br>
 * <i>3 - Criteria is a calling address [for future use]</i></td>
 * </tr>
 * <tr>
 * <td>criteria</td>
 * <td>18</td>
 * <td>(2 byte length + 32 packed digits)<br>
 * <i>Currently always 0 [for future use]</i></td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * This message applies only to load-sharing fastSCPs.
 *
 * @author Telsis
 */
public class CallGap extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.CALL_GAP;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 30;

    /** The length of the criteria field. */
    private static final int            CRITERIA_LENGTH = 18;

    /**
     * Set duration to this to indicate that network specific gapping is to be
     * used.
     */
    public static final short           DURATION_NETWORK_SPECIFIC = -2;
    /** Set duration to this to indicate that gapping is on indefinitely. */
    public static final short           DURATION_INDEFINITE       = -1;
    /** Set duration to this to disable gapping. */
    public static final short           DURATION_DISABLED         = 0;

    /** The duration. */
    private short                       duration;
    /** The interval. */
    private short                       interval;
    /** The file number to play. */
    private int                         messageID;
    /** The release cause. */
    private short                       releaseCause;
    /** The control type. */
    private byte                        controlType;
    /** The criteria type. */
    private byte                        criteriaType;
    /** The criteria. */
    private byte[]                      criteria = new byte[CRITERIA_LENGTH];

    /**
     * Decode the buffer into a Call Gap message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public CallGap(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        this.duration = buffer.getShort();
        this.interval = buffer.getShort();
        this.messageID = buffer.getInt();
        this.releaseCause = buffer.getShort();
        this.controlType = buffer.get();
        this.criteriaType = buffer.get();
        buffer.get(this.criteria);
    }

    /**
     * Instantiates a new Call Gap message.
     */
    public CallGap() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(this.duration);
        buffer.putShort(this.interval);
        buffer.putInt(this.messageID);
        buffer.putShort(this.releaseCause);
        buffer.put(this.controlType);
        buffer.put(this.criteriaType);
        buffer.put(this.criteria);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public final short getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param newDuration the new duration
     */
    public final void setDuration(final short newDuration) {
        this.duration = newDuration;
    }

    /**
     * Gets the interval.
     *
     * @return the interval
     */
    public final short getInterval() {
        return interval;
    }

    /**
     * Sets the interval.
     *
     * @param newInterval the new interval
     */
    public final void setInterval(final short newInterval) {
        this.interval = newInterval;
    }

    /**
     * Gets the message ID.
     *
     * @return the message ID
     */
    public final int getMessageID() {
        return messageID;
    }

    /**
     * Sets the message ID.
     *
     * @param newMessageID the new message ID
     */
    public final void setMessageID(final int newMessageID) {
        this.messageID = newMessageID;
    }

    /**
     * Gets the release cause.
     *
     * @return the release cause
     */
    public final short getReleaseCause() {
        return releaseCause;
    }

    /**
     * Sets the release cause.
     *
     * @param newReleaseCause the new release cause
     */
    public final void setReleaseCause(final short newReleaseCause) {
        this.releaseCause = newReleaseCause;
    }

    /**
     * Gets the control type.
     *
     * @return the control type
     */
    public final byte getControlType() {
        return controlType;
    }

    /**
     * Sets the control type.
     *
     * @param newControlType the new control type
     */
    public final void setControlType(final byte newControlType) {
        this.controlType = newControlType;
    }

    /**
     * Gets the criteria type.
     *
     * @return the criteria type
     */
    public final byte getCriteriaType() {
        return criteriaType;
    }

    /**
     * Sets the criteria type.
     *
     * @param newCriteriaType the new criteria type
     */
    public final void setCriteriaType(final byte newCriteriaType) {
        this.criteriaType = newCriteriaType;
    }

    /**
     * Gets the criteria.
     *
     * @return the criteria
     */
    public final byte[] getCriteria() {
        return Arrays.copyOf(criteria, criteria.length);
    }

    /**
     * Sets the criteria.
     *
     * @param newCriteria the new criteria
     */
    public final void setCriteria(final byte[] newCriteria) {
        this.criteria = Arrays.copyOf(newCriteria, newCriteria.length);
    }

    @Override
    public final String toString() {
        return "Call gap";
    }
}
