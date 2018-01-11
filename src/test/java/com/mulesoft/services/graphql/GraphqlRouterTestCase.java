package com.mulesoft.services.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Ignore;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

import java.io.ByteArrayInputStream;
import java.util.Map;

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

}
