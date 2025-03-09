package pleinlib.utils.futures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public class CompletableFutureUtils {
    
    /**
     * Run the completable futures and try to collect the first {@code size}. If {@code timeoutMs} is reached will return the
     * first {@code size} futures that have been completed in order.
     * @param futures to run and collect result.
     * @param size desired futures that must be retrieved.
     * @param timeoutMs maximum time to wait for completition.
     * @return the result of the first {@code size} futures completed on time or otherwise add results from other futures completed until
     * reaching desired {@code size}, if possible.
     */
    public static <T> List<T> collectInOrder(final List<CompletableFuture<T>> futures, final int size, final int timeoutMs) {
        if (size <= 0) {
            return List.of();
        }

        long startTimeNanos = System.nanoTime();
        List<Result<T>> results = new ArrayList<>();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<Result<T>> completionService = startFutures(futures, executor, timeoutMs, startTimeNanos);        

        Set<Integer> toComplete = new HashSet<>();
        IntStream.of(Math.min(futures.size(), size)).forEach(i -> toComplete.add(i));

        try {
            long timeLeft = timeoutMs - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
            while (!toComplete.isEmpty() && timeLeft > 0) {
                Future<Result<T>> completedFuture = completionService.poll(timeLeft, TimeUnit.MILLISECONDS);
                if (completedFuture != null) {
                    results.add(completedFuture.get());
                    toComplete.remove(completedFuture.get().index);
                }
                timeLeft = timeoutMs - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        // Sort results by original order before returning
        return results.stream()
                .sorted(Comparator.comparingInt(r -> r.index))
                .map(r -> r.value)
                .filter(Objects::nonNull)
                .limit(size)
                .toList();
    }

    /**
     * Run the completable futures and return the first {@code size} to complete with non Null result. The order is preserved.
     * If {@code timeoutMs} is reached, return the collected results until that point.
     * @param futures to run and collect result.
     * @param size desired futures that must be retrieved.
     * @param timeoutMs maximum time to wait for completition.
     * @return the first {@code size} futures results to complete with non Null result.
     */
    public static <T> List<T> collectCompleted(final List<CompletableFuture<T>> futures, final int size, final int timeoutMs) {
        if (size <= 0) {
            return List.of();
        }

        long startTimeNanos = System.nanoTime();
        List<Result<T>> results = new ArrayList<>();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<Result<T>> completionService = startFutures(futures, executor, timeoutMs, startTimeNanos);

        try {
            long timeLeft = timeoutMs - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
            while (results.size() < Math.min(futures.size(), size) && timeLeft > 0) {
                Future<Result<T>> completedFuture = completionService.poll(timeLeft, TimeUnit.MILLISECONDS);
                if (completedFuture != null) {
                    results.add(completedFuture.get());
                }
                timeLeft = timeoutMs - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        // Sort results by original order before returning
        return results.stream()
                .sorted(Comparator.comparingInt(r -> r.index))
                .map(r -> r.value)
                .filter(Objects::nonNull)
                .toList();
    }

    private static <T> CompletionService<Result<T>> startFutures(final List<CompletableFuture<T>> futures, 
        final ExecutorService executor, final int timeoutMs, final long startTimeNanos) {
        CompletionService<Result<T>> completionService = new ExecutorCompletionService<>(executor);

        // Submit all futures to the completion service with their index
        for (int i = 0; i < futures.size(); i++) {
            int index = i;
            completionService.submit(() -> {
                long timeLeft = timeoutMs - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
                if (timeLeft <= 0) throw new TimeoutException();
                return new Result<>(index, futures.get(index).get(timeLeft, TimeUnit.MILLISECONDS));
            });
        }
        return completionService;
    }


    private static class Result<T> {
        final int index;
        final T value;

        Result(int index, T value) {
            this.index = index;
            this.value = value;
        }
    }
}
