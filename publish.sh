#!/bin/bash

set -e

if [[ -f publish.cfg ]]; then source publish.cfg; fi

if [[ -z ${ORG_ID} ]]; then echo "ORG_ID not defined"; exit 1; fi
if [[ -z ${REPO_ID} ]]; then echo "REPO_ID not defined"; exit 1; fi
if [[ -z ${VERSION} ]]; then echo "VERSION not defined"; exit 1; fi

mvn versions:set -DnewVersion=${VERSION}
sed -e "s/com.mulesoft.services/${ORG_ID}/" pom.xml >pom.xml.updated
mv pom.xml.updated pom.xml
mvn -B clean deploy -DaltDeploymentRepository=${REPO_ID}::default::https://maven.anypoint.mulesoft.com/api/v1/organizations/${ORG_ID}/maven
mvn versions:revert
