package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * Provides an {@link java.util.concurrent.ExecutorService} for running the
 * cases through a project. This does require a supplier for each language
 * to specify how exactly the future task should be formed. The main goal
 * of this is to run all the cases asynchronous to the other tasks to
 * reduce running time.
 * <p>
 * This also means that should a case take too long, the others will not be
 * held up and may actually complete.
 * <p>
 * Another benefit would be that the program can then utilize otherwise
 * unused cores. Though, the average running time for a single execution is
 * rather low, this is merely a micro-optimization to help improve speeds.
 *
 * @author Obicere
 * @version 1.0
 */
public class LanguageExecutorService {

    private final Case[]      cases;
    private final Future<?>[] futures;

    private final int threadPoolSize;

    /**
     * Constructs a new executor service for handling the asynchronous
     * execution of the cases. Upon the construction, the tasks will be
     * submitted after being provided through the <code>futureSupplier</code>.
     *
     * @param cases          The cases to provide tasks for and to run.
     * @param futureSupplier The language-specific way to run the task.
     */

    public LanguageExecutorService(final Case[] cases, final Function<Case, FutureTask<Object>> futureSupplier) {
        Objects.requireNonNull(cases);
        this.threadPoolSize = cases.length;

        if (threadPoolSize == 0) {
            throw new IllegalArgumentException("Amount of cases to run must be greater than 0.");
        }

        this.futures = new Future<?>[threadPoolSize];
        this.cases = cases;

        final ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        for (int i = 0; i < cases.length; i++) {
            final FutureTask<Object> future = futureSupplier.apply(cases[i]);
            service.execute(future);
            futures[i] = future;
        }
    }

    /**
     * Polls the cases in order they were added to the executor service.
     * The cases are then transformed into {@link org.obicere.cc.executor.Result}s
     * according to what happens here. Should the execution be interrupted
     * by the timeout timer, then a timed-out result is returned. In the
     * case where there is an exception during execution, then an error
     * result is returned.
     * <p>
     * In the case of normal execution, then the result is formed as normal
     * with the user's result. It is possible to have some results time out
     * and others succeed. Basically any combination can happen and this is
     * reflected aptly in the results table.
     * <p>
     * Any errors that occur are printed out to the console.
     *
     * @return The list of results, either normal, error or timeout
     * results.
     */

    public Result[] requestResults() {
        final Result[] results = new Result[threadPoolSize];
        for (int i = 0; i < threadPoolSize; i++) {
            final Case thisCase = cases[i];
            try {
                final Object result = futures[i].get();
                results[i] = new Result(result, thisCase);
            } catch (final InterruptedException e) {
                // No need to throw exception. We know the error.
                results[i] = Result.newTimedOutResult(thisCase);
            } catch (final ExecutionException e) {
                e.printStackTrace();
                results[i] = Result.newErrorResult(thisCase);
            }
        }
        return results;
    }

}
