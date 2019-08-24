package com.mulesoft.services.graphql.internal.wiring;

import com.mulesoft.services.graphql.internal.GraphqlFieldResolver;
import graphql.schema.AsyncDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.mule.runtime.api.message.Error;
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
        return new AsyncDataFetcher(dataFetchingEnvironment -> {
            GraphqlWiringContext context = new GraphqlWiringContext(dataFetchingEnvironment);
            String pathStr = dataFetchingEnvironment.getFieldTypeInfo().getPath().toString();
            logger.debug("Matching graphql request {} to flow matcher: {}", pathStr, environment.getFieldDefinition().getName());
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
                    logger.debug("Matcher is a path, creating pattern and matching {} against {}", pathStr, patternStr);
                    if (pattern.matcher(pathStr).matches()) {
                        logger.debug("Successfully match {} against {}", pathStr, patternStr);
                        matched = true;
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Didn't match {} against {}", pathStr, patternStr);
                    }
                } else if (match.equalsIgnoreCase("type:" + dataFetchingEnvironment.getFieldType().getName())) {
                    logger.debug("matched type {}", dataFetchingEnvironment.getFieldType().getName());
                    matched = true;
                }
                if (matched) {
                    logger.debug("Successfully matched {} against {}", pathStr, match);
                    resolver.handleQuery(context);
                    try {
                        context.awaitForResponse(5, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    Error error = context.getError();
                    if (error != null) {
                        throw new RuntimeException(error.getErrorMessage() != null ? error.getErrorMessage().toString() : error.toString(), error.getCause());
                    }
                    return context.executionResult.orElse(null);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Failed to match {} against {}", pathStr, match);
                }
            }
            logger.debug("No match found, checking source for pre-loaded data");
            Object source = context.getDataFetchingEnvironment().getSource();
            if (source != null) {
                logger.debug("Found pre-loaded source data");
                if (source instanceof Map) {
                    Object preloadedData = ((Map) source).get(dataFetchingEnvironment.getFieldDefinition().getName());
                    logger.debug("Preloaded data=" + preloadedData);
                    return preloadedData;
                } else {
                    logger.error("graphql preloaded source data isn't a map: " + source.getClass() + " for path " + pathStr);
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
