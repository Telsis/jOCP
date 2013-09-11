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
package com.telsis.jocp;

import java.util.List;
import java.util.Properties;

import com.telsis.jutils.watchdog.GenericWatchdog;

/**
 * Generic interface for an OCP client. In addition to the methods contained in
 * this interface, an implementation <b>must</b> have a constructor that takes
 * a single {@link Properties} parameter.
 */
public interface OCPClient {
    /**
     * Connect to the remote units. This method returns asynchronously.
     */
    void connect();

    /**
     * Connect to the remote units. This method returns asynchronously.
     *
     * @param watchdog
     *            the watchdog to use
     */
    void connect(GenericWatchdog watchdog);

    /**
     * Register a handler for the management task ID. This handler will receive
     * all OCP call control messages for the task ID 0xFFFFFFFF. Only one
     * handler can be registered at a time for a given task ID - if the task ID
     * is already in use, the existing handler will be removed.
     * <p>
     * This handler does not count as an active call.
     *
     * @param dispatcher
     *            The handler to register.
     * @see #deregisterManagementTidHandler()
     */
    void registerManagementTidHandler(OCPMessageHandler dispatcher);

    /**
     * Deregister a handler for the management task ID.
     *
     * @see #registerManagementTidHandler(OCPMessageHandler)
     */
    void deregisterManagementTidHandler();

    /**
     * Disconnect from the remote units. This method does not return until all
     * units have been disconnected.
     */
    void disconnect();

    /**
     * Reload the properties using the specified Properties object. This will
     * result in all OCP links being temporarily lost.
     *
     * @param prop
     *            The Properties object to use
     */
    void reloadProperties(Properties prop);

    /**
     * Select an OCP link to use for a new call. Any links that are specified in
     * triedLinks will not be returned by this function.
     *
     * @param triedLinks
     *            A list of links that have already been tried and so may not be
     *            returned by this function.
     * @return The OCP link to try next. If null is returned then there are no
     *         more available links.
     */
    OCPLink getLink(List<OCPLink> triedLinks);
}
