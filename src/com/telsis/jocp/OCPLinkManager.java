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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.telsis.jocp.messages.Abort;
import com.telsis.jocp.messages.CallControlMessage;
import com.telsis.jocp.messages.CallGap;
import com.telsis.jocp.messages.Heartbeat;
import com.telsis.jocp.messages.LinkCommandUnsupported;
import com.telsis.jocp.messages.LinkMessage;
import com.telsis.jocp.messages.PreferredUnit;
import com.telsis.jocp.messages.StatusRequest;
import com.telsis.jocp.messages.StatusResponse;
import com.telsis.jutils.UtilitiesFactory;
import com.telsis.jutils.interfaces.StatsInterface;
import com.telsis.jutils.watchdog.GenericWatchdog;
import com.telsis.jutils.watchdog.NullGenericWatchdog;

/**
 * This class manages a single OCP link.
 * <p/>
 * The following properties are used (where &lt;index&gt; is the index number of
 * this instance as specified in the constructor):
 * <table>
 * <tr>
 * <th>Setting</th>
 * <th>Meaning</th>
 * <th>Type/units</th>
 * <th>Default</th>
 * <th>Range</th>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;FixedHeartbeat</td>
 * <td>The frequency in seconds that a Heartbeat is sent irrespective of other
 * traffic on the link.</td>
 * <td>Number (seconds)</td>
 * <td>10</td>
 * <td>1-3600</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;InactiveHeartbeat</td>
 * <td>The frequency that a Heartbeat is sent if there is no other traffic on
 * the link.</td>
 * <td>Number (seconds)</td>
 * <td>1</td>
 * <td>1-3600</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;RemoteAddress</td>
 * <td>The remote IP address (or hostname) that this link will connect to.</td>
 * <td>String</td>
 * <td>localhost</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;RemotePort</td>
 * <td>The remote TCP / IP port that this link will connect to.</td>
 * <td>Number</td>
 * <td>10012</td>
 * <td>0-65535</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;Timeout</td>
 * <td>If no data is received from the remote unit for this period, then the
 * link will be deemed to have failed and will be automatically reconnected.
 * This is also set as the TCP/IP system timeout.</td>
 * <td>Number (seconds)</td>
 * <td>30</td>
 * <td>1-43200</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;LocalAddress</td>
 * <td>The local IP address (or hostname) that this link will bind to.</td>
 * <td>String</td>
 * <td>localhost</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>ocpLink&lt;index&gt;LocalPort</td>
 * <td>The local port for the TCP connection. If zero a local port is selected
 * by the operating system.</td>
 * <td>Number</td>
 * <td>0</td>
 * <td>0-65535</td>
 * </tr>
 * <tr>
 * <td>ocpUnitName</td>
 * <td>The name to report in OCP {@link StatusResponse} messages. If blank, this
 * will be our FQDN. This property is truncated to 31 characters.</td>
 * <td>String</td>
 * <td><i>blank</i></td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 *
 * @author Telsis
 */
public class OCPLinkManager implements OCPMessageHandler, OCPLink {
    /**
     * This enumeration contains the possible states that an
     * {@link OCPLinkManager} instance can be in.
     *
     * @author Telsis
     */
    public enum LinkStates {
        /**
         * When in this state, this object is connected to a master SCP.
         */
        MASTER,
        /**
         * When in this state, this object is connected to a slave SCP.
         */
        SLAVE,
        /**
         * When in this state, this object is connected to a loadsharing SCP.
         */
        LOADSHARE,
        /**
         * When in this state, this object is in the process of opening a
         * connection to a SCP.
         */
        CONNECTING,
        /**
         * When in this state, this object is disconnected from the SCP and is
         * not attempting to open the connection.
         */
        DISCONNECTED
    }

    /**
     * This enumeration contains the possible states that the internal RxThread
     * object can be in.
     *
     * @author Telsis
     */
    private enum RxThreadStates {
        /**
         * The thread is not yet initialised.
         */
        UNINITIALISED,
        /**
         * The thread has been reset.
         */
        RESET,
        /**
         * The thread is disconnected.
         */
        UNCONNECTED,
        /**
         * The thread is in the process of opening a connection.
         */
        CONNECTING,
        /**
         * The thread has successfully connected to the remote server.
         */
        CONNECTED,
        /**
         * The thread is connected, and is not currently synchronised to the
         * incoming message stream.
         */
        NO_SYNC,
        /**
         * The thread is connected, and has found the first byte of a potential
         * EOM marker. The thread is waiting to see if the next byte is the
         * second EOM byte.
         */
        SECOND_EOM_BYTE,
        /**
         * The thread is connected, and has found a potential EOM marker. The
         * thread is validating the message length.
         */
        MATCH_LENGTH,
        /**
         * The thread is connected and synchronised, and is receiving a message
         * header.
         */
        GET_MESSAGE_HEADER,
        /**
         * The thread is connected and synchronised, and is receiving a message
         * body.
         */
        GET_MESSAGE_BODY,
        /**
         * The thread is connected and synchronised, and is attempting to decode
         * a complete message.
         */
        PROCESS_MESSAGE
    }

    /**
     * This enumeration contains the possible states that the internal TxThread
     * object can be in.
     *
     * @author Telsis
     */
    private enum TxThreadStates {
        /**
         * The thread is not yet initialised.
         */
        UNINITIALISED,
        /**
         * The thread is in the process of transmitting a message.
         */
        SENDING_MESSAGE,
        /**
         * The thread is obtaining the next message from the transmit queue.
         */
        GET_NEXT_MESSAGE,
        /**
         * The thread is generating a heartbeat to transmit.
         */
        GENERATE_HEARTBEAT
    }

    // Internal constants
    /** The length of the transmit queue. */
    private static final int    TX_QUEUE_LENGTH               = 100;
    /** Conversion between seconds and milliseconds. */
    private static final int    MILLISECONDS                  = 1000;
    // Configuration defaults
    /** The prefix for the per-link configuration settings. */
    private static final String CONFIG_PREFIX                 = "ocpLink";
    /** The fixed heartbeat interval. */
    private static final String CONFIG_FIXED_HEARTBEAT        = "FixedHeartbeat";
    /** The default value for the fixed heartbeat interval. */
    private static final String CONFIG_FIXED_HEARTBEAT_DEF    = "10";
    /** The minimum value for the fixed heartbeat interval. */
    private static final int    CONFIG_FIXED_HEARTBEAT_MIN    = 1;
    /** The maximum value for the fixed heartbeat interval. */
    private static final int    CONFIG_FIXED_HEARTBEAT_MAX    = 3600;
    /** The inactive heartbeat interval. */
    private static final String CONFIG_INACTIVE_HEARTBEAT     = "InactiveHeartbeat";
    /** The default value for the inactive heartbeat interval. */
    private static final String CONFIG_INACTIVE_HEARTBEAT_DEF = "1";
    /** The minimum value for the inactive heartbeat interval. */
    private static final int    CONFIG_INACTIVE_HEARTBEAT_MIN = 1;
    /** The maximum value for the inactive heartbeat interval. */
    private static final int    CONFIG_INACTIVE_HEARTBEAT_MAX = 3600;
    /** The remote address. */
    private static final String CONFIG_REMOTE_ADDRESS         = "RemoteAddress";
    /** The default value for the remote address. */
    private static final String CONFIG_REMOTE_ADDRESS_DEF     = "localhost";
    /** The remote port. */
    private static final String CONFIG_REMOTE_PORT            = "RemotePort";
    /** The default value for the remote port. */
    private static final String CONFIG_REMOTE_PORT_DEF        = "10012";
    /** The minimum value for the remote port. */
    private static final int    CONFIG_REMOTE_PORT_MIN        = 0;
    /** The maximum value for the remote port. */
    private static final int    CONFIG_REMOTE_PORT_MAX        = 65535;
    /** The connection timeout. */
    private static final String CONFIG_TIMEOUT                = "Timeout";
    /** The default value for the connection timeout. */
    private static final String CONFIG_TIMEOUT_DEF            = "30";
    /** The minimum value for the connection timeout. */
    private static final int    CONFIG_TIMEOUT_MIN            = 1;
    /** The maximum value for the connection timeout. */
    private static final int    CONFIG_TIMEOUT_MAX            = 43200;
    /** The local address. */
    private static final String CONFIG_LOCAL_ADDRESS          = "LocalAddress";
    /** The default value for the local address. */
    private static final String CONFIG_LOCAL_ADDRESS_DEF      = "localhost";
    /** The local port. */
    private static final String CONFIG_LOCAL_PORT             = "LocalPort";
    /** The default value for the local port. */
    private static final String CONFIG_LOCAL_PORT_DEF         = "0";
    /** The minimum value for the local port. */
    private static final int    CONFIG_LOCAL_PORT_MIN         = 0;
    /** The maximum value for the local port. */
    private static final int    CONFIG_LOCAL_PORT_MAX         = 65535;
    /** The unit name. */
    private static final String CONFIG_UNIT_NAME              = "ocpSystemUnitName";
    /** The maximum value for the unit name. */
    private static final int    CONFIG_UNIT_NAME_MAX          = 31;


