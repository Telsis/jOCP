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
package com.telsis.jutils.signalling;

import java.util.Arrays;

/**
 * This is a generic representation of a telephone number. A generic telephone
 * number contains a type and any number of (unpacked) digits from 0x00 to 0x0F.
 *
 * @author Telsis
 */
public class GenericTelno {
    /** Telephone number type. */
    private TelnoType type = TelnoType.UNKNOWN;
    /** Telephone number digits. */
    private byte[] digits = new byte[]{};

    /**
     * Instantiates a new empty generic telno.
     */
    public GenericTelno() {
    }

    /**
     * Instantiates a new generic telno.
     *
     * @param newType
     *            the type of the telephone number
     * @param newDigits
     *            the digits of the telephone number
     */
    public GenericTelno(final TelnoType newType, final byte[] newDigits) {
        this.type = newType;
        this.digits = newDigits.clone();
    }

    /**
     * Gets the type of the telephone number.
     *
     * @return the type
     */
    public final TelnoType getType() {
        return type;
    }

    /**
     * Sets the type of the telephone number.
     *
     * @param newType the new type
     */
    public final void setType(final TelnoType newType) {
        this.type = newType;
    }

    /**
     * Gets the digits of the telephone number.
     *
     * @return the digits
     */
    public final byte[] getDigits() {
        return digits.clone();
    }

    /**
     * Sets the digits of the telephone number.
     *
     * @param newDigits the new digits
     */
    public final void setDigits(final byte[] newDigits) {
        this.digits = newDigits.clone();
    }

    // CSOFF: DesignForExtension
    // CSOFF: InlineConditional
    // CSOFF: Braces
    @Override
    public int hashCode() {
        // Auto-generated using Eclipse
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(digits);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Auto-generated using Eclipse
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GenericTelno other = (GenericTelno) obj;
        if (!Arrays.equals(digits, other.digits)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }
}
