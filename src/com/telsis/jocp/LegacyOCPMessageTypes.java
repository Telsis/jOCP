/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2010-2013.
 *
 * This program is free software: you can redistribute it and/or modify it under
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
 * An enumeration of all supported OCP Messages.
 *
 * @author Telsis
 */
public enum LegacyOCPMessageTypes {
    // Link Messages
    /**
     * Send this message to check the status of the link between the sending and
     * remote platforms.
     *
     * @see Heartbeat
     */
    HEARTBEAT((short) 0x0000, Heartbeat.class, null, true, true),
    /**
     * Send this message to determine if the remote platform is running as
     * master or slave.
     *
     * @see AreYouMaster
     */
    ARE_YOU_MASTER((short) 0x0001, AreYouMaster.class, null, false, true),
    /**
     * Send this message to force the remote master/slave platform to run as
     * master.
     *
     * @see BecomeMaster
     */
    BECOME_MASTER((short) 0x0002, BecomeMaster.class, null, false, true),
    /**
     * Send this message in response to a {@link StatusRequest Status Request}
     * message to indicate the number of calls in progress on the platform.
     *
     * @see StatusResponse
     */
    STATUS_RESPONSE((short) 0x0003, StatusResponse.class, null, false, true),
    /**
     * Send this message in response to an {@link AreYouMaster Are You Master}
     * message to indicate the platform's master/slave status.
     *
     * @see AreYouMasterReply
     */
    ARE_YOU_MASTER_REPLY((short) 0x0004, AreYouMasterReply.class, null, true,
            false),
    /**
     * Send this message to get the remote platform's status details.
     *
     * @see StatusRequest
     */
    STATUS_REQUEST((short) 0x0005, StatusRequest.class, null, true, false),
    /**
     * Send this message when the active link between the sending and remote
     * platforms is down or if the sending platform needs to swap links to
     * recover from errors (for example, errors caused by corrupt data packets).
     *
     * @see ChangeActiveLink
     */
    CHANGE_ACTIVE_LINK((short) 0x0006, ChangeActiveLink.class, null, false,
            true),
    /**
     * Send this message in response to the fastSSP's {@link ChangeActiveLink
     * Change Active Link} message.
     *
     * @see ChangeActiveLinkAck
     */
    CHANGE_ACTIVE_LINK_ACK((short) 0x0007, ChangeActiveLinkAck.class, null,
            true, false),
    /**
     * Send this message when the platform receives a link management message it
     * is unable to interpret.
     *
     * @see LinkCommandUnsupported
     */
    LINK_COMMAND_UNSUPPORTED((short) 0x0008, LinkCommandUnsupported.class,
            null, true, true),
    /**
     * Send this message to tell the remote platform to gap calls in accordance
     * with the specified criteria.
     *
     * @see CallGap
     */
    CALL_GAP((short) 0x0009, CallGap.class, null, true, false),
    /**
     * Send this message to indicate which load-sharing fastSCP should take
     * exclusive control of calls.
     *
     * @see PreferredUnit
     */
    PREFERRED_UNIT((short) 0x000a, PreferredUnit.class, null, true, false),

