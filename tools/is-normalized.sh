#!/bin/bash

if [ $# -eq "0" ] ; then
    ARGS=`eval echo "../{lib\/Utils,project}/res/values*/{strings,donottranslate}.xml"`
else
    ARGS=$*
fi

for i in $ARGS ; do
    echo $i

    # The cache is a 2x speedup but may not work on different systems so I'll leave this here
    #./charlint.pl -f UnicodeData.txt -q -c $i > /dev/null
    ./charlint.pl -s CharlintCache.data -q -c $i > /dev/null
done