    /** The prefix for the per-link statistics. */
    private static final String STAT_PREFIX = "ocpLink";
    /** The prefix for the per-link statistics, including link ID. */
    private String statPrefix;
    /** Statistic keyword for OCP messages that are bad in some way. */
    private static final String STAT_BAD_MESSAGE = "BadMessage";
    /** Statistic keyword for requested outgoing heartbeats. */
    private static final String STAT_OUTGOING_HEARTBEAT = "HeartbeatRequested";
    /** Statistic keyword for successful connection attempts. */
    private static final String STAT_CONNECTION_SUCCESS = "Established";
    /** Statistic keyword for failed connections. */
    private static final String STAT_CONNECTION_FAIL = "Failed";
    /** Statistic keyword for master/slave swap events. */
    private static final String STAT_MASTER_SLAVE_SWAP = "MasterSlaveSwap";

    /** Base statistic keyword for received messages. */
    private static final String STAT_MESSAGE_RX_BASE = "MessageRx";
    /** Base statistic keyword for transmitted messages. */
    private static final String STAT_MESSAGE_TX_BASE = "MessageTx";
    /** Statistic name for transmission queue length. */
    private static final String STAT_QUEUE = "TxQueueLength";

    // Configuration
    /** The properties object. */
    private Properties  prop;
    /** The index of this link. */
    private int linkIndex;
    /** The fixed heartbeat interval in milliseconds. */
    private int         fixedHeartbeatInterval;
    /** The inactive heartbeat interval in milliseconds. */
    private int         inactiveHeartbeatInterval;
    /** The remote address. */
    private InetAddress remoteAddress;
    /** The remote port. */
    private int         remotePort;
    /** The timeout in milliseconds. */
    private int         timeout;
    /** The local address. */
    private InetAddress localAddress;
    /** The local port. */
    private int         localPort;
    /** The unit name to report. */
    private String      unitName;

    /** The current state of the link. */
    private LinkStates linkState;
    /** The unit ID reported by the remote unit. */
    private int        unitID;
    /** The unit enabled state reported by the remote unit. */
    private boolean    unitEnabled;
    /** The cluster ID reported by the remote unit. */
    private int        clusterID;

    /** The transmit thread. */
    private TxThread                            txThread;
    /** The receive thread. */
    private RxThread                            rxThread;
    /** The link's socket. */
    private Socket                              socket;
    /** The link's socket channel. */
    private SocketChannel                       channel;
    /** The transmit queue. */
    private ArrayBlockingQueue<LegacyOCPMessage>      txMessages;
    /** All registered link state handlers. */
    private HashSet<OCPLinkStateHandler>        linkStateHandlers;
    /** Mapping between task IDs and registered message handlers. */
    private HashMap<Integer, OCPMessageHandler> tidHandlers;
    /** The management task ID handler. */
    private OCPMessageHandler managementTidHandler;
    /** The watchdog to use. */
    private GenericWatchdog watchdog;
    /** Whether or not this link has been cleaned up. */
    private volatile boolean cleanedUp = false;

    /** The logger for this class. */
    private static Logger log = Logger.getLogger("ocpLinkManager");
    /** The statistics interface. */
    private static StatsInterface stats = UtilitiesFactory
            .getUtilInterface().getStatsInterface();

    /**
     * Creates an OCP Link Manager in the DISCONNECTED state using the specified
     * properties object.
     *
     * @param properties
     *            The properties object used.
     * @param index
     *            The index of this OCP link. This parameter is used to
     *            distinguish between multiple OCP links defined in the same
     *            properties object.
     */
    public OCPLinkManager(final Properties properties, final int index) {
        this.prop = properties;

        new Version();

        linkIndex = index;
        loadConfig(properties);

        statPrefix = STAT_PREFIX + linkIndex;
        stats.registerStat(statPrefix + STAT_BAD_MESSAGE, true);
        stats.registerStat(statPrefix + STAT_OUTGOING_HEARTBEAT, true);
        stats.registerStat(statPrefix + STAT_CONNECTION_SUCCESS, true);
        stats.registerStat(statPrefix + STAT_CONNECTION_FAIL, true);
        stats.registerStat(statPrefix + STAT_MASTER_SLAVE_SWAP, true);
        stats.registerStat(statPrefix + STAT_QUEUE, false);

        EnumSet<LegacyOCPMessageTypes> typeSet =
                EnumSet.allOf(LegacyOCPMessageTypes.class);

        for (LegacyOCPMessageTypes type : typeSet) {
            if (type.getSupportRx()) {
                stats.registerStat(statPrefix + STAT_MESSAGE_RX_BASE
                        + type.getImplementation().getSimpleName(), true);
            }
            if (type.getSupportTx()) {
                stats.registerStat(statPrefix + STAT_MESSAGE_TX_BASE
                        + type.getImplementation().getSimpleName(), true);
            }
        }

        linkState = LinkStates.DISCONNECTED;
        linkStateHandlers = new HashSet<OCPLinkStateHandler>();
        tidHandlers = new HashMap<Integer, OCPMessageHandler>();
        managementTidHandler = null;
        txMessages = new ArrayBlockingQueue<LegacyOCPMessage>(TX_QUEUE_LENGTH);
    }

