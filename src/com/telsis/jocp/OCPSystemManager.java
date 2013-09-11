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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.telsis.jocp.OCPLinkManager.LinkStates;
import com.telsis.jocp.OCPLinkManager.OCPLinkStateHandler;
import com.telsis.jocp.messages.BecomeMaster;
import com.telsis.jocp.messages.CallGap;
import com.telsis.jutils.UtilitiesFactory;
import com.telsis.jutils.enums.ActiveStates;
import com.telsis.jutils.enums.CongestionType;
import com.telsis.jutils.interfaces.AlarmInterface;
import com.telsis.jutils.interfaces.ServerEventNotifier;
import com.telsis.jutils.watchdog.GenericWatchdog;
import com.telsis.jutils.watchdog.NullGenericWatchdog;

/**
 * This class manages a set of OCP links.
 * <p/>
 * The following properties are used:
 * <table>
 * <tr>
 * <th>Setting</th>
 * <th>Meaning</th>
 * <th>Type/units</th>
 * <th>Default</th>
 * <th>Range</th>
 * </tr>
 * <tr>
 * <td>ocpSystemNumLinks</td>
 * <td>The number of configured OCP links.</td>
 * <td>Number</td>
 * <td>0</td>
 * <td>0-2</td>
 * </tr>
 * <tr>
 * <td>ocpSystemMasterSlaveSwapTimeout</td>
 * <td>The delay before we force a Master / Slave swap.</td>
 * <td>Number (seconds)</td>
 * <td>30</td>
 * <td>1-43200</td>
 * </tr>
 * <tr>
 * <td>ocpSystemSuspectTimeout</td>
 * <td>If no data is received from the remote unit for this period, then the
 * link is considered to be suspect and will have a lower priority for
 * load-sharing.</td>
 * <td>Number (seconds)</td>
 * <td>3</td>
 * <td>1-43200</td>
 * </tr>
 * <tr>
 * <td>ocpSystemLoggingLevel</td>
 * <td>The level of detail that is to be included in logs.</td>
 * <td>Log4j constant</td>
 * <td>WARN</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 *
 * @see OCPLinkManager
 * @author Telsis
 */
public class OCPSystemManager implements ServerEventNotifier, OCPClient {
    /**
     * This enum contains the possible states that an {@link OCPSystemManager}
     * object can be in.
     *
     * @author Telsis
     */
    public enum SystemState {
        /**
         * The OCP Link Managers are not running.
         */
        STOPPED,
        /**
         * The OCP Link Managers are running, but none of the link managers are
         * currently connected.
         */
        CONNECTING,
        /**
         * The OCP Link Managers are running, but the states reported by the
         * links are not consistent. This could occur with the following
         * situations:
         * <ul>
         * <li>The links are connected to a set of servers running in
         * master/slave mode, and more than one server claims to be master.</li>
         * <li>The links are connected to a set of servers of which some claim
         * to be running in master/slave mode and others claim to be
         * loadsharing.</li>
         * <li>The links are connected to a set of servers running in
         * loadsharing mode with different cluster IDs.</li>
         * </ul>
         * The OCP System Manager will automatically recover once the
         * inconsistency has been resolved.
         */
        INCONSISTENT,
        /**
         * The OCP Link Managers are running and are connected to a set of
         * servers running in master/slave mode, however none of the connected
         * servers claim to be master. If this state persists then the OCP
         * System Manager will automatically promote a link to master.
         */
        MISSING_MASTER,
        /**
         * The OCP Link Managers are running and are connected to a set of
         * servers running in master/slave mode.
         */
        MASTER_SLAVE,
        /**
         * The OCP Link Managers are running and are connected to a set of
         * servers running in loadsharing mode.
         */
        LOADSHARING,
        /**
         * The OCP System Manager is running, but no links are configured.
         */
        NO_LINKS
    };

    // Internal constants
    /** Conversion between seconds and milliseconds. */
    private static final int    MILLISECONDS                 = 1000;