    // Call Messages
    /**
     * Send this message to inform an SCP about the arrival of a call.
     *
     * @see InitialDP
     */
    INITIAL_DP((short) 0x1000, InitialDP.class, OCPMessageTypes.INITIAL_DP,
            false, true),
    /**
     * Send this message to inform an SCP about the arrival of a call.
     *
     * @see InitialDPServiceKey
     */
    INITIAL_DP_SERVICE_KEY((short) 0x1001, InitialDPServiceKey.class,
            OCPMessageTypes.INITIAL_DP_SERVICE_KEY, false, true),
    /**
     * Send this message to tell an SCP that a call has cleared down.
     *
     * @see CallCleardown
     */
    CALL_CLEARDOWN((short) 0x1002, CallCleardown.class,
            OCPMessageTypes.CALL_CLEARDOWN, false, true),
    /**
     * Send this message in response to the {@link DeliverTo Deliver To}
     * message.
     *
     * @see DeliverToResult
     */
    DELIVER_TO_RESULT((short) 0x1003, DeliverToResult.class,
            OCPMessageTypes.DELIVER_TO_RESULT, false, true),
    /**
     * Send this message in response to a {@link TelsisHandler Telsis Handler}
     * or {@link TelsisHandlerWithParty Telsis Handler With Party} message.
     *
     * @see TelsisHandlerResult
     */
    TELSIS_HANDLER_RESULT((short) 0x1007, TelsisHandlerResult.class,
            OCPMessageTypes.TELSIS_HANDLER_RESULT, false, true),
    /**
     * Send this message to instruct a call-handling platform to outdial using
     * the specified number and connect an existing caller to the outdialled
     * party.
     *
     * @see DeliverTo
     */
    DELIVER_TO((short) 0x100C, DeliverTo.class, OCPMessageTypes.DELIVER_TO,
            true, false),
    /**
     * Send this message to instruct the call-handling platform to clear a call.
     *
     * @see RequestCleardown
     */
    REQUEST_CLEARDOWN((short) 0x100D, RequestCleardown.class,
            OCPMessageTypes.REQUEST_CLEARDOWN, true, false),
    /**
     * Send this message to instruct a call-handling platform to answer a call
     * and to set up a forward audio path (a forward audio path allows the
     * calling party to be heard by the called party).
     *
     * @see AnswerCall
     */
    ANSWER_CALL((short) 0x100E, AnswerCall.class, OCPMessageTypes.ANSWER_CALL,
            true, false),
    /**
     * Send this message to run a function handler on the remote platform.
     *
     * @see TelsisHandler
     */
    TELSIS_HANDLER((short) 0x1012, TelsisHandler.class,
            OCPMessageTypes.TELSIS_HANDLER, true, false),
    /**
     * Send this message to tell the remote platform to continue processing a
     * call that is suspended at a detection point.
     *
     * @see INAPContinue
     */
    INAP_CONTINUE((short) 0x1013, INAPContinue.class,
            OCPMessageTypes.INAP_CONTINUE, true, false),
    /**
     * Send this message to determine whether a particular task on the remote
     * platform is currently active (that is, running).
     *
     * @see TaskActive
     */
    TASK_ACTIVE((short) 0x1014, TaskActive.class, OCPMessageTypes.TASK_ACTIVE,
            true, false),
    /**
     * Send this message in response to a {@link TaskActive Task Active}
     * message.
     *
     * @see TaskActiveResult
     */
    TASK_ACTIVE_RESULT((short) 0x1015, TaskActiveResult.class,
            OCPMessageTypes.TASK_ACTIVE_RESULT, false, true),
    /**
     * Send this message to tell a call-handling platform that the SCP has no
     * free capacity to run a map to control the call.
     *
     * @see InsufficientResources
     */
    INSUFFICIENT_RESOURCES((short) 0x1016, InsufficientResources.class,
            OCPMessageTypes.INSUFFICIENT_RESOURCES, true, false),
    /**
     * Send this message to a remote platform if the task to which a message
     * from that platform was directed is no longer running or if the leg of the
     * call to which the message was directed is no longer present.
     *
     * @see Abort
     */
    ABORT((short) 0x1017, Abort.class, OCPMessageTypes.ABORT, true, true),
    /**
     * Send this message in response to an OCP service layer message that is
     * unrecognised, is incorrectly formatted or has been sent at an invalid
     * time.
     *
     * @see CallCommandUnsupported
     */
    CALL_COMMAND_UNSUPPORTED((short) 0x1018, CallCommandUnsupported.class,
            OCPMessageTypes.CALL_COMMAND_UNSUPPORTED, true, true),
    /**
     * Send this message (if required) in response to {@link InitialDP Initial
     * DP} or {@link InitialDPServiceKey Initial DP Service Key}.
     *
     * @see InitialDPResponse
     */
    INITIAL_DP_RESPONSE((short) 0x1019, InitialDPResponse.class,
            OCPMessageTypes.INITIAL_DP_RESPONSE, true, false),
    /**
     * Send this message in response to an {@link AnswerCall Answer Call}
     * message.
     *
     * @see AnswerResult
     */
    ANSWER_RESULT((short) 0x101A, AnswerResult.class,
            OCPMessageTypes.ANSWER_RESULT, false, true),
    /**
     * Send this message if you want to include custom Call Detail Record (CDR)
     * information to a particular call leg on a remote call-handling platform.
     *
     * @see SetCDRExtendedFieldData
     */
    SET_CDR_EXTENDED_FIELD_DATA((short) 0x101F, SetCDRExtendedFieldData.class,
            OCPMessageTypes.SET_CDR_EXTENDED_FIELD_DATA, true, false),
    /**
     * Send this message in response to the {@link SetCDRExtendedFieldData}
     * message.
     *
     * @see SetCDRExtendedFieldDataResult
     */
    SET_CDR_EXTENDED_FIELD_DATA_RESULT((short) 0x1020,
            SetCDRExtendedFieldDataResult.class,
            OCPMessageTypes.SET_CDR_EXTENDED_FIELD_DATA_RESULT, false, true),
    /**
     * Send this message to instruct a call-handling platform to connect to an
     * external resource.
     *
     * @see ConnectToResource
     */
    CONNECT_TO_RESOURCE((short) 0x1021, ConnectToResource.class,
            OCPMessageTypes.CONNECT_TO_RESOURCE, true, false),
    /**
     * Send this message in response to the {@link ConnectToResource Connect to
     * Resource} message.
     *
     * @see ConnectToResourceAck
     */
    CONNECT_TO_RESOURCE_ACK((short) 0x1022, ConnectToResourceAck.class,
            OCPMessageTypes.CONNECT_TO_RESOURCE_ACK, false, true),
    /**
     * Send this message to instruct the call-handling platform to disconnect
     * from the external resource.
     *
     * @see DisconnectFromResource
     */
    DISCONNECT_FROM_RESOURCE((short) 0x1023, DisconnectFromResource.class,
            OCPMessageTypes.DISCONNECT_FROM_RESOURCE, true, false),
    /**
     * Send this message in response to the {@link DisconnectFromResource
     * Disconnect From Resource} message.
     *
     * @see DisconnectFromResourceAck
     */
    DISCONNECT_FROM_RESOURCE_ACK((short) 0x1024,
            DisconnectFromResourceAck.class,
            OCPMessageTypes.DISCONNECT_FROM_RESOURCE_ACK, false, true),
    /**
     * Send this message to run a function handler on the remote platform.
     *
     * @see TelsisHandlerWithParty
     */
    TELSIS_HANDLER_WITH_PARTY((short) 0x1029, TelsisHandlerWithParty.class,
            OCPMessageTypes.TELSIS_HANDLER_WITH_PARTY, true, false),
    /**
     * Send this message to instruct a call-handling platform to outdial using
     * the specified number and connect an existing caller to the outdialled
     * party.
     *
     * @see DeliverToWithFlags
     */
    DELIVER_TO_WITH_FLAGS((short) 0x102A, DeliverToWithFlags.class,
            OCPMessageTypes.DELIVER_TO_WITH_FLAGS, true, false);

