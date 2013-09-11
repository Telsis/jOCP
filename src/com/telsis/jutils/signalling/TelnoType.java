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

/**
 * This enumeration represents the type of a telephone number.
 *
 * @author Telsis
 */
public enum TelnoType {
    /**
     * This represents a telno of an unknown type. In legacy signalling
     * schemes this should be represented by the type and plan "unknown".
     */
    UNKNOWN,

    /**
     * This represents a telno following the international E.164 numbering
     * plan. In legacy signalling schemes this should be represented by the
     * type "international" and plan "E.164".
     */
    INTERNATIONAL,

    /**
     * This represents a telno following a private numbering plan. In legacy
     * signalling schemes this should be represented by the type "unknown"
     * and plan "private".
     */
    PRIVATE,

    /**
     * This represents a telno following the ISDN numbering plan.
     * It will require processing for normalisation.
     * In legacy signalling schemes this should be represented by the
     * type "unknown" and plan "E.164".
     */
    UNKNOWN_TELEPHONY
}
