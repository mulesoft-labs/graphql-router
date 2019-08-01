package com.mulesoft.services.graphql.internal.wiring;

import com.mulesoft.services.graphql.internal.GraphqlFieldResolver;
import graphql.schema.AsyncDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.mule.runtime.api.transformation.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SourcePublishingWiringFactory implements WiringFactory {
    private HashSet<GraphqlFieldResolver> resolvers = new HashSet<>();
    private HashMap<String, Pattern> patternCache = new HashMap<>();

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
                boolean matched = false;
                if (match.toLowerCase().startsWith("path:")) {
                    String patternStr = match.substring(5);
                    Pattern pattern = patternCache.get(patternStr);
                    if (pattern == null) {
                        pattern = Pattern.compile(patternStr);
                        patternCache.put(patternStr, pattern);
                    }
                    if (pattern.matcher(dataFetchingEnvironment.getFieldTypeInfo().getPath().toString()).matches()) {
                        matched = true;
                    }
                } else if (match.equalsIgnoreCase("type:" + dataFetchingEnvironment.getFieldType().getName())) {
                    matched = true;
                }
                if (matched) {
                    resolver.handleQuery(context);
                    try {
                        context.awaitForResponse(5, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    return context.executionResult.orElse(null);
                }
            }
            Object source = context.getDataFetchingEnvironment().getSource();
            if (source != null) {
                if (source instanceof Map) {
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
