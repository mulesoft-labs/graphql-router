package com.mulesoft.services.graphql.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Extension(name = "graphql")
@Configurations(GraphqlConfiguration.class)
public class GraphqlExtension {

}
