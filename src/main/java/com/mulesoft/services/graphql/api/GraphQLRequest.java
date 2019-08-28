package com.mulesoft.services.graphql.api;

import com.mulesoft.services.graphql.internal.GraphQLSelection;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import java.util.*;

public class GraphQLRequest {
    private String name;
    private String path;
    private String type;
    private Map<String, Object> args;
    private Map<String, GraphQLSelection> selection = new HashMap<>();
    private List<GraphQLSelection> selectionList = new ArrayList<>();

    public GraphQLRequest(DataFetchingEnvironment dataFetchingEnvironment) {
        name = dataFetchingEnvironment.getFieldDefinition().getName();
        path = dataFetchingEnvironment.getFieldTypeInfo().getPath().toString();
        type = dataFetchingEnvironment.getFieldDefinition().getType().getName();
        args = dataFetchingEnvironment.getArguments();
        for (List<Field> fields : dataFetchingEnvironment.getSelectionSet().get().values()) {
            for (Field field : fields) {
                GraphQLSelection sel = new GraphQLSelection(field);
                selection.put(field.getName(), sel);
                selectionList.add(sel);
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

    public List<GraphQLSelection> getSelectionList() {
        return selectionList;
    }

    public void setSelectionList(List<GraphQLSelection> selectionList) {
        this.selectionList = selectionList;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GraphQLRequest.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("path='" + path + "'")
                .add("type='" + type + "'")
                .add("args=" + args)
                .add("selection=" + selection)
                .toString();
    }
}
