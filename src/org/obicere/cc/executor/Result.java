package org.obicere.cc.executor;

public class Result {

    private final Object result;
    private final Object correctAnswer;
    private final Object[] parameters;

    public Result(Object result, Object correctAnswer, Object... parameters) {
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
        return result.equals(correctAnswer);
    }

}
