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
 * Objects that are to be used as the payload of a Telsis Handler message
 * must implement this interface.
 *
 * TelsisHandlerPayload instances must provide a no-args constructor for
 * use when decoding a TelsisHandler message.
 */
public interface TelsisHandlerPayload {

    /**
     * Gets the length of the payload.
     *
     * @return the payload length
     */
    short getLength();

    /**
     * Encodes the payload into the given ByteBuffer in a format
     * suitable for sending in the payload of a Telsis Handler message.
     *
     * @param buffer
     *            the buffer into which the payload is encoded.
     */
    void encode(ByteBuffer buffer);

    /**
     * Decodes the payload from the given ByteBuffer.
     *
     * @param buffer
     *            the buffer from which the payload is decoded.
     */
    void decode(ByteBuffer buffer);

}
