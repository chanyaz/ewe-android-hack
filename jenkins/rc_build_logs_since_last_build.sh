#!/usr/bin/env bash

git fetch --tags
# Make release notes (logs) since last build
branch_name=$1
release_version=$2
output_file=$3
tag_name=last-rc-build-for-$branch_name-tag

if git rev-parse -q --verify $tag_name;
then
	git log $tag_name..HEAD > $output_file # output log of commits since last build
	git tag --delete $tag_name
else
	echo "No tag found. Must be the first build" > $output_file
fi

git tag $tag_name
git push -f origin $tag_name
