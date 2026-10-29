package com.atm;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Dependency-free tests; run this class with assertions enabled or use the custom checks below. */
public class ATMTest {
    private ATMTest() {
    }

    public static void main(String[] args) throws Exception {
        testGreedyBreakdown();
        testOptimalSucceedsWhereGreedyFails();
        testFailedWithdrawalDoesNotChangeBalanceOrCash();
        testConcurrentWithdrawalsStayConsistent();
        testAuthenticationBlocksAfterThreeFailures();
        System.out.println("All ATM tests passed.");
    }

    private static void testGreedyBreakdown() throws Exception {
        ATM atm = new ATM(fullInventory(), new GreedyDispenser());
        Account account = new Account("card-1", 1234, 10_000, 10_000);
        Map<Denomination, Integer> notes = atm.withdraw(account, 2_870);
        check(notes.get(Denomination.N500) == 5, "Expected five Rs.500 notes");
        check(notes.get(Denomination.N200) == 1, "Expected one Rs.200 note");
        check(notes.get(Denomination.N100) == 1, "Expected one Rs.100 note");
        check(notes.get(Denomination.N50) == 1, "Expected one Rs.50 note");
        check(notes.get(Denomination.N20) == 1, "Expected one Rs.20 note");
        check(account.getBalance() == 7_130, "Balance should be debited by Rs.2,870");
    }

    private static void testOptimalSucceedsWhereGreedyFails() throws Exception {
        Map<Denomination, Integer> notes = emptyInventoryCounts();
        notes.put(Denomination.N500, 2);
        notes.put(Denomination.N200, 3);
        Map<Denomination, Integer> snapshot = Map.copyOf(notes);

        expectThrows(InsufficientCashException.class,
                () -> new GreedyDispenser().compute(600, snapshot));

        Map<Denomination, Integer> result = new OptimalDispenser().compute(600, snapshot);
        check(result.size() == 1 && result.get(Denomination.N200) == 3,
                "Optimal strategy should use three Rs.200 notes for Rs.600");
    }

    private static void testFailedWithdrawalDoesNotChangeBalanceOrCash() throws Exception {
        ATM atm = new ATM(fullInventory(), new GreedyDispenser());
        Account account = new Account("card-2", 1234, 500, 10_000);
        int cashBefore = atm.getCashAvailable();
        int balanceBefore = account.getBalance();

        expectThrows(InvalidAmountException.class, () -> atm.withdraw(account, 155));
        check(atm.getCashAvailable() == cashBefore, "Cash inventory changed after invalid request");
        check(account.getBalance() == balanceBefore, "Balance changed after invalid request");

        expectThrows(InsufficientFundsException.class, () -> atm.withdraw(account, 1_000));
        check(atm.getCashAvailable() == cashBefore, "Cash inventory changed after insufficient-funds error");
        check(account.getBalance() == balanceBefore, "Balance changed after insufficient-funds error");
        check(account.miniStatement().size() == 2, "Both failed attempts should be recorded");
    }

    private static void testConcurrentWithdrawalsStayConsistent() throws Exception {
        ATM atm = new ATM(fullInventory(), new GreedyDispenser());
        Account account = new Account("card-3", 1234, 3_000, 3_000);
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        try {
            @SuppressWarnings("unchecked")
            Future<?>[] futures = new Future<?>[threadCount];
            for (int i = 0; i < threadCount; i++) {
                futures[i] = executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        atm.withdraw(account, 1_000);
                        successes.incrementAndGet();
                    } catch (ATMException expected) {
                        // The remaining withdrawals should be refused once funds are exhausted.
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(exception);
                    }
                });
            }
            check(ready.await(5, TimeUnit.SECONDS), "Threads did not become ready");
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
            check(successes.get() == 3, "Exactly three Rs.1,000 withdrawals should succeed");
            check(account.getBalance() == 0, "Balance must not become negative");
            check(account.getWithdrawnToday() == 3_000, "Daily total should match successful withdrawals");
            check(account.miniStatement().size() == 5, "All attempts should be recorded");
        } finally {
            executor.shutdownNow();
        }
    }

    private static void testAuthenticationBlocksAfterThreeFailures() throws Exception {
        Account account = new Account("card-4", 1234, 1_000, 1_000);
        for (int i = 0; i < 3; i++) {
            try {
                account.authenticate("card-4", 9999);
                throw new AssertionError("Wrong PIN should have failed");
            } catch (AuthenticationException expected) {
                // Expected for all three attempts.
            }
        }
        check(account.isLocked(), "Card should be blocked after three incorrect PINs");
        expectThrows(AuthenticationException.class, () -> account.authenticate("card-4", 1234));
    }

    private static CashInventory fullInventory() {
        Map<Denomination, Integer> notes = emptyInventoryCounts();
        notes.put(Denomination.N500, 10);
        notes.put(Denomination.N200, 10);
        notes.put(Denomination.N100, 20);
        notes.put(Denomination.N50, 20);
        notes.put(Denomination.N20, 20);
        notes.put(Denomination.N10, 20);
        return new CashInventory(notes);
    }

    private static Map<Denomination, Integer> emptyInventoryCounts() {
        Map<Denomination, Integer> notes = new EnumMap<>(Denomination.class);
        for (Denomination denomination : Denomination.values()) {
            notes.put(denomination, 0);
        }
        return notes;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectThrows(Class<? extends Throwable> expectedType, ThrowingRunnable action)
            throws Exception {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }
            throw new AssertionError("Expected " + expectedType.getSimpleName()
                    + " but got " + actual.getClass().getSimpleName(), actual);
        }
        throw new AssertionError("Expected exception: " + expectedType.getSimpleName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