    /**
     * Loads the configuration.
     * @param properties The properties object to use
     * @return <code>true</code> if the configuration has changed
     *          enough to require a link restart.
     */
    private boolean loadConfig(final Properties properties) {

        log.warn("loadConfig link " + linkIndex);
        boolean significantChange = false;
        InetAddress tempAddress;
        int tempPort;
        this.prop = properties;

        String prefix = CONFIG_PREFIX + linkIndex;

        fixedHeartbeatInterval = Integer.parseInt(prop.getProperty(prefix
                + CONFIG_FIXED_HEARTBEAT, CONFIG_FIXED_HEARTBEAT_DEF));
        if (fixedHeartbeatInterval < CONFIG_FIXED_HEARTBEAT_MIN
                || fixedHeartbeatInterval > CONFIG_FIXED_HEARTBEAT_MAX) {
            throw new IllegalArgumentException(
                    "The fixed heartbeating interval is outside the valid "
                    + "range of " + CONFIG_FIXED_HEARTBEAT_MIN + " to "
                    + CONFIG_FIXED_HEARTBEAT_MAX);
        }
        fixedHeartbeatInterval *= MILLISECONDS;

        inactiveHeartbeatInterval = Integer.parseInt(prop.getProperty(prefix
                + CONFIG_INACTIVE_HEARTBEAT, CONFIG_INACTIVE_HEARTBEAT_DEF));
        if (inactiveHeartbeatInterval < CONFIG_INACTIVE_HEARTBEAT_MIN
                || inactiveHeartbeatInterval > CONFIG_INACTIVE_HEARTBEAT_MAX) {
            throw new IllegalArgumentException(
                    "The inactive heartbeating interval is outside the valid "
                    + "range of " + CONFIG_INACTIVE_HEARTBEAT_MIN + " to "
                    + CONFIG_INACTIVE_HEARTBEAT_MAX);
        }
        inactiveHeartbeatInterval *= MILLISECONDS;

        try {
            tempAddress = InetAddress.getByName(prop.getProperty(prefix
                    + CONFIG_REMOTE_ADDRESS, CONFIG_REMOTE_ADDRESS_DEF));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(
                    "The remote address is not valid", e);
        }
        if (!tempAddress.equals(remoteAddress)) {
            // Remote address has changed. Restart required.
            remoteAddress = tempAddress;
            significantChange = true;
        }

        tempPort = Integer.parseInt(prop.getProperty(prefix
                + CONFIG_REMOTE_PORT, CONFIG_REMOTE_PORT_DEF));
        if (tempPort < CONFIG_REMOTE_PORT_MIN
                || tempPort > CONFIG_REMOTE_PORT_MAX) {
            throw new IllegalArgumentException("The remote port is not valid.");
        }
        if (tempPort != remotePort) {
            // Remote port has changed. Restart required.
            remotePort = tempPort;
            significantChange = true;
        }

        timeout = Integer.parseInt(prop.getProperty(prefix + CONFIG_TIMEOUT,
                CONFIG_TIMEOUT_DEF));
        if (timeout < CONFIG_TIMEOUT_MIN || timeout > CONFIG_TIMEOUT_MAX) {
            throw new IllegalArgumentException(
                    "The timeout is outside the valid range of "
                    + CONFIG_TIMEOUT_MIN + " to " + CONFIG_TIMEOUT_MAX);
        }
        timeout *= MILLISECONDS;

        try {
            tempAddress = InetAddress.getByName(prop.getProperty(prefix
                    + CONFIG_LOCAL_ADDRESS, CONFIG_LOCAL_ADDRESS_DEF));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(
                    "The local address is not valid", e);
        }
        if (!tempAddress.equals(localAddress)) {
            // Local address has changed. Restart required.
            localAddress = tempAddress;
            significantChange = true;
        }

        tempPort = Integer.parseInt(prop.getProperty(prefix
                + CONFIG_LOCAL_PORT, CONFIG_LOCAL_PORT_DEF));
        if (tempPort < CONFIG_LOCAL_PORT_MIN
                || tempPort > CONFIG_LOCAL_PORT_MAX) {
            throw new IllegalArgumentException("The local port is not valid.");
        }
        if (tempPort != localPort) {
            // Local port has changed. Restart required.
            localPort = tempPort;
            significantChange = true;
        }

        String localFQDN;
        try {
            localFQDN = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            // Cannot resolve the local host
            localFQDN = "";
        }
        unitName = prop.getProperty(CONFIG_UNIT_NAME, localFQDN);
        if (unitName.length() > CONFIG_UNIT_NAME_MAX) {
            unitName = unitName.substring(0, CONFIG_UNIT_NAME_MAX);
        }

        log.setLevel(Level.toLevel(prop.getProperty("ocpSystemLoggingLevel",
                "WARN")));

        return significantChange;
    }

    /**
     * Reloads the configuration, restarting the link if necessary.
     *
     * @param properties
     *            The properties object to use
     * @return true, if the configuration has changed enough to require a link
     *         restart to fully apply (note that the link will not have been
     *         restarted by this method)
     */
    public final boolean reloadConfig(final Properties properties) {
        log.warn("reloadConfig Link " + linkIndex + " From " + properties);
        if (loadConfig(properties)) {
            log.warn("Link " + linkIndex + ": Restart required");
            return true;
        }
        return false;
    }
    /**
     * Clean up the OCP Link Manager. This method disconnects from the remote
     * server, removes all registered handlers and de-registers all statistics.
     * After calling this method the object can no longer be used.
     */
    protected final void cleanup() {
        if (cleanedUp) {
            return;
        }

        disconnect();

        stats.unregisterStat(statPrefix + STAT_BAD_MESSAGE);
        stats.unregisterStat(statPrefix + STAT_OUTGOING_HEARTBEAT);
        stats.unregisterStat(statPrefix + STAT_CONNECTION_SUCCESS);
        stats.unregisterStat(statPrefix + STAT_CONNECTION_FAIL);
        stats.unregisterStat(statPrefix + STAT_MASTER_SLAVE_SWAP);

        EnumSet<LegacyOCPMessageTypes> typeSet =
                EnumSet.allOf(LegacyOCPMessageTypes.class);

        for (LegacyOCPMessageTypes type : typeSet) {
            if (type.getSupportRx()) {
                stats.unregisterStat(statPrefix + STAT_MESSAGE_RX_BASE
                        + type.getImplementation().getSimpleName());
            }
            if (type.getSupportTx()) {
                stats.unregisterStat(statPrefix + STAT_MESSAGE_TX_BASE
                        + type.getImplementation().getSimpleName());
            }
        }
        tidHandlers.clear();
        linkStateHandlers.clear();
        cleanedUp = true;
    }

    @Override
    protected final void finalize() throws Throwable {
        cleanup();
    }

    /**
     * Inspect the message type and destination task ID, and despatch it to the
     * appropriate handler.
     *
     * @param message
     *            the OCP message to despatch
     * @throws LinkMessageException
     *             if the message is an unrecognised Link message
     */
    private void despatchMessage(final LegacyOCPMessage message)
            throws LinkMessageException {
        OCPMessageHandler handler = null;

        if (message instanceof LinkMessage) {
            // Link message, handle it internally
            handleMessage(message);
        } else if (message instanceof CallControlMessage) {
            // Call control message
            int taskID = message.getDestTID();
            synchronized (tidHandlers) {
                handler = tidHandlers.get(taskID);
            }
            if (handler != null) {
                // The task ID has been registered. Send it to the handler.
                try {
                    handler.queueMessage(message, this);
                } catch (Exception e) {
                    log.warn("Exception occurred in handler " + handler, e);
                }
            } else if (taskID == OCPMessage.MANAGEMENT_TASK_ID
                    && managementTidHandler != null) {
                // The message is for the management task ID and we have a
                // registered handler.
                try {
                    managementTidHandler.queueMessage(message, this);
                } catch (Exception e) {
                    log.warn("Exception occurred in management handler "
                            + managementTidHandler, e);
                }
            } else {
                // We don't know about this task ID. Reject the message.
                log.warn("Message received for unregistered task: command "
                        + message.getCommandCode() + ", destination " + taskID);
                if (!(message instanceof Abort)) { // Do not reply to an Abort
                    Abort abort = new Abort();
                    abort.setDestTID(message.getOrigTID());
                    abort.setOrigTID(message.getDestTID());
                    abort.setInvalidLegID(Abort.TASK_NOT_RUNNING);
                    this.queueMessage(abort, null);
                }
            }
        } else {
            // Unknown message type (but one that OCPMessage knows about!)
            log.warn("Received an OCP message with an unexpected command type");
        }
    }

