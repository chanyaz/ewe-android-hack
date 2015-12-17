#!/bin/bash

if [ ! -d "lib/mocked/templates" ] ; then
    echo "Usage: run from the root of the project"
    exit 1
fi

# We have to remove the template args because ${foo} is not a valid integer
function isValid() {
    cat "$1" | sed -e 's/\${[a-zA-Z]*}/1111/'| python -m json.tool 2> /dev/null > /dev/null
}

function printError() {
    echo "${1}: can't validate"
}

find lib/mocked/templates -type f -name "*.json" |
{
    ERROR=0
    while read i ; do
        isValid "$i"
        if [ $? -ne 0 ] ; then
            printError "$i"
            ERROR=1
        fi
    done

    exit $ERROR
}

