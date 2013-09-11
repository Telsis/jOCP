/*
 * Telsis Limited jOCP library
 *
 * Copyright (C) Telsis Ltd. 2013.
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
package com.telsis.jocp.sampleApp;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.telsis.jocp.OCPLink;
import com.telsis.jocp.OCPMessage;
import com.telsis.jocp.OCPMessageHandler;
import com.telsis.jocp.OCPSystemManager;
import com.telsis.jocp.OCPTelno;
import com.telsis.jocp.OCPUtil;
import com.telsis.jocp.messages.DeliverTo;
import com.telsis.jocp.messages.DeliverToResult;
import com.telsis.jocp.messages.InitialDP;
import com.telsis.jocp.messages.InitialDPResponse;
import com.telsis.jutils.signalling.GenericTelno;
import com.telsis.jutils.signalling.SignallingUtil;
import com.telsis.jutils.signalling.TelnoType;

/**
 * Basic implementation of an application that sends and receives OCP events.
 * <h3>Message flow</h3>
 * <table>
 * <thead>
 * <tr><th>OCPApp</th><th> -&gt; / &lt;- </th><th>SCP</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>Connects</td><td> -&gt;</td><td></td></tr>
 * <tr><td>Initial DP</td><td> -&gt;</td><td></td></tr>
 * <tr><td></td><td> &lt;-</td><td>DeliverTo</td></tr>
 * <tr><td>DeliverToResult</td><td> -&gt;</td><td></td></tr>
 * </tbody>
 * <tfoot>
 * <tr><th>OCPApp</th><th> -&gt; / &lt;- </th><th>SCP</th></tr>
 * </tfoot>
 * </table>
 * @author Telsis Ltd.
 * @version 1.0.0
 *
 */
public final class BasicOCPApp {
    /** Time to wait to allow the link to connect. */
    private static final long LINK_WAIT_TIME = 1000;
    /** Destination (FIN). **/
    private static final byte[] FIN = new byte []{
        4, 4, 1, 4, 8, 9, 7, 6, 0, 0, 0, 0};
    /** Source (CLI). **/
    private static final byte[] CLI = new byte []{
        4, 4, 1, 4, 8, 9, 7, 6, 0, 0, 0, 1};
    /** Local IP to bind on. */
    private static final String LOCAL_IP = "172.16.0.204";
    /** Remote IP to connect to. */
    private static final String REMOTE_IP = "172.16.0.63";
    /**
     * The amount of time to wait before checking again to see whether the call has finished.
     */
    private static final long WAIT_TIME = 1000;
    /** The current link for transmitting OCP messages. */
    private static OCPLink link;
    /** An array of failed links. */
    private static ArrayList<OCPLink> failedLinks;
    /** OCP system manager. */
    private static OCPSystemManager sysManager = null;
    /** The OCP message dispatcher. */
    private static OCPMessageHandler handler = null;
    /** The local task ID - unique to a call. */
    private static int localTID = 1;
    /** The remote task ID. */
    private static int remoteTID = -1;
    /** Logging instance.     */
    private static Logger logger = Logger.getLogger(BasicOCPApp.class);
    /**
     * Whether we have sent a result message yet.
     */
    private static volatile boolean sentResult = false;
    /**
     * Entry point.
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        setup();
        //Wait a bit so the link has time to come up
        try {
            Thread.sleep(LINK_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean haveLink = getLink();
        if (haveLink) {
            OCPMessage message = buildInitialDP();
            sendOCPMessage(message);
        } else {
            logger.fatal("No link was available");
            System.exit(1);
        }
        while (!sentResult) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        shutdown();
        System.exit(0);
    }
    /**
     * Retrieves a link and registers the task ID handler.
     * @return <code>true</code> if there's an available link
     */
    private static boolean getLink() {
        link = sysManager.getLink(failedLinks);
        if (link != null) {
            link.registerTidHandler(handler, localTID);
            return true;
        }
        return false;
    }

