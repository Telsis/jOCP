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
 * An enumeration of all supported reasons for congestion.
 */
public enum CongestionType {
    /** The server is out of memory. */
    OUT_OF_MEMORY,

    /** The server has a high CPU load. */
    HIGH_CPU_LOAD,

    /** The customer has exceeded the configured licence limit. */
    LICENCE_LIMIT_EXCEEDED,

    /** A message buffer is almost full. */
    BUFFERS_ALMOST_FULL,

    /** A message buffer is full. */
    BUFFERS_FULL,

    /** A message buffer has overflowed. */
    BUFFER_OVERFLOW,

    /** All available ports are in use. */
    ALL_AVAILABLE_PORTS_IN_USE,

    /**
     * The incoming call rate is too high so calls are not being cleared fast
     * enough.
     */
    INCOMING_CALL_RATE_TOO_HIGH,

    /**
     * The server cannot process new messages so a backlog is being created.
     */
    UNABLE_TO_PROCESS_NEW_MESSAGES;
}
