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

import com.telsis.jutils.UtilitiesFactory;

/**
 * This class contains the jOCP library name and version number. The name and
 * version number are updated automatically at compile time, and are registered
 * with jUtils when this class is loaded.
 *
 * @author Telsis
 */
public final class Version { //CSIGNORE: HideUtilityClassConstructor
    /**
     * The name of this library.
     */
    public static final String LIBRARY_NAME = "jOCP";

    /**
     * The version of this library. This is generated automatically at compile
     * time by build.xml.
     */
    public static final String LIBRARY_VERSION = "1.3.0";

    static {
        UtilitiesFactory.getUtilInterface().setLibraryVersion(LIBRARY_NAME,
                LIBRARY_VERSION);
    }
}
