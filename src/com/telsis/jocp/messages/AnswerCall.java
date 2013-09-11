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
 * Send this message to instruct a call-handling platform to answer a call and
 * to set up a forward audio path (a forward audio path allows the calling party
 * to be heard by the called party). The expected reply to this message is
 * {@link AnswerResult Answer Result}.
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
 * <td>Leg ID on call handling unit to act upon</td>
 * </tr>
 * <tr>
 * <td>ansMode</td>
 * <td>2</td>
 * <td>0 = Charged<br/>
 * 1 = Free</td>
 * </tr>
 * <tr>
 * <td>spare</td>
 * <td>1</td>
 * <td><i>For word alignment</i></td>
 * </tr>
 * <tr>
 * <td>zipNumber</td>
 * <td>1</td>
 * <td>The index into the zip table on the SCP for this result</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * The fastSSP always answers calls as charged. If the Answer Call message's
 * Answer Mode field is received with bit 0 unset (that is, the call is not to
 * be answered as charged), the fastSSP will return a
 * {@link CallCommandUnsupported Call Command Error} message to the fastSCP.
 * <p/>
 * Note that charging is the domain of the network. Actual charges applied are
 * ultimately the responsibility of the billing system. Telsis disclaims all
 * liability connected to call charging and any loss of revenue.
 *
 * @author Telsis
 */
public class AnswerCall extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
            LegacyOCPMessageTypes.ANSWER_CALL;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 6;

    /** The Leg ID on call handling unit to act upon. */
    private short                       destLegID;
    /** The answer mode. */
    private short                       ansMode;
    /** The The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;

    /**
     * Decode the buffer into an Answer Call message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public AnswerCall(final ByteBuffer buffer) throws OCPException {
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
        ansMode = buffer.getShort();
        buffer.get(); // for word alignment
        zipNumber = buffer.get();
    }

    /**
     * Instantiates a new Answer Call message.
     */
    public AnswerCall() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(destLegID);
        buffer.putShort(ansMode);
        buffer.put((byte) 0);
        buffer.put(zipNumber);
    }

    /**
     * Gets the destination leg ID.
     *
     * @return the destination leg ID
     */
    public final short getDestLegID() {
        return destLegID;
    }

    /**
     * Sets the destination leg ID.
     *
     * @param newDestLegID the new destination leg ID
     */
    public final void setDestLegID(final short newDestLegID) {
        this.destLegID = newDestLegID;
    }

    /**
     * Gets the answer mode.
     *
     * @return the answer mode
     */
    public final short getAnsMode() {
        return ansMode;
    }

    /**
     * Sets the answer mode.
     *
     * @param newAnsMode the new answer mode
     */
    public final void setAnsMode(final short newAnsMode) {
        this.ansMode = newAnsMode;
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

    @Override
    public final String toString() {
        return "Answer Call";
    }
}