    /**
     * Handle incoming link messages. The following link messages are supported:
     * <p/>
     * <table>
     * <tr>
     * <th>Message type</th>
     * <th>Action</th>
     * </tr>
     * <tr>
     * <td>{@link Heartbeat}</td>
     * <td>No action is taken</td>
     * </tr>
     * <tr>
     * <td>{@link StatusRequest}</td>
     * <td>The link enabled/disabled state, unit ID, cluster ID, and link status
     * is updated according to the fields in the Status Request. All registered
     * {@link OCPLinkStateHandler} instances are notified of any changes. A
     * {@link StatusResponse} is then sent containing the local unit name and
     * the number of calls in process (determined by the number of registered
     * task ID handlers).</td>
     * </tr>
     * <tr>
     * <td>{@link LinkCommandUnsupported}</td>
     * <td>The message details are logged.</td>
     * </tr>
     * <tr>
     * <td>{@link CallGap}</td>
     * <td>All registered {@link OCPLinkStateHandler} instances are notified of
     * the call gapping information.</td>
     * </tr>
     * <tr>
     * <td>{@link PreferredUnit}</td>
     * <td>All registered {@link OCPLinkStateHandler} instances are notified of
     * the preferred unit.</td>
     * </tr>
     * </table>
     * <p/>
     * Any unsupported messages results in an
     * {@link UnknownLinkMessageException} being thrown.
     *
     * @param message
     *            the OCP message to handle
     * @throws UnknownLinkMessageException
     *             if the command code is not recognised
     */
    private void handleMessage(final LegacyOCPMessage message)
            throws LinkMessageException {
        log.debug("Received " + message.getClass().getName() + ": " + message);

        if (message instanceof Heartbeat) { // CSIGNORE: EmptyBlock
            // No need to do anything
        } else if (message instanceof StatusRequest) {
            StatusRequest sr = (StatusRequest) message;

            boolean newUnitEnabled = ((sr.getFlags()
                    & StatusRequest.FLAG_UNIT_ENABLED)
                    == StatusRequest.FLAG_UNIT_ENABLED);
            if (newUnitEnabled != unitEnabled) {
                // Unit enabled/disabled state has changed, notify handlers
                changeEnabledState(newUnitEnabled);
            }

            this.unitID = sr.getUnitID();
            this.clusterID = sr.getClusterID();

            if (clusterID == StatusRequest.CLUSTERID_MASTERSLAVE) {
                // Running in master/slave mode
                if ((sr.getFlags() & StatusRequest.FLAG_MASTER)
                        == StatusRequest.FLAG_MASTER) {
                    if (linkState != LinkStates.MASTER) {
                        // Have moved into the master state
                        if (linkState == LinkStates.SLAVE) {
                            stats.incrementStat(
                                    statPrefix + STAT_MASTER_SLAVE_SWAP);
                        }
                        changeLinkState(LinkStates.MASTER);
                    }
                } else if (linkState != LinkStates.SLAVE) {
                    // Have moved into the slave state
                    if (linkState == LinkStates.MASTER) {
                        stats.incrementStat(
                                statPrefix + STAT_MASTER_SLAVE_SWAP);
                    }
                    changeLinkState(LinkStates.SLAVE);
                }
            } else if (linkState != LinkStates.LOADSHARE) {
                // Have moved into the loadsharing state
                changeLinkState(LinkStates.LOADSHARE);
            }

            StatusResponse response = new StatusResponse();
            response.setUnitName(unitName);
            // Each TID mapping is a call, so the number if active calls is
            // the number of TID mappings.
            synchronized (tidHandlers) {
                response.setActiveCalls((short) tidHandlers.size());
            }
            queueMessage(response, null);
        } else if (message instanceof LinkCommandUnsupported) {
            LinkCommandUnsupported lcu = (LinkCommandUnsupported) message;
            log.warn("Received LinkCommandUnsupported: commandCode "
                    + lcu.getCommandCode() + " reason " + lcu.getReason()
                    + " value " + lcu.getValue());
        } else if (message instanceof CallGap) {
            CallGap gap = (CallGap) message;
            changeCallGap(gap.getDuration());
        } else if (message instanceof PreferredUnit) {
            PreferredUnit prefUnit = (PreferredUnit) message;

            try {
                InetAddress preferredSCP = InetAddress.getByAddress(prefUnit
                        .getPreferredSCP());
                InetAddress secondarySCP = InetAddress.getByAddress(prefUnit
                        .getSecondarySCP());
                changePreferredUnit(preferredSCP, secondarySCP);
            } catch (UnknownHostException e) {
                // Should never happen = getByAddress doesn't do a reverse
                // lookup
                log.warn("Caught an exception while trying to decode a "
                        + "PreferredUnit message", e);
            }
        } else {
            // It's not a Link message we recognise, so send a LCU
            throw new LinkMessageException(message.getCommandCode(),
                    LinkCommandUnsupported.REASON_COMMAND_CODE_UNSUPPORTED);
        }
    }

    /**
     * Notify registered handlers that the link state has changed.
     *
     * @param newState
     *            the new state of the link
     */
    private void changeLinkState(final LinkStates newState) {
        if (linkState == newState) {
            return;
        }
        log.info("changeLinkState: changed from " + linkState + " to "
                + newState);
        linkState = newState;

        synchronized (linkStateHandlers) {
            Iterator<OCPLinkStateHandler> it = linkStateHandlers.iterator();
            while (it.hasNext()) {
                it.next().linkStateChanged(linkState);
            }
        }

        if (linkState == LinkStates.CONNECTING
                || linkState == LinkStates.DISCONNECTED) {
            changeEnabledState(false); // A disconnected link cannot be enabled
        }
    }

    /**
     * Notify registered handlers that the enabled state of the link has
     * changed.
     *
     * @param newEnabledState
     *            the new enabled state of the link
     */
    private void changeEnabledState(final boolean newEnabledState) {
        if (unitEnabled == newEnabledState) {
            return;
        }
        unitEnabled = newEnabledState;

        synchronized (linkStateHandlers) {
            Iterator<OCPLinkStateHandler> it = linkStateHandlers.iterator();
            while (it.hasNext()) {
                it.next().unitEnabledChanged(unitEnabled);
            }
        }
    }

    /**
     * Notify registered handlers that the preferred SCP has changed.
     *
     * @param preferredSCP
     *            the new preferred SCP.
     * @param secondarySCP
     *            the new secondary SCP.
     */
    private void changePreferredUnit(final InetAddress preferredSCP,
            final InetAddress secondarySCP) {
        synchronized (linkStateHandlers) {
            Iterator<OCPLinkStateHandler> it = linkStateHandlers.iterator();
            while (it.hasNext()) {
                it.next().receivedPreferredUnit(preferredSCP, secondarySCP);
            }
        }
    }

    /**
     * Notify handlers that the call gapping status has changed.
     *
     * @param duration
     *            the new call gapping duration
     */
    private void changeCallGap(final short duration) {
        synchronized (linkStateHandlers) {
            Iterator<OCPLinkStateHandler> it = linkStateHandlers.iterator();
            while (it.hasNext()) {
                it.next().receivedCallGap(duration);
            }
        }
    }

    /**
     * Connect to the remote unit. This method returns asynchronously.
     */
    public final void connect() {
        connect(new NullGenericWatchdog());
    }

    /**
     * Connect to the remote unit. This method returns asynchronously.
     *
     * @param watchdog
     *            the watchdog to use
     */
    public final void connect(final GenericWatchdog watchdog) { // CSIGNORE: HiddenField
        if (linkState != LinkStates.DISCONNECTED) {
            return; // Already connected
        }

        this.watchdog = watchdog;

        changeLinkState(LinkStates.CONNECTING);

        rxThread = new RxThread();
        new Thread(rxThread, this + ".RxThread").start();
        // RxThread manages the TxThread for us, so no need to start it here
    }

