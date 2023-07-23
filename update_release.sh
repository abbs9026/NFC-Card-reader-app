#!/bin/bash
set -e

# Get versionName and versionCore
v_name="$1"
v_code=$(echo "$v_name" | sed -e 's/\.//g' -e 's/^[0]*//')

# Check version name is correct
if echo $v_name | grep -Eq '^([0-9]+)\.([0-9]+)\.([0-9]+)$'; then
    echo "::notice::Version $v_name is correct format"
else
    echo "::error::Version $v_name has wrong format"
    exit 1
fi

# Update version in project files
#   Library
sed -i -e "s/version = .*/version = \"$v_name\"/g" library/build.gradle
#   App
sed -i -e "s/versionName .*/versionName \"$v_name\"/g" app/build.gradle
sed -i -e "s/versionCode .*/versionCode $v_code/g" app/build.gradle

echo "::notice::Project updated with version name ($v_name) and version code ($v_code)"
