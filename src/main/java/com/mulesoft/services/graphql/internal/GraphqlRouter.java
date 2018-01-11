package com.mulesoft.services.graphql.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class GraphqlRouter {


    @Inject
    private ExpressionManager expressionManager;

    private static final Logger logger = LoggerFactory.getLogger(TransformationService.class);

    /**
     * Graphql Router
     * @param config the config.
     * @param payload the mule message to route
     * @return
     */
    @OutputResolver(output = RouterOuputTypeResolver.class)
    @MediaType("application/json")
    public Result<String, Map> router(@Config GraphqlConfiguration config, Map<String, Object> payload) {

        String query = (String) payload.get("query");
        String operation = (String) payload.get("operationName");

        ExecutionInput input  = ExecutionInput.newExecutionInput()
            .query(query)
            .operationName(operation)
            .build();


        ExecutionResult executionResult = config.getEngine().execute(input);

        Map<String, Object> result = executionResult.toSpecification();

        BindingContext ctx = BindingContext.builder()
                .addBinding("payload", new TypedValue(result, DataType.fromType(result.getClass())))
                .build();

        return Result.<String, Map>builder().attributes(new HashMap<String, String>())
                .mediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON)
                .output(expressionManager.evaluate("payload", DataType.JSON_STRING, ctx).getValue().toString())
                .build();
    }

}
