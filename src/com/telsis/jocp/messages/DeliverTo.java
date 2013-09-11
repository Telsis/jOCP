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

import com.telsis.jocp.CallMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;
import com.telsis.jocp.OCPTelno;

/**
 * Send this message to instruct a call-handling platform to outdial using the
 * specified number and connect an existing caller to the outdialled party. The
 * expected reply to this message is {@link DeliverToResult Deliver To Result}.
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
 * <td>origLegID</td>
 * <td>2</td>
 * <td>SCP's Leg ID for the new party</td>
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
 * <tr>
 * <td>cliMode</td>
 * <td>2</td>
 * <td>0 if no CLI to be supplied<br/>
 * 1 if unit to use A party's CLI<br/>
 * 2 if CLI specified</td>
 * </tr>
 * <tr>
 * <td>timeout</td>
 * <td>2</td>
 * <td>Time to wait for answer</td>
 * </tr>
 * <tr>
 * <td>spare</td>
 * <td>1</td>
 * <td><i>For word alignment</i></td>
 * </tr>
 * <tr>
 * <td>outdialNoTypePlan</td>
 * <td>1</td>
 * <td>Q931 Type in high nibble and Plan in low nibble for outdialled number</td>
 * </tr>
 * <tr>
 * <td>outdialNo</td>
 * <td>18</td>
 * <td>2 byte length followed by 32 packed digits</td>
 * </tr>
 * <tr>
 * <td>cliPresScreen</td>
 * <td>1</td>
 * <td>Q931 CLI Presentation and Screening indicators (only meaningful if
 * CLIMode is 2)</td>
 * </tr>
 * <tr>
 * <td>cliTypePlan</td>
 * <td>1</td>
 * <td>Q931 Type in high nibble and Plan in low nibble for CLI (only meaningful
 * if CLIMode is 2)</td>
 * </tr>
 * <tr>
 * <td>cliNo</td>
 * <td>18</td>
 * <td>2 byte length followed by 32 packed digits (only meaningful if CLIMode is
 * 2)</td>
 * </tr>
 * </table>
 *
 * @see DeliverToResult
 * @author Telsis
 */
