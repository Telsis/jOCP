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

import com.telsis.jocp.LinkMessageException;
import com.telsis.jocp.OCPException;
import com.telsis.jocp.LegacyOCPMessageTypes;

/**
 * Send this message to get the remote platform's status details. You should
 * send the message immediately after the platform has accepted a connection to
 * the remote platform. The platform sends details such as the date and time.
 * <p/>
 * Status Request is effectively a handshake in which remote platform is told,
 * 'Here are my status details. Please send me yours too.'
 * <p/>
 * The expected reply to this message is {@link StatusResponse Status Response}.
 * <p/>
 * This message has the following parameters:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>year</td>
 * <td>2</td>
 * <td>Absolute year (4 digits)</td>
 * </tr>
 * <tr>
 * <td>month</td>
 * <td>2</td>
 * <td>1=Jan, 2=Feb, etc.</td>
 * </tr>
 * <tr>
 * <td>day</td>
 * <td>2</td>
 * <td>The day of the month</td>
 * </tr>
 * <tr>
 * <td>hour</td>
 * <td>2</td>
 * <td>Hour part of the time (24 hour clock)</td>
 * </tr>
 * <tr>
 * <td>minute</td>
 * <td>2</td>
 * <td>Minute part of the time</td>
 * </tr>
 * <tr>
 * <td>second</td>
 * <td>2</td>
 * <td>Second part of the time</td>
 * </tr>
 * <tr>
 * <td>unitID</td>
 * <td>4</td>
 * <td>The ID of the sending unit. Remote uses for arbitration purposes</td>
 * </tr>
 * <tr>
 * <td>clusterID</td>
 * <td>1</td>
 * <td>The ClusterID of the fastSCP. Each load sharing fastSCP will have a
 * unique ClusterID. In Master-Slave the ClusterID of the unit is 0xFF.</td>
 * </tr>
 * <tr>
 * <td>flags</td>
 * <td>1</td>
 * <td>Bit 0 = Unit enabled<br>
 * Bit 1 = Time to be set on call handling unit<br>
 * Bit 2 = Master Flag</td>
 * </tr>
 * </table>
 *
 * @see StatusResponse
 * @author Telsis
 */
public class StatusRequest extends LinkMessage {
    /** The message type. */
    public static final LegacyOCPMessageTypes TYPE = LegacyOCPMessageTypes.STATUS_REQUEST;
    /** The expected length of this message. */
    private static final int            EXPECTED_LENGTH = 18;

    /** Indicates that this unit is enabled. */
    public static final byte            FLAG_UNIT_ENABLED     = 0x01;
    /** Indicates that the time should be set on the call handling unit. */
    public static final byte            FLAG_SET_TIME         = 0x02;
    /** Indicates that this unit is master. */
    public static final byte            FLAG_MASTER           = 0x04;

    /** Set clusterID to this to indicate that the unit is master-slave. */
    public static final byte            CLUSTERID_MASTERSLAVE = (byte) 0xff;

    /** The year. */
    private short                       year;
    /** The month. */
    private short                       month;
    /** The day. */
    private short                       day;
    /** The hour. */
    private short                       hour;
    /** The minute. */
    private short                       minute;
    /** The second. */
    private short                       second;
    /** The ID of the sending unit. */
    private int                         unitID;
    /** The ClusterID of the fastSCP. */
    private byte                        clusterID;
    /** The flags. */
    private byte                        flags;

    /**
     * Decode the buffer into a Status Request message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    public StatusRequest(final ByteBuffer buffer) throws OCPException {
        super(buffer);
        super.advance(buffer);

        if (buffer.limit() != EXPECTED_LENGTH) {
            throw new LinkMessageException(
                    TYPE.getCommandCode(),
                    LinkCommandUnsupported.REASON_LENGTH_UNSUPPORTED,
                    (short) buffer.limit());
        }

        year = buffer.getShort();
        month = buffer.getShort();
        day = buffer.getShort();
        hour = buffer.getShort();
        minute = buffer.getShort();
        second = buffer.getShort();
        this.unitID = buffer.getInt();
        this.clusterID = buffer.get();
        this.flags = buffer.get();
    }

    /**
     * Instantiates a new Status Request message.
     */
    public StatusRequest() {
        super(TYPE.getCommandCode());
    }

    @Override
    protected final void encode(final ByteBuffer buffer) {
        super.encode(buffer);

        buffer.putShort(year);
        buffer.putShort(month);
        buffer.putShort(day);
        buffer.putShort(hour);
        buffer.putShort(minute);
        buffer.putShort(second);
        buffer.putInt(this.unitID);
        buffer.put(this.clusterID);
        buffer.put(this.flags);
    }

    /**
     * Gets the year.
     *
     * @return the year
     */
    public final short getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param newYear the new year
     */
    public final void setYear(final short newYear) {
        this.year = newYear;
    }

    /**
     * Gets the month.
     *
     * @return the month
     */
    public final short getMonth() {
        return month;
    }

    /**
     * Sets the month.
     *
     * @param newMonth the new month
     */
    public final void setMonth(final short newMonth) {
        this.month = newMonth;
    }

    /**
     * Gets the day.
     *
     * @return the day
     */
    public final short getDay() {
        return day;
    }

    /**
     * Sets the day.
     *
     * @param newDay the new day
     */
    public final void setDay(final short newDay) {
        this.day = newDay;
    }

    /**
     * Gets the hour.
     *
     * @return the hour
     */
    public final short getHour() {
        return hour;
    }

    /**
     * Sets the hour.
     *
     * @param newHour the new hour
     */
    public final void setHour(final short newHour) {
        this.hour = newHour;
    }

    /**
     * Gets the minute.
     *
     * @return the minute
     */
    public final short getMinute() {
        return minute;
    }

    /**
     * Sets the minute.
     *
     * @param newMinute the new minute
     */
    public final void setMinute(final short newMinute) {
        this.minute = newMinute;
    }

    /**
     * Gets the second.
     *
     * @return the second
     */
    public final short getSecond() {
        return second;
    }

    /**
     * Sets the second.
     *
     * @param newSecond the new second
     */
    public final void setSecond(final short newSecond) {
        this.second = newSecond;
    }

    /**
     * Gets the unit ID.
     *
     * @return the unit ID
     */
    public final int getUnitID() {
        return unitID;
    }

    /**
     * Sets the unit ID.
     *
     * @param newUnitID the new unit ID
     */
    public final void setUnitID(final int newUnitID) {
        this.unitID = newUnitID;
    }

    /**
     * Gets the cluster ID.
     *
     * @return the cluster ID
     */
    public final byte getClusterID() {
        return clusterID;
    }

    /**
     * Sets the cluster ID.
     *
     * @param newClusterID the new cluster ID
     */
    public final void setClusterID(final byte newClusterID) {
        this.clusterID = newClusterID;
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

    @Override
    public final String toString() {
        return "Status request";
    }

}
