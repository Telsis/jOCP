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
 * Send this message to inform an SCP about the arrival of a call. The expected
 * reply to this message is {@link InitialDPResponse Initial DP Response}.
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
 * <td>The Leg ID for the new call on the call handling unit</td>
 * </tr>
 * <tr>
 * <td>CPC</td>
 * <td>2</td>
 * <td>Calling Party Category - bitmap as used in SSP, 0xFFFF if not available</td>
 * </tr>
 * <tr>
 * <td>spare</td>
 * <td>1</td>
 * <td><i>For word alignment</i></td>
 * </tr>
 * <tr>
 * <td>FINTypePlan</td>
 * <td>1</td>
 * <td>Q931 Type in high nibble and Plan in low nibble for indialled number</td>
 * </tr>
 * <tr>
 * <td>FIN</td>
 * <td>18</td>
 * <td>Indialled number - 2 byte length followed by 32 packed digits</td>
 * </tr>
 * <tr>
 * <td>CLIPresScreen</td>
 * <td>1</td>
 * <td>Q931 CLI Presentation and Screening indicators</td>
 * </tr>
 * <tr>
 * <td>CLITypePlan</td>
 * <td>1</td>
 * <td>Q931 Type in high nibble and Plan in low nibble for CLI</td>
 * </tr>
 * <tr>
 * <td>CLI</td>
 * <td>18</td>
 * <td>CLI - 2 byte length followed by 32 packed digits</td>
 * </tr>
 * <tr>
 * <td>time</td>
 * <td>4</td>
 * <td>Ocean Time of call arriving</td>
 * </tr>
 * </table>
 * <b>Notes</b>
 * <p/>
 * For a given call, the sending platform sends either this message or
 * {@link InitialDPServiceKey Initial DP Service Key}. Message Initial DP
 * Service Key is sent if the in-rule for the call on the sending platform has
 * been configured (via the Route application for the Ocean fastSSP) to use
 * service keys. If it hasn't been configured, this message is sent.
 *
 * @see InitialDPServiceKey
 * @see InitialDPResponse
 * @author Telsis
 */