    // Configuration defaults
    /** The number of links. */
    private static final String CONFIG_LINKS_COUNT
            = "ocpSystemNumLinks";
    /** The default value for the number of links. */
    private static final String CONFIG_LINKS_COUNT_DEF       = "0";
    /** The minimum value for the number of links. */
    private static final int    CONFIG_LINKS_COUNT_MIN       = 0;
    /** The maximum value for number of links. */
    private static final int    CONFIG_LINKS_COUNT_MAX       = 2;
    /** The master/slave swap timeout. */
    private static final String CONFIG_MASTER_SLAVE_SWAP
            = "ocpSystemMasterSlaveSwapTimeout";
    /** The default value for the master/slave swap timeout. */
    private static final String CONFIG_MASTER_SLAVE_SWAP_DEF = "30";
    /** The minimum value for the master/slave swap timeout. */
    private static final int    CONFIG_MASTER_SLAVE_SWAP_MIN = 1;
    /** The maximum value for master/slave swap timeout. */
    private static final int    CONFIG_MASTER_SLAVE_SWAP_MAX = 43200;
    /** The suspect link timeout. */
    private static final String CONFIG_SUSPECT_TIMEOUT
            = "ocpSystemSuspectTimeout";
    /** The default value for the suspect link timeout. */
    private static final String CONFIG_SUSPECT_TIMEOUT_DEF   = "3";
    /** The minimum value for the suspect link timeout. */
    private static final int    CONFIG_SUSPECT_TIMEOUT_MIN   = 1;
    /** The maximum value for suspect link timeout. */
    private static final int    CONFIG_SUSPECT_TIMEOUT_MAX   = 3600;
    /** The logging level. */
    private static final String CONFIG_LOGGING_LEVEL
            = "ocpSystemLoggingLevel";
    /** The default value for the logging level. */
    private static final String CONFIG_LOGGING_LEVEL_DEF     = "WARN";
    /** Base alarm code for OCP client alarms. */
    private static final int    ALARM_ID_BASE = 200;
    /** The name of alarm "Configured OCP link not available". */
    private static final String ALARM_LINK_NOT_AVAILABLE = "OCPLinkNotAvailable";
    /** Alarm ID for "Configured OCP link not available". */
    private static final int    ALARM_LINK_NOT_AVAILABLE_ID = ALARM_ID_BASE + 0;
    /** Alarm severity for "Configured OCP link not available". */
    private static final int    ALARM_LINK_NOT_AVAILABLE_SEV = 1;
    /** The name of alarm "OCP Module cannot take calls". */
    private static final String ALARM_CANNOT_TAKE_CALLS = "OCPModuleCannotTakeCalls";
    /** Alarm ID for "OCP Module cannot take calls". */
    private static final int    ALARM_CANNOT_TAKE_CALLS_ID = ALARM_ID_BASE + 1;
    /** Alarm severity for "OCP Module cannot take calls". */
    private static final int    ALARM_CANNOT_TAKE_CALLS_SEV = 3;

    // Configuration
    /** The properties object. */
    private Properties prop;
    /** The master/slave swap timeout in milliseconds. */
    private int        masterSlaveSwapTimeout;
    /** The link suspect timeout in milliseconds. */
    private int        linkSuspectTimeout;

    /** An array of the links. */
    private CopyOnWriteArrayList<Link> links;
    /** The system state. */
    private SystemState     state = SystemState.STOPPED;
    /** The preferred link. */
    private Link            preferredLink;
    /** The system thread. */
    private SystemThread    sysThread;
    /** The timestamp of when the MISSING_MASTER state was entered. */
    private long            missingMasterStart;
    /** True if the gapping alarm is currently active. */
    private boolean         gappingAlarmActive = false;
    /** The watchdog to use. */
    private GenericWatchdog watchdog;

    /** The logger for this class. */
    private static Logger log = Logger.getLogger("ocpSystemManager");

    /** The alarms interface. */
    private static AlarmInterface alarms = UtilitiesFactory
            .getUtilInterface().getAlarmInterface();

    static {
        alarms.registerAlarm(ALARM_LINK_NOT_AVAILABLE, Link.class,
                ALARM_LINK_NOT_AVAILABLE_ID, ALARM_LINK_NOT_AVAILABLE_SEV);
        alarms.registerAlarm(ALARM_CANNOT_TAKE_CALLS, OCPSystemManager.class,
                ALARM_CANNOT_TAKE_CALLS_ID, ALARM_CANNOT_TAKE_CALLS_SEV);
    }

    /**
     * Creates a disconnected OCP System Manager using the properties specified
     * in <tt>jOCP.properties</tt>.
     */
    public OCPSystemManager() {
        FileInputStream in = null;
        prop = new Properties();

        try {
            in = new FileInputStream("jOCP.properties");
            prop.load(in);
        } catch (FileNotFoundException e) {
            log.warn("Could not find the properties file \"jOCP.properties\". "
                    + "Falling back to defaults.", e);
        } catch (IOException e) {
            log.error("Error loading properties file \"jOCP.properties\". "
                    + "Falling back to defaults.", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) { // CSIGNORE: EmptyBlock
            }
        }
        links = new CopyOnWriteArrayList<Link>();
        UtilitiesFactory.getUtilInterface().register(this, true);
        sysThread = new SystemThread();
        init();
    }

    /**
     * Creates a disconnected OCP System Manager using the specified Properties
     * object.
     *
     * @param properties
     *            The Properties object to use
     */
    public OCPSystemManager(final Properties properties) {
        this.prop = properties;
        links = new CopyOnWriteArrayList<Link>();
        UtilitiesFactory.getUtilInterface().register(this, true);
        sysThread = new SystemThread();
        init();
    }

