package pleinlib.utils.futures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class CompletableFutureUtilsTest {
    
    @Test
    void testCollectInOrder_AllFuturesComplete() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(1),
                CompletableFuture.completedFuture(2),
                CompletableFuture.completedFuture(3)
        );

        List<Integer> result = CompletableFutureUtils.collectInOrder(futures, 2, 1000);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void testCollectInOrder_MoreFuturesThanNeeded() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(10),
                CompletableFuture.completedFuture(20),
                CompletableFuture.completedFuture(30)
        );

        List<Integer> result = CompletableFutureUtils.collectInOrder(futures, 1, 1000);
        assertEquals(List.of(10), result);
    }

    @Test
    void testCollectInOrder_TimeoutBeforeEnoughFuturesComplete() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.supplyAsync(() -> {
                    sleep(200); return 100;
                }),
                CompletableFuture.supplyAsync(() -> {
                    sleep(500); return 200;
                })
        );

        List<Integer> result = CompletableFutureUtils.collectInOrder(futures, 2, 300);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0)); // Only the first one should complete
    }

    @Test
    void testCollectInOrder_NoFuturesCompleteInTime() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.supplyAsync(() -> {
                    sleep(500); return 999;
                })
        );

        List<Integer> result = CompletableFutureUtils.collectInOrder(futures, 1, 100);
        assertTrue(result.isEmpty(), "Expected an empty list due to timeout.");
    }

    @Test
    void testCollectInOrder_EmptyList() {
        List<CompletableFuture<Integer>> futures = List.of();
        List<Integer> result = CompletableFutureUtils.collectInOrder(futures, 3, 1000);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCollectInOrder_NegativeOrZeroSize() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(42)
        );

        assertTrue(CompletableFutureUtils.collectInOrder(futures, 0, 1000).isEmpty());
        assertTrue(CompletableFutureUtils.collectInOrder(futures, -5, 1000).isEmpty());
    }

    @Test
    void testCollectCompleted_AllFuturesComplete() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(1),
                CompletableFuture.completedFuture(2),
                CompletableFuture.completedFuture(3)
        );

        List<Integer> result = CompletableFutureUtils.collectCompleted(futures, 3, 1000);
        assertEquals(3, result.size());
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void testCollectCompleted_MoreFuturesThanNeeded() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(10),
                CompletableFuture.completedFuture(20),
                CompletableFuture.completedFuture(30)
        );

        List<Integer> result = CompletableFutureUtils.collectCompleted(futures, 1, 1000);
        assertEquals(1, result.size());
        assertEquals(List.of(10), result);
    }

    @Test
    void testCollectCompleted_TimeoutBeforeEnoughFuturesComplete() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.supplyAsync(() -> {
                    sleep(200);
                    return 100;
                }),
                CompletableFuture.supplyAsync(() -> {
                    sleep(500);
                    return 200;
                })
        );

        List<Integer> result = CompletableFutureUtils.collectCompleted(futures, 2, 300);
        assertEquals(1, result.size()); // Only the first one should complete
        assertEquals(List.of(100), result);
    }

    @Test
    void testCollectCompleted_NoFuturesCompleteInTime() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.supplyAsync(() -> {
                    sleep(500);
                    return 999;
                })
        );

        List<Integer> result = CompletableFutureUtils.collectCompleted(futures, 1, 100);
        assertTrue(result.isEmpty(), "Expected an empty list due to timeout.");
    }

    @Test
    void testCollectCompleted_EmptyList() {
        List<CompletableFuture<Integer>> futures = List.of();
        List<Integer> result = CompletableFutureUtils.collectCompleted(futures, 3, 1000);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCollectCompleted_NegativeOrZeroSize() {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.completedFuture(42)
        );

        assertTrue(CompletableFutureUtils.collectCompleted(futures, 0, 1000).isEmpty());
        assertTrue(CompletableFutureUtils.collectCompleted(futures, -5, 1000).isEmpty());
    }

    @Test
    void testCollectCompleted_WithNullValues() {
        List<CompletableFuture<String>> futures = List.of(
                CompletableFuture.completedFuture("Hello"),
                CompletableFuture.completedFuture(null),
                CompletableFuture.completedFuture("World")
        );

        List<String> result = CompletableFutureUtils.collectCompleted(futures, 3, 1000);
        assertEquals(2, result.size());
    }


    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
