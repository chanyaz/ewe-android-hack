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

    # Fix tab indentation
    gsed -i "s/^\t/    /" $i
    # Fix unneccesary indentation
    gsed -i "s/^ \+$//" $i

    # Fix too many backslash escapes
    gsed -i 's/\\\\/\\/g' $i

    # Fix bad elipsis
    gsed -i 's/\([^\.]\)\.\.\.\([^\.]\)/\1…\2/g' $i

    # Fix bad trailing space - ignore " : "
    gsed -i 's/\([^:]\)\s*<\/string>/\1<\/string>/' $i
    gsed -i 's/\([^:]\)\s*<\/item>/\1<\/item>/' $i

    # Fix bad preceding space
    gsed -i 's/<string \(name="[a-zA-Z1-9_]*"\)>\s*/<string \1>/' $i
    gsed -i 's/<item \(quantity="[a-zA-Z1-9_]*"\)>\s*/<item \1>/' $i

    # Fix multiple spaces
    gsed -i 's/\(>.*[^.!]\)  /\1 /' $i
done

