#!/bin/bash

if [ "$1" = "" ] ; then
    echo "error: First arg must be path to res folder"
    exit 1
fi

path=$1

# Labels for sed fix-point detection loop
start_loop=":repeat; "
end_loop="; trepeat"

# RE's to find and fix
good_apos="\\\\\&apos;"
fix_comment="$start_loop s/\(<!--.*\)’/\1'/ $end_loop"
fix_string="$start_loop s/\(<string.*\)’/\1$good_apos/ $end_loop"
fix_item="$start_loop s/\(<item.*\)’/\1$good_apos/ $end_loop"

for i in $path/values*/strings.xml ; do
    echo $i
    gsed -i "$fix_comment" $i
    gsed -i "$fix_string" $i
    gsed -i "$fix_item" $i
    gsed -i "s/^\t/    /" $i
    gsed -i 's/\\\\/\\/g' $i
done

# TESTING
#echo "<!-- Can’t Sav’e Traveler -->" | gsed "$fix_comment"
#echo "<!-- Can’t Sav’e Traveler -->" | gsed "$fix_string"
#echo "<string name=\"test\">’’’’</string>" | gsed "$fix_string"
#echo "<item name=\"test\">’’’’</string>" | gsed "$fix_item"
