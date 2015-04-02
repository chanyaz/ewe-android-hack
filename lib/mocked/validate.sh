#!/bin/bash

# We have to remove the template args because ${foo} is not a valid integer
function isValid() {
    cat "$1" | sed -e 's/\${[a-zA-Z]*}/1111/'| python -m json.tool 2> /dev/null > /dev/null
}

function printError() {
    echo "${1}: can't validate"
}

find templates -type f -name "*.json" |
{
    while read i ; do
        isValid "$i"
        if [ $? -ne 0 ] ; then
            printError "$i"
        fi
    done
}

