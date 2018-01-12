package com.mulesoft.services.graphql.internal;

import graphql.schema.DataFetchingEnvironment;

import java.io.Serializable;
import java.util.Map;

public class DefaultGraphqlWiringAttributes implements Serializable, com.mulesoft.services.graphql.api.GraphqlWiringAttributes {

    private final DataFetchingEnvironment wrappedEnvironment;

    public DefaultGraphqlWiringAttributes(DataFetchingEnvironment wrappedEnvironment) {
        this.wrappedEnvironment = wrappedEnvironment;
    }

    @Override
    public Object getSource() {
        return wrappedEnvironment.getSource();
    }

    @Override
    public Map<String, Object> getArguments() {
        return wrappedEnvironment.getArguments();
    }

}
