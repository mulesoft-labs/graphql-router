package com.mulesoft.services.graphql.internal.wiring;

import graphql.language.FieldDefinition;
import graphql.schema.AsyncDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SourcePublishingWiringFactory implements WiringFactory {

    private final Map<String, BlockingQueue<GraphqlWiringContext>> workQueues;

    private static final Logger logger = LoggerFactory.getLogger(SourcePublishingWiringFactory.class);

    public SourcePublishingWiringFactory() {
        workQueues = new ConcurrentHashMap<>();
    }

    public BlockingQueue<GraphqlWiringContext> registerQueue(String queueName) throws RuntimeException {

        //TODO - I may need to synchronize this with regards to the work queues to avoid registering 2 queues for the same endpoint.

        BlockingQueue<GraphqlWiringContext> workQueue = workQueues.get(queueName);
        if (workQueue != null) {
            logger.debug("Queue {} has been already registered, returning it...", queueName);
            return workQueue;
        }

        logger.debug("Registering queue for endpoint {}", queueName);

        //TODO - Harcoding values!!!
        workQueue = new LinkedBlockingDeque<>(1000);

        workQueues.put(queueName, workQueue);

        return workQueue;
    }


    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        logger.debug("Called provides data fetcher for field name: {}", environment.getFieldDefinition().getName());
        return true;
    }

    @Override
    public DataFetcher getDataFetcher(final FieldWiringEnvironment environment) {
        logger.debug("Called get data fetcher for field name: {}", environment.getFieldDefinition().getName());

        return new AsyncDataFetcher(dataFetchingEnvironment -> {
            GraphqlWiringContext context = new GraphqlWiringContext(dataFetchingEnvironment);
            BlockingQueue<GraphqlWiringContext> queue = getListenerQueue(dataFetchingEnvironment.getFieldDefinition().getDefinition());

            if (queue == null) {
                logger.warn("Could not find a work queue for field {} and resorted to the default data fetcher!!", dataFetchingEnvironment.getFieldDefinition().getName());
                return getDefaultDataFetcher(environment).get(dataFetchingEnvironment);
            }

            try {
                //TODO - More harcoding!!
                queue.offer(context, 1, TimeUnit.MINUTES);
                Object result = context.awaitForResponse(1, TimeUnit.MINUTES);
                return result;
            } catch (RuntimeException ex) {
                logger.error("Timeout wiring field", ex);
            } catch (Exception ex) {
                logger.error("Error while wiring field", ex);
            }
            return null;
        });
    }

    private boolean hasListenerQueue(FieldDefinition definition) {
        return getListenerQueue(definition) != null;
    }

    private BlockingQueue<GraphqlWiringContext> getListenerQueue(FieldDefinition definition) {
        //define in a single place what will be used to process, either field name or graph path.
        String fieldName = definition.getName();
        return workQueues.get(fieldName);
    }
}
