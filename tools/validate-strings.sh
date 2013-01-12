#!/bin/bash

if [ "$1" = "" ] ; then
    echo "error: First arg must be path to res folder"
    exit 1
fi

path=$1

echo "Validating xml: no output is good news"
for i in $path/values*/strings.xml ; do
    xmllint --format --noout $i
    grep --color=always -n -H "[^\]&apos;" $i
    grep --color=always -n -H "&amp;amp;" $i
    grep --color=always -n -H "&amp;apos;" $i
    grep --color=always -n -H "\\\\apos;" $i
done

