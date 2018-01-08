#!/bin/bash

if [ "$1" = "" ] ; then
    echo "error: First arg must be path to res folder"
    exit 1
fi

path=$1
srcPath=$2
options="-n -H"

# Mal escaped characters
searchRegex[0]="[^\]&apos;"
searchRegex[1]="&amp;amp;"
searchRegex[2]="&amp;apos;"
searchRegex[3]="\\\\apos;"

# wrong elipsis
searchRegex[4]="[^\.]\.\.\.[^\.]"

# preceding and trailing space - ignore ": "
searchRegex[5]="\"> "
searchRegex[6]="[^:] </string>"
searchRegex[7]="[^:] </item>"

# check for bunk apos
searchRegex[8]="’"
searchRegex[9]="<string.*[^\]'"
searchRegex[10]="<item.*[^\]'"

# check for too many spaces
searchRegex[11]=">.*[^.!]  "

# check for unescaped html tags
#searchRegex[12]="<a "
#searchRegex[13]="<a>"
#searchRegex[14]="</a>"

# We choose to not exit at the first error so that we can see all the issues in the console log.
exitStatus=0
for i in $path/values*/strings.xml ; do
    # Xml file validity
    xmllint --format --noout $i

    for j in "${searchRegex[@]}" ; do
        if grep $options "$j" $i
            then
              echo "Error found:" $j
              exitStatus=1
        fi
    done

    # check for duplicate strings
    duplicates=$(grep 'string name="[a-zA-Z_]*"' $i | sed 's/^.*string name="\([a-zA-Z_]*\)".*$/\1/' | uniq -d)
    wordCount=$(wc -w <<< "$duplicates")
    if [ $wordCount -gt 0 ]
        then
            echo $i " Found " $wordCount " duplicate(s):" $duplicates
            exitStatus=1
    fi

    # check for unicode non-breaking space
    if pcregrep -n '\xC2\xA0' $i
        then
            echo "unicode non-breaking space found"
            exitStatus=1
    fi

done

root="./$srcPath/res"
for i in $path/values*/strings.xml ; do
    other=${i/$path/$root}

    diff_lines=`diff $i $other | grep "^>" | wc -l`
    if [ "$diff_lines" -gt "200" ] ; then
        echo $i "too many lines changes" $diff_lines
    fi
done
exit $exitStatus

