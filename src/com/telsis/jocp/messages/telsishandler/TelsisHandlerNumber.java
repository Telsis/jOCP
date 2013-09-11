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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Enum for the various Telsis Handlers. */
public enum TelsisHandlerNumber {

    /** The GET_UPDATED_DIGITS handler. */
    GET_UPDATED_DIGITS          ((short) 0x0002),
    /** The RUN_MAP_FROM_KEY handler. */
    RUN_MAP_FROM_KEY            ((short) 0x0003, RunMapFromKeyPayload.class),
    /** The UPDATE_MATCHED_DIGITS handler. */
    UPDATE_MATCHED_DIGITS       ((short) 0x0005, UpdateMatchedDigitsPayload.class),
    /** The FURNISH_CHARGING_INFORMATION handler. */
    FURNISH_CHARGING_INFORMATION((short) 0x0009),
    /** The GET_NP_PARAMETERS handler. */
    GET_NP_PARAMETERS           ((short) 0x001D),
    /** The SET_NP_PARAMETERS handler. */
    SET_NP_PARAMETERS           ((short) 0x001E, NumberPortabilityPayload.class);

    private final short number;
    private final Class<? extends TelsisHandlerPayload> payloadClass;

    private TelsisHandlerNumber(final short newNumber) {
        this(newNumber, null);
    }

    private TelsisHandlerNumber(final short newNumber,
            final Class<? extends TelsisHandlerPayload> newPayloadClass) {
        this.number = newNumber;
        this.payloadClass = newPayloadClass;
    }

    /**
     * @return the handler number
     */
    public short getNumber() {
        return number;
    }

    /**
     * @return the class of the payload, or null if there is not payload
     */
    public Class<? extends TelsisHandlerPayload> getPayloadClass() {
        return payloadClass;
    }

    /**
     * A mapping of handler number to TelsisHandlerNumber instances.
     */
    private static Map<Short, TelsisHandlerNumber> handlerNumbers
            = new HashMap<Short, TelsisHandlerNumber>();

    static {
        // Load all handler numbers defined in TelsisHandlerNumber
        for (TelsisHandlerNumber handler : EnumSet.allOf(TelsisHandlerNumber.class)) {
            handlerNumbers.put(handler.getNumber(), handler);
        }
    }

    /**
     * Lookup the TelsisHandlerNumber using the received number.
     * @param number
     *            the received number
     * @return the TelsisHandlerNumber that it resolved to
     */
    public static TelsisHandlerNumber fromShort(final short number) {
        TelsisHandlerNumber handlerNumber = handlerNumbers.get(number);

        if (handlerNumber == null) {
            throw new IllegalArgumentException("Invalid handler number");
        }

        return handlerNumber;
    }

}
