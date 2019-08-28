package com.mulesoft.services.graphql;

import org.junit.Assert;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

public class GraphqlRouterFailTestCase extends MuleArtifactFunctionalTestCase {

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile() {
        return "test-mule-fail-config.xml";
    }

    @Test
    public void exerciseEmptyResult() throws Exception {

        try {
            String payloadValue = (String) flowRunner("router")
                    .withPayload(new TypedValue<>("{\"query\": \"{hello}\"}", DataType.JSON_STRING))
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue();
            Assert.fail("Flow didn't fail");
        } catch (MuleException e) {
            // good
        }
    }
}