    /**
     * Carry out the actual initialisation work. This reads in the system-level
     * configuration, creates all configured links, and starts the system
     * management thread.
     */
    private synchronized void init() {
        int numLinks;

        numLinks = Integer.parseInt(prop.getProperty(CONFIG_LINKS_COUNT,
                CONFIG_LINKS_COUNT_DEF));
        if (numLinks < CONFIG_LINKS_COUNT_MIN
                || numLinks > CONFIG_LINKS_COUNT_MAX) {
            throw new IllegalArgumentException(
                    "The number of links specified is outside the valid range "
                    + "of " + CONFIG_LINKS_COUNT_MIN + " to "
                    + CONFIG_LINKS_COUNT_MAX);
        }

        masterSlaveSwapTimeout = Integer.parseInt(prop.getProperty(
                CONFIG_MASTER_SLAVE_SWAP, CONFIG_MASTER_SLAVE_SWAP_DEF));
        if (masterSlaveSwapTimeout < CONFIG_MASTER_SLAVE_SWAP_MIN
                || masterSlaveSwapTimeout > CONFIG_MASTER_SLAVE_SWAP_MAX) {
            throw new IllegalArgumentException(
                    "The master slave swap timeout is outside the valid range "
                    + "of " + CONFIG_MASTER_SLAVE_SWAP_MIN + " to "
                    + CONFIG_MASTER_SLAVE_SWAP_MAX);
        }
        masterSlaveSwapTimeout *= MILLISECONDS;

        linkSuspectTimeout = Integer.parseInt(prop.getProperty(
                CONFIG_SUSPECT_TIMEOUT, CONFIG_SUSPECT_TIMEOUT_DEF));
        if (linkSuspectTimeout < CONFIG_SUSPECT_TIMEOUT_MIN
                || linkSuspectTimeout > CONFIG_SUSPECT_TIMEOUT_MAX) {
            throw new IllegalArgumentException(
                    "The suspect link timeout is outside the valid range of "
                    + CONFIG_SUSPECT_TIMEOUT_MIN + " to "
                    + CONFIG_SUSPECT_TIMEOUT_MAX);
        }
        linkSuspectTimeout *= MILLISECONDS;

        log.setLevel(Level.toLevel(prop.getProperty(
                CONFIG_LOGGING_LEVEL,
                CONFIG_LOGGING_LEVEL_DEF)));

        // If the new configuration has fewer links, remove the old ones
        for (int i = links.size() - 1; i >= numLinks; i--) {
            Link link = links.remove(i);
            link.cleanup();
        }

        // Update any links present in both old and new configuration
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            if (link.reloadConfiguration(prop)) {
                // Significant configuration change (i.e. IP address or
                // similar), so drop and recreate link
                link.cleanup();

                link = new Link(prop, i);
                if (state != SystemState.STOPPED) {
                    link.linkManager.connect(watchdog);
                }
                links.set(i, link);
            }
        }

        // If the new configuration has more links, add the new ones
        for (int i = links.size(); i < numLinks; i++) {
            Link link = new Link(prop, i);
            if (state != SystemState.STOPPED) {
                link.linkManager.connect(watchdog);
            }
            links.add(link);
        }

