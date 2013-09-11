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

import com.telsis.jocp.messages.LinkCommandUnsupported;

/**
 * Signals that an error was detected while decoding a
 * {@link com.telsis.jocp.messages.LinkMessage link message}.
 *
 * @author Telsis
 */
public class LinkMessageException extends MessageException {
    /** The serial number. */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new LinkMessageException.
     *
     * @param commandCode
     *            The command code of the message that caused this exception
     * @param reason
     *            The reason for this exception
     * @param value
     *            The additional information for this exception, if relevant
     */
    public LinkMessageException(final short commandCode, final short reason,
            final int value) {
        super(commandCode, reason, value);
    }

    /**
     * Create a new LinkMessageException.
     *
     * @param commandCode
     *            The command code of the message that caused this exception
     * @param reason
     *            The reason for this exception
     */
    public LinkMessageException(final short commandCode, final short reason) {
        this(commandCode, reason, 0);
    }

    /**
     * Gets a {@link LinkCommandUnsupported} message containing details of the
     * exception. This message can be sent in response to the
     * {@link com.telsis.jocp.messages.LinkMessage Link message} that caused
     * this exception.
     *
     * @return a {@link LinkCommandUnsupported} message containing details of
     *         the {@link com.telsis.jocp.messages.LinkMessage Link message}
     *         that caused this exception.
     */
    @Override
    public final LegacyOCPMessage getErrorMessage() {
        LinkCommandUnsupported lcu = new LinkCommandUnsupported();
        lcu.setNestedCommandCode(getCommandCode());
        lcu.setReason(getReason());
        lcu.setValue(getValue());
        return lcu;
    }
}