    /**
     * The command code for this OCP message.
     */
    private short                             commandCode;
    /**
     * The {@link Class} that implements this OCP message. This must be a
     * subclass of {@link LegacyOCPMessage}.
     */
    private Class<? extends LegacyOCPMessage> implementation;
    /**
     * The {@link OCPMessageTypes} of this OCP message.
     */
    private OCPMessageTypes                   baseMessageType;

    /**
     * Boolean indicating that we support receiving this message.
     */
    private boolean                           supportRx;

    /**
     * Boolean indicating that we support sending this message.
     */
    private boolean                           supportTx;

    /**
     * Called by Java to create the individual enumeration values.
     *
     * @param newCommandCode
     *            the command code for this message type
     * @param newImplementation
     *            the class that implements this message type
     */
    private LegacyOCPMessageTypes(final short newCommandCode,
            final Class<? extends LegacyOCPMessage> newImplementation,
            final OCPMessageTypes newBaseMessageType,
            final boolean newSupportRx, final boolean newSupportTx) {
        this.commandCode = newCommandCode;
        this.implementation = newImplementation;
        this.baseMessageType = newBaseMessageType;
        this.supportRx = newSupportRx;
        this.supportTx = newSupportTx;
    }

    /**
     * Gets the command code for this message type.
     *
     * @return the command code for this message type
     */
    public short getCommandCode() {
        return commandCode;
    }

    /**
     * Gets the class that implements this message type.
     *
     * @return the class that implements this message type
     */
    public Class<? extends LegacyOCPMessage> getImplementation() {
        return implementation;
    }

    /**
     * Gets the {@link OCPMessageTypes} of this OCP message.
     *
     * @return the {@link OCPMessageTypes} of this OCP message
     */
    public final OCPMessageTypes getBaseMessageType() {
        return baseMessageType;
    }

    /**
     * Gets the receive support indicator for this message type.
     *
     * @return a boolean indicating we support receiving this message
     */
    public boolean getSupportRx() {
        return supportRx;
    }

    /**
     * Gets the transmit support indicator for this message type.
     *
     * @return a boolean indicating we support sending this message
     */
    public boolean getSupportTx() {
        return supportTx;
    }
}
