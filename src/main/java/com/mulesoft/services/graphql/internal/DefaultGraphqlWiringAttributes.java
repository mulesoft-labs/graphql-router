package com.mulesoft.services.graphql.internal;

import com.mulesoft.services.graphql.api.GraphQLRequest;
import graphql.schema.DataFetchingEnvironment;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

public class DefaultGraphqlWiringAttributes implements Serializable, com.mulesoft.services.graphql.api.GraphqlWiringAttributes {
    private final DataFetchingEnvironment env;
    private GraphQLRequest request;

    public DefaultGraphqlWiringAttributes(DataFetchingEnvironment env, GraphQLRequest request) {
        this.env = env;
        this.request = request;
    }

    public DataFetchingEnvironment getEnv() {
        return env;
    }

    @Override
    public Object getSource() {
        return env.getSource();
    }

    @Override
    public Map<String, Object> getArguments() {
        return env.getArguments();
    }

    public GraphQLRequest getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultGraphqlWiringAttributes.class.getSimpleName() + "[", "]")
                .add("env=" + env)
                .add("request=" + request)
                .toString();
    }
}
