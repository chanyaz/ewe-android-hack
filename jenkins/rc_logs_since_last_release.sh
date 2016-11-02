#!/usr/bin/env bash

git fetch --tags
last_release_tag=$1
output_file=$2

if git rev-parse -q --verify $last_release_tag;
then
	echo -e "Commits since release with tag: $last_release_tag\n\n" > $output_file
	git log $last_release_tag..HEAD >> $output_file
else
	echo "Cannot find release tag: $last_release_tag" > $output_file
	exit 1
fi

