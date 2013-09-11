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

import java.util.Arrays;

/**
 * Utility class for handling OCP-format telnos.
 * <p/>
 * An OCP telno is a binary string consisting of a 2-byte length followed by a
 * series of packed nibbles. The length is stored in network order and the
 * digits are packed with the left-most digit in the most significant nibble.
 * <p/>
 * This class creates telnos 18 bytes in length, but can decode telnos of other
 * lengths.
 *
 * @author Telsis
 */
public class OCPTelno {
    /** The maximum possible number of digits in a standard-length telno. */
    public static final byte MAX_TELNO_LENGTH = 32;

    /** The number of bytes in a standard-length telno. */
    public static final int TELNO_BYTES_LENGTH = 18;

    /** The type/plan. */
    private byte mTypePlan;

    /** The encoded telno. */
    private byte[] mTelno = new byte[TELNO_BYTES_LENGTH];

    /**
     * Decode an existing OCP telno. The telno does not have to be 18 bytes
     * long - telnos of other lengths can be decoded.
     *
     * @param typePlan the type plan
     * @param telno the telno
     */
    public OCPTelno(final byte typePlan, final byte[] telno) {
        this.mTypePlan = typePlan;
        this.mTelno = telno.clone();
    }


    /**
     * Instantiates a new OCP telno.
     *
     * @param typePlan the type plan
     * @param outDigits the number of valid nibbles in the packed telno
     * @param packedTelno the packed telno
     */
    public OCPTelno(final byte typePlan, final byte outDigits,
            final byte[] packedTelno) {
        setTypePlan(typePlan);
        setTelno(outDigits, packedTelno);
    }

    /**
     * Decode an existing OCP telno. The telno does not have to be 18 bytes
     * long - telnos of other lengths can be decoded.
     *
     * @param telno the telno
     */
    public OCPTelno(final byte[] telno) {
        this.mTypePlan = 0;
        this.mTelno = telno.clone();
    }


    /**
     * Gets the type/plan.
     *
     * @return the type/plan
     */
    public final byte getTypePlan() {
        return mTypePlan;
    }

    /**
     * Sets the type/plan.
     *
     * @param typePlan the new type/plan
     */
    public final void setTypePlan(final byte typePlan) {
        this.mTypePlan = typePlan;
    }

    /**
     * Gets the encoded telno.
     *
     * @return the telno
     */
    public final byte[] getTelno() {
        return mTelno.clone();
    }

    /**
     * Sets the telno using the given length and packed digits. The length is
     * prepended to the digits array.
     *
     * @param length the length
     * @param packedDigits the packed digits
     */
    public final void setTelno(final byte length, final byte[] packedDigits) {
        if (length > MAX_TELNO_LENGTH
                || length < 0
                || packedDigits.length < ((length + 1) / 2)) {
            throw new IllegalArgumentException();
        }

        mTelno = new byte[TELNO_BYTES_LENGTH];
        mTelno[1] = length;
        for (int i = 0; i < packedDigits.length && i < ((length + 1) / 2);
        i++) {
            mTelno[i + 2] = packedDigits[i];
        }
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public final byte getLength() {
        return mTelno[1];
    }

    /**
     * Gets the packed digits.
     *
     * @return the packed digits
     */
    public final byte[] getPackedDigits() {
        return Arrays.copyOfRange(mTelno, 2, TELNO_BYTES_LENGTH - 1);
    }

    /**
     * Gets the unpacked digits.
     *
     * @return the unpacked digits
     */
    public final byte[] getUnpackedDigits() {
        byte[] unpacked = new byte[getLength()];
        for (int i = 0; i < getLength(); i++) {
            //CSOFF: MagicNumber Some bit twiddling here
            if ((i % 2) == 0) {
                unpacked[i] = (byte) ((mTelno[(i / 2) + 2] >> 4) & 0x0F);
            } else {
                unpacked[i] = (byte) (mTelno[(i / 2) + 2] & 0x0F);
            }
            //CSON: MagicNumber
        }
        return unpacked;
    }

    @Override
    public final int hashCode() {
        // Auto-generated by Eclipse
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(mTelno);
        result = prime * result + mTypePlan;
        return result;
    }

    /**
     * Compare two OCPTelnos. <tt>obj</tt> is considered equal to this object if
     * all of the following are true:
     * <ul>
     * <li><tt>obj</tt> is not null.</li>
     * <li><tt>obj</tt> is an instance of OCPTelno.</li>
     * <li>The full packed digits in <tt>obj</tt> are equal to those in this
     * object.</li>
     * <li><tt>obj</tt> has the same type and plan as this object.</li>
     * </ul>
     *
     * @param obj
     *            the object to compare this one to
     * @return true if obj is equal to this object
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OCPTelno)) {
            return false;
        }
        OCPTelno other = (OCPTelno) obj;
        if (!Arrays.equals(mTelno, other.mTelno)) {
            return false;
        }

        if (mTypePlan != other.mTypePlan) {
            return false;
        }

        return true;
    }

}
