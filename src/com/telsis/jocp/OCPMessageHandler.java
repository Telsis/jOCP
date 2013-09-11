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
 * Interface for receiving OCP messages. If a class is capable of receiving OCP
 * messages it must implement this interface.
 *
 * @author Telsis
 */
public interface OCPMessageHandler {
    /**
     * Receive an OCP message. This function may be called from multiple
     * threads, so implementations need to ensure any data structures used are
     * thread-safe.
     * <p/>
     * If this function is called by the OCP Link Manager, then it will pass a
     * reference to itself as an {@link OCPLink} as the callingLink parameter.
     * <p/>
     * Implementations may block if congested.
     *
     * @param message
     *            The OCP message to be processed by the implementation.
     * @param callingLink
     *            The OCPLink instance that called queueMessage, if available.
     */
    void queueMessage(OCPMessage message, OCPLink callingLink);
}
