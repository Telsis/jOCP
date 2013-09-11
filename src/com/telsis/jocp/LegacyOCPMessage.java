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
package com.telsis.jocp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import com.telsis.jocp.messages.CallCommandUnsupported;
import com.telsis.jocp.messages.LinkCommandUnsupported;

/**
 * Superclass of all legacy OCP messages.
 * <p/>
 * All messages have the following structure:
 * <table>
 * <tr>
 * <th>Field Name</th>
 * <th>Size</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>commandCode</td>
 * <td>2</td>
 * <td>4 bit Command Type followed by 12 bit Code.</td>
 * </tr>
 * <tr>
 * <td>length</td>
 * <td>2</td>
 * <td>Number of bytes in message after this field.</td>
 * </tr>
 * <tr>
 * <td>destTID</td>
 * <td>4</td>
 * <td>The Task ID on the unit to which this message is being sent.</td>
 * </tr>
 * <tr>
 * <td>origTID</td>
 * <td>4</td>
 * <td>The Task ID on the unit from which this message is being sent.</td>
 * </tr>
 * <tr>
 * <td>payload</td>
 * <td>variable</td>
 * <td>The data contained in the message.</td>
 * </tr>
 * <tr>
 * <td>terminator</td>
 * <td>2</td>
 * <td>Must always take the value 0x55AA.</td>
 * </tr>
 * </table>
 * <p/>
 * Notes:
 * <ul>
 * <li>The length field contains the number of bytes in the message after the
 * length field. Hence, in the above example, the length field would contain
 * (4+4+???+2).</li>
 * <li>For messages with the Command Type set to "Link", dest and orig Task IDs
 * are unused.</li>
 * <li>Unused Task IDs (eg the destTID on an initial DP message or all TIDs on
 * link related messages) take the value 0xFFFFFFFF.</li>
 * </ul>
 *
 * @author Telsis
 * @see com.telsis.jocp.messages.LinkMessage
 * @see com.telsis.jocp.messages.CallControlMessage
 */
public abstract class LegacyOCPMessage implements OCPMessage {

   /**
     * The minimum possible OCP message length.
     */
    public static final int   OCP_MIN_LENGTH        = 14;
    /**
     * The maximum possible OCP message length.
     */
    public static final int   OCP_MAX_LENGTH        = 1024;
    /**
     * The length of the command code field in the OCP message header.
     */
    public static final int   OCP_CMD_CODE_LENGTH   = 2;
    /**
     * The length of the length field in the OCP message header.
     */
    public static final int   OCP_LEN_LENGTH        = 2;
    /**
     * The length of the task ID field in the OCP message header. Note that a
     * header contains two task ID fields.
     */
    public static final int   OCP_TID_LENGTH        = 4;
    /**
     * The length of the end-of-message terminator.
     */
    public static final int   OCP_EOM_LENGTH        = 2;
    /**
     * The length of the OCP message header.
     */
    public static final int   OCP_HEADER_LENGTH     = OCP_CMD_CODE_LENGTH
                                                            + OCP_LEN_LENGTH;
    /**
     * The offset of the command code field.
     */
    public static final int   OCP_CMD_CODE_OFFSET   = 0;
    /**
     * The offset of the length field.
     */
    public static final int   OCP_LEN_OFFSET        = 2;
    /**
     * The offset of the destination task ID field.
     */
    public static final int   OCP_DEST_TID_OFFSET   = 4;
    /**
     * The offset of the origination task ID field.
     */
    public static final int   OCP_ORIG_TID_OFFSET   = 8;
    /**
     * The offset of the message payload.
     */
    public static final int   OCP_PAYLOAD_OFFSET    = 12;
    /**
     * The first end-of-message marker byte.
     */
    public static final byte  OCP_EOM_FIRST_BYTE    = 0x55;
    /**
     * The second end-of-message marker byte.
     */
    public static final byte  OCP_EOM_SECOND_BYTE   = (byte) 0xAA;

    /**
     * The command type portion of the command code field.
     */
    public static final short OCP_COMMAND_TYPE_MASK = (short) 0xF000;
    /**
     * The command type for {@link com.telsis.jocp.messages.LinkMessage link
     * messages}.
     */
    public static final short OCP_COMMAND_TYPE_LINK = (short) 0x0000;
    /**
     * The command type for {@link com.telsis.jocp.messages.CallControlMessage
     * call messages}.
     */
    public static final short OCP_COMMAND_TYPE_CALL = (short) 0x1000;

