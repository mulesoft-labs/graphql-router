package com.mulesoft.services.graphql.internal;

import com.mulesoft.services.graphql.api.GraphqlWiringAttributes;
import com.mulesoft.services.graphql.internal.wiring.GraphqlWiringContext;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EmitsResponse
public class GraphqlFieldResolver extends Source<Void, GraphqlWiringAttributes> {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlFieldResolver.class);
    public static final String WIRING_CONTEXT = "wiringContext";

    @Config
    private GraphqlConfiguration config;

    @Parameter
    private String fieldName;

    private boolean listening;

    private Executor executor;

    @Override
    public void onStart(SourceCallback<Void, GraphqlWiringAttributes> sourceCallback) throws MuleException {

        BlockingQueue<GraphqlWiringContext> queue = config.registerQueue(fieldName);
        listening = true;

        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            Thread.currentThread().setName("graphql-field-" + fieldName);

            logger.debug("Listenting for messages in the queue for field {}", fieldName);

            try {
                while (listening) {
                    GraphqlWiringContext wiringContext = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (wiringContext == null) {
                        continue;
                    }
                    logger.debug("Received message for field {}", fieldName);
                    SourceCallbackContext context = sourceCallback.createContext();
                    context.addVariable(WIRING_CONTEXT, wiringContext);


                    //prepare the message
                    DefaultGraphqlWiringAttributes attributes = new DefaultGraphqlWiringAttributes(wiringContext.getDataFetchingEnvironment());

                    //handle it!
                    sourceCallback.handle(Result.<Void,GraphqlWiringAttributes>builder().attributes(attributes).build(), context);
                }
            } catch (InterruptedException ex) {
                logger.error("Error while ");
            }
        });
    }

    @OnSuccess
    public void onSuccess(@Content Object responseBody, SourceCallbackContext ctx) {
        logger.debug("Will send successful response from queue {}", fieldName);
        GraphqlWiringContext wiringContext = ctx.<GraphqlWiringContext>getVariable(WIRING_CONTEXT)
                .orElseThrow(() -> new RuntimeException("Incorrect wiring of flows..."));
        wiringContext.sendResponse(responseBody);
    }

    @OnError
    public void onError(SourceCallbackContext ctx) {
        logger.debug("Got exception while executing flow in queue {}", fieldName);
        logger.error("Exception while executing flow");
        GraphqlWiringContext wiringContext = ctx.<GraphqlWiringContext>getVariable(WIRING_CONTEXT)
                .orElseThrow(() -> new RuntimeException("Incorrect wiring of flows..."));
        wiringContext.sendResponse(null);
    }

    @Override
    public void onStop() {
        listening = false;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
