package com.mulesoft.services.graphql.internal;

import graphql.language.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphQLSelection {
    private String name;
    private HashMap<String, Object> args;
    private List<GraphQLSelection> subs;

    public GraphQLSelection(Field field) {
        name = field.getName();
        args = new HashMap<>();
        for (Argument argument : field.getArguments()) {
            args.put(argument.getName(), valueToObj(argument.getValue()));
        }
        subs = new ArrayList<>();
        if (field.getSelectionSet() != null && field.getSelectionSet().getSelections() != null) {
            List<Selection> selections = field.getSelectionSet().getSelections();
            for (Selection selection : selections) {
                if( selection instanceof Field ) {
                    subs.add(new GraphQLSelection((Field)selection));
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getArgs() {
        return args;
    }

    public void setArgs(HashMap<String, Object> args) {
        this.args = args;
    }

    public List<GraphQLSelection> getSubs() {
        return subs;
    }

    public void setSubs(List<GraphQLSelection> subs) {
        this.subs = subs;
    }

    public static Object valueToObj(Value value) {
        if (value instanceof StringValue) {
            return ((StringValue) value).getValue();
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue();
        } else if (value instanceof EnumValue) {
            return ((EnumValue) value).getName();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        } else if (value instanceof IntValue) {
            return ((IntValue) value).getValue();
        } else if (value instanceof NullValue) {
            return null;
        } else if (value instanceof ArrayValue) {
            ArrayList<Object> array = new ArrayList<>();
            for (Value v : ((ArrayValue) value).getValues()) {
                array.add(valueToObj(v));
            }
            return array;
        } else {
            return value.toString();
        }
    }
}
