package org.obicere.cc.executor;

import java.util.Arrays;


public class Case {

    private final Object expectedResult;
    private final Object[] parameters;

    public Case(final Object expectedResult, final Object... parameters){
        this.expectedResult = expectedResult;
        this.parameters = parameters;
    }

    public Object getExpectedResult(){
        return expectedResult;
    }

    public Object[] getParameters(){
        return parameters;
    }

    @Override
    public boolean equals(final Object obj){
        if(obj instanceof Case){
            final Case param = (Case) obj;
            return param.getExpectedResult().equals(getExpectedResult()) && Arrays.equals(param.getParameters(), getParameters());
        }
        return false;
    }

}
