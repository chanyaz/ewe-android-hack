#!/bin/bash

function pretty() {
    TMP=`mktemp /tmp/pretty.XXXXXXXX`
    python -m json.tool "$1" > "$TMP"
    mv "$TMP" "$1"
}

function permissions() {
    chmod ugo-x "$i"
    chmod go-w "$i"
    chmod u+rw "$i"
}

function clearAttrs() {
    xattr -c "$1"
}

find templates -exec xattr -c {} \;
find templates -type f -name "*.json" |
{
    while read i ; do
        permissions "$i"
        pretty "$i"
    done
}