    /**
     * Perform set-up of the OCP link and logging.
     */
    private static void setup() {
        BasicConfigurator.configure();
        Properties properties = new Properties();
        properties.setProperty("ocpSystemUnitName", "BasicOCPApp");
        properties.setProperty("ocpSystemNumLinks", "1");
        properties.setProperty("ocpLink0RemoteAddress", REMOTE_IP);
        properties.setProperty("ocpLink0LocalAddress", LOCAL_IP);
        properties.setProperty("ocpSystemLoggingLevel", "INFO");
        sysManager = new OCPSystemManager(properties);
        sysManager.connect();
        handler = new OCPMessageHandler() {
            @Override
            public void queueMessage(final OCPMessage message, final OCPLink callingLink) {
                logger.info("Recieved OCP message");
                handleMessage(message);
            }
        };
        sysManager.registerManagementTidHandler(handler);
    }

    private static void shutdown() {
        sysManager.disconnect();
        sysManager.deregisterManagementTidHandler();
    }

    /**
     * Handle a message received from the link.
     * @param message Message received.
     */
    private static void handleMessage(final OCPMessage message) {
        logger.info("Handling OCP message: " + message.getMessageType());
        switch(message.getMessageType()) {
            case INITIAL_DP_RESPONSE:
                handleInitialDPResponse((InitialDPResponse) message);
                break;
            case DELIVER_TO:
                doDeliverTo((DeliverTo) message);
                break;
            default:
                logger.warn("Unhandled message type");
                logger.info("Type:" + message.getMessageType());
                logger.info("Orig:" + message.getOrigTID());
                logger.info("Dest:" + message.getDestTID());
        }
    }
    /** Handle the response to the initial DP. */
    private static void handleInitialDPResponse(
            final InitialDPResponse iDPresponse) {
        remoteTID = iDPresponse.getOrigTID();
    }
    /**
     * Handle a deliver to message.
     * @param deliverTo Deliver To request
     */
    private static void doDeliverTo(final DeliverTo deliverTo) {
        remoteTID = deliverTo.getOrigTID();
        //Respond to the deliver to message
        DeliverToResult returnMessage = new DeliverToResult();
        returnMessage.setFlags(DeliverToResult.FLAG_OUTDIAL_SUCCEEDED);
        returnMessage.setTime(SignallingUtil.getOceanTime());
        returnMessage.setZipNumber(deliverTo.getZipNumber());
        sendOCPMessage(returnMessage);
        sentResult = true;
    }
    /**
     * Build an initial DP (detection point) message to the OCP link.
     * @return InitialDP message
     */
    private static InitialDP buildInitialDP() {
        InitialDP message = new InitialDP();
        GenericTelno gFIN = new GenericTelno(TelnoType.INTERNATIONAL, FIN);
        GenericTelno gCLI = new GenericTelno(TelnoType.INTERNATIONAL, CLI);
        OCPTelno ocpFIN = OCPUtil.convertGenericTelnoToOCPTelno(gFIN);
        OCPTelno ocpCLI = OCPUtil.convertGenericTelnoToOCPTelno(gCLI);
        message.setOrigLegID((short) 1);
        message.setCPC(InitialDP.CPC_UNUSED);
        message.setFINTypePlan(ocpFIN.getTypePlan());
        message.setFIN(ocpFIN.getTelno());

        message.setCLI(ocpCLI.getTelno());
        message.setCLITypePlan(ocpFIN.getTypePlan());
        message.setCLIPresScreen((byte) (SignallingUtil.Q931_CLI_NETWORK
                | SignallingUtil.Q931_CLI_PRESENTATION_ALLOWED));
        message.setOceanTime(SignallingUtil.getOceanTime());
        return message;
    }

    /**
     * Send a OCP message over the link.
     * @param message Message to send
     */
    private static void sendOCPMessage(final OCPMessage message) {
        logger.info("Sending OCP message: " + message.getMessageType());
        if (link != null) {
            message.setOrigTID(localTID);
            message.setDestTID(remoteTID);
            link.queueMessage(message, null);
        } else {
            logger.fatal("No link was available");
            System.exit(1);
        }
    }

    /**
     * Default constructor.
     */
    private BasicOCPApp() {
    }
}
