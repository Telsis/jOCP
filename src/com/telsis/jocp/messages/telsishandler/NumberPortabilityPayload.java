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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents the payload of a Set Number Portability Params Telsis Handler and
 * a Get Number Portability Params Telsis Handler Result.
 */
public class NumberPortabilityPayload implements TelsisHandlerPayload {

    /** Length. */
    private static final short LENGTH = 62;
    private static final int MAX_TELNO_DIGITS = 28;

    private static final Pattern TELNO_REGEX =
            Pattern.compile("^[0-9A-F]*$", Pattern.CASE_INSENSITIVE);

    /** An internal enum for the flags field. */
    private static enum Flag {
        NPDI       (0x0001),
        GLOBAL_RN  (0x0002),
        GLOBAL_CIC (0x0004);

        private final short bit;

        private Flag(final int newBit) {
            this.bit = (short) newBit;
        }
    }

    private static final Set<Flag> ALL_FLAGS =
            Collections.unmodifiableSet(EnumSet.allOf(Flag.class));

    private Set<Flag> flags   = EnumSet.noneOf(Flag.class);
    private String rn         = "";
    private String rnContext  = "";
    private String cic        = "";
    private String cicContext = "";

    @Override
    public final short getLength() {
        return LENGTH;
    }

    @Override
    public final void encode(final ByteBuffer buffer) {
        if (buffer.remaining() < LENGTH) {
            throw new IllegalArgumentException("Invalid buffer length");
        }

        short bits = 0;
        for (Flag f : flags) {
            bits |= f.bit;
        }

        buffer.putShort(bits);
        encodeTelno(buffer, rn);
        encodeTelno(buffer, rnContext);
        encodeTelno(buffer, cic);
        encodeTelno(buffer, cicContext);
    }

    @Override
    public final void decode(final ByteBuffer buffer) {
        if (buffer.remaining() < LENGTH) {
            throw new IllegalArgumentException("Invalid buffer length");
        }

        short bits = buffer.getShort();
        for (Flag f : ALL_FLAGS) {
            if ((bits & f.bit) != 0) {
                flags.add(f);
            }
        }

        rn         = decodeTelno(buffer);
        rnContext  = decodeTelno(buffer);
        cic        = decodeTelno(buffer);
        cicContext = decodeTelno(buffer);
    }

    /**
     * @return the npdi
     */
    public final boolean isNPDI() {
        return getFlag(Flag.NPDI);
    }

    /**
     * @param newNpdi the npdi to set
     */
    public final void setNPDI(final boolean newNpdi) {
        setFlag(Flag.NPDI, newNpdi);
    }

    /**
     * @return the rnGlobal
     */
    public final boolean isRNGlobal() {
        return getFlag(Flag.GLOBAL_RN);
    }

    /**
     * @param newRnGlobal the rnGlobal to set
     */
    public final void setRNGlobal(final boolean newRnGlobal) {
        setFlag(Flag.GLOBAL_RN, newRnGlobal);
    }

    /**
     * @return the rn
     */
    public final String getRN() {
        return rn;
    }

    /**
     * @param newRn the rn to set
     */
    public final void setRN(final String newRn) {
        checkTelno(newRn);
        this.rn = newRn.toUpperCase();
    }

    /**
     * @return the rnContext
     */
    public final String getRNContext() {
        return rnContext;
    }

    /**
     * @param newRnContext the rnContext to set
     */
    public final void setRNContext(final String newRnContext) {
        checkTelno(newRnContext);
        this.rnContext = newRnContext.toUpperCase();
    }

    /**
     * @return the cicGlobal
     */
    public final boolean isCICGlobal() {
        return getFlag(Flag.GLOBAL_CIC);
    }

    /**
     * @param newCicGlobal the cicGlobal to set
     */
    public final void setCICGlobal(final boolean newCicGlobal) {
        setFlag(Flag.GLOBAL_CIC, newCicGlobal);
    }

    /**
     * @return the cic
     */
    public final String getCIC() {
        return cic;
    }

    /**
     * @param newCic the cic to set
     */
    public final void setCIC(final String newCic) {
        checkTelno(newCic);
        this.cic = newCic.toUpperCase();
    }

    /**
     * @return the cicContext
     */
    public final String getCICContext() {
        return cicContext;
    }

    /**
     * @param newCicContext the cicContext to set
     */
    public final void setCICContext(final String newCicContext) {
        checkTelno(newCicContext);
        this.cicContext = newCicContext.toUpperCase();
    }

    private boolean getFlag(final Flag flag) {
        return flags.contains(flag);
    }

    private void setFlag(final Flag flag, final boolean newValue) {
        if (newValue) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }

    private void checkTelno(final String telno) {
        if (telno == null) {
            throw new NullPointerException();
        }

        if (!TELNO_REGEX.matcher(telno).matches()) {
            throw new IllegalArgumentException(telno + " not a valid telno");
        }
    }

    private void encodeTelno(final ByteBuffer buffer, final String telno) {
        byte[] digits = Arrays.copyOfRange(telno.getBytes(Charset.forName("US-ASCII"))
                , 0, MAX_TELNO_DIGITS);

        // write the length bytes
        buffer.put((byte) Math.min(telno.length(), MAX_TELNO_DIGITS));

        // nibble-pack the digits
        for (byte i = 0, b = 0; i < digits.length; i++) {
            b |= encodeDigit(digits[i]);
            if (i % 2 != 0) {
                buffer.put(b);
                b = 0;
            } else {
                b <<= 4; // CSIGNORE: MagicNumber
            }
        }
    }

    private String decodeTelno(final ByteBuffer buffer) {
        byte length = buffer.get();

        int fieldEnd = buffer.position() + (MAX_TELNO_DIGITS / 2);

        // check the length looks sensible
        if (length < 0 || length > MAX_TELNO_DIGITS) {
            throw new IllegalArgumentException("invalid length: " + length);
        }

        StringBuilder telno = new StringBuilder(length);

        // nibble-unpack the digits
        for (byte i = 0, b = 0; i < length; i++) {
            if (i % 2 != 0) {
                telno.append(decodeDigit((byte) (b & 0x0F))); // CSIGNORE: MagicNumber
            } else {
                b = buffer.get();
                telno.append(decodeDigit((byte) ((b & 0xF0) >> 4))); // CSIGNORE: MagicNumber
            }
        }

        // move past this field
        buffer.position(fieldEnd);

        return telno.toString();
    }

    private byte encodeDigit(final byte digit) {
        // checkTelno has validated the digits are all within range so this is safe
        if (digit == 0) {
            return (byte) 0xF; // CSIGNORE: MagicNumber pad with 0xF
        } else if (digit <= '9') {
            return (byte) (digit - '0');
        } else {
            return (byte) (digit - 'A' + 0xA); // CSIGNORE: MagicNumber
        }
    }

    private char decodeDigit(final byte digit) {
        if (digit <= 9) { // CSIGNORE: MagicNumber
            return (char) (digit + '0');
        } else {
            return (char) (digit - 0xA + 'A'); // CSIGNORE: MagicNumber
        }
    }

}