public class DeliverTo extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.DELIVER_TO;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 50;

    /** The length of the outdialNo field. */
    private static final int            OUTDIAL_NO_LENGTH = 18;
    /** The length of the CLI field. */
    private static final int            CLI_LENGTH = 18;

    /** Set cliMode to this to indicate that no CLI is to be supplied. */
    public static final short           CLI_MODE_USE_NONE      = 0;
    /** Set cliMode to this to indicate to use the A party's CLI. */
    public static final short           CLI_MODE_USE_A_PARTY   = 1;
    /** Set cliMode to this to indicate to use the specified CLI. */
    public static final short           CLI_MODE_USE_SPECIFIED = 2;

    /** The Leg ID on call handling unit to act upon. */
    private short                       destLegID;
    /** The SCP's Leg ID for the new party. */
    private short                       origLegID;
    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The CLI mode. */
    private short                       cliMode;
    /** The Time to wait for answer. */
    private short                       timeout;
    /** The outdial type and plan. */
    private byte                        outdialNoTypePlan;
    /** The outdial number. */
    private byte[]                      outdialNo = new byte[OUTDIAL_NO_LENGTH];
    /** The CLI Presentation and Screening indicators. */
    private byte                        cliPresScreen;
    /** The CLI type and plan. */
    private byte                        cliTypePlan;
    /** The CLI. */
    private byte[]                      cliNo = new byte[CLI_LENGTH];

    /**
     * Decode the buffer into a DeliverTo message.
     *
     * @param buffer
     *            the message to decode
     * @param minLength
     *            the minimum length of the message
     * @param maxLength
     *            the maximum length of the message
     * @param decode
     *            true if the payload should be decoded; false to skip decoding
     *            the payload. For use by subclasses that need to decode fields
     *            in a different order
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    protected DeliverTo(final ByteBuffer buffer, final int minLength,
            final int maxLength, final boolean decode) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() < minLength
                || buffer.limit() > maxLength) {
            throw new CallMessageException(
                    getDestTID(),
                    getOrigTID(),
                    this.getCommandCode(),
                    CallCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        if (!decode) {
            return;
        }

        destLegID = buffer.getShort();
        origLegID = buffer.getShort();
        buffer.get(); // for word alignment
        zipNumber = buffer.get();
        cliMode = buffer.getShort();
        timeout = buffer.getShort();
        buffer.get(); // for word alignment
        outdialNoTypePlan = buffer.get();
        buffer.get(outdialNo);
        cliPresScreen = buffer.get();
        cliTypePlan = buffer.get();
        buffer.get(cliNo);
    }

    /**
     * Decode the buffer into a Deliver To message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public DeliverTo(final ByteBuffer buffer) throws OCPException {
        this(buffer, EXPECTED_LENGTH, EXPECTED_LENGTH, true);
    }

    /**
     * Calls {@link CallControlMessage#CallControlMessage(short)} directly with
     * the specified message type. For use by subclasses.
     *
     * @param commandCode
     *            the command code
     */
    protected DeliverTo(final short commandCode) {
        super(commandCode);
    }

    /**
     * Instantiates a new Deliver To message.
     */
    public DeliverTo() {
        this(TYPE.getCommandCode());
    }

    /**
     * Encode the header and payload into the buffer. This modifies the buffer
     * in-place and sets the buffer's position to the end of the payload.
     *
     * @param buffer
     *            The buffer to insert the message into
     * @param encode
     *            true if the payload should be encoded; false to skip encoding
     *            the payload. For use by subclasses that need to encode fields
     *            in a different order
     */
    // CSOFF: DesignForExtension
    protected void encode(final ByteBuffer buffer, final boolean encode) {
        // CSON: DesignForExtension
        super.encode(buffer);

        if (!encode) {
            return;
        }

        buffer.putShort(destLegID);
        buffer.putShort(origLegID);
        buffer.put((byte) 0); // for word alignment
        buffer.put(zipNumber);
        buffer.putShort(cliMode);
        buffer.putShort(timeout);
        buffer.put((byte) 0); // for word alignment
        buffer.put(outdialNoTypePlan);
        buffer.put(outdialNo);
        buffer.put(cliPresScreen);
        buffer.put(cliTypePlan);
        buffer.put(cliNo);
    }

    @Override // CSIGNORE: DesignForExtension
    protected void encode(final ByteBuffer buffer) {
        encode(buffer, true);
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
     * @param newdestLegID
     *            the new dest leg ID
     */
    public final void setDestLegID(final short newdestLegID) {
        this.destLegID = newdestLegID;
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
     * @param neworigLegID
     *            the new orig leg ID
     */
    public final void setOrigLegID(final short neworigLegID) {
        this.origLegID = neworigLegID;
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
     * @param newzipNumber
     *            the new zip number
     */
    public final void setZipNumber(final byte newzipNumber) {
        this.zipNumber = newzipNumber;
    }

    /**
     * Gets the CLI mode.
     *
     * @return the CLI mode
     */
    public final short getCliMode() {
        return cliMode;
    }

    /**
     * Sets the CLI mode.
     *
     * @param newcliMode
     *            the new CLI mode
     */
    public final void setCliMode(final short newcliMode) {
        this.cliMode = newcliMode;
    }

    /**
     * Gets the timeout.
     *
     * @return the timeout
     */
    public final short getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout.
     *
     * @param newtimeout
     *            the new timeout
     */
    public final void setTimeout(final short newtimeout) {
        this.timeout = newtimeout;
    }

    /**
     * Gets the outdial type and plan.
     *
     * @return the outdial type and plan
     */
    public final byte getOutdialNoTypePlan() {
        return outdialNoTypePlan;
    }

    /**
     * Sets the outdial type and plan.
     *
     * @param newoutdialNoTypePlan
     *            the new outdial type and plan
     */
    public final void setOutdialNoTypePlan(final byte newoutdialNoTypePlan) {
        this.outdialNoTypePlan = newoutdialNoTypePlan;
    }

    /**
     * Gets the outdial digits.
     *
     * @return the outdial digits
     */
    public final byte[] getOutdialNo() {
        return Arrays.copyOf(outdialNo, outdialNo.length);
    }

    /**
     * Sets the outdial digits.
     *
     * @param newoutdialNo
     *            the new outdial digits
     */
    public final void setOutdialNo(final byte[] newoutdialNo) {
        this.outdialNo = Arrays.copyOf(newoutdialNo, newoutdialNo.length);
    }

    /**
     * Gets the CLI digits, type and plan.
     *
     * @return the CLI digits, type and plan
     */
    public final OCPTelno getOutdialTelno() {
        return new OCPTelno(this.outdialNoTypePlan, this.outdialNo);
    }

    /**
     * Sets the CLI digits, type and plan.
     *
     * @param newOutdial the new CLI digits, type and plan
     */
    public final void setOutdialTelno(final OCPTelno newOutdial) {
        this.outdialNoTypePlan = newOutdial.getTypePlan();
        this.outdialNo = newOutdial.getTelno();
    }

    /**
     * Gets the CLI Presentation and Screening indicators.
     *
     * @return the CLI Presentation and Screening indicators
     */
    public final byte getCliPresScreen() {
        return cliPresScreen;
    }

    /**
     * Sets the CLI Presentation and Screening indicators.
     *
     * @param newcliPresScreen
     *            the new CLI Presentation and Screening indicators
     */
    public final void setCliPresScreen(final byte newcliPresScreen) {
        this.cliPresScreen = newcliPresScreen;
    }

    /**
     * Gets the CLI type and plan.
     *
     * @return the CLI type and plan
     */
    public final byte getCliTypePlan() {
        return cliTypePlan;
    }

    /**
     * Sets the CLI type and plan.
     *
     * @param newcliTypePlan
     *            the new CLI type and plan
     */
    public final void setCliTypePlan(final byte newcliTypePlan) {
        this.cliTypePlan = newcliTypePlan;
    }

    /**
     * Gets the CLI digits.
     *
     * @return the CLI digits
     */
    public final byte[] getCliNo() {
        return Arrays.copyOf(cliNo, cliNo.length);
    }

    /**
     * Sets the CLI digits.
     *
     * @param newcliNo
     *            the new CLI digits
     */
    public final void setCliNo(final byte[] newcliNo) {
        this.cliNo = Arrays.copyOf(newcliNo, newcliNo.length);
    }

    /**
     * Gets the CLI digits, type and plan.
     *
     * @return the CLI digits, type and plan
     */
    public final OCPTelno getCLITelno() {
        return new OCPTelno(this.cliTypePlan, this.cliNo);
    }

    /**
     * Sets the CLI digits, type and plan.
     *
     * @param newCLI the new CLI digits, type and plan
     */
    public final void setCLITelno(final OCPTelno newCLI) {
        this.cliTypePlan = newCLI.getTypePlan();
        this.cliNo = newCLI.getTelno();
    }

    @Override // CSIGNORE: DesignForExtension
    public String toString() {
        return "Deliver To";
    }

}
