#!/bin/bash

if [ "$1" = "" ] ; then
    echo "error: First arg must be path to res folder"
    exit 1
fi

path=$1
options="--color=always -n -H"

echo "Validating xml: no output is good news"
for i in $path/values*/strings.xml ; do
    # Xml file validity
    xmllint --format --noout $i

    # Mal escaped characters
    grep $options "[^\]&apos;" $i
    grep $options "&amp;amp;" $i
    grep $options "&amp;apos;" $i
    grep $options "\\\\apos;" $i

    # wrong elipsis
    grep $options "[^\.]\.\.\.[^\.]" $i

    # preceding and trailing space - ignore ": "
    grep $options "\"> " $i
    grep $options "[^:] </string>" $i
    grep $options "[^:] </item>" $i

    # check for bunk apos
    grep $options "’" $i

    # check for unescaped html tags
    #grep $options "<a " $i
    #grep $options "<a>" $i
    #grep $options "</a>" $i

    # check for too many spaces
    grep $options ">.*[^.!]  " $i

    # check for duplicate strings
    grep 'string name="[a-zA-Z_]*"' $i | sed 's/^.*string name="\([a-zA-Z_]*\)".*$/\1/' | uniq -d

    # check for unicode non-breaking space
    pcregrep --color=always -n '\xC2\xA0' $i
done

root="../project/src/main/res"
for i in $path/values*/strings.xml ; do
    other=${i/$path/$root}

    #total_lines=`wc -l $other | awk '{ print $1 }'`
    diff_lines=`diff $i $other | wc -l`
    if [ "$diff_lines" -gt "200" ] ; then
        echo $i "too many lines changes" $diff_lines
    fi
done

