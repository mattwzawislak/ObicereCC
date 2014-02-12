/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.tasks.projects;

import org.obicere.cc.executor.Case;
/**
 * All Runners must extend this class.
 *
 * @author Obicere
 * @since 1.0
 */

public abstract class Runner {

    /**
     * Runs the user's code and is used to set up specific tests. This is the
     * only required method in a Runner.
     *
     * @since 1.0
     */

    public abstract Case[] getCases();

    public abstract Parameter[] getParameters();

    public abstract String getMethodName();

    public abstract Class<?> getReturnType();

}
