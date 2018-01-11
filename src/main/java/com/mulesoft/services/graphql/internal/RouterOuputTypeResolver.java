package com.mulesoft.services.graphql.internal;

import org.mule.metadata.api.annotation.TypeAnnotation;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.visitor.MetadataTypeVisitor;
import org.mule.metadata.java.api.JavaTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.AttributesTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public class RouterOuputTypeResolver implements OutputTypeResolver {
    @Override
    public MetadataType getOutputType(MetadataContext metadataContext, Object o) throws MetadataResolvingException, ConnectionException {
        return BaseTypeBuilder.create(MetadataFormat.JSON)
                .anyType()
                .build();
    }

    @Override
    public String getCategoryName() {
        return "gqlresult";
    }
}