public class InitialDP extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.INITIAL_DP;
    /** The minimum length of this message (main part). */
    protected static final int          MIN_LENGTH = 48;
    /** The maximum length of this message (main part). */
    protected static final int          MAX_LENGTH = MIN_LENGTH;
    /** The length of the optional part of this message. */
    protected static final int          OPT_LENGTH = 20;

    /** The length of the FIN field. */
    protected static final int          FIN_LENGTH = 18;
    /** The length of the CLI field. */
    protected static final int          CLI_LENGTH = 18;
    /** The length of the Redirecting Number field. */
    protected static final int          REDIR_LENGTH = 18;

    /** Indicates that the calling party category is not available. */
    public static final short           CPC_UNUSED = (short) 0xFFFF;

    // CSOFF: VisibilityModifier

    /** The leg ID for the new call on the call handling unit. */
    protected short                     origLegID;
    /** The Calling Party Category. */
    protected short                     cpc;
    /** The indialled number type and plan. */
    protected byte                      finTypePlan;
    /** The indialled number. */
    protected byte[]                    fin = new byte[FIN_LENGTH];
    /** The CLI Presentation and Screening indicators. */
    protected byte                      cliPresScreen;
    /** The CLI type and plan. */
    protected byte                      cliTypePlan;
    /** The CLI. */
    protected byte[]                    cli = new byte[CLI_LENGTH];
    /** The Ocean Time of call arriving. */
    protected int                       time;
    /** The Redirecting Presentation and Screening indicators. */
    protected byte                      redirPresScreen;
    /** The Redirecting type and plan. */
    protected byte                      redirTypePlan;
    /** The Redirecting number. */
    protected byte[]                    redir;

    // CSON: VisibilityModifier

    /**
     * Decode the buffer into an InitialDP message.
     *
     * @param buffer
     *            the message to decode
     * @param minLength
     *            the minimum length of the message
     * @param maxLength
     *            the maximum length of the message
     * @param optLength
     *            the length of the optional (redirecting) part of the message
     * @param decode
     *            true if the payload should be decoded; false to skip decoding
     *            the payload. For use by subclasses that need to decode fields
     *            in a different order
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    protected InitialDP(final ByteBuffer buffer, final int minLength,
            final int maxLength, final int optLength, final boolean decode)
            throws OCPException {
        super(buffer);
        super.advance(buffer);

        int limit = buffer.limit();
        if (!((limit >= minLength && limit <= maxLength)
                || (limit >= minLength + optLength
                        && limit <= maxLength + optLength)
                    )) {
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

        decodeMain(buffer);
        time = buffer.getInt();
        decodeRedir(buffer);
    }

    /**
     * Decode the buffer into an Initial DP message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public InitialDP(final ByteBuffer buffer) throws OCPException {
        this(buffer, MIN_LENGTH, MAX_LENGTH, OPT_LENGTH, true);
    }

    /**
     * Calls {@link CallControlMessage#CallControlMessage(short)} directly with
     * the specified message type. For use by subclasses.
     *
     * @param commandCode
     *            the command code
     */
    protected InitialDP(final short commandCode) {
        super(commandCode);
    }

    /**
     * Instantiates a new Initial DP message.
     */
    public InitialDP() {
        this(TYPE.getCommandCode());
    }

    /**
     * Create a copy of an existing InitialDP message.
     *
     * @param other
     *            The InitialDP message to copy
     */
    public InitialDP(final InitialDP other) {
        this(TYPE.getCommandCode(), other);
    }

    /**
     * Create a copy of an existing InitialDP message, overriding the message
     * type. For use by subclasses.
     *
     * @param commandCode
     *            The command code
     * @param other
     *            The InitialDP message to copy
     */
    protected InitialDP(final short commandCode, final InitialDP other) {
        this(commandCode);
        origLegID = other.origLegID;
        cpc = other.cpc;
        finTypePlan = other.finTypePlan;
        fin = Arrays.copyOf(other.fin, other.fin.length);
        cliPresScreen = other.cliPresScreen;
        cliTypePlan = other.cliTypePlan;
        cli = Arrays.copyOf(other.cli, other.cli.length);
        time = other.time;
        if (other.hasRedirectingNumber()) {
            redirPresScreen = other.redirPresScreen;
            redirTypePlan = other.redirTypePlan;
            redir = Arrays.copyOf(other.redir, other.redir.length);
        }
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
    protected void encode(final ByteBuffer buffer, // CSIGNORE: DesignForExtension
            final boolean encode) {
        super.encode(buffer);

        if (!encode) {
            return;
        }

        encodeMain(buffer);
        buffer.putInt(time);
        encodeRedir(buffer);
    }

    @Override // CSIGNORE: DesignForExtension
    protected void encode(final ByteBuffer buffer) {
        encode(buffer, true);
    }

    /**
     * Helper function to decode the Main bulk of an initialDP message.
     * <p>
     * Decodes the origLegID, CPC, FIN and CLI, should be followed by the time.
     *
     * @param buffer    The buffer to decode.
     */
    protected void decodeMain(final ByteBuffer buffer) { // CSIGNORE: DesignForExtension
        origLegID = buffer.getShort();
        cpc = buffer.getShort();
        buffer.get(); // For word alignment
        finTypePlan = buffer.get();
        buffer.get(fin);
        cliPresScreen = buffer.get();
        cliTypePlan = buffer.get();
        buffer.get(cli);
    }

    /**
     * Helper function to decode the Redirecting number.
     * <p>
     * Decodes the redir iff there are bytes remaining.
     *
     * @param buffer    The buffer to decode.
     */
    protected void decodeRedir(final ByteBuffer buffer) { // CSIGNORE: DesignForExtension
        if (buffer.hasRemaining()) {
            redirPresScreen = buffer.get();
            redirTypePlan = buffer.get();
            redir = new byte[REDIR_LENGTH];
            buffer.get(redir);
        }
    }

    /**
     * Helper function to encode the Main bulk of an initialDP message.
     * <p>
     * Encodes the origLegID, CPC, FIN and CLI, should be followed the time.
     *
     * @param buffer    The buffer to encode into.
     */
    protected void encodeMain(final ByteBuffer buffer) { // CSIGNORE: DesignForExtension
        buffer.putShort(origLegID);
        buffer.putShort(cpc);
        buffer.put((byte) 0); // For word alignment
        buffer.put(finTypePlan);
        buffer.put(fin);
        buffer.put(cliPresScreen);
        buffer.put(cliTypePlan);
        buffer.put(cli);
    }

    /**
     * Helper function to encode the Redirecting Number.
     * <p>
     * Encodes the redir iff it is not null.
     *
     * @param buffer    The buffer to encode into.
     */
    protected void encodeRedir(final ByteBuffer buffer) { // CSIGNORE: DesignForExtension
        if (redir != null) {
            buffer.put(redirPresScreen);
            buffer.put(redirTypePlan);
            buffer.put(redir);
        }
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
        origLegID = newOrigLegID;
    }

    /**
     * Gets the Calling Party Category.
     *
     * @return the Calling Party Category
     */
    public final short getCPC() {
        return cpc;
    }

    /**
     * Sets the Calling Party Category.
     *
     * @param newCPC the new Calling Party Category
     */
    public final void setCPC(final short newCPC) {
        cpc = newCPC;
    }

    /**
     * Gets the FIN type and plan.
     *
     * @return the FIN type and plan
     */
    public final byte getFINTypePlan() {
        return finTypePlan;
    }

    /**
     * Sets the FIN type and plan.
     *
     * @param newFINTypePlan the new FIN type and plan
     */
    public final void setFINTypePlan(final byte newFINTypePlan) {
        finTypePlan = newFINTypePlan;
    }

    /**
     * Gets the FIN digits.
     *
     * @return the FIN digits
     */
    public final byte[] getFIN() {
        return Arrays.copyOf(fin, fin.length);
    }

    /**
     * Sets the FIN digits.
     *
     * @param newFIN the new FIN digits
     */
    public final void setFIN(final byte[] newFIN) {
        fin = Arrays.copyOf(newFIN, newFIN.length);
    }

    /**
     * Gets the FIN digits, type and plan.
     *
     * @return the FIN digits, type and plan
     */
    public final OCPTelno getFINTelno() {
        return new OCPTelno(finTypePlan, fin);
    }

    /**
     * Sets the FIN digits, type and plan.
     *
     * @param newFIN the new FIN digits, type and plan
     */
    public final void setFINTelno(final OCPTelno newFIN) {
        finTypePlan = newFIN.getTypePlan();
        fin = newFIN.getTelno();
    }

    /**
     * Gets the CLI Presentation and Screening indicators.
     *
     * @return the CLI Presentation and Screening indicators
     */
    public final byte getCLIPresScreen() {
        return cliPresScreen;
    }

    /**
     * Sets the CLI Presentation and Screening indicators.
     *
     * @param newCLIPresScreen the new CLI Presentation and Screening indicators
     */
    public final void setCLIPresScreen(final byte newCLIPresScreen) {
        cliPresScreen = newCLIPresScreen;
    }

    /**
     * Gets the CLI Type and Plan.
     *
     * @return the CLI Type and Plan
     */
    public final byte getCLITypePlan() {
        return cliTypePlan;
    }

    /**
     * Sets the CLI Type and Plan.
     *
     * @param newCLITypePlan the new CLI Type and Plan
     */
    public final void setCLITypePlan(final byte newCLITypePlan) {
        cliTypePlan = newCLITypePlan;
    }

    /**
     * Gets the CLI digits.
     *
     * @return the CLI digits
     */
    public final byte[] getCLI() {
        return Arrays.copyOf(cli, cli.length);
    }

    /**
     * Sets the CLI digits.
     *
     * @param newCLI the new CLI digits
     */
    public final void setCLI(final byte[] newCLI) {
        cli = Arrays.copyOf(newCLI, newCLI.length);
    }

    /**
     * Gets the CLI digits, type and plan.
     *
     * @return the CLI digits, type and plan
     */
    public final OCPTelno getCLITelno() {
        return new OCPTelno(cliTypePlan, cli);
    }

    /**
     * Sets the CLI digits, type and plan.
     *
     * @param newCLI the new CLI digits, type and plan
     */
    public final void setCLITelno(final OCPTelno newCLI) {
        cliTypePlan = newCLI.getTypePlan();
        cli = newCLI.getTelno();
    }

    /**
     * Gets the ocean time.
     *
     * @return the ocean time
     */
    public final int getOceanTime() {
        return time;
    }

    /**
     * Sets the ocean time.
     *
     * @param newOceanTime the new ocean time
     */
    public final void setOceanTime(final int newOceanTime) {
        time = newOceanTime;
    }

    /**
     * Whether the message has the Redirecting Number.
     *
     * @return true if so, false otherwise.
     */
    public final boolean hasRedirectingNumber() {
        return redir != null;
    }

    /**
     * Gets the Redirecting Presentation and Screening.
     *
     * @return the Redirecting Presentation and Screening.
     */
    public final byte getRedirectingPresScreen() {
        return redirPresScreen;
    }

    /**
     * Sets the Redirecting Presentation and Screening.
     *
     * @param newRedirPresScreen the new Redirecting Presentation and Screening.
     */
    public final void setRedirectingPresScreen(final byte newRedirPresScreen) {
        redirPresScreen = newRedirPresScreen;
    }

    /**
     * Gets the Redirecting Type and Plan.
     *
     * @return the Redirecting Type and Plan.
     */
    public final byte getRedirectingTypePlan() {
        return redirTypePlan;
    }

    /**
     * Sets the Redirecting Type and Plan.
     *
     * @param newRedirTypePlan the new Redirecting Type and Plan.
     */
    public final void setRedirectingTypePlan(final byte newRedirTypePlan) {
        redirTypePlan = newRedirTypePlan;
    }

    /**
     * Gets the Redirecting Number digits.
     *
     * @return the Redirecting Number digits.
     */
    public final byte[] getRedirectingNumber() {
        if (hasRedirectingNumber()) {
            return Arrays.copyOf(redir, REDIR_LENGTH);
        }
        return null;
    }

    /**
     * Sets the Redirecting Number digits.
     *
     * @param newRedir the new Redirecting Number digits.
     */
    public final void setRedirectingNumber(final byte[] newRedir) {
        redir = Arrays.copyOf(newRedir, newRedir.length);
    }

    /**
     * Gets the Redirecting Number digits, type and plan.
     *
     * @return the Redirecting Number digits, type and plan.
     */
    public final OCPTelno getRedirectingTelno() {
        if (hasRedirectingNumber()) {
            return new OCPTelno(redirTypePlan, redir);
        }
        return null;
    }

    /**
     * Sets the Redirecting Number digits, type and plan.
     *
     * @param newRedir the new Redirecting Number digits, type and plan.
     */
    public final void setRedirectingTelno(final OCPTelno newRedir) {
        redirTypePlan = newRedir.getTypePlan();
        redir = newRedir.getTelno();
    }

    @Override // CSIGNORE: DesignForExtension
    public String toString() {
        return "Initial DP";
    }
}
