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

/**
 * Signals that an error was detected while decoding
 * an {@link LegacyOCPMessage}.
 *
 * @author Telsis
 */
public abstract class MessageException extends OCPException {
    /** The serial number. */
    private static final long serialVersionUID = 1L;
    /** The command code of the message that caused this exception. */
    private short commandCode;
    /** The reason for this exception. */
    private short reason;
    /** The additional information for this exception. */
    private int value;

    // CSOFF: HiddenField for constructors
    /**
     * Create a new MessageException.
     *
     * @param commandCode
     *            The command code of the message that caused this exception
     * @param reason
     *            The reason for this exception
     * @param value
     *            The additional information for this exception, if relevant
     */
    protected MessageException(final short commandCode, final short reason,
            final int value) {
        this.commandCode = commandCode;
        this.reason = reason;
        this.value = value;
    }
    // CSON: HiddenField

    /**
     * Gets the command code of the message that caused this exception.
     *
     * @return The command code of the message that caused this exception
     */
    public final short getCommandCode() {
        return commandCode;
    }

    /**
     * Gets the reason for this exception.
     *
     * @return the reason for this exception
     */
    public final short getReason() {
        return reason;
    }

    /**
     * Gets the additional information for this exception.
     *
     * @return the additional information for this exception
     */
    public final int getValue() {
        return value;
    }

    /**
     * Gets an {@link LegacyOCPMessage} containing details of the exception.
     * This message can be sent in response to the message that caused this
     * exception.
     *
     * @return a suitable {@link LegacyOCPMessage} containing details of this
     *         exception
     */
   public abstract LegacyOCPMessage getErrorMessage();

   @Override
   public final String toString() {
       LegacyOCPMessageTypes type = LegacyOCPMessage.getOCPType(commandCode);

       return this.getClass().getSimpleName() + ": command " + type + " (0x"
               + Integer.toHexString(commandCode) + "), reason " + reason
               + ", value " + value;
   }
}
