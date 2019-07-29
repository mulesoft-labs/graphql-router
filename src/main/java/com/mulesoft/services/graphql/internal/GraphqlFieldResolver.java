package com.mulesoft.services.graphql.internal;

import com.mulesoft.services.graphql.api.GraphqlWiringAttributes;
import com.mulesoft.services.graphql.internal.wiring.GraphqlWiringContext;
import graphql.schema.DataFetchingEnvironment;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
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
@MetadataScope(outputResolver = RouterOuputTypeResolver.class)
public class GraphqlFieldResolver extends Source<GraphQLRequest, GraphqlWiringAttributes> {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlFieldResolver.class);
    public static final String WIRING_CONTEXT = "wiringContext";

    @Config
    private GraphqlConfiguration config;

    @Parameter
    private String match;

    private boolean listening;

    private Executor executor;
    private SourceCallback<GraphQLRequest, GraphqlWiringAttributes> sourceCallback;

    @Override
    public void onStart(SourceCallback<GraphQLRequest, GraphqlWiringAttributes> sourceCallback) throws MuleException {
        config.registerResolver(this);
        this.sourceCallback = sourceCallback;
    }

    @OnSuccess
    public void onSuccess(@Content Object responseBody, SourceCallbackContext ctx) {
        GraphqlWiringContext wiringContext = ctx.<GraphqlWiringContext>getVariable(WIRING_CONTEXT)
                .orElseThrow(() -> new RuntimeException("Incorrect wiring of flows..."));
        wiringContext.sendResponse(responseBody);
    }

    @OnError
    public void onError(SourceCallbackContext ctx) {
        logger.error("Exception while executing flow");
        GraphqlWiringContext wiringContext = ctx.<GraphqlWiringContext>getVariable(WIRING_CONTEXT)
                .orElseThrow(() -> new RuntimeException("Incorrect wiring of flows..."));
        wiringContext.sendResponse(null);
    }

    @Override
    public void onStop() {
        config.unregisterResolver(this);
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void handleQuery(GraphqlWiringContext wiringContext) {
        SourceCallbackContext context = sourceCallback.createContext();
        context.addVariable(WIRING_CONTEXT, wiringContext);

        DefaultGraphqlWiringAttributes attributes = new DefaultGraphqlWiringAttributes(wiringContext.getDataFetchingEnvironment());

        //handle it!
        sourceCallback.handle(Result.<GraphQLRequest,GraphqlWiringAttributes>builder()
                .output(new GraphQLRequest(wiringContext.getDataFetchingEnvironment()))
                .attributes(attributes).build(), context);
    }
}
