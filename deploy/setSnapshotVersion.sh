#!/bin/bash
set -o errexit

if [[ "$OSTYPE" == "darwin"* ]]; then
  echo "Not meant to be used on Mac OS!"
  exit 1
fi

CURRENT=$(./deploy/getVersion.sh)

NEW_VERSION=$(./deploy/getVersion.sh)-SNAPSHOT

echo "Change version from $CURRENT to $NEW_VERSION"

sed -i "s/version '$CURRENT'/version '$NEW_VERSION'/" build.gradle >> build.gradle
