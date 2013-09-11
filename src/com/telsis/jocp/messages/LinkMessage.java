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
import com.telsis.jocp.LegacyOCPMessage;

/**
 * This class encapsulates all OCP Link messages.
 *
 * @author Telsis
 */
public abstract class LinkMessage extends LegacyOCPMessage {

    /**
     * Decode the buffer into a link message.
     *
     * @param buffer
     *            the message to decode
     * @throws OCPException
     *             if the buffer could not be decoded
     */
    protected LinkMessage(final ByteBuffer buffer) throws OCPException {
        super(buffer);
    }

    /**
     * Instantiates a new link message.
     *
     * @param commandCode the command code
     */
    protected LinkMessage(final short commandCode) {
        super(commandCode);

        // Link messages always have taskIDs of 0xFFFFFFFF
        super.setDestTID(0xFFFFFFFF);
        super.setOrigTID(0xFFFFFFFF);
    }
}
