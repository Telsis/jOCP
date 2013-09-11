/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2012-2013.
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
package com.telsis.jocp.messages.telsishandler;

import java.nio.ByteBuffer;

/**
 * Represents the payload of a Run Map From Key Telsis Handler.
 */
public class RunMapFromKeyPayload implements TelsisHandlerPayload {

    private static final int LENGTH = 6;

    private int serviceKey = 0;
    private short notifiedLength = 0;

    /**
     * @return the service key
     */
    public final int getServiceKey() {
        return serviceKey;
    }

    /**
     * @param newServiceKey the service key to set
     */
    public final void setServiceKey(final int newServiceKey) {
        this.serviceKey = newServiceKey;
    }

    /**
     * @return the number of digits the SCP has been notified with
     */
    public final short getNotifiedLength() {
        return notifiedLength;
    }

    /**
     * @param newNotifiedLength the notified length to set
     */
    public final void setNotifiedLength(final short newNotifiedLength) {
        this.notifiedLength = newNotifiedLength;
    }

    @Override
    public final short getLength() {
        return LENGTH;
    }

    @Override
    public final void encode(final ByteBuffer buffer) {
        if (buffer.remaining() < LENGTH) {
            throw new IllegalArgumentException("Invalid buffer length");
        }

        buffer.putInt(serviceKey);
        buffer.putShort(notifiedLength);
    }

    @Override
    public final void decode(final ByteBuffer buffer) {
        if (buffer.remaining() < LENGTH) {
            throw new IllegalArgumentException("Invalid buffer length");
        }

        serviceKey = buffer.getInt();
        notifiedLength = buffer.getShort();
    }

}
