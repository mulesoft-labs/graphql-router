package com.mulesoft.services.graphql;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GraphqlRouterTestCase extends MuleArtifactFunctionalTestCase {

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile() {
        return "test-mule-config.xml";
    }

    @Test
    public void exerciseRouter() throws Exception {

        String payloadValue = (String) flowRunner("router")
                .withPayload(new TypedValue<>("{\"query\": \"{hello}\"}", DataType.JSON_STRING))
                .run()
                .getMessage()
                .getPayload()
                .getValue();

        assertThat(payloadValue, is("{\n" +
                "  \"data\": {\n" +
                "    \"hello\": \"world\"\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void exerciseRouterPerson() throws Exception {
        String payloadValue = (String) flowRunner("router")
                .withPayload(new TypedValue<>("{\"query\": \"{person(foo : \\\"bar\\\") {name, car { model }, active(query: \\\"true\\\"') }}\"}", DataType.JSON_STRING))
                .run()
                .getMessage()
                .getPayload()
                .getValue();

        assertThat(payloadValue.replace(" ", "").replace("\n", ""), is("{\"data\":{\"person\":{\"name\":\"johnsmith\",\"car\":{\"model\":\"honda\"},\"active\":null}}}"));
    }
}
