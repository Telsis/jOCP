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

import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;
import com.telsis.jocp.OCPTelno;

/**
 * Send this message to inform an SCP about the arrival of a call. The
 * call-handling platform will already have performed some number analysis to a
 * select a service (via a service key) on the SCP.
 * <p/>
 * The expected reply to this message is {@link InitialDPResponse Initial DP
 * Response}.
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
 * <td>Calling Party Category - bitmap as used in SSP, 0xFFFF if not available
 * </td>
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
 * <td>2 byte length followed by 32 packed digits</td>
 * </tr>
 * <tr>
 * <td>spare</td>
 * <td>1</td>
 * <td><i>For word alignment</i></td>
 * </tr>
 * <tr>
 * <td>mobileLocationTypePlan</td>
 * <td>1</td>
 * <td>Q931 Type in high nibble and Plan in low nibble for mobile location</td>
 * </tr>
 * <tr>
 * <td>mobileLocation</td>
 * <td>18</td>
 * <td>2 byte length followed by 32 packed digits</td>
 * </tr>
 * <tr>
 * <td>serviceKey</td>
 * <td>4</td>
 * <td>Service key- used to select the service to run</td>
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
 * {@link InitialDP Initial DP}. This message is sent if the in rule for the
 * call on the call-handling platform has been configured (via the Route
 * application for the Ocean fastSSP) to use service keys. If it hasn't been
 * configured, message {@link InitialDP Initial DP} is sent.
 *
 * @see InitialDP
 * @see InitialDPResponse
 * @author Telsis
 */
public class InitialDPServiceKey extends InitialDP {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.INITIAL_DP_SERVICE_KEY;
    /** The minimum length of this message (main part). */
    protected static final int          MIN_LENGTH = 72;
    /** The maximum length of this message (main part). */
    protected static final int          MAX_LENGTH = MIN_LENGTH;

    /** The length of the mobileLocation field. */
    private static final int            MOBILE_LOCATION_LENGTH = 18;

    /** Indicates that the calling party category is not available. */
    public static final short           CPC_UNUSED = (short) 0xFFFF;

    /** The mobile location type and plan. */
    private byte                        mobileLocationTypePlan;
    /** The mobile location. */
    private byte[]                      mobileLocation = new byte[MOBILE_LOCATION_LENGTH];
    /** The service key. */
    private int                         serviceKey;

    /**
     * Decode the buffer into an Initial DP Service Key message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public InitialDPServiceKey(final ByteBuffer buffer) throws OCPException {
        super(buffer, MIN_LENGTH, MAX_LENGTH, OPT_LENGTH, false); // Need custom decoding

        decodeMain(buffer);
        time = buffer.getInt();
        decodeRedir(buffer);
    }

    /**
     * Instantiates a new Initial DP Service Key message.
     */
    public InitialDPServiceKey() {
        super(TYPE.getCommandCode());
    }

    /**
     * Create a copy of an existing InitialDP message.
     *
     * @param other
     *            The InitialDP message to copy
     */
    public InitialDPServiceKey(final InitialDP other) {
        super(TYPE.getCommandCode(), other);
        if (other instanceof InitialDPServiceKey) {
            InitialDPServiceKey otherIDPSK = (InitialDPServiceKey) other;
            mobileLocationTypePlan = otherIDPSK.mobileLocationTypePlan;
            mobileLocation = Arrays.copyOf(otherIDPSK.mobileLocation,
                    otherIDPSK.mobileLocation.length);
            serviceKey = otherIDPSK.serviceKey;
        }
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer, false); // Need custom encoding

        encodeMain(buffer);
        buffer.putInt(time);
        encodeRedir(buffer);
    }

    /**
     * Helper function to decode the main bulk of an InitialDPServiceKey.
     * <p>
     * Calls InitialDP.decodeMain, then decodes the mobile location and the
     * service Key.
     *
     * @param buffer    The buffer to decode.
     */
    @Override // CSIGNORE: DesignForExtension
    protected void decodeMain(final ByteBuffer buffer) {
        super.decodeMain(buffer);
        buffer.get(); // for word alignment
        mobileLocationTypePlan = buffer.get();
        mobileLocation = new byte[MOBILE_LOCATION_LENGTH];
        buffer.get(mobileLocation);
        serviceKey = buffer.getInt();
    }

    /**
     * Helper function to decode the main bulk of an InitialDPServiceKey.
     * <p>
     * Calls InitialDP.encodeMain, then encodes the mobile location and the
     * service Key.
     *
     * @param buffer    The buffer to decode.
     */
    @Override // CSIGNORE: DesignForExtension
    protected void encodeMain(final ByteBuffer buffer) {
        super.encodeMain(buffer);
        buffer.put((byte) 0); // for word alignment
        buffer.put(mobileLocationTypePlan);
        buffer.put(mobileLocation);
        buffer.putInt(serviceKey);
    }

    /**
     * Gets the mobile location type and plan.
     *
     * @return the mobile location type and plan
     */
    public final byte getMobileLocationTypePlan() {
        return mobileLocationTypePlan;
    }

    /**
     * Sets the mobile location type and plan.
     *
     * @param newMobileLocationTypePlan the new mobile location type and plan
     */
    public final void setMobileLocationTypePlan(final byte newMobileLocationTypePlan) {
        mobileLocationTypePlan = newMobileLocationTypePlan;
    }

    /**
     * Gets the mobile location digits.
     *
     * @return the mobile location digits
     */
    public final byte[] getMobileLocation() {
        return Arrays.copyOf(mobileLocation, mobileLocation.length);
    }

    /**
     * Sets the mobile location digits.
     *
     * @param newMobileLocation the new mobile location
     */
    public final void setMobileLocation(final byte[] newMobileLocation) {
        mobileLocation = Arrays.copyOf(newMobileLocation,
                newMobileLocation.length);
    }

    /**
     * Gets the mobile location digits, type and plan.
     *
     * @return the mobile location digits, type and plan
     */
    public final OCPTelno getMobileLocationTelno() {
        return new OCPTelno(mobileLocationTypePlan, mobileLocation);
    }

    /**
     * Sets the mobile location digits, type and plan.
     *
     * @param newMobileLocation the new mobile location digits, type and plan
     */
    public final void setMobileLocationTelno(final OCPTelno newMobileLocation) {
        mobileLocationTypePlan = newMobileLocation.getTypePlan();
        mobileLocation = newMobileLocation.getTelno();
    }

    /**
     * Gets the service key.
     *
     * @return the service key
     */
    public final int getServiceKey() {
        return serviceKey;
    }

    /**
     * Sets the service key.
     *
     * @param newServiceKey the new service key
     */
    public final void setServiceKey(final int newServiceKey) {
        serviceKey = newServiceKey;
    }

    /**
     * Gets the time (in OceanTime format).
     *
     * @return the time
     */
    public final int getTime() {
        return time;
    }

    /**
     * Sets the time (in OceanTime format).
     *
     * @param newTime the new time
     */
    public final void setTime(final int newTime) {
        time = newTime;
    }

    @Override
    public final String toString() {
        return "Initial DP service key";
    }

}
