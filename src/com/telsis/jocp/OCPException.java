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

/**
 * Signals that an OCP exception has occurred. This is used either when a more
 * specific exception is not available, or a non-OCP exception was detected (in
 * which case that will be stored as the cause of this exception).
 *
 * @author Telsis
 */
public class OCPException extends Exception {
    /**
     * The serial number.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new OCPException.
     */
    public OCPException() {
        super();
    }

    /**
     * Create a new OCPException with the specified message and cause.
     *
     * @param message
     *            a description for this exception
     * @param cause
     *            the exception that caused this exception
     */
    public OCPException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new OCPException with the specified message.
     *
     * @param message
     *            a description for this exception
     */
    public OCPException(final String message) {
        super(message);
    }

    /**
     * Create a new OCPException with the specified cause.
     *
     * @param cause
     *            the exception that caused this exception
     */
    public OCPException(final Throwable cause) {
        super(cause);
    }
}
