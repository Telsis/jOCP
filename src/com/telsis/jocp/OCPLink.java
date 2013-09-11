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
 * Interface for sending and receiving messages via an external OCP link. If a
 * class is capable of sending and receiving OCP messages it must implement this
 * interface.
 * <p/>
 * To transmit an OCP message, call {@link OCPMessageHandler#queueMessage}.
 * <p/>
 * To receive OCP messages, first register an implementation of
 * {@link OCPMessageHandler} for a local task ID using
 * {@link #registerTidHandler(OCPMessageHandler, int)}. The registered class'
 * {@link OCPMessageHandler#queueMessage} method will then be called for each
 * message received from the external link that matches the registered
 * destination taskID. To stop receiving messages, call
 * {@link #deregisterTidHandler(int)}.
 *
 * @author Telsis
 */
public interface OCPLink extends OCPMessageHandler {
    /**
     * Register a handler for a local task ID. The registered handler will
     * receive all OCP messages for the registered task ID. Only one handler can
     * be registered at a time for a given task ID - if the task ID is already
     * in use, the existing handler will be removed.
     *
     * @param handler
     *            The handler to register
     * @param tid
     *            The OCP Task ID that the handler is interested in
     */
    void registerTidHandler(OCPMessageHandler handler, int tid);

    /**
     * Deregister a handler for a local task ID.
     *
     * @param tid
     *            The task ID to remove the handler for
     */
    void deregisterTidHandler(int tid);
}
