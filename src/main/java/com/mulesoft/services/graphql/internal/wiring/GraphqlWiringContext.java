package com.mulesoft.services.graphql.internal.wiring;

import graphql.schema.DataFetchingEnvironment;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GraphqlWiringContext {
    private final DataFetchingEnvironment dataFetchingEnvironment;
    private final CountDownLatch syncLatch;

    public Optional<Object> executionResult;

    public GraphqlWiringContext(DataFetchingEnvironment dataFetchingEnvironment) {
        this.dataFetchingEnvironment = dataFetchingEnvironment;
        syncLatch = new CountDownLatch(1);
    }

    public Object awaitForResponse(long timeout, TimeUnit unit) throws InterruptedException {
        if (syncLatch.await(timeout, unit)) {
            return executionResult.orElse(null);
        }
        throw new RuntimeException("Timed out while awaiting for response.");
    }

    public synchronized void sendResponse(Object value) {
        if (executionResult != null) {
            throw new IllegalStateException("Wiring context allows only one use!");
        }
        executionResult = Optional.of(value);
        syncLatch.countDown();
    }

    public DataFetchingEnvironment getDataFetchingEnvironment() {
        return dataFetchingEnvironment;
    }
}