    /**
     * A mapping of OCP command codes to OCPMessageType instances.
     */
    private static HashMap<Short, LegacyOCPMessageTypes> messageTypes
            = new HashMap<Short, LegacyOCPMessageTypes>();

    static {
        // Load all message types defined in OCPMessageTypes
        EnumSet<LegacyOCPMessageTypes> typeSet =
                EnumSet.allOf(LegacyOCPMessageTypes.class);
        Iterator<LegacyOCPMessageTypes> it = typeSet.iterator();
        LegacyOCPMessageTypes type;

        while (it.hasNext()) {
            type = it.next();
            messageTypes.put(type.getCommandCode(), type);
        }
    }

    /**
     * The command code for this OCP message.
     */
    private short commandCode;
    /**
     * The destination task ID for this OCP message.
     */
    private int   destTID;
    /**
     * The origination task ID for this OCP message.
     */
    private int   origTID;

    /**
     * Lookup the OCP Message Type for a specific command code.
     *
     * @param commandCode
     *            the command code from the OCP message
     * @return the OCPMessageTypes value for the specified command code, or null
     *         if the code is not recognised.
     */
    public static LegacyOCPMessageTypes getOCPType(final short commandCode) {
        return messageTypes.get(commandCode);
    }

    /**
     * Lookup the OCPMessage implementation for a specific command code.
     *
     * @param commandCode
     *            the command code from the OCP message
     * @return the Class that handles the specified command code
     */
    public static Class<? extends LegacyOCPMessage> getOCPClass(
            final short commandCode) {
        LegacyOCPMessageTypes type = getOCPType(commandCode);

        if (type != null) {
            return type.getImplementation();
        } else {
            return null;
        }
    }

    //CSOFF: JavadocMethod
    /**
     * Decode a binary OCP message into an instance of the implementing class.
     *
     * @param buffer
     *            an OCP message
     * @return a subclass of OCPMessage that handles the binary message
     * @throws OCPException
     *             if an error occurs while decoding the message
     * @throws UnknownMessageTypeException
     *             if the command type is not recognised
     * @throws LinkMessageException
     *             if the command code is that of an unimplemented link command
     * @throws CallMessageException
     *             if the command code is that of an unimplemented call command
     */
    public static LegacyOCPMessage decodeBuffer(final ByteBuffer buffer)
            throws OCPException {
        if (buffer.limit() < OCP_MIN_LENGTH) {
            // Too short to possibly be a valid message
            throw new OCPException();
        }

        Class<? extends LegacyOCPMessage> implementation;
        Constructor<? extends LegacyOCPMessage> constructor;

        short commandCode = buffer.getShort(OCP_CMD_CODE_OFFSET);
        implementation = getOCPClass(commandCode);
        if (implementation == null) {
            // Unimplemented OCP message. Try and work out what sort it was.
            switch (commandCode & OCP_COMMAND_TYPE_MASK) {
            case OCP_COMMAND_TYPE_LINK:
                throw new LinkMessageException(
                        commandCode,
                        LinkCommandUnsupported.REASON_COMMAND_CODE_UNSUPPORTED);
            case OCP_COMMAND_TYPE_CALL:
                // Extract the task IDs from the original message
                throw new CallMessageException(
                        buffer.getInt(LegacyOCPMessage.OCP_DEST_TID_OFFSET),
                        buffer.getInt(LegacyOCPMessage.OCP_ORIG_TID_OFFSET),
                        commandCode,
                        CallCommandUnsupported.REASON_COMMAND_CODE_UNSUPPORTED);
            default:
                throw new UnknownMessageTypeException();
            }
        }

        try {
            constructor = implementation.getConstructor(ByteBuffer.class);
            return constructor.newInstance(buffer);
        } catch (InvocationTargetException e) {
            // The constructor threw an exception
            if (e.getTargetException() instanceof OCPException) {
                // Propogate OCPExceptions.
                throw (OCPException) e.getTargetException();
            } else {
                // Wrap non-OCPExceptions.
                throw new OCPException(e.getTargetException());
            }
        } catch (Exception e) {
            // Attempting to call the constructor failed. Wrap in an
            // OCPException.
            throw new OCPException(e);
        }
    }
    //CSON: JavadocMethod

    /**
     * Encode an OCPMessage object into a binary OCP message.
     *
     * @param message
     *            the object to encode
     * @return a ByteBuffer containing the binary OCP message
     */
    public static ByteBuffer encodeMessage(final LegacyOCPMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(OCP_MAX_LENGTH);
        buffer.order(ByteOrder.BIG_ENDIAN); // Network order
        message.encode(buffer);

        // Add message terminator
        buffer.flip();
        buffer.limit(buffer.limit() + OCP_EOM_LENGTH);
        buffer.put(buffer.limit() - 2, OCP_EOM_FIRST_BYTE);
        buffer.put(buffer.limit() - 1, OCP_EOM_SECOND_BYTE);

        // Set length
        buffer.putShort(2, (short) (buffer.limit() - OCP_HEADER_LENGTH));

        return buffer;
    }

    /**
     * Create a new OCPMessage instance from a binary OCP message. Child classes
     * must call this method in their own implementation.
     *
     * @param buffer
     *            the buffer to decode
     * @throws OCPException
     *             if the message could not be decoded
     */
    protected LegacyOCPMessage(final ByteBuffer buffer) throws OCPException {
        if (buffer.limit() < OCP_MIN_LENGTH) {
            // Too short to be a valid message.
            throw new OCPException("Bad buffer length: expected at least "
                    + OCP_MIN_LENGTH + " bytes but got " + buffer.limit());
        }

        if (buffer.get(buffer.limit() - 2) != OCP_EOM_FIRST_BYTE) {
            throw new OCPException("First EOM byte is missing: expected "
                    + OCP_EOM_FIRST_BYTE + " but got "
                    + buffer.get(buffer.limit() - 2));
        }
        if (buffer.get(buffer.limit() - 1) != OCP_EOM_SECOND_BYTE) {
            throw new OCPException("second EOM byte is missing: expected "
                    + OCP_EOM_SECOND_BYTE + " but got "
                    + buffer.get(buffer.limit() - 1));
        }

        commandCode = buffer.getShort(0);
        destTID = buffer.getInt(OCP_DEST_TID_OFFSET);
        origTID = buffer.getInt(OCP_ORIG_TID_OFFSET);

        if (buffer.getShort(OCP_LEN_LENGTH) + OCP_HEADER_LENGTH
                != buffer.limit()) {
            throw new OCPException("Bad length in header: expected "
                    + (buffer.limit() - OCP_HEADER_LENGTH) + " but got "
                    + buffer.getShort(OCP_LEN_LENGTH));
        }
    }

    /**
     * Create a new OCPMessage instance with the specified command code. Child
     * classes must call this method in their own implementation.
     *
     * @param newCommandCode
     *            the command code to use
     */
    protected LegacyOCPMessage(final short newCommandCode) {
        this.commandCode = newCommandCode;
    }

    /**
     * Strips the header from the start of the buffer and the terminator from
     * the end. This modifies the buffer in-place. Child classes should call
     * this before attempting to do any message-specific parsing.
     *
     * @param buffer
     *            The buffer to modify.
     */
    protected final void advance(final ByteBuffer buffer) {
        buffer.position(OCP_PAYLOAD_OFFSET);
        buffer.limit(buffer.limit() - OCP_EOM_LENGTH);
        buffer.compact(); // Move position...limit to start of buffer
        buffer.flip();
    }

    /**
     * Encode the header into the buffer. This modifies the buffer in-place and
     * sets the buffer's position to the start of the payload. Child classes
     * must call this method in their own implementation.
     *
     * @param buffer
     *            The buffer to insert the message into
     */
    protected void encode(final ByteBuffer buffer) { // CSIGNORE: DesignForExtension
        buffer.putShort(commandCode);
        buffer.putShort((short) 0); // will be replaced in encodeMessage()
        buffer.putInt(destTID);
        buffer.putInt(origTID);
    }

    /**
     * Gets the command code for this message. This is a 4 bit Command Type
     * followed by a 12 bit Code.
     *
     * @return the command code
     */
    public final short getCommandCode() {
        return commandCode;
    }

    @Override
    public final OCPMessageTypes getMessageType() {
        return getOCPType(commandCode).getBaseMessageType();
    }

    @Override
    public final int getDestTID() {
        return destTID;
    }

    @Override
    public final void setDestTID(final int newDestTID) {
        this.destTID = newDestTID;
    }

    @Override
    public final int getOrigTID() {
        return origTID;
    }

    @Override
    public final void setOrigTID(final int newOrigTID) {
        this.origTID = newOrigTID;
    }
}
