package com.mulesoft.services.graphql.internal.wiring;

import com.mulesoft.services.graphql.api.GraphqlQuery;
import com.mulesoft.services.graphql.internal.GraphqlFieldResolver;
import graphql.execution.ExecutionPath;
import graphql.language.FieldDefinition;
import graphql.schema.AsyncDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.transformation.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SourcePublishingWiringFactory implements WiringFactory {
    private HashSet<GraphqlFieldResolver> resolvers = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(SourcePublishingWiringFactory.class);

    @Inject
    private TransformationService transformationService;

    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        logger.debug("Called provides data fetcher for field name: {}", environment.getFieldDefinition().getName());
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataFetcher getDataFetcher(final FieldWiringEnvironment environment) {
        logger.debug("Called get data fetcher for field name: {}", environment.getFieldDefinition().getName());

        return new AsyncDataFetcher(dataFetchingEnvironment -> {
            GraphqlWiringContext context = new GraphqlWiringContext(dataFetchingEnvironment);

            for (GraphqlFieldResolver resolver : resolvers) {
                String match = resolver.getMatch();
                if( match.equalsIgnoreCase("type:"+dataFetchingEnvironment.getFieldType().getName()) ||
                        match.equalsIgnoreCase("path:"+dataFetchingEnvironment.getFieldTypeInfo().getPath()) ) {
                    resolver.handleQuery(context);
                    try {
                        context.awaitForResponse(5,TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    return context.executionResult.orElse(null);
                }
            }
            Object source = context.getDataFetchingEnvironment().getSource();
            if( source != null ) {
                if( source instanceof Map ) {
                    return ((Map) source).get(dataFetchingEnvironment.getFieldDefinition().getName());
                }
            }
            return null;
        });
    }

    public void registerResolver(GraphqlFieldResolver resolver) {
        resolvers.add(resolver);
    }

    public void unregisterResolver(GraphqlFieldResolver resolver) {
        resolvers.remove(resolver);
    }
}
