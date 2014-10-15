package org.obicere.cc.executor;

import java.util.Arrays;

public class Result {

    private final Object   result;
    private final Object   correctAnswer;
    private final Object[] parameters;

    public Result(final Object result, final Object correctAnswer, final Object... parameters) {
        this.result = result;
        this.correctAnswer = correctAnswer;
        this.parameters = parameters;
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

}
