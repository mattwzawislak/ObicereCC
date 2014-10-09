package org.obicere.cc.executor;

import java.util.Arrays;

public class Case {

    private final Object   expectedResult;
    private final Object[] parameters;

    public Case(final Object expectedResult, final Object... parameters) {
        this.expectedResult = expectedResult;
        this.parameters = parameters;
    }

    public Object getExpectedResult() {
        return expectedResult;
    }

    public Object[] getParameters() {
        return parameters;
    }

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
