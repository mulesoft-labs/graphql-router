package com.mulesoft.services.graphql.api;

import graphql.schema.GraphQLType;

import java.util.HashMap;
import java.util.Map;

public class GraphqlQuery {
    private String name;
    private String path;
    private Map<String,Object> arguments = new HashMap<>();
    private Map<String,GraphqlQuery> elements = new HashMap<>();

    public GraphqlQuery() {
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public Map<String, GraphqlQuery> getElements() {
        return elements;
    }
}
