package com.mulesoft.services.graphql.internal;

import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphQLRequest {
    private String name;
    private String path;
    private String type;
    private Map<String,Object> args;
    private Map<String,GraphQLSelection> selection = new HashMap<>();

    public GraphQLRequest(DataFetchingEnvironment dataFetchingEnvironment) {
        name = dataFetchingEnvironment.getFieldDefinition().getName();
        path = dataFetchingEnvironment.getFieldTypeInfo().getPath().toString();
        type = dataFetchingEnvironment.getFieldDefinition().getType().getName();
        args = dataFetchingEnvironment.getArguments();
        for (List<Field> fields : dataFetchingEnvironment.getSelectionSet().get().values()) {
            for (Field field : fields) {
                selection.put(field.getName(),new GraphQLSelection(field));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public Map<String, GraphQLSelection> getSelection() {
        return selection;
    }

    public void setSelection(Map<String, GraphQLSelection> selection) {
        this.selection = selection;
    }
}