    /**
     * Disconnect from the remote unit. This immediately disconnects <b>without
     * </b> flushing the transmit queue. This method does not return until the
     * unit has been disconnected.
     */
    public final void disconnect() {
        if (linkState == LinkStates.DISCONNECTED) {
            return; // Not connected
        }

        if (rxThread != null) {
            rxThread.shutdownThread();
            rxThread = null;
        }
        // RxThread manages the TxThread for us, so no need to stop it here

        changeLinkState(LinkStates.DISCONNECTED);
    }

    /**
     * Register a link status handler. Link status handlers are informed of any
     * changes to the link state.
     *
     * @param handler
     *            The link status handler to register.
     */
    public final void registerLinkStatusHandler(
            final OCPLinkStateHandler handler) {
        synchronized (linkStateHandlers) {
            linkStateHandlers.add(handler);
        }
    }

    /**
     * Deregister a link status handler.
     *
     * @param handler
     *            The link status handler to deregister.
     */
    public final void deregisterLinkStatusHandler(
            final OCPLinkStateHandler handler) {
        synchronized (linkStateHandlers) {
            linkStateHandlers.remove(handler);
        }
    }

    /**
     * Deregister all link status handlers.
     */
    public final void deregisterAllLinkStatusHandlers() {
        synchronized (linkStateHandlers) {
            linkStateHandlers.clear();
        }
    }

    /**
     * Register a handler for a local task ID. This handler will receive all OCP
     * call control messages for the registered task ID. Only one handler can be
     * registered at a time for a given task ID - if the task ID is already in
     * use, the existing handler will be removed.
     * <p>
     * Each registered handler counts as an active call.
     *
     * @param handler
     *            The handler to register.
     * @param tid
     *            The OCP Task ID that the handler is interested in.
     * @see #deregisterTidHandler(int)
     */
    @Override
    public final void registerTidHandler(final OCPMessageHandler handler,
            final int tid) {
        synchronized (tidHandlers) {
            tidHandlers.put(tid, handler);
        }
    }

    /**
     * Deregister a handler for a local task ID.
     *
     * @param tid
     *            The task ID to remove the handler for.
     * @see #registerTidHandler(OCPMessageHandler, int)
     */
    @Override
    public final void deregisterTidHandler(final int tid) {
        synchronized (tidHandlers) {
            tidHandlers.remove(tid);
        }
    }

    /**
     * Register a handler for the management task ID. This handler will receive
     * all OCP call control messages for the task ID 0xFFFFFFFF. Only one
     * handler can be registered at a time for a given task ID - if the task ID
     * is already in use, the existing handler will be removed.
     * <p>
     * This handler does not count as an active call.
     *
     * @param handler
     *            The handler to register.
     * @see #deregisterManagementTidHandler()
     */
    public final void registerManagementTidHandler(
            final OCPMessageHandler handler)    {
        managementTidHandler = handler;
    }

    /**
     * Deregister a handler for the management task ID.
     *
     * @see #registerManagementTidHandler(OCPMessageHandler)
     */
    public final void deregisterManagementTidHandler() {
        managementTidHandler = null;
    }

