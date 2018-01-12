package com.mulesoft.services.graphql.api;

import java.util.Map;

public interface GraphqlWiringAttributes {
    /**
     * Returns the source object that originated this mapping.
     * @return
     */
    Object getSource();

    /**
     * Returns the arguments passed to the query.
     * @return
     */
    Map<String, Object> getArguments();
}
