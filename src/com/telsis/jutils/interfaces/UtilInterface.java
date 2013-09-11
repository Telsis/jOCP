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
package com.telsis.jutils.interfaces;


/**
 * @author Telsis Ltd.
 */
public class UtilInterface {
    /** Alarms interface. */
    private AlarmInterface alarmInterface = new AlarmInterface();
    /** Statistics interface. */
    private StatsInterface statsInterface = new StatsInterface();

    /**
     * @return statsInterface
     */
    public final StatsInterface getStatsInterface() {
        return statsInterface;
    }

    /**
     * @return alarmInterface
     */
    public final AlarmInterface getAlarmInterface() {
        return alarmInterface;
    }

    /**
     * @param s event notifier
     * @param b boolean
     */
    public final void register(final ServerEventNotifier s, final boolean b) {
    }

    /**
     * @param libraryName libraryName
     * @param libraryVersion libraryVersion
     */
    public final void setLibraryVersion(final String libraryName, final String libraryVersion) {
    }

}
