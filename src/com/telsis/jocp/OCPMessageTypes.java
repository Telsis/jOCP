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

import com.telsis.jocp.messages.*; // CSIGNORE: AvoidStarImport This uses almost everything there

/**
 * An enumeration of all known OCP Messages.
 *
 * @author Telsis
 */
public enum OCPMessageTypes {
    // Call Messages
    /**
     * Send this message to inform an SCP about the arrival of a call.
     *
     * @see InitialDP
     */
    INITIAL_DP,
    /**
     * Send this message to inform an SCP about the arrival of a call.
     *
     * @see InitialDPServiceKey
     */
    INITIAL_DP_SERVICE_KEY,
    /**
     * Send this message to tell an SCP that a call has cleared down.
     *
     * @see CallCleardown
     */
    CALL_CLEARDOWN,
    /**
     * Send this message in response to the {@link DeliverTo Deliver To}
     * message.
     *
     * @see DeliverToResult
     */
    DELIVER_TO_RESULT,
    /**
     * Send this message in response to a {@link TelsisHandler Telsis Handler}
     * or {@link TelsisHandlerWithParty Telsis Handler With Party} message.
     *
     * @see TelsisHandlerResult
     */
    TELSIS_HANDLER_RESULT,
    /**
     * Send this message to instruct a call-handling platform to outdial using
     * the specified number and connect an existing caller to the outdialled
     * party.
     *
     * @see DeliverTo
     */
    DELIVER_TO,
    /**
     * Send this message to instruct the call-handling platform to clear a call.
     *
     * @see RequestCleardown
     */
    REQUEST_CLEARDOWN,
    /**
     * Send this message to instruct a call-handling platform to answer a call
     * and to set up a forward audio path (a forward audio path allows the
     * calling party to be heard by the called party).
     *
     * @see AnswerCall
     */
    ANSWER_CALL,
    /**
     * Send this message to run a function handler on the remote platform.
     *
     * @see TelsisHandler
     */
    TELSIS_HANDLER,
    /**
     * Send this message to tell the remote platform to continue processing a
     * call that is suspended at a detection point.
     *
     * @see INAPContinue
     */
    INAP_CONTINUE,
    /**
     * Send this message to determine whether a particular task on the remote
     * platform is currently active (that is, running).
     *
     * @see TaskActive
     */
    TASK_ACTIVE,
    /**
     * Send this message in response to a {@link TaskActive Task Active}
     * message.
     *
     * @see TaskActiveResult
     */
    TASK_ACTIVE_RESULT,
    /**
     * Send this message to tell a call-handling platform that the SCP has no
     * free capacity to run a map to control the call.
     *
     * @see InsufficientResources
     */
    INSUFFICIENT_RESOURCES,
    /**
     * Send this message to a remote platform if the task to which a message
     * from that platform was directed is no longer running or if the leg of the
     * call to which the message was directed is no longer present.
     *
     * @see Abort
     */
    ABORT,
    /**
     * Send this message in response to an OCP service layer message that is
     * unrecognised, is incorrectly formatted or has been sent at an invalid
     * time.
     *
     * @see CallCommandUnsupported
     */
    CALL_COMMAND_UNSUPPORTED,
    /**
     * Send this message (if required) in response to {@link InitialDP Initial
     * DP} or {@link InitialDPServiceKey Initial DP Service Key}.
     *
     * @see InitialDPResponse
     */
    INITIAL_DP_RESPONSE,
    /**
     * Send this message in response to an {@link AnswerCall Answer Call}
     * message.
     *
     * @see AnswerResult
     */
    ANSWER_RESULT,
    /**
     * Send this message if you want to include custom Call Detail Record (CDR)
     * information to a particular call leg on a remote call-handling platform.
     *
     * @see SetCDRExtendedFieldData
     */
    SET_CDR_EXTENDED_FIELD_DATA,
    /**
     * Send this message in response to the {@link SetCDRExtendedFieldData}
     * message.
     *
     * @see SetCDRExtendedFieldDataResult
     */
    SET_CDR_EXTENDED_FIELD_DATA_RESULT,
    /**
     * Send this message to instruct a call-handling platform to connect to an
     * external resource.
     *
     * @see ConnectToResource
     */
    CONNECT_TO_RESOURCE,
    /**
     * Send this message in response to the {@link ConnectToResource Connect to
     * Resource} message.
     *
     * @see ConnectToResourceAck
     */
    CONNECT_TO_RESOURCE_ACK,
    /**
     * Send this message to instruct the call-handling platform to disconnect
     * from the external resource.
     *
     * @see DisconnectFromResource
     */
    DISCONNECT_FROM_RESOURCE,
    /**
     * Send this message in response to the {@link DisconnectFromResource
     * Disconnect From Resource} message.
     *
     * @see DisconnectFromResourceAck
     */
    DISCONNECT_FROM_RESOURCE_ACK,
    /**
     * Send this message to run a function handler on the remote platform.
     *
     * @see TelsisHandlerWithParty
     */
    TELSIS_HANDLER_WITH_PARTY,
    /**
     * Send this message to instruct a call-handling platform to outdial using
     * the specified number and connect an existing caller to the outdialled
     * party.
     *
     * @see DeliverToWithFlags
     */
    DELIVER_TO_WITH_FLAGS,
}
