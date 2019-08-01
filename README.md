# GraphQL mule router

## Overview

## Usage

In order to use this router, you should first add the router to your HTTP flow (it's ok to use APIKit before it in order 
to implement policies).

Please note the router by default expects the graphql query to be wrapped in a json structure, and the returned data is wrapped with a "data" element.

So if you want it to handle the request as a pure graphql payload, and return a standard graphql response, use the following:

```
<graphql:router doc:name="Route to graphql handling flow" config-ref="Graphql_Config" 
payload='#[output application/java --- { "query": payload as String }]' 
targetValue="#[output application/json --- payload.data]"/>
```

You will then need to create flows to generate the data to be returned. Each flow should use the "graphql field resolver"
connector. This connector then needs to be configured to specify what part of the graph it will handle.

This is done by setting the "Match" value.

The match value can match based on graphql type by using 'type:'.

For example if your graphql schema contains

```
type Person {
	name: String,
}
```

you can use `type:Person` to match all Person objects. ie:

```xml
<graphql:graphql-field-resolver match="type:Person" config-ref="Graphql_Config"/>
```

Alternatively you can match by the path within the graph hierarchy by using the "path:" prefix. For example
to match `/person/car` use:

```xml
<graphql:graphql-field-resolver match="path:/person/car" config-ref="Graphql_Config"/>
```

Note: support for regex will be added shortly to the path handling

## Publish to exchange

In order to publish this connector to your organization's exchange, you will need:

Add your exchange credentials to your [maven settings.xml](https://maven.apache.org/settings.html). For example (note you can pick any id for the server, it just needs to match what you set in REPO_ID in the config file below)

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">
  ...
  <servers>
    <server>
      <id>my-exchange-repo</id>
      <username>my_login</username>
      <password>my_password</password>
    </server>
  </servers>
  ...
</settings>
```

Please note that if your anypoint organization uses federated authentication it's a bit more tricky (see https://docs.mulesoft.com/exchange/to-publish-assets-maven for more extensive details], you will need to authenticate with your browser, and go to the following page https://anypoint.mulesoft.com/accounts/api/profile to find the access_token and put in your settings.xml as in below example (please note that token expires after a short while, so you'll have to repeat the step)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>my-exchange-repo</id>
      <username>~~~Token~~~</username>
      <password>ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```
 
add a file named `publish.cfg`, which needs to contain:

`REPO_ID`: This needs to match the server id in your settings.xml that contains your exchange credentials or access token.
`ORG_ID`: Exchange organization / business group id.
`REPO_ID`: The server id corresponding to where you're added your credentials.
`VERSION`: The version for the connector to be published as

ie:

```bash
REPO_ID=my-exchange-repo-id
ORG_ID=b7ddb4e8-cc87-430d-a5a0-fdsamfk3823
VERSION=1.0.0
```

Then execute the bash script: `deploy.sh` (on windows that means you might need to install cygwin or similar)