        state = recalcMode();
    }

    /**
     * Clean up the OCP System Manager. This method disconnects and removes all
     * links and deregisters all alarms. After calling this method
     * {@link #init()} must be called before the object can be used again.
     */
    private void cleanup() {
        disconnect();

        for (Link link : links) {
            link.cleanup();
        }
        links.clear();
        alarms.clearAllAlarms(this);
    }

    @Override
    protected final void finalize() throws Throwable {
        cleanup();
    }

    @Override
    public final synchronized void connect() {
        connect(new NullGenericWatchdog());
    }

    @Override
    // CSOFF: HiddenField
    public final synchronized void connect(final GenericWatchdog watchdog) {
        // CSON: HiddenField
        if (state != SystemState.STOPPED) {
            return; // Already connected
        }

        this.watchdog = watchdog;
        new Thread(sysThread, "OCPSystemManager.SystemThread").start();
    }

    /**
     * Disconnect from the remote units. This method does not return until all
     * units have been disconnected.
     */
    @Override
    public final synchronized void disconnect() {
        if (state == SystemState.STOPPED) {
            return; // Already stopped
        }

        sysThread.shutdownThread();
        state = recalcMode();
    }

    /**
     * Reload the properties from the default file jOCP.properties. This will
     * result in all OCP links being temporarily lost.
     */
    public final void reloadProperties() {
        FileInputStream in = null;
        Properties myProp = new Properties();

        try {
            in = new FileInputStream("jOCP.properties");
            myProp.load(in);
        } catch (FileNotFoundException e) {
            log.warn("Could not find the properties file \"jOCP.properties\". "
                    + "Falling back to defaults.", e);
        } catch (IOException e) {
            log.error("Error reloading properties file \"jOCP.properties\". "
                    + "Falling back to defaults.", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) { // CSIGNORE: EmptyBlock
            }
        }

        reloadProperties(myProp);
    }

    /**
     * Reload the properties using the specified Properties object. This will
     * result in all OCP links being temporarily lost.
     *
     * @param properties
     *            The Properties object to use
     */
    @Override
    public final void reloadProperties(final Properties properties) {

        this.prop = properties;
        init();

    }

    /**
     * Recalculate the system state, based on the state of each connected link.
     * The system state is calculated as follows:
     * <ul>
     * <li>If the OCP System Manager is not running, then the system state is
     * set to STOPPED.</li>
     * <li>If all links are inactive (i.e. in the CONNECTING or DISCONNECTED
     * states), then the system state is set to CONNECTING.</li>
     * <li>If all active links are in the SLAVE state, then the system state is
     * set to MISSING_MASTER.</li>
     * <li>If exactly one link is in the MASTER state and all other active links
     * are in the SLAVE state, then the system state is set to MASTER_SLAVE.
     * </li>
     * <li>If all active links are in the LOADSHARE state, then the system state
     * is set to LOADSHARING.</li>
     * <li>If the links are in any other combination, then the system state is
     * set to INCONSISTENT.</li>
     * </ul>
     * Additionally, if the system state is one of STOPPED, CONNECTING or
     * INCONSISTENT then an alarm will be raised signalling that the OCP module
     * cannot take calls. An alarm will also be raised for each link that is in
     * the CONNECTING or DISCONNECTED state.
     *
     * @return the new state of the system
     */
    private SystemState recalcMode() {
        SystemState newState;
        Link master = null;

        if (sysThread.myThread == null) {
            // The SystemThread is not running, so we must be STOPPED.
            newState = SystemState.STOPPED;
        } else {
            // The SystemThread is running, so we can't be STOPPED.
            newState = SystemState.CONNECTING;
            if (links.isEmpty()) {
                newState = SystemState.NO_LINKS;
            }
            for (Link link : links) {
                switch (link.linkState) {
                case CONNECTING:
                case DISCONNECTED:
                    link.raiseLinkAlarm();
                    break;

                case LOADSHARE:
                    alarms.clearAlarm(ALARM_LINK_NOT_AVAILABLE, link);
                    switch (newState) {
                    case CONNECTING:
                        newState = SystemState.LOADSHARING;
                        break;

                    case MASTER_SLAVE:
                    case MISSING_MASTER:
                        newState = SystemState.INCONSISTENT;
                        break;

                    case LOADSHARING:
                    case INCONSISTENT:
                        break;

                    default:
                        log.debug("Unexpected newState " + newState + " in "
                                + link + " state " + link.linkState);
                        break;
                    }
                    break;

                case MASTER:
                    alarms.clearAlarm(ALARM_LINK_NOT_AVAILABLE, link);
                    switch (newState) {
                    case CONNECTING:
                    case MISSING_MASTER:
                        newState = SystemState.MASTER_SLAVE;
                        master = link;
                        break;

                    case MASTER_SLAVE:
                    case LOADSHARING:
                        newState = SystemState.INCONSISTENT;
                        break;

                    case INCONSISTENT:
                        break;

                    default:
                        log.debug("Unexpected newState " + newState + " in "
                                + link + " state " + link.linkState);
                        break;
                    }
                    break;

                case SLAVE:
                    alarms.clearAlarm(ALARM_LINK_NOT_AVAILABLE, link);
                    switch (newState) {
                    case CONNECTING:
                        newState = SystemState.MISSING_MASTER;
                        break;

                    case LOADSHARING:
                        newState = SystemState.INCONSISTENT;
                        break;

                    case MASTER_SLAVE:
                    case MISSING_MASTER:
                    case INCONSISTENT:
                        break;

                    default:
                        log.debug("Unexpected newState " + newState + " in "
                                + link + " state " + link.linkState);
                        break;
                    }
                    break;

                default:
                    log.debug("Unexpected linkState " + link.linkState + " in "
                            + link.linkState);
                    break;
                }
            }
        }

        if (state != newState) {
            switch (newState) {
            case CONNECTING:
            case INCONSISTENT:
                raiseCannotTakeCalls(newState);
                break;

            case MISSING_MASTER:
                raiseCannotTakeCalls(newState);
                missingMasterStart = System.currentTimeMillis();
                break;

            case MASTER_SLAVE:
                preferredLink = master;
                break;

            case LOADSHARING:
                break;

            case STOPPED: // Clear alarms when stopped
            case NO_LINKS: // Clear alarm as this may be intentional
                alarms.clearAlarm(ALARM_CANNOT_TAKE_CALLS, this);
                break;

            default:
                log.debug("Unexpected newState " + newState);
                break;
            }
        }

        if (newState == SystemState.MASTER_SLAVE
                || newState == SystemState.LOADSHARING) {
            // Check if at least one link is active
            boolean foundActiveLink = false;
            for (Link link : links) {
                if (link.isUnitActive()) {
                    foundActiveLink = true;
                    break;
                }
            }

            if (foundActiveLink) {
                alarms.clearAlarm(ALARM_CANNOT_TAKE_CALLS, this);
                gappingAlarmActive = false;
            } else if (!gappingAlarmActive) {
                raiseCannotTakeCalls(newState);
                gappingAlarmActive = true;
            }
        }

        return newState;
    }

    /**
     * Raise the ocpCannotTakeCalls alarm.
     *
     * @param newState the state that the system manager is about to enter
     */
    private void raiseCannotTakeCalls(final SystemState newState) {
        String[] params = new String[links.size() + 2];
        params[0] = newState.toString();
        params[1] = Integer.toString(links.size());
        for (int i = 0; i < links.size(); i++) {
            params[i + 2] = links.get(i).linkState.toString();
        }

        alarms.raiseAlarm(ALARM_CANNOT_TAKE_CALLS, this, params);
    }

    /**
     * Recalculate the preferred unit. The preferred unit is chosen as follows:
     * <p/>
     * <b>Master/slave:</b> The preferred unit is the master unit.
     * <p/>
     * <b>Loadsharing:</b> If there is only one link, then it is chosen.
     * Otherwise, if all units report the same unit as being preferred and it is
     * a unit that this object is connected to then it is chosen. Otherwise
     * there is no preferred unit.
     *
     * @param newState the new system state to use for recalculating.
     */
    private void recalcPreferredUnit(final SystemState newState) {
        Link newPreferred = null;
        boolean unitsMatch = false;

        switch (newState) {
        case LOADSHARING:
            if (links.size() == 1) {
                newPreferred = links.get(0);
            } else {
                // We have multiple links. Check which unit each link thinks
                // is preferred - if they all agree, then we use that.
                InetAddress preferredUnit = links.get(0).preferredSCP;
                InetAddress secondaryUnit = links.get(0).secondarySCP;

                if (preferredUnit != null && secondaryUnit != null) {
                    unitsMatch = true;

                    for (Link link : links) {
                        if (!preferredUnit.equals(link.preferredSCP)
                                && !secondaryUnit.equals(link.secondarySCP)) {
                            unitsMatch = false;
                            break;
                        }
                        if (preferredUnit.equals(link.linkManager
                                .getRemoteAddress())) {
                            newPreferred = link;
                        }
                    }

                    // Note that it is possible to get here without a preferred
                    // link even if all units agree, if the link they agree on
                    // is not one of ours. In that case we don't have a
                    // preferred link.
                }

                if (!unitsMatch) {
                    newPreferred = null;
                }
            }
            break;

        case MASTER_SLAVE:
            for (Link link : links) {
                if (link.linkState == LinkStates.MASTER) {
                    newPreferred = link;
                }
            }
            break;

        default:
            newPreferred = null;
            break;
        }

        if (newPreferred != preferredLink) {
            log.info("recalcPreferredUnit: changed from " + preferredLink
                    + " to " + newPreferred);
            preferredLink = newPreferred;
        }
    }

    /**
     * Select an OCP link to use for a new call. The link chosen depends on the
     * current mode:
     * <p/>
     * <b>Master/slave:</b> if the master can take calls, then the master is
     * returned. Otherwise null is returned.
     * <p/>
     * <b>Loadsharing:</b> if the connected servers have selected a preferred
     * server, then this is returned. Otherwise the server with the fewest
     * active calls is returned.
     * <p/>
     * Additionally, any links that are specified in triedLinks will not be
     * returned by this function. If the above process would select one of these
     * then the next available link will be returned instead. This function will
     * also not return any links that are considered to be suspect or are not
     * currently capable of taking new calls.
     *
     * @param triedLinks
     *            A list of links that have already been tried and so may not be
     *            returned by this function.
     * @return The OCP link to try next. If null is returned then there are no
     *         more available links.
     */
    @Override
    public final OCPLink getLink(final List<OCPLink> triedLinks) {
        Link current = null;
        ArrayList<Link> sortedLinks;

        if (state == SystemState.MASTER_SLAVE) {
            if (preferredLink != null && preferredLink.isUnitActive()) {
                log.debug("getLink: Master/slave, selecting preferred "
                        + preferredLink);
                current = preferredLink;
            } else {
                // The master can't take calls (or doesn't exist).
                log.debug("getLink: Master/slave, returning no link");
                return null;
            }
        } else if (state == SystemState.LOADSHARING) {
            if (preferredLink != null && preferredLink.isUnitActive()) {
                log.debug("getLink: Loadsharing, selecting preferred "
                        + preferredLink);
                current = preferredLink;
            } else {
                // Either we don't have a preferred link or it can't take
                // calls.
                log.debug("getLink: Loadsharing, no preferred link");
                current = null;
            }
        } else {
            // We're not in a state where we can take calls, therefore there
            // are no available links.
            log.debug("getLink: Other, returning no link. State:"
                    + state.toString());
            return null;
        }

        if (current != null) {
            if (triedLinks == null || !triedLinks.contains(current)) {
                // The caller has not tried this one.
                log.debug("getLink: Returning preferred link");
                return current;
            }
        }

        // Either we don't have a preferred link, or the caller has tried it
        // and it didn't work. Find an alternate link.
        if (state == SystemState.MASTER_SLAVE) {
            log.debug("getLink: Master/slave, returning no fallback link");
            return null;
        }

        // Create a local shallow copy, and sort it by calls in progress. Since
        // the master list isn't being modified we don't need to synchronise on
        // it.
        sortedLinks = new ArrayList<Link>(links);
        if (triedLinks != null) {
            log.debug("getLink: Loadsharing, removing " + triedLinks.size()
                    + " link(s)");
            sortedLinks.removeAll(triedLinks);
        }
        Collections.sort(sortedLinks, Collections.<Link>reverseOrder());

        // Find the first available unit
        for (Link link : sortedLinks) {
            if (link.isUnitActive()) {
                log.debug("getLink: Loadsharing, returning fallback "
                        + link);
                return link;
            }
        }

        log.debug("getLink: Loadsharing, returning no fallback link");
        return null;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public final SystemState getState() {
        return state;
    }

    /**
     * Gets the preferred link.
     *
     * @return the preferred link
     */
    public final Link getPreferredLink() {
        return preferredLink;
    }

    /**
     * Register a handler for the management task ID on all links.
     *
     * @param handler the handler to register
     */
    @Override
    public final void registerManagementTidHandler(
            final OCPMessageHandler handler) {
        for (Link link : links) {
            link.registerManagementTidHandler(handler);
        }
    }

    /**
     * Remove the handler for the management task ID from all links.
     */
    @Override
    public final void deregisterManagementTidHandler() {
        for (Link link : links) {
            link.deregisterManagementTidHandler();
        }
    }

    @Override
    public void congestionEvent(
            final EnumMap<CongestionType, Integer> congestionList) {
        // Not used, but required by ServerEventNotifier
    }

    @Override
    public void reloadConfiguration() {
        // Not used, but required by ServerEventNotifier
    }

    @Override
    public void stateEvent(final ActiveStates activeState) {
        // Not used, but required by ServerEventNotifier
    }

    /**
     * Internal structure for managing a single OCP Link. This structure is
     * sorted by the number of active calls on the link.
     * <p/>
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @author Telsis
     */
    private class Link implements OCPLinkStateHandler, Comparable<Link>,
            OCPMessageHandler, OCPLink, ServerEventNotifier {
        /** The link manager. */
        private OCPLinkManager linkManager;
        /** The cached unit enabled status. */
        private boolean        unitEnabled;
        /** A flag to indicate if gapping is currently active. */
        private boolean        gappingActive;
        /** A flag to indicate if this link is suspect. */
        private boolean        linkSuspect;
        /** The cached link state. */
        private LinkStates     linkState;
        /** The cached preferred SCP. */
        private InetAddress    preferredSCP;
        /** The cached secondary SCP. */
        private InetAddress    secondarySCP;
        /** The timestamp of when gapping started. */
        private long           gappingStart;
        /** The timestamp of when gapping should end. */
        private long           gappingEnd;
        /** The index of this link. */
        private int            linkID;
        /** Whether or not this link has been cleaned up. */
        private volatile boolean cleanedUp = false;

        /**
         * Instantiates a new link. This creates the underlying
         * {@link OCPLinkManager} instance and prepares it for use.
         *
         * @param properties
         *            the properties object to load configuration from
         * @param index
         *            the index number of this link
         */
        public Link(final Properties properties, final int index) {
            UtilitiesFactory.getUtilInterface().register(this, false);
            log.info(this + " changed from null to DISCONNECTED");
            linkState = LinkStates.DISCONNECTED;
            linkManager = new OCPLinkManager(properties, index);
            linkManager.registerLinkStatusHandler(this);
            linkID = index;
        }

        /**
         * Clean up the link. After calling this the link can no longer be used.
         * <p>
         * Note: this method should not be called concurrently.
         */
        public void cleanup() {
            cleanedUp = true;
            log.info(this + " changed from " + linkState + " to DISCONNECTED");
            linkState = LinkStates.DISCONNECTED;
            linkManager.cleanup();
            synchronized (this) {
                /*
                 * Synchronize here and in raiseLinkAlarm() to ensure that the
                 * alarm will not be raised when this method returns. Don't
                 * synchronize on the entire method to avoid a deadlock between
                 * linkStateChanged() (called from linkManager.cleanup above,
                 * holding this object and locking sysThread) and
                 * raiseLinkAlarm() (called from recalcMode() in sysThread,
                 * holding sysThread and locking this object).
                 */
                alarms.clearAlarm(ALARM_LINK_NOT_AVAILABLE, this);
            }
        }

        /**
         * Checks if the link is active. A link is active if the connected SCP
         * is enabled and not gapped.
         *
         * @return true if the link is active
         */
        public boolean isUnitActive() {
            return (unitEnabled && !gappingActive);
        }

        /**
         * Gets the index of this link.
         *
         * @return the index of this link
         */
        public int getLinkID() {
            return linkID;
        }

        /**
         * Receive notification that the link state has changed. If the new
         * state is different to the cached value, then the cached link state is
         * updated and the system thread is notified.
         *
         * @param newState
         *            The new state of the link
         */
        @Override
        public void linkStateChanged(final LinkStates newState) {
            if (this.linkState != newState) {
                log.debug(this + " changed from "
                        + linkState + " to " + newState);
                this.linkState = newState;
                synchronized (sysThread) {
                    sysThread.notify();
                }
            }
        }

        /**
         * Receive notification that a {@link CallGap} message has been
         * received. The gapping start, end and duration values are updated and
         * the system thread is notified.
         * <p/>
         * Note: {@link CallGap#DURATION_NETWORK_SPECIFIC} is not currently
         * supported, and will result in no change to the current gapping state.
         *
         * @param duration
         *            The duration in seconds for which call gapping is active,
         *            or one of {@link CallGap#DURATION_NETWORK_SPECIFIC},
         *            {@link CallGap#DURATION_INDEFINITE} or
         *            {@link CallGap#DURATION_DISABLED}.
         */
        @Override
        public void receivedCallGap(final short duration) {
            switch (duration) {
            case CallGap.DURATION_NETWORK_SPECIFIC:
                // not supported here
                break;

            case CallGap.DURATION_INDEFINITE:
                this.gappingStart = System.currentTimeMillis();
                this.gappingEnd = Long.MAX_VALUE;
                this.gappingActive = true;
                break;

            case CallGap.DURATION_DISABLED:
                this.gappingStart = 0;
                this.gappingEnd = 0;
                this.gappingActive = false;
                break;

            default:
                // gap calls for duration seconds
                this.gappingStart = System.currentTimeMillis();
                this.gappingEnd = this.gappingStart + duration * MILLISECONDS;
                this.gappingActive = true;
                break;
            }
            log.debug(this + " gapping changed: duration " + duration
                    + " gappingStart " + this.gappingStart
                    + " gappingEnd " + this.gappingEnd
                    + " gappingActive " + this.gappingActive);
            synchronized (sysThread) {
                sysThread.notify();
            }
        }

        /**
         * Receive notification that a
         * {@link com.telsis.jocp.messages.PreferredUnit} message has been
         * received. The cached values are updated and the system thread is
         * notified.
         *
         * @param newPreferredSCP
         *            The address of the preferred SCP
         * @param newSecondarySCP
         *            The address of the secondary SCP
         */
        @Override
        public void receivedPreferredUnit(final InetAddress newPreferredSCP,
                final InetAddress newSecondarySCP) {
            this.preferredSCP = newPreferredSCP;
            this.secondarySCP = newSecondarySCP;
            synchronized (sysThread) {
                sysThread.notify();
            }
        }

        /**
         * Receive notification that the unit enabled state has changed. The
         * cached flag is updated and the system thread is notified.
         *
         * @param enabled
         *            The new enabled state of the unit
         */
        @Override
        public void unitEnabledChanged(final boolean enabled) {
            unitEnabled = enabled;
            synchronized (sysThread) {
                sysThread.notify();
            }
        }

        /**
         * Compares this instance with the specified Link. Links are ordered by
         * the link suspect flag, then by the number of active calls.
         *
         * @param other
         *            the Link to compare this instance to
         */
        @Override
        public int compareTo(final Link other) {
            if (this.linkSuspect && !other.linkSuspect) {
                return -1;
            } else if (!this.linkSuspect && other.linkSuspect) {
                return 1;
            } else {
                return other.linkManager.getNumCalls()
                        - this.linkManager.getNumCalls();
            }
        }

        @Override
        public void queueMessage(final OCPMessage message,
                final OCPLink callingLink) {
            linkManager.queueMessage(message, callingLink);
        }

        @Override
        public void deregisterTidHandler(final int tid) {
            linkManager.deregisterTidHandler(tid);
        }

        @Override
        public void registerTidHandler(final OCPMessageHandler handler,
                final int tid) {
            linkManager.registerTidHandler(handler, tid);
        }

        /**
         * De-register the handler that manages task IDs.
         */
        public void deregisterManagementTidHandler() {
            linkManager.deregisterManagementTidHandler();
        }

        /**
         * Register a handler to manage task IDs.
         * @param handler handler
         */
        public void registerManagementTidHandler(
                final OCPMessageHandler handler) {
            linkManager.registerManagementTidHandler(handler);
        }

        /**
         * Reload the OCP module configuration using the given new properties.
         * @param properties properties
         * @return <code>true</code> if and only if reload was successful
         */
        public boolean reloadConfiguration(final Properties properties) {
            log.debug("reloadConfiguration");
            return linkManager.reloadConfig(properties);
        }

        /**
         * Raise a link alarm on the current link.
         */
        public synchronized void raiseLinkAlarm() {
            if (!cleanedUp) {
                alarms.raiseAlarm(ALARM_LINK_NOT_AVAILABLE, this, getLinkID(),
                        new String[] {toString()});
            }
        }

        @Override
        public String toString() {
            return "Link(" + linkManager + ")";
        }

        @Override
        public void congestionEvent(
                final EnumMap<CongestionType, Integer> congestionList) {
            // Not used, but required by ServerEventNotifier
        }

        @Override
        public void reloadConfiguration() {
            // Not used, but required by ServerEventNotifier
        }

        @Override
        public void stateEvent(final ActiveStates activeState) {
            // Not used, but required by ServerEventNotifier
        }
    }

    /**
     * Management thread for the OCP system manager. This thread periodically
     * manages the following:
     * <ul>
     * <li>Promotion of slave links.</li>
     * <li>Gapping updates.</li>
     * <li>Suspect link detection.</li>
     * <li>Preferred unit selection.</li>
     * <li>System state.</li>
     * </ul>
     *
     * @author Telsis
     */
    private class SystemThread implements Runnable {
        /** The thread that this object is running in. */
        private Thread  myThread;
        /** A flag to detect when this thread should be shut down. */
        private boolean shutdownThread;

        /**
         * Create a new stopped SystemThread.
         */
        public SystemThread() {
            super();

            shutdownThread = false;
            myThread = null;
        }

        /**
         * Entry point for the thread. This function should not be called
         * directly. On start-up, the thread brings up the OCP links and then
         * loops updating the system status at regular intervals. When a
         * shut down request is detected, the OCP links are disconnected and the
         * thread terminates.
         */
        @Override
        public void run() {
            myThread = Thread.currentThread();
            log.debug("SystemThread: thread starting");
            watchdog.start();

            synchronized (this) {
                state = recalcMode();

                // Bring up the OCP Links
                for (Link link : links) {
                    link.linkManager.connect(watchdog);
                }

                while (true) {
                    watchdog.pat();
                    internalRun();

                    if (shutdownThread) {
                        break;
                    }

                    try {
                        this.wait(1 * MILLISECONDS);
                    } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
                    }
                }

                log.debug("SystemThread: shutting down");
                // Shutdown the OCP links
                for (Link link : links) {
                    link.linkManager.disconnect();
                }
            }
            watchdog.stop();
            log.debug("SystemThread: thread exiting");
        }

        /**
         * Shut down the thread. This method waits for the thread to terminate
         * before returning. If the thread has already terminated or has not
         * been started, then this method will return immediately.
         */
        public void shutdownThread() {
            log.debug("SystemThread: requesting shutdown");

            synchronized (this) {
                shutdownThread = true;
                this.notify();
            }

            if (myThread != null) {
                while (myThread.isAlive()) {
                    try {
                        myThread.join();
                    } catch (InterruptedException e) { // CSIGNORE: EmptyBlock
                    }
                }
            }
            myThread = null;
        }

        /**
         * Main loop procedure for the thread.
         * <p/>
         * This method is called every polling interval and carries out the
         * following actions:
         * <ol>
         * <li>Each gapped link is checked to see if the gapping duration has
         * elapsed, and if so gapping is disabled on that link.</li>
         * <li>The last activity timestamp is checked on each link, and any
         * links that have been inactive for more than the configured duration
         * are marked as suspect.</li>
         * <li>The system state is recalculated.</li>
         * <li>The preferred unit is recalculated.</li>
         * <li>If the system state is MISSING_MASTER and the configured timeout
         * has elapsed, then the unit with the highest ID is sent a
         * {@link BecomeMaster} request and the timeout is reset.
         * </ol>
         */
        private void internalRun() {
            SystemState newState;

            long now = System.currentTimeMillis();
            // Update per-link details
            for (Link link : links) {
                // Check gapping
                if (link.gappingActive && now > link.gappingEnd) {
                    link.gappingActive = false;
                    log.debug("Gapping expired on link " + link);
                }

                // Check activity
                if (now - link.linkManager.lastActivity()
                        > linkSuspectTimeout) {
                    link.linkSuspect = true;
                } else {
                    link.linkSuspect = false;
                }
            }

            newState = recalcMode();
            recalcPreferredUnit(newState);
            state = newState;

            // Check if we need to promote a slave
            if (state == SystemState.MISSING_MASTER
                    && now - missingMasterStart > masterSlaveSwapTimeout) {
                // Select the unit with the highest unit ID, and promote it
                Link chosenUnit = null;
                for (Link link : links) {
                    if (chosenUnit == null) {
                        chosenUnit = link;
                    } else {
                        if (link.linkManager.getUnitID()
                                > chosenUnit.linkManager.getUnitID()) {
                            chosenUnit = link;
                        }
                    }
                }

                log.debug("Promoting unit: state " + state + " now " + now
                        + " missingMasterStart " + missingMasterStart
                        + " masterSlaveSwapTimeout " + masterSlaveSwapTimeout
                        + " chosenUnit " + chosenUnit);

                if (chosenUnit != null
                        && chosenUnit.linkState == LinkStates.SLAVE) {
                    missingMasterStart = now; // Avoid spamming a unit
                    chosenUnit.queueMessage(new BecomeMaster(), null);
                }
            }
        }
    }
}
