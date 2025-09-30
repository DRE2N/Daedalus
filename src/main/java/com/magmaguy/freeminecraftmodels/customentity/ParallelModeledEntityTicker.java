package com.magmaguy.freeminecraftmodels.customentity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Models add a lot of entities, and we want to tick them in parallel.
 * We can do that as they are packet-based and don't have interactions with the world.
 *
 * Partitioning is necessary to avoid contention. This gets quite slow if we have thousands of entities,
 * so we only partition every 10 seconds.
 */
public class ParallelModeledEntityTicker {

    private static final long TICK_PERIOD_MS = 50;
    private static final long REBALANCE_PERIOD_SECONDS = 10;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService workerPool;
    private final int numThreads;

    private ScheduledFuture<?> dispatcherTask;
    private ScheduledFuture<?> rebalanceTask;

    private final ConcurrentLinkedQueue<ModeledEntity> pendingAdditions = new ConcurrentLinkedQueue<>();
    private final List<List<ModeledEntity>> partitions = new ArrayList<>();
    private final AtomicInteger nextPartitionIndex = new AtomicInteger(0);
    private final ReentrantLock rebalanceLock = new ReentrantLock();


    public ParallelModeledEntityTicker(int numThreads) {
        if (numThreads <= 0) throw new IllegalArgumentException("Number of threads must be positive.");
        this.numThreads = numThreads;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Daedalus-Scheduler"));
        this.workerPool = Executors.newFixedThreadPool(numThreads, r -> new Thread(r, "Daedalus-Worker"));

        // Initialize empty partitions
        for (int i = 0; i < numThreads; i++) {
            partitions.add(new CopyOnWriteArrayList<>());
        }
    }

    public void registerNewEntity(ModeledEntity entity) {
        pendingAdditions.offer(entity);
    }

    public void start() {
        if (dispatcherTask != null && !dispatcherTask.isDone()) return;
        this.dispatcherTask = scheduler.scheduleAtFixedRate(this::dispatchWork, 0, TICK_PERIOD_MS, TimeUnit.MILLISECONDS);

        this.rebalanceTask = scheduler.scheduleAtFixedRate(this::rebalancePartitions, REBALANCE_PERIOD_SECONDS, REBALANCE_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void dispatchWork() {
        rebalanceLock.lock();
        try {
            processPendingAdditions();

            for (final List<ModeledEntity> partition : partitions) {
                if (!partition.isEmpty()) {
                    workerPool.submit(() -> partition.forEach(ModeledEntity::tick));
                }
            }
        } finally {
            rebalanceLock.unlock();
        }
    }

    private void processPendingAdditions() {
        ModeledEntity entity;
        while ((entity = pendingAdditions.poll()) != null) {
            int partitionIndex = nextPartitionIndex.getAndIncrement() % numThreads;
            partitions.get(partitionIndex).add(entity);
        }
    }

    private void rebalancePartitions() {
        rebalanceLock.lock();
        try {
            List<ModeledEntity> allEntities = new ArrayList<>(ModeledEntity.getLoadedModeledEntities());

            for (List<ModeledEntity> partition : partitions) {
                partition.clear();
            }

            if (allEntities.isEmpty()) {
                return;
            }

            int index = 0;
            for (ModeledEntity entity : allEntities) {
                partitions.get(index % numThreads).add(entity);
                index++;
            }

            nextPartitionIndex.set(index);

            processPendingAdditions();

        } catch (Exception e) {
            System.err.println("Error during modeled entity rebalance");
            e.printStackTrace();
        } finally {
            rebalanceLock.unlock();
        }
    }

    public void shutdown() {
        if (dispatcherTask != null) dispatcherTask.cancel(false);
        if (rebalanceTask != null) rebalanceTask.cancel(false);

        // Standard graceful shutdown logic
        scheduler.shutdown();
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) workerPool.shutdownNow();
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) scheduler.shutdownNow();
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getPendingCount() {
        return pendingAdditions.size();
    }

    public int getPartitionCount() {
        return partitions.size();
    }

    public int getAveragePartitionSize() {
        return partitions.stream().mapToInt(List::size).sum() / partitions.size();
    }

}