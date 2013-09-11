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
 * Send this message in response to the {@link DeliverTo Deliver To} message.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>origLegID</td>
 * <td>2</td>
 * <td>Leg ID for the new party on call handling unit</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>1</td>
 * <td>Bit 7 = Outdial succeeded</td>
 * </tr>
 * <tr>
 * <td>zipNumber</td>
 * <td>1</td>
 * <td>The index into the zip table on the SCP for this result</td>
 * </tr>
 * <tr>
 * <td>rawFailureReason</td>
 * <td>1</td>
 * <td>Signalling scheme's reason for failure, if available</td>
 * </tr>
 * <tr>
 * <td>outdialFailureReason</td>
 * <td>1</td>
 * <td>Q850 reason for outdial failing</td>
 * </tr>
 * <tr>
 * <td>time</td>
 * <td>4</td>
 * <td>Time from A party arriving when outdial started (10ths second)</td>
 * </tr>
 * </table>
 *
 * @see DeliverTo
 * @author Telsis
 */
public class DeliverToResult extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.DELIVER_TO_RESULT;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 10;

    /** Set this bit in flags to indicate that outdial succeeded. */
    public static final byte            FLAG_OUTDIAL_SUCCEEDED = (byte) 0x80;

    /** The leg ID for the new party on call handling unit. */
    private short                       origLegID;
    /** The flags. */
    private byte                        flags;
    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The signalling scheme's reason for failure, if available. */
    private byte                        rawFailureReason;
    /** The Q850 reason for outdial failing. */
    private byte                        outdialFailureReason;
    /** The time from A party arriving when outdial started (10ths second). */
    private int                         time;

    /**
     * Decode the buffer into a Deliver To Result message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public DeliverToResult(final ByteBuffer buffer) throws OCPException {
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

        origLegID = buffer.getShort();
        flags = buffer.get();
        zipNumber = buffer.get();
        rawFailureReason = buffer.get();
        outdialFailureReason = buffer.get();
        time = buffer.getInt();
    }

    /**
     * Instantiates a new Deliver To Result message.
     */
    public DeliverToResult() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(origLegID);
        buffer.put(flags);
        buffer.put(zipNumber);
        buffer.put(rawFailureReason);
        buffer.put(outdialFailureReason);
        buffer.putInt(time);
    }

    /**
     * Gets the orig leg ID.
     *
     * @return the orig leg ID
     */
    public final short getOrigLegID() {
        return origLegID;
    }

    /**
     * Sets the orig leg ID.
     *
     * @param newOrigLegID the new orig leg ID
     */
    public final void setOrigLegID(final short newOrigLegID) {
        this.origLegID = newOrigLegID;
    }

    /**
     * Gets the flags.
     *
     * @return the flags
     */
    public final byte getFlags() {
        return flags;
    }

    /**
     * Sets the flags.
     *
     * @param newFlags the new flags
     */
    public final void setFlags(final byte newFlags) {
        this.flags = newFlags;
    }

    /**
     * Gets the zip number.
     *
     * @return the zip number
     */
    public final byte getZipNumber() {
        return zipNumber;
    }

    /**
     * Sets the zip number.
     *
     * @param newZipNumber the new zip number
     */
    public final void setZipNumber(final byte newZipNumber) {
        this.zipNumber = newZipNumber;
    }

    /**
     * Gets the raw failure reason.
     *
     * @return the raw failure reason
     */
    public final byte getRawFailureReason() {
        return rawFailureReason;
    }

    /**
     * Sets the raw failure reason.
     *
     * @param newRawFailureReason the new raw failure reason
     */
    public final void setRawFailureReason(final byte newRawFailureReason) {
        this.rawFailureReason = newRawFailureReason;
    }

    /**
     * Gets the outdial failure reason.
     *
     * @return the outdial failure reason
     */
    public final byte getOutdialFailureReason() {
        return outdialFailureReason;
    }

    /**
     * Sets the outdial failure reason.
     *
     * @param newOutdialFailureReason the new outdial failure reason
     */
    public final void setOutdialFailureReason(
            final byte newOutdialFailureReason) {
        this.outdialFailureReason = newOutdialFailureReason;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public final int getTime() {
        return time;
    }

    /**
     * Sets the time.
     *
     * @param newTime the new time
     */
    public final void setTime(final int newTime) {
        this.time = newTime;
    }

    @Override
    public final String toString() {
        return "Deliver To result";
    }

}
