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

package org.obicere.cc.executor;

/**
 * Post compilation during execution these results are provided by the Runners.
 * They provide insight as to how will the user did with their code.
 *
 * @author Obicere
 * @since 1.0
 */

public class Result {

    private final Object result;
    private final Object correctAnswer;
    private final Object[] parameters;

    /**
     * Creates a new Result instance.
     *
     * @param result        The answer provided by the user.
     * @param correctAnswer The correct answer provided by the runner.
     * @param parameters    The list of parameters for displaying in the tables.
     */

    public Result(Object result, Object correctAnswer, Object... parameters) {
        this.result = result;
        this.correctAnswer = correctAnswer;
        this.parameters = parameters;
    }

    /**
     * Returns the user's-code's result per method call.
     *
     * @return <tt>Object</tt> of which the user calculated.
     * @see Result#isCorrect()
     * @since 1.0
     */

    public Object getResult() {
        return result;
    }

    /**
     * Returns the result provided by the author of the Runner.
     *
     * @return <tt>Object</tt> of which the author calculated.
     * @see Result#isCorrect()
     * @since 1.0
     */

    public Object getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * For usage in the {@link org.obicere.cc.gui.projects.ResultsTable ResultsTable}
     * instance. It is neither compared or used in any other class.
     *
     * @return Array of type <tt>Object</tt> to be used for displaying results.
     * @since 1.0
     */

    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Returns true if the user output equals the author output. For most cases,
     * classes that override the {@link Object#equals(Object)
     * Object#equals(Object)} should be used. If not, results are not
     * guaranteed.
     *
     * @return <tt>true</tt> if the user output equals the author output.
     * @since 1.0
     */

    public boolean isCorrect() {
        return result.equals(correctAnswer);
    }

}
