package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * @author Obicere
 */
public class LanguageExecutorService {

    private final ExecutorService service;

    private final Case[] cases;
    private final List<Future<?>> futures = new ArrayList<>();

    private final int threadPoolSize;

    public LanguageExecutorService(final Case[] cases, final Function<Case, FutureTask<Object>> futureSupplier) {
        Objects.requireNonNull(cases);
        this.threadPoolSize = cases.length;
        this.cases = cases;
        this.service = Executors.newFixedThreadPool(cases.length);

        if (threadPoolSize == 0) {
            throw new IllegalArgumentException("Amount of cases to run must be greater than 0.");
        }
        for (final Case c : cases) {
            final FutureTask<Object> future = futureSupplier.apply(c);
            service.execute(future);
            futures.add(future);
        }
    }

    public Result[] requestResults() {
        final Result[] results = new Result[threadPoolSize];
        for (int i = 0; i < threadPoolSize; i++) {
            final Case thisCase = cases[i];
            try {
                final Object result = futures.get(i).get();
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
