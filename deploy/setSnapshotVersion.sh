#!/bin/bash
set -o errexit

BUILD_NUMBER=$1
CURRENT=$(./deploy/getVersion.sh)

NEW_VERSION=$(./deploy/getVersion.sh)-SNAPSHOT-$BUILD_NUMBER

echo "Change version from $CURRENT to $NEW_VERSION"

sed -i "" "s/version '$CURRENT'/version '$NEW_VERSION'/" build.gradle >> build.gradle
