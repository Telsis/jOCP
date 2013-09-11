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

/**
 * Generic interface for OCP messages.
 *
 * @author Telsis
 */
public interface OCPMessage {
    /**
     * The task ID for management messages.
     */
    int MANAGEMENT_TASK_ID = 0xFFFFFFFF;

    /**
     * Gets the message type, if relevant. Messages that are link-specific
     * should return null.
     *
     * @return the message type.
     */
    OCPMessageTypes getMessageType();

    /**
     * Gets the destination task ID (also known as the forward task ID) for this
     * message. This is the Task ID on the unit to which this message is being
     * sent.
     *
     * @return the destination task ID.
     */
    int getDestTID();

    /**
     * Sets the destination task ID (also known as the forward task ID) for this
     * message. This is the Task ID on the unit to which this message is being
     * sent.
     *
     * @param newDestTID
     *            the new destination task ID.
     */
    void setDestTID(final int newDestTID);

    /**
     * Gets the origination task ID (also known as the backwards task ID) for
     * this message. This is the Task ID on the unit from which this message is
     * being sent
     *
     * @return the origination task ID.
     */
    int getOrigTID();

    /**
     * Sets the origination task ID (also known as the backwards task ID) for
     * this message. This is the Task ID on the unit from which this message is
     * being sent
     *
     * @param newOrigTID
     *            the new origination task ID.
     */
    void setOrigTID(final int newOrigTID);
}
