package com.mulesoft.services.graphql.internal;

import graphql.language.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

public class GraphQLSelection {
    private String name;
    private HashMap<String, Object> args;
    private HashMap<String,GraphQLSelection> childrens = new HashMap<>();

    public GraphQLSelection(Field field) {
        name = field.getName();
        args = new HashMap<>();
        for (Argument argument : field.getArguments()) {
            args.put(argument.getName(), valueToObj(argument.getValue()));
        }
        if (field.getSelectionSet() != null && field.getSelectionSet().getSelections() != null) {
            List<Selection> selections = field.getSelectionSet().getSelections();
            for (Selection selection : selections) {
                if( selection instanceof Field ) {
                    Field selField = (Field) selection;
                    childrens.put(selField.getName(),new GraphQLSelection(selField));
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

    public HashMap<String, GraphQLSelection> getChildrens() {
        return childrens;
    }

    public void setChildrens(HashMap<String, GraphQLSelection> childrens) {
        this.childrens = childrens;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", GraphQLSelection.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("args=" + args)
                .add("childrens=" + childrens)
                .toString();
    }
}
