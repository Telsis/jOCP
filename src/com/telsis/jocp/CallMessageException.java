/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2011-2013.
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
package com.telsis.jocp;

import com.telsis.jocp.messages.CallCommandUnsupported;

/**
 * Signals that an error was detected while decoding a
 * {@link com.telsis.jocp.messages.CallControlMessage call message}.
 *
 * @author Telsis
 */
public class CallMessageException extends MessageException {
    /** The serial number. */
    private static final long serialVersionUID = 1L;
    /** The destination task ID of the original message. */
    private int   destTID;
    /** The origination task ID of the original message. */
    private int   origTID;

    // CSOFF: HiddenField for constructors
    /**
     * Create a new CallMessageException.
     *
     * @param destTID
     *            The destination task ID of the original message.
     * @param origTID
     *            The origination task ID of the original message.
     * @param commandCode
     *            The command code of the message that caused this exception
     * @param reason
     *            The reason for this exception
     * @param value
     *            The additional information for this exception, if relevant
     */
    public CallMessageException(final int destTID, final int origTID,
            final short commandCode, final short reason, final int value) {
        super(commandCode, reason, value);
        this.destTID = destTID;
        this.origTID = origTID;
    }

    /**
     * Create a new CallMessageException.
     *
     * @param destTID
     *            The destination task ID of the original message.
     * @param origTID
     *            The origination task ID of the original message.
     * @param commandCode
     *            The command code of the message that caused this exception
     * @param reason
     *            The reason for this exception
     */
    public CallMessageException(final int destTID, final int origTID,
            final short commandCode, final short reason) {
        this(destTID, origTID, commandCode, reason, 0);
    }
    // CSON: HiddenField for constructors

    /**
     * Gets the destination task ID of the original message.
     *
     * @return the destination task ID of the original message
     */
    public final int getDestTID() {
        return destTID;
    }

    /**
     * Gets the origination task ID of the original message.
     *
     * @return the origination task ID of the original message
     */
    public final int getOrigTID() {
        return origTID;
    }

    /**
     * Gets a {@link CallCommandUnsupported} message containing details of the
     * exception. This message can be sent in response to the
     * {@link com.telsis.jocp.messages.CallControlMessage Call message} that
     * caused this exception.
     *
     * @return a {@link CallCommandUnsupported} message containing details of
     *         the {@link com.telsis.jocp.messages.CallControlMessage Call
     *         message} that caused this exception.
     */
    @Override
    public final LegacyOCPMessage getErrorMessage() {
        CallCommandUnsupported ccu = new CallCommandUnsupported();
        ccu.setDestTID(origTID);
        ccu.setOrigTID(destTID);
        ccu.setNestedCommandCode(getCommandCode());
        ccu.setReason(getReason());
        ccu.setValue(getValue());
        return ccu;
    }
}
