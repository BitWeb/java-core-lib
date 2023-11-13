#!/bin/bash
set -o errexit

echo -e "repoUser=$1" >> "$3"
echo -e "repoPassword=$2" >> "$3"
