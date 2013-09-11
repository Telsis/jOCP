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
 * Stats interface.
 * @author Philip.Whitehouse
 *
 */
public final class StatsInterface {
    /**
     * Unregister stat.
     * @param name Statistic name
     */
    public void unregisterStat(final String name) {
    }

    /**
     * Register stat.
     * @param name Statistic name
     * @param extended Whether statistics on how the stat changes over time should be kept.
     */
    public void registerStat(final String name, final boolean extended) {
    }

    /**
     * Increment stat.
     * @param name Statistic name
     */
    public void incrementStat(final String name) {
    }

    /**
     * Decrement stat.
     * @param name Statistic name
     */
    public void decrementStat(final String name) {
    }

}
