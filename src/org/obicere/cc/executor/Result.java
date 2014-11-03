package org.obicere.cc.executor;

import java.util.Arrays;
import java.util.Objects;

public class Result {

    public static final String TIMED_OUT = "Timed out...";

    public static final String ERROR = "Error";

    private final Object   result;
    private final Object   correctAnswer;
    private final Object[] parameters;

    public Result(final Object result, final Object correctAnswer, final Object... parameters) {
        this.result = result;
        this.correctAnswer = correctAnswer;
        this.parameters = parameters;
    }

    public Result(final Object result, final Case cas){
        Objects.requireNonNull(cas);
        this.result = result;
        this.correctAnswer = cas.getExpectedResult();
        this.parameters = cas.getParameters();
    }

    public Object getResult() {
        return result;
    }

    public Object getCorrectAnswer() {
        return correctAnswer;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public boolean isCorrect() {
        if (result == null) {
            return correctAnswer == null;
        }
        if (result.getClass().isArray()) {
            return Arrays.deepEquals(new Object[]{result}, new Object[]{correctAnswer});
        }
        return result.equals(correctAnswer);
    }

    public static Result newTimedOutResult(final Case cas){
        return new Result(TIMED_OUT, cas);
    }

    public static Result newErrorResult(final Case cas){
        return new Result(ERROR, cas);
    }

}
