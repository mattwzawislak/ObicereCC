package org.obicere.cc.executor;

import java.util.Arrays;

/**
 * A runner-generated test case for the project. The case differs from a
 * {@link org.obicere.cc.executor.Result} as it does not contain the
 * correct answer. This also means that 3rd party {@link
 * org.obicere.cc.projects.Runner}'s do not need to worry about properly
 * nulling out the answer field.
 * <p>
 * This also makes the intention of the {@link org.obicere.cc.projects.Runner}
 * a bit clearer and won't lead to ambiguity when dealing with both {@link
 * org.obicere.cc.projects.Runner} and {@link org.obicere.cc.executor.Case}
 * instances.
 *
 * @author Obicere
 * @version 1.0
 */

public class Case {

    private final Object   expectedResult;
    private final Object[] parameters;

    /**
     * Constructs a new case with the correct answer and the parameters
     * passed into the function to produce said answer.
     *
     * @param expectedResult The answer generated from the parameters.
     * @param parameters     The specific parameters for the function.
     */

    public Case(final Object expectedResult, final Object... parameters) {
        this.expectedResult = expectedResult;
        this.parameters = parameters;
    }

    /**
     * Retrieves the correct answer, as generated by the {@link
     * org.obicere.cc.projects.Runner}.
     *
     * @return The expected result.
     */

    public Object getExpectedResult() {
        return expectedResult;
    }

    /**
     * Retrieves the parameters passed into a function to produce the
     * expected result.
     *
     * @return The parameters used to generate the expected result.
     */

    public Object[] getParameters() {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Case) {
            final Case param = (Case) obj;
            if (!Arrays.deepEquals(param.getParameters(), getParameters())) {
                return false;
            }
            if (expectedResult.getClass().isArray()) {
                return Arrays.deepEquals(new Object[]{param.getExpectedResult()}, new Object[]{getExpectedResult()});
            }
            return param.getExpectedResult().equals(getExpectedResult());
        }
        return false;
    }

}
