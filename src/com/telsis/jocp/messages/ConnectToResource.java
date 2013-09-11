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

/**
 * Send this message to instruct a call-handling platform to connect to an
 * external resource. The expected reply to this message is
 * {@link ConnectToResourceAck Connect To Resource Ack}.
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
 * <td>This is used to indicate the party on the fastSCP being connected to the
 * resource</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>1</td>
 * <td>Bit 0 is set to force immediate audio routing upon connection</td>
 * </tr>
 * <tr>
 * <td>zipNumber</td>
 * <td>1</td>
 * <td>The index into the zip table on the SCP for this result</td>
 * </tr>
 * <tr>
 * <td>routingPrefix</td>
 * <td>8</td>
 * <td>Prefix for the outdialled number - 2 byte length followed by 10 packed
 * digits</td>
 * </tr>
 * <tr>
 * <td>correlationInfo</td>
 * <td>8</td>
 * <td>Combination of the correlation ID and the one digit of sequence info - 2
 * byte length followed by 10 packed digits</td>
 * </tr>
 * <tr>
 * <td>clusterID</td>
 * <td>4</td>
 * <td>The clusterID to send in the Ctr message - 2 byte length followed by 4
 * packed digits</td>
 * </tr>
 * <tr>
 * <td>postFix</td>
 * <td>4</td>
 * <td>Postfix for outdialled number, used for channel selection on resource
 * unit - 2 byte length followed by 4 packed digits</td>
 * </tr>
 * </table>
 *
 * @see ConnectToResourceAck
 * @author Telsis
 */
public class ConnectToResource extends CallControlMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.CONNECT_TO_RESOURCE;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 30;

    /** The length of the routingPrefix field. */
    private static final int            ROUTING_PREFIX_LENGTH = 8;
    /** The length of the correlationInfo field. */
    private static final int            CORRELATION_INFO_LENGTH = 8;
    /** The length of the clusterID field. */
    private static final int            CLUSTER_ID_LENGTH = 4;
    /** The length of the postFix field. */
    private static final int            POSTFIX_LENGTH = 4;

    /** The leg ID on call handling unit to act upon. */
    private short                       destLegID;
    /** The party on the fastSCP being connected to the resource. */
    private short                       origLegID;
    /** The flags. */
    private byte                        flags;
    /** The index into the zip table on the SCP for this result. */
    private byte                        zipNumber;
    /** The prefix for the outdialled number. */
    private byte[]                      routingPrefix = new byte[ROUTING_PREFIX_LENGTH];
    /** The correlation ID. */
    private byte[]                      correlationInfo = new byte[CORRELATION_INFO_LENGTH];
    /** The cluster ID. */
    private byte[]                      clusterID = new byte[CLUSTER_ID_LENGTH];
    /** The postfix for outdialled number. */
    private byte[]                      postFix = new byte[POSTFIX_LENGTH];

    /**
     * Decode the buffer into a Connect To Resource message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public ConnectToResource(final ByteBuffer buffer) throws OCPException {
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
        origLegID = buffer.getShort();
        flags = buffer.get();
        zipNumber = buffer.get();
        buffer.get(routingPrefix);
        buffer.get(correlationInfo);
        buffer.get(clusterID);
        buffer.get(postFix);
    }

    /**
     * Instantiates a new Connect To Resource message.
     */
    public ConnectToResource() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);
        buffer.putShort(destLegID);
        buffer.putShort(origLegID);
        buffer.put(flags);
        buffer.put(zipNumber);
        buffer.put(routingPrefix);
        buffer.put(correlationInfo);
        buffer.put(clusterID);
        buffer.put(postFix);
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
     * @param newDestLegID the new dest leg ID
     */
    public final void setDestLegID(final short newDestLegID) {
        this.destLegID = newDestLegID;
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
     * Gets the routing prefix.
     *
     * @return the routing prefix
     */
    public final byte[] getRoutingPrefix() {
        return Arrays.copyOf(routingPrefix, routingPrefix.length);
    }

    /**
     * Sets the routing prefix.
     *
     * @param newRoutingPrefix the new routing prefix
     */
    public final void setRoutingPrefix(final byte[] newRoutingPrefix) {
        this.routingPrefix = Arrays.copyOf(newRoutingPrefix, newRoutingPrefix.length);
    }

    /**
     * Gets the correlation info.
     *
     * @return the correlation info
     */
    public final byte[] getCorrelationInfo() {
        return Arrays.copyOf(correlationInfo, correlationInfo.length);
    }

    /**
     * Sets the correlation info.
     *
     * @param newCorrelationInfo the new correlation info
     */
    public final void setCorrelationInfo(final byte[] newCorrelationInfo) {
        this.correlationInfo = Arrays.copyOf(newCorrelationInfo,
                newCorrelationInfo.length);
    }

    /**
     * Gets the cluster ID.
     *
     * @return the cluster ID
     */
    public final byte[] getClusterID() {
        return Arrays.copyOf(clusterID, clusterID.length);
    }

    /**
     * Sets the cluster ID.
     *
     * @param newClusterID the new cluster ID
     */
    public final void setClusterID(final byte[] newClusterID) {
        this.clusterID = Arrays.copyOf(newClusterID, newClusterID.length);
    }

    /**
     * Gets the postfix.
     *
     * @return the postfix
     */
    public final byte[] getPostFix() {
        return Arrays.copyOf(postFix, postFix.length);
    }

    /**
     * Sets the postfix.
     *
     * @param newPostFix the new postfix
     */
    public final void setPostFix(final byte[] newPostFix) {
        this.postFix = Arrays.copyOf(newPostFix, newPostFix.length);
    }

    @Override
    public final String toString() {
        return "Connect to Resource";
    }

}
