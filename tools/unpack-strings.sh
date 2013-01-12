#!/bin/bash

silent() {
    $* 2>&1 > /dev/null
}

if [ -z "$1" ] ; then
    echo "This program takes a zip archive of a loc dump"
fi

archive=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

silent pushd `dirname $archive`

rm -r strings

mkdir -p strings
echo "Unpack dump"
silent unzip -d strings $archive || exit 1

echo "Unpack localizations"
silent pushd strings
for i in *.zip ; do
    thedir=${i/.zip/}
    mkdir -p $thedir ; mv $i $thedir/
    silent unzip -d $thedir $thedir/$i
    rm $thedir/$i
    silent pushd $thedir
    for xmlfile in *.xml */*.xml ; do
        mkdir -p project
        mv $xmlfile project/strings.xml
    done
    silent popd
done
silent popd
silent popd

echo "Import strings"
rm -r newstrs
mkdir -p newstrs
python $DIR/strings-import.py -d strings -o newstrs
