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

import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Deliver To With Flags.
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
 * <td>0 if no CLI to be supplied, 1 if unit to use A party's CLI, 2 if CLI
 * specified</td>
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
 * <td>Q931 Type in high nibble and Plan in low nibble for outdialled number
 * </td>
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
 * <tr>
 * <td>outdialFlags</td>
 * <td>4</td>
 * <td>Flags to control propagation. Currently supported values:
 * OD_PROPAGATE_WITH_FOLLOWON (0x000000C7) optionally OR'd with the following:
 * OD_PROPAGATE_FAILURE (0x00000100) and/or OD_PROPAGATE_CLEAR_B_TO_A
 * (0x00000008).</td>
 * </tr>
 * </table>
 *
 * @author Telsis
 */
public class DeliverToWithFlags extends DeliverTo {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE =
        LegacyOCPMessageTypes.DELIVER_TO_WITH_FLAGS;
    /** The expected length of the message. */
    private static final int            EXPECTED_LENGTH = 54;

    /** Set cliMode to this to indicate that no CLI is to be supplied. */
    public static final short           CLI_MODE_USE_NONE      = 0;
    /** Set cliMode to this to indicate to use the A party's CLI. */
    public static final short           CLI_MODE_USE_A_PARTY   = 1;
    /** Set cliMode to this to indicate to use the specified CLI. */
    public static final short           CLI_MODE_USE_SPECIFIED = 2;

    /** The Flags to control propagation. */
    private int                         outdialFlags;

    /**
     * Decode the buffer into a Deliver To With Flags message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public DeliverToWithFlags(final ByteBuffer buffer) throws OCPException {
        super(buffer, EXPECTED_LENGTH, EXPECTED_LENGTH, true);

        outdialFlags = buffer.getInt();
    }

    /**
     * Instantiates a new Deliver To With Flags message.
     */
    public DeliverToWithFlags() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer, true);
        buffer.putInt(outdialFlags);
    }

    /**
     * Gets the outdial flags.
     *
     * @return the outdial flags
     */
    public final int getOutdialFlags() {
        return outdialFlags;
    }

    /**
     * Sets the outdial flags.
     *
     * @param newoutdialFlags
     *            the new outdial flags
     */
    public final void setOutdialFlags(final int newoutdialFlags) {
        this.outdialFlags = newoutdialFlags;
    }

    @Override
    public final String toString() {
        return "Deliver To with flags";
    }
}
