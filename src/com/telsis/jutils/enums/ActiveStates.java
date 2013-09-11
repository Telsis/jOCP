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

package com.telsis.jutils.enums;

/**
 * An enumeration of the states the server could be in.
 */
public enum ActiveStates {
    /** The server is active and can receive calls. */
    ACTIVE,

    /** The server is inactive and will reject new calls. */
    INACTIVE,

    /**
     * The server is going inactive. New calls will be rejected and existing
     * calls are being forcibly cleared.
     */
    GOING_INACTIVE_FORCED,

    /**
     * The server is going inactive. New calls will be rejected. Existing calls
     * will be permitted to continue.
     */
    GOING_INACTIVE_UNFORCED;
}
