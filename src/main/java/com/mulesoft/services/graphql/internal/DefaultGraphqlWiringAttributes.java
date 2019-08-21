package com.mulesoft.services.graphql.internal;

import com.mulesoft.services.graphql.api.GraphQLRequest;
import graphql.schema.DataFetchingEnvironment;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

public class DefaultGraphqlWiringAttributes implements Serializable, com.mulesoft.services.graphql.api.GraphqlWiringAttributes {
    private final DataFetchingEnvironment wrappedEnvironment;
    private GraphQLRequest request;

    public DefaultGraphqlWiringAttributes(DataFetchingEnvironment wrappedEnvironment, GraphQLRequest request) {
        this.wrappedEnvironment = wrappedEnvironment;
        this.request = request;
    }

    @Override
    public Object getSource() {
        return wrappedEnvironment.getSource();
    }

    @Override
    public Map<String, Object> getArguments() {
        return wrappedEnvironment.getArguments();
    }

    public GraphQLRequest getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultGraphqlWiringAttributes.class.getSimpleName() + "[", "]")
                .add("wrappedEnvironment=" + wrappedEnvironment)
                .add("request=" + request)
                .toString();
    }
}
