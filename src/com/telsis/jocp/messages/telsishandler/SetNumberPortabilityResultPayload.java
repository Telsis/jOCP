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
 * Represents the payload of a Set Number Portability Params Telsis Handler Result.
 */
public class SetNumberPortabilityResultPayload implements TelsisHandlerPayload {

    /** Length. */
    private static final int LENGTH = 2;

    /** Success result value. */
    public static final int SUCCESS = 0;
    /** Failure result value. */
    public static final int INVALID_PARAMETERS = 1;
    /** Result value. */
    private int result = SUCCESS;

    /**
     * @return the result
     */
    public final int getResult() {
        return result;
    }

    /**
     * @param newResult the result to set
     */
    public final void setResult(final int newResult) {
        this.result = newResult;
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

        buffer.putShort((short) result);
    }

    @Override
    public final void decode(final ByteBuffer buffer) {
        if (buffer.remaining() < LENGTH) {
            throw new IllegalArgumentException("Invalid buffer length");
        }

        result = buffer.getShort() & 0xFFFF; // CSIGNORE: MagicNumber
    }

}
