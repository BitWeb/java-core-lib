#!/bin/bash

set -o errexit

function prop {
    ./gradlew properties | grep "${1}" |cut -d':' -f2
}

echo $(prop 'version')
