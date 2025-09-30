package com.magmaguy.freeminecraftmodels.customentity;

public class ModeledEntitiesClock {

    public static final int THREAD_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    public static ParallelModeledEntityTicker ticker = null;

    private ModeledEntitiesClock() {
    }

    public static void start() {
        if (ticker != null) {
            return;
        }
        System.out.println("Starting Daedalus with " + THREAD_COUNT + " worker threads.");
        ticker = new ParallelModeledEntityTicker(THREAD_COUNT);

        for (ModeledEntity entity : ModeledEntity.getLoadedModeledEntities()) {
            ticker.registerNewEntity(entity);
        }

        ticker.start();
    }

    public static void register(ModeledEntity entity) {
        if (ticker != null) {
            ticker.registerNewEntity(entity);
        } else {
            System.err.println("Warning: ModeledEntity registered before clock has started.");
        }
    }

    public static void shutdown() {
        if (ticker != null) {
            System.out.println("Shutting down Daedalus ticking...");
            ticker.shutdown();
            ticker = null;
        }
    }
}