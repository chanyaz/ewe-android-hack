#!/usr/bin/env bash
#set -Eeuxo pipefail

unzipPath=$1
brand=$2
handbackFileLocation=$3
srcPath=$4

echo "Step 1: Unpacking handback file"
unzip $handbackFileLocation -d $unzipPath

echo "Step 2: Validating strings handback"
./tools/validate-strings.sh $unzipPath $srcPath/$brand

echo "Step 3: Fixing strings handback"
./tools/fix-strings.sh $unzipPath

echo "Step 4: Validating strings handback"
./tools/validate-strings.sh $unzipPath $srcPath/$brand

if [ $? != 0 ]; then
    echo "Unable to fix strings"
    exit 1
fi

if [ -d "$unzipPath/values-vi" ]; then
    echo 'Step 5: Normalizing VI strings'
    cd tools
    ./normalize-it.sh ../$unzipPath/values-vi/strings.xml foo.xml
    mv foo.xml ../$unzipPath/values-vi/strings.xml
    cd ..
    echo 'Step 6: Validating strings handback'
    ./tools/validate-strings.sh $unzipPath $srcPath/$brand
    if [ $? != 0 ]; then
        exit 1
    fi
fi

cd $unzipPath
for i in values-*/strings.xml ; do cp $i ../../$srcPath/$brand/res/$i ; done

cd ../..
git add $srcPath/$brand/res
rm -rf $unzipPath tools/foo.xml

echo "Done fixing strings!"
