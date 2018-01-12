package com.mulesoft.services.graphql.internal;

import com.mulesoft.services.graphql.internal.wiring.GraphqlWiringContext;
import com.mulesoft.services.graphql.internal.wiring.SourcePublishingWiringFactory;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(GraphqlRouter.class)
@Sources(GraphqlFieldResolver.class)
public class GraphqlConfiguration implements Startable {

    protected static final Logger logger = LoggerFactory.getLogger(GraphqlConfiguration.class);

    private GraphQL engine;

    @Inject
    private Registry muleRegistry;

    @Parameter
    private String configName;

    @Parameter
    private String schemaLocation;

    private SourcePublishingWiringFactory wiringFactory = new SourcePublishingWiringFactory();

    public String getName() {
        return configName;
    }


    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    @Override
    public void start() throws MuleException {
        logger.info("Starting Graphql configuration with ID: " + getName());
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(retrieveSchema());

        //FlowCallingWiringFactory wiringFactory = new FlowCallingWiringFactory(muleRegistry, getName());

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .wiringFactory(wiringFactory)
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        engine = GraphQL.newGraphQL(graphQLSchema).build();
        logger.info("Started Graphql configuration with ID: " + getName());
    }

    public GraphQL getEngine() {
        return engine;
    }

    private Reader retrieveSchema() {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream((getSchemaLocation())));
    }

    public BlockingQueue<GraphqlWiringContext> registerQueue(String queueName) {
        return wiringFactory.registerQueue(queueName);
    }
}