    /**
     * Queue an OCP message for transmitting to the remote unit.
     *
     * @param message
     *            The OCP message to be processed by the implementation.
     * @param callingLink
     *            Not used.
     */
    @Override
    public final void queueMessage(final OCPMessage message,
            final OCPLink callingLink) {
        // no need to synchronise as BlockingQueues are thread-safe
        try {
            stats.incrementStat(statPrefix + STAT_QUEUE);
            txMessages.put((LegacyOCPMessage) message);
        } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
        }
    }

    /**
     * Gets the fixed heartbeating interval. This is the frequency that a
     * heartbeat will be sent regardless of other traffic on the link.
     *
     * @return The fixed heartbeating interval, in seconds.
     */
    public final int getFixedHeartbeatInterval() {
        return fixedHeartbeatInterval / MILLISECONDS;
    }

    /**
     * Sets the fixed heartbeating interval. This is the frequency that a
     * heartbeat will be sent regardless of other traffic on the link.
     *
     * @param newFfixedHeartbeatInterval
     *            The fixed heartbeating interval, in seconds.
     */
    public final void setFixedHeartbeatInterval(
            final int newFfixedHeartbeatInterval) {
        this.fixedHeartbeatInterval = newFfixedHeartbeatInterval * MILLISECONDS;
    }

    /**
     * Gets the fixed heartbeating interval. This is the frequency that a
     * heartbeat will be if there is no other traffic on the link.
     *
     * @return The inactive heartbeating interval, in seconds.
     */
    public final int getInactiveHeartbeatInterval() {
        return inactiveHeartbeatInterval / MILLISECONDS;
    }

    /**
     * Gets the fixed heartbeating interval. This is the frequency that a
     * heartbeat will be if there is no other traffic on the link.
     *
     * @param newInactiveHeartbeatInterval
     *            The inactive heartbeating interval, in seconds.
     */
    public final void setInactiveHeartbeatInterval(
            final int newInactiveHeartbeatInterval) {
        this.inactiveHeartbeatInterval = newInactiveHeartbeatInterval
                * MILLISECONDS;
    }

    /**
     * Gets the link timeout duration. This is the period of silence on the link
     * after which the link is deemed to have failed.
     *
     * @return the link timeout duration, in seconds.
     */
    public final int getTimeout() {
        return timeout / MILLISECONDS;
    }

    /**
     * Sets the link timeout duration. This is the period of silence on the link
     * after which the link is deemed to have failed.
     *
     * @param newTimeout
     *            the link timeout duration, in seconds.
     */
    public final void setTimeout(final int newTimeout) {
        this.timeout = newTimeout * MILLISECONDS;
    }

    /**
     * Get the current status of the link.
     *
     * @return The current status of the link
     */
    public final LinkStates getLinkState() {
        return linkState;
    }

    /**
     * Get the unit ID of the remote unit.
     *
     * @return The unit ID of the remote unit
     */
    public final int getUnitID() {
        return unitID;
    }

    /**
     * Get the enabled status of the remote unit. A disabled unit cannot handle
     * new calls, but may be able to process OCP messages for existing calls.
     *
     * @return True if the remote unit is enabled, false otherwise.
     */
    public final boolean isUnitEnabled() {
        return unitEnabled;
    }

    /**
     * Gets the address of the remote unit.
     *
     * @return the remote address
     */
    public final InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Gets the port number that will be connected to on the remote unit.
     *
     * @return the remote port
     */
    public final int getRemotePort() {
        return remotePort;
    }

    /**
     * Gets the port number that outgoing connections will be made from.
     *
     * @return the local port
     */
    public final int getLocalPort() {
        return localPort;
    }

    /**
     * Gets the name that will be reported to the remote unit.
     *
     * @return the unit name
     */
    public final String getUnitName() {
        return unitName;
    }

    /**
     * Gets the cluster ID reported by the remote unit.
     *
     * @return the cluster ID
     */
    public final int getClusterID() {
        return clusterID;
    }

    /**
     * Gets the number of calls in progress on this link. This is determined by
     * counting the number of registered task ID handlers.
     *
     * @return the number of calls in progress
     */
    public final int getNumCalls() {
        synchronized (tidHandlers) {
            return tidHandlers.size();
        }
    }

    /**
     * Gets the timestamp of the last activity on this link. Only incoming data
     * and socket events are considered for this - outgoing messages do not
     * update this timestamp.
     *
     * @return the timestamp of the last activity, as returned by
     *         {@link System#currentTimeMillis}
     */
    public final long lastActivity() {
        if (rxThread != null) {
            return rxThread.lastActivity;
        } else {
            return 0;
        }
    }

    @Override
    public final String toString() {
        return "OCPLinkManager(" + remoteAddress + ":" + remotePort + ")";
    }

    /**
     * Receive thread for the OCP link. This thread manages the connection and
     * decodes incoming OCP messages.
     *
     * @author Telsis
     */
    private class RxThread implements Runnable {
        /**
         * The current state of this thread.
         */
        private RxThreadStates currentState;
        /**
         * A flag to detect when this thread should be shut down.
         */
        private boolean        shutdownThread;
        /**
         * The thread that this object is running in.
         */
        private Thread         myThread;
        /**
         * The selector for the link.
         */
        private Selector       socketSelector;
        /**
         * The receive buffer.
         */
        private ByteBuffer     buffer;
        /**
         * The timestamp of the last activity.
         */
        private long           lastActivity;
        /**
         * Used by TxThread to signal this thread to go directly to the RESET
         * state (due to a connection failure).
         */
        private boolean        resetNeeded;

        /**
         * Create a new stopped RxThread.
         */
        public RxThread() {
            super();

            currentState = RxThreadStates.UNINITIALISED;
            shutdownThread = false;
            myThread = null;
            socketSelector = null;
            lastActivity = System.currentTimeMillis();
            resetNeeded = false;
        }

        @Override
        public void run() {
            log.debug("RxThread: thread starting");
            watchdog.start();
            myThread = Thread.currentThread();
            boolean moreToDo = false;
            lastActivity = System.currentTimeMillis();

            buffer = ByteBuffer.allocate(LegacyOCPMessage.OCP_MAX_LENGTH);
            buffer.order(ByteOrder.BIG_ENDIAN); // network order

            while (true) {
                watchdog.pat();
                if (resetNeeded) {
                    log.info("RxThread: socket shutdown detected");
                    currentState = RxThreadStates.RESET;
                }
                moreToDo = internalRun();

                if (!moreToDo) {
                    try {
                        socketSelector.select(1 * MILLISECONDS);
                        socketSelector.selectedKeys().clear();
                    } catch (IOException e) {
                        log.error("RxThread: exception while waiting on "
                                + "selector", e);
                        currentState = RxThreadStates.RESET;
                    }
                }

                if (System.currentTimeMillis() - lastActivity > timeout) {
                    // Timeout: reset state machine and try again
                    log.warn("RxThread: timeout detected while in state "
                            + currentState.name());
                    currentState = RxThreadStates.RESET;
                }

                if (shutdownThread) {
                    cleanup();
                    break;
                }
            }
            watchdog.stop();
            log.debug("RxThread: thread exiting");
        }

        /**
         * Shut down this thread.
         */
        public void shutdownThread() {
            log.debug("RxThread: thread shutdown requested");
            shutdownThread = true;
            if (myThread != null) {
                while (myThread.isAlive()) {
                    try {
                        myThread.join();
                    } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
                    }
                }
            }
        }

        /**
         * Internal state machine.
         *
         * @return true if there is more work to be done, otherwise false if the
         *         caller should sleep until activity is detected on the
         *         socketSelector.
         */
        private boolean internalRun() {
            log.debug("RxThread: entering internalRun() in state "
                    + currentState);

            switch (currentState) {
            case RESET:
                return handleReset();

            case UNCONNECTED:
                return handleUnconnected();

            case CONNECTING:
                return handleConnecting();

            case CONNECTED:
                return handleConnected();

            case NO_SYNC:
                return handleNoSync();

            case SECOND_EOM_BYTE:
                return handleSecondEOMByte();

            case MATCH_LENGTH:
                return handleMatchLength();

            case GET_MESSAGE_HEADER:
                return handleGetMessageHeader();

            case GET_MESSAGE_BODY:
                return handleGetMessageBody();

            case PROCESS_MESSAGE:
                return handleProcessMessage();

            default:
                currentState = RxThreadStates.RESET;
                return true;
            }
        }

        /**
         * Handle the RESET state. This disconnects any current connection and
         * returns the thread to a known internal state, then enters the
         * UNCONNECTED state.
         *
         * @return true
         */
        private boolean handleReset() {
            lastActivity = System.currentTimeMillis();

            cleanup();
            buffer.clear();

            currentState = RxThreadStates.UNCONNECTED;
            return true;
        }

        /**
         * Handle the UNCONNECTED state. This creates an outgoing TCP/IP
         * connection.
         *
         * @return true if the socket is being connected, false if the
         *         connection attempt failed
         */
        private boolean handleUnconnected() {
            lastActivity = System.currentTimeMillis();

            try {
                channel = SocketChannel.open();
                socket = channel.socket();
                socket.bind(new InetSocketAddress(localAddress, localPort));
                socket.setSoTimeout(timeout);
                channel.configureBlocking(false);
                while (socketSelector == null) {
                    // Workaround for Java bug 6427854: "(se)
                    // NullPointerException in Selector.open()" (fixed in
                    // 7 b08).
                    // http://bugs.sun.com/view_bug.do?bug_id=6427854
                    try {
                        socketSelector = Selector.open();
                    } catch (NullPointerException e) {
                        log.debug("RxThread: Java bug 6427854 seen");
                    }
                }
                channel.register(socketSelector, SelectionKey.OP_CONNECT);

                if (channel.connect(new InetSocketAddress(remoteAddress,
                        remotePort))) {
                    currentState = RxThreadStates.CONNECTED;
                } else {
                    // Connection attempt blocked, wait for it to complete.
                    currentState = RxThreadStates.CONNECTING;
                }
                changeLinkState(LinkStates.CONNECTING);
            } catch (IOException e) {
                // Connection failure, try again.
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.debug("RxThread: exception caught while trying to "
                        + "connect to server", e);
                currentState = RxThreadStates.RESET;
                // Delay the retry to avoid spamming the other server with
                // connection attempts
                return false;
            }
            return true;
        }

        /**
         * Handle the CONNECTING state. This polls the socket until the
         * connection is established.
         *
         * @return true if the socket is connected, false if the connection
         *         attempt failed or has not yet finished
         */
        private boolean handleConnecting() {
            try {
                if (channel.finishConnect()) {
                    currentState = RxThreadStates.CONNECTED;
                } else {
                    // Not yet connected
                    return false;
                }
            } catch (IOException e) {
                // Connection failure, try again.
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.debug("RxThread: exception caught while trying to "
                        + "connect to server", e);
                currentState = RxThreadStates.RESET;
                // Delay the retry to avoid spamming the other server with
                // connection attempts
                return false;
            }
            return true;
        }

        /**
         * Handle the CONNECTED state. This prepares the connected socket for
         * reading and writing, and starts the transmit thread.
         *
         * @return true
         */
        private boolean handleConnected() {
            lastActivity = System.currentTimeMillis();

            try {
                // Change our selector to watch for reads
                socketSelector.close();
                socketSelector = Selector.open();
                channel.register(socketSelector, SelectionKey.OP_READ);
            } catch (IOException e) {
                // Couldn't change the selector. Kill the connection and try
                // again
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.debug("RxThread: exception caught while trying to "
                        + "connect to server", e);
                currentState = RxThreadStates.RESET;
                return true;
            }

            // Start up TX thread
            txThread = new TxThread();
            new Thread(txThread, OCPLinkManager.this + ".TxThread").start();

            stats.incrementStat(statPrefix + STAT_CONNECTION_SUCCESS);

            currentState = RxThreadStates.NO_SYNC;
            return true;
        }

        /**
         * Handle the NO_SYNC state. This reads incoming data a byte at a time
         * until the first end-of-message byte is received.
         *
         * @return false if the socket's receive buffer is empty, true otherwise
         */
        private boolean handleNoSync() {
            try {
                if (readByte() == 0) {
                    return false;
                }
            } catch (IOException e) {
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.info("RxThread: exception caught while trying to read "
                        + "from server", e);
                currentState = RxThreadStates.RESET;
                return true;
            }

            if (buffer.position() > 0 && buffer.get(buffer.position() - 1)
                    == LegacyOCPMessage.OCP_EOM_FIRST_BYTE
                    && buffer.hasRemaining()) {
                // Found the first EOM byte, and have enough space for the
                // second EOM byte.
                currentState = RxThreadStates.SECOND_EOM_BYTE;
            }
            return true;
        }

        /**
         * Handle the SECOND_EOM_BYTE state. This reads the next byte from the
         * socket and checks to see if it is the second end-of-message byte. If
         * it is not, then it returns to the NO_SYNC state to wait for another
         * possible end-of-message marker.
         *
         * @return false if the socket's receive buffer is empty, true otherwise
         */
        private boolean handleSecondEOMByte() {
            try {
                if (readByte() == 0) {
                    return false;
                }
            } catch (IOException e) {
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.info("RxThread: exception caught while trying to read "
                        + "from server", e);
                currentState = RxThreadStates.RESET;
                return true;
            }

            if (buffer.get(buffer.position() - 1)
                    == LegacyOCPMessage.OCP_EOM_SECOND_BYTE) {
                // Found the second EOM byte.
                currentState = RxThreadStates.MATCH_LENGTH;
            }
            return true;
        }

        /**
         * Handle the MATCH_LENGTH state. This searches from the start of the
         * buffer until a value that matches the expected message length is
         * found. If the end of the buffer is reached, then it returns to the
         * NO_SYNC state to wait for another possible end-of-message marker.
         *
         * @return true
         */
        private boolean handleMatchLength() {
            // Work out expected length
            int len = buffer.position();
            len = len - LegacyOCPMessage.OCP_CMD_CODE_LENGTH;
            len = len - LegacyOCPMessage.OCP_LEN_LENGTH;

            for (int i = LegacyOCPMessage.OCP_CMD_CODE_LENGTH; i <= buffer
                    .position()
                    - LegacyOCPMessage.OCP_LEN_LENGTH
                    - LegacyOCPMessage.OCP_TID_LENGTH
                    * 2 - LegacyOCPMessage.OCP_EOM_LENGTH; ++i, --len) {
                if (buffer.getShort(i) == len) {
                    // Found an OCP message!
                    // Shift the message down to the start of the buffer and set
                    // it up for reading.
                    buffer.limit(buffer.position());
                    buffer.position(i - (LegacyOCPMessage.OCP_CMD_CODE_LENGTH));
                    buffer.compact();
                    buffer.limit(len + LegacyOCPMessage.OCP_CMD_CODE_LENGTH
                            + LegacyOCPMessage.OCP_LEN_LENGTH);
                    currentState = RxThreadStates.PROCESS_MESSAGE;
                    return true;
                }
            }

            // If we get here then the length didn't match anything. Keep
            // the buffer so far and look for the next terminator.
            currentState = RxThreadStates.NO_SYNC;
            return true;
        }

        /**
         * Handle the GET_MESSAGE_HEADER state. This reads data until enough for
         * a message header is reached, then extracts the message length. If the
         * length is larger than would fit in the buffer then we assume that
         * synchronisation has been lost.
         *
         * @return false if the entire header is not yet available, true
         *         otherwise
         */
        private boolean handleGetMessageHeader() {
            final int expectedBytes;
            final int bytesRead;
            try {
                expectedBytes = buffer.remaining();
                bytesRead = channel.read(buffer);
                if (bytesRead == -1) { // EOF
                    currentState = RxThreadStates.RESET;
                    return true;
                } else if (bytesRead < expectedBytes) {
                    // Didn't read the entire header, so wait for more data
                    return false;
                }
            } catch (IOException e) {
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.info("RxThread: exception caught while reading data",
                        e);
                currentState = RxThreadStates.RESET;
                return true;
            }

            int length = buffer.getShort(LegacyOCPMessage.OCP_CMD_CODE_LENGTH);

            // Check if the remaining buffer is large enough for this
            buffer.limit(buffer.capacity());
            if (buffer.remaining() < length) {
                // The buffer isn't large enough to take this message.
                // Assume that we've lost sync.
                currentState = RxThreadStates.NO_SYNC;
                return true;
            }

            // Set the write limit to the message length
            buffer.limit(length + LegacyOCPMessage.OCP_HEADER_LENGTH);
            currentState = RxThreadStates.GET_MESSAGE_BODY;
            return true;
        }

        /**
         * Handle the GET_MESSAGE_BODY state. This reads data until the entire
         * expected message has been received, then validates the end-of-message
         * marker. If the marker is invalid then it enters the NO_SYNC state.
         *
         * @return false if the entire message is not yet available, true
         *         otherwise
         */
        private boolean handleGetMessageBody() {
            final int expectedBytes;
            final int bytesRead;
            try {
                expectedBytes = buffer.remaining();
                bytesRead = channel.read(buffer);
                if (bytesRead == -1) { // EOF
                    currentState = RxThreadStates.RESET;
                    return true;
                } else if (bytesRead < expectedBytes) {
                    // Didn't read the entire header, so wait for more data
                    return false;
                }
            } catch (IOException e) {
                stats.incrementStat(statPrefix + STAT_CONNECTION_FAIL);
                log.info("RxThread: exception caught while reading data",
                        e);
                currentState = RxThreadStates.RESET;
                return false;
            }

            // Check EOM bytes
            if (buffer.get(buffer.position() - 2)
                    == LegacyOCPMessage.OCP_EOM_FIRST_BYTE
                    && buffer.get(buffer.position() - 1)
                    == LegacyOCPMessage.OCP_EOM_SECOND_BYTE) {
                // EOM matched! Reset the buffer for reading.
                buffer.flip();
                currentState = RxThreadStates.PROCESS_MESSAGE;
            } else {
                // EOM didn't match. Reset the buffer to the full capacity and
                // try to regain sync
                buffer.limit(buffer.capacity());
                currentState = RxThreadStates.NO_SYNC;
            }
            return true;
        }

        /**
         * Handle the PROCESS_MESSAGE state. This attempts to decode the message
         * and despatch it to the appropriate handler. If an invalid or
         * unrecognised message is received then a
         * {@link com.telsis.jocp.messages.CallCommandUnsupported} or
         * {@link LinkCommandUnsupported} reply is sent as appropriate.
         *
         * @return true
         */
        private boolean handleProcessMessage() {
            LegacyOCPMessage message;

            lastActivity = System.currentTimeMillis();

            try {
                message = LegacyOCPMessage.decodeBuffer(buffer);
                stats.incrementStat(statPrefix + STAT_MESSAGE_RX_BASE
                        + message.getClass().getSimpleName());
                despatchMessage(message);
            } catch (MessageException e) {
                stats.incrementStat(statPrefix + STAT_BAD_MESSAGE);
                queueMessage(e.getErrorMessage(), null);
            } catch (OCPException e) {
                stats.incrementStat(statPrefix + STAT_BAD_MESSAGE);
                log.debug("RxThread: unexpected exception when decoding "
                        + "message", e);
            }

            // Empty the buffer and set the write limit to the header length.
            buffer.clear();
            buffer.limit(LegacyOCPMessage.OCP_HEADER_LENGTH);
            currentState = RxThreadStates.GET_MESSAGE_HEADER;
            return true;
        }

        /**
         * Stop the transmit thread and close all sockets.
         */
        private void cleanup() {
            // Stop TX thread if it's running
            if (txThread != null) {
                txThread.shutdownThread();
                txThread = null;
                resetNeeded = false;
            }

            // Close down socket
            if (socketSelector != null) {
                try {
                    socketSelector.close();
                } catch (IOException e) {
                    // Treat this as a success, as we're trying to close the
                    // socket
                    log.debug("RxThread: exception caught while closing "
                            + "selector", e);
                }
                socketSelector = null;
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // Treat this as a success, as we're trying to close the
                    // socket
                    log.debug("RxThread: exception caught while closing "
                            + "channel", e);
                }
                channel = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Treat this as a success, as we're trying to close the
                    // socket
                    log.debug("RxThread: exception caught while closing "
                            + "socket", e);
                }
            }
        }

        /**
         * Read a single byte.
         *
         * @return The number of bytes read (either 0 or 1).
         * @throws IOException
         *             if an I/O error occurs.
         */
        private int readByte() throws IOException {
            ByteBuffer singleByte = ByteBuffer.allocate(1);
            if (channel.read(singleByte) == 0) {
                return 0;
            } else {
                if (!buffer.hasRemaining()) {
                    // Buffer overflow. This method is only used when trying to
                    // resync, so empty the buffer and try again.
                    log.debug("RxThread: readByte: buffer overflow");
                    buffer.clear();
                }
                singleByte.flip();
                buffer.put(singleByte);
                return 1;
            }
        }
    }

    /**
     * Transmit thread for the OCP link. This thread encodes and transmits OCP
     * messages.
     *
     * @author Telsis
     */
    private class TxThread implements Runnable {
        /**
         * The current state of the thread.
         */
        private TxThreadStates currentState;
        /**
         * A flag to detect when this thread should be shut down.
         */
        private boolean        shutdownThread;
        /**
         * The thread that this object is running in.
         */
        private Thread         myThread;
        /**
         * The write selector for the socket.
         */
        private Selector       socketSelector;
        /**
         * The transmit buffer.
         */
        private ByteBuffer     txBuffer;
        /**
         * A flag to detect when a heartbeat should be sent.
         */
        private boolean        needHeartbeat;
        /**
         * The timestamp of the last activity.
         */
        private long           lastActivity;
        /**
         * The timestamp of the last heartbeat transmitted.
         */
        private long           lastHeartbeat;

        /**
         * Creates a new stopped TxThread.
         */
        public TxThread() {
            super();

            shutdownThread = false;
            myThread = null;
        }

        @Override
        public void run() {
            log.debug("TxThread: thread starting");
            watchdog.start();
            myThread = Thread.currentThread();
            boolean moreToDo = false;

            lastActivity = System.currentTimeMillis();
            lastHeartbeat = lastActivity;

            try {
                socketSelector = Selector.open();
                channel.register(socketSelector, SelectionKey.OP_WRITE);
                currentState = TxThreadStates.UNINITIALISED;

                while (true) {
                    watchdog.pat();
                    moreToDo = internalRun();

                    if (shutdownThread) {
                        break;
                    }

                    long now = System.currentTimeMillis();
                    if (now - lastActivity > inactiveHeartbeatInterval
                            || now - lastHeartbeat > fixedHeartbeatInterval) {
                        needHeartbeat = true;
                        log.debug("TxThread: requested heartbeat");
                    }

                    if (!moreToDo) {
                        socketSelector.select(1 * MILLISECONDS);
                        socketSelector.selectedKeys().clear();
                    }
                }
            } catch (IOException e) {
                log.error("TxThread: exception in thread", e);
            } finally {
                try {
                    socketSelector.close();
                } catch (IOException e) { // CSIGNORE: EmptyBlock
                    // The thread is exiting anyway, so no need to do anything
                }
            }
            watchdog.stop();
            log.debug("TxThread: thread exiting");
        }

        /**
         * Shut down this thread.
         */
        public void shutdownThread() {
            shutdownThread = true;
            if (myThread != null) {
                while (myThread.isAlive()) {
                    try {
                        myThread.join();
                    } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
                    }
                }
            }
        }

        /**
         * Internal state machine.
         *
         * @return true if there is more work to do, otherwise false if the
         *         caller should sleep on the socketSelector.
         */
        private boolean internalRun() {
            log.debug("TxThread: entering internalRun() in state "
                    + currentState);

            switch (currentState) {
            case SENDING_MESSAGE:
                // Transmit the current message
                lastActivity = System.currentTimeMillis();
                try {
                    channel.write(txBuffer);
                } catch (IOException e) {
                    // The most likely cause is connection failure, so shutdown
                    // this thread and tell RxThread to reconnect.
                    log.debug("TxThread: exception caught while sending "
                            + "message", e);
                    rxThread.resetNeeded = true;
                    shutdownThread = true;
                    return false;
                }

                if (txBuffer.hasRemaining()) {
                    // The socket TX buffer is full, so wait for it to become
                    // ready
                    return false;
                } else {
                    // We've sent the message, so go and get the next one
                    txBuffer = null;
                    currentState = TxThreadStates.GET_NEXT_MESSAGE;
                    return true;
                }

            case GET_NEXT_MESSAGE:
                // Read the next message from the transmit queue
                if (needHeartbeat) {
                    // Heartbeats get priority
                    currentState = TxThreadStates.GENERATE_HEARTBEAT;
                    return true;
                }

                LegacyOCPMessage message = null;
                try {
                    message = txMessages.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
                }

                if (message == null) {
                    // Nothing available. Don't sleep as we've already done so
                    return true;
                }
                stats.decrementStat(statPrefix + STAT_QUEUE);

                stats.incrementStat(statPrefix + STAT_MESSAGE_TX_BASE
                        + message.getClass().getSimpleName());

                log.debug("Transmitting " + message.getClass().getName() + ": "
                        + message);
                txBuffer = LegacyOCPMessage.encodeMessage(message);
                currentState = TxThreadStates.SENDING_MESSAGE;
                return true;

            case GENERATE_HEARTBEAT:
                // Preempt the transmit queue with a heartbeat message
                lastHeartbeat = System.currentTimeMillis();
                lastActivity = lastHeartbeat;
                needHeartbeat = false;
                stats.incrementStat(statPrefix + STAT_OUTGOING_HEARTBEAT);
                txBuffer = LegacyOCPMessage.encodeMessage(new Heartbeat());
                currentState = TxThreadStates.SENDING_MESSAGE;
                return true;

            default:
                txBuffer = null;
                currentState = TxThreadStates.GET_NEXT_MESSAGE;
                return true;

            }
        }
    }

    /**
     * Implement this interface and register with
     * {@link OCPLinkManager#registerLinkStatusHandler} to be notified of any
     * changes to the link state.
     *
     * @author Telsis
     */
    public interface OCPLinkStateHandler {
        /**
         * This method is called whenever the state of the link changes.
         *
         * @param newState
         *            The new state of the link
         */
        void linkStateChanged(OCPLinkManager.LinkStates newState);

        /**
         * This method is called whenever the enabled state of the connected
         * unit changes. A disabled unit cannot handle new calls, but may be
         * able to continue to process existing calls.
         *
         * @param enabled
         *            The new enabled state of the unit
         */
        void unitEnabledChanged(boolean enabled);

        /**
         * This method is called whenever an OCP Preferred Unit message is
         * received from the connected unit.
         *
         * @param preferredSCP
         *            The address of the preferred SCP
         * @param secondarySCP
         *            The address of the secondary SCP
         */
        void receivedPreferredUnit(InetAddress preferredSCP,
                InetAddress secondarySCP);

        /**
         * This method is called whenever an OCP Call Gap message is received
         * from the connected unit. When call gapping is enabled, new calls
         * should not be sent to this link, however existing calls will still be
         * handled.
         *
         * @param duration
         *            The duration in seconds for which call gapping is active.
         *            If this value is 0, then call gapping is disabled and new
         *            calls can be sent to this link. If this value is -1, then
         *            call gapping is active with no timeout.
         */
        void receivedCallGap(short duration);
    }

}
