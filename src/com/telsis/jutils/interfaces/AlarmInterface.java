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
 * Alarm interface.
 * @author Telsis Ltd.
 */
public final class AlarmInterface {

    /**
     * Register alarm.
     * @param text text
     * @param alarmSource Alarm source
     * @param id ID
     * @param severity Severity
     */
    public void registerAlarm(final String text, final Class<?> alarmSource,
            final int id, final int severity) {
    }

    /**
     * Clear all alarms.
     * @param s The related event notifier.
     */
    public void clearAllAlarms(final ServerEventNotifier s) {
    }

    /**
     * Raise alarm.
     * @param name text
     * @param source source
     * @param linkID linkID
     * @param params Alarm parameters
     */
    public void raiseAlarm(final String name, final Object source, final int linkID,
            final String[] params) {
    }

    /**
     * Clear alarm.
     * @param name name
     * @param source source
     */
    public void clearAlarm(final String name, final Object source) {
    }

    /**
     * Raise alarm.
     * @param name name
     * @param s Event notifier
     * @param params Alarm parameters
     */
    public void raiseAlarm(final String name,
            final ServerEventNotifier s, final String[] params) {
    }

}
