#!/bin/bash

set -o errexit

./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository --info

curl -X POST -H "Authorization: Bearer $(printf \"${ORG_GRADLE_PROJECT_mavenCentralUsername}:${ORG_GRADLE_PROJECT_mavenCentralPassword}\" | base64)" https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/ee.bitweb
