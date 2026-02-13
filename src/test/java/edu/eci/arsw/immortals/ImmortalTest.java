package edu.eci.arsw.immortals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the Highlander Simulator.
 * Validates synchronization, health invariants, and deadlock prevention.
 */
public class ImmortalTest {

    private ImmortalManager manager;
    private final int INITIAL_COUNT = 10;
    private final int INITIAL_HEALTH = 100;
    private final int DAMAGE = 10;

    @BeforeEach
    public void setup() {
        manager = new ImmortalManager(INITIAL_COUNT, "ordered", INITIAL_HEALTH, DAMAGE);
    }

    @Test
    public void shouldMaintainHealthInvariant() throws InterruptedException {
        long expectedHealth = (long) INITIAL_COUNT * INITIAL_HEALTH;

        manager.start();
        Thread.sleep(2000);
        manager.pause();
        assertEquals(expectedHealth, manager.totalHealth(),
                "The invariant failed: Total health changed during simulation.");

        manager.stop();
    }

    @Test
    public void shouldStopThreadsWhenPaused() throws InterruptedException {
        manager.start();
        Thread.sleep(500);
        manager.pause();

        long battlesAtPause = manager.scoreBoard().totalFights();
        Thread.sleep(1000);
        assertEquals(battlesAtPause, manager.scoreBoard().totalFights(),
                "Pause failed: Immortals continued fighting after pause signal.");

        manager.stop();
    }

    @Test
    public void shouldNotDeadlockInOrderedMode() throws InterruptedException {
        manager = new ImmortalManager(100, "ordered", 100, 10);
        manager.start();

        Thread.sleep(2000);
        long firstSnapshot = manager.scoreBoard().totalFights();
        assertTrue(firstSnapshot > 0, "No fights were recorded. Possible early deadlock.");

        Thread.sleep(500);
        long secondSnapshot = manager.scoreBoard().totalFights();
        assertTrue(secondSnapshot > firstSnapshot,
                "The system froze. Deadlock detected in Ordered mode.");

        manager.stop();
    }

    @Test
    public void shouldPruneDeadAndMaintainHistoricalCount() throws InterruptedException {
        manager = new ImmortalManager(10, "ordered", 20, 20);
        manager.start();

        Thread.sleep(2000);
        manager.pause();
        manager.pruneDead();
        int totalAccountedFor = manager.aliveCount() + manager.deadCount();

        assertEquals(INITIAL_COUNT, totalAccountedFor,
                "Population management failed: Lost track of immortals after pruning.");

        manager.stop();
    }

    @Test
    public void shouldReachEquilibriumWithOneSurvivor() throws InterruptedException {
        manager = new ImmortalManager(5, "ordered", 100, 100);
        manager.start();

        Thread.sleep(3000);
        manager.pause();

        int alive = manager.aliveCount();
        long totalHealth = manager.totalHealth();
        assertEquals(500, totalHealth, "Health leaked during mass elimination.");
        assertTrue(alive >= 1, "All immortals died; at least one should remain with all health.");

        manager.stop();
    }

    @Test
    public void shouldHandleMassiveConcurrency() throws InterruptedException {
        int massiveCount = 500;
        manager = new ImmortalManager(massiveCount, "ordered", 100, 5);
        manager.start();

        Thread.sleep(3000);
        manager.pause();
        assertEquals((long) massiveCount * 100, manager.totalHealth(),
                "Invariant violated under high thread contention.");

        assertTrue(manager.scoreBoard().totalFights() > 1000,
                "Virtual threads are not performing enough work.");

        manager.stop();
    }

    @Test
    public void shouldBeRobustAgainstFrequentPruning() throws InterruptedException {
        manager = new ImmortalManager(20, "ordered", 50, 50);
        manager.start();

        for(int i = 0; i < 5; i++) {
            Thread.sleep(500);
            manager.pause();
            manager.pruneDead();
            manager.resume();
        }

        manager.pause();
        int totalAccounted = manager.aliveCount() + manager.deadCount();

        assertEquals(20, totalAccounted,
                "Frequent pruning caused loss of historical data or population drift.");

        manager.stop();
    }
}
