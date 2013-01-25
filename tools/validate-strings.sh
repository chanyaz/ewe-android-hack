#!/bin/bash

if [ "$1" = "" ] ; then
    echo "error: First arg must be path to res folder"
    exit 1
fi

path=$1
options="--color=always -n -H"

echo "Validating xml: no output is good news"
for i in $path/values*/strings.xml ; do
    xmllint --format --noout $i
    grep $options "[^\]&apos;" $i
    grep $options "&amp;amp;" $i
    grep $options "&amp;apos;" $i
    grep $options "\\\\apos;" $i
    grep $options "\"> " $i
    grep $options " </string>" $i
    grep $options " </item>" $i
done

