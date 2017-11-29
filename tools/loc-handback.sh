#!/usr/bin/env bash

unzipPath=$1
brand=$2
handbackFileLocation=$3
baseBranch=$4
mingleCardNumber=$5

echo "Unpacking handback file"
unzip $handbackFileLocation -d $unzipPath

echo "Validating strings handback"
./tools/validate-strings.sh $unzipPath $brand

echo "Fixing strings handback"
./tools/fix-strings.sh $unzipPath

echo "Validating strings handback"
./tools/validate-strings.sh $unzipPath $brand

if [ $? != 0 ]; then
    echo "Unable to fix strings"
    exit 1
fi

if [ -d "$unzipPath/values-vi" ]; then
    echo 'Normalizing strings'
    cd tools
    ./normalize-it.sh ../$unzipPath/values-vi/strings.xml foo.xml
    mv foo.xml ../$unzipPath/values-vi/strings.xml
    cd ..
    ./tools/validate-strings.sh $unzipPath $brand
    if [ $? != 0 ]; then
        exit 1
    fi
fi

cd $unzipPath
for i in values-*/strings.xml ; do cp $i ../../project/src/$brand/res/$i ; done

cd ../..
rm -rf $unzipPath tools/foo.xml

brandNameInMessage=$brand
handBackDate=`date +"%b_%d_%Y"`

if [[ "$brandNameInMessage" == "main" ]]
then
    brandNameInMessage="expedia"
fi

gitCommitMessage="Localization handback - $handBackDate - $brandNameInMessage"
gitBranchName="s/rft-$mingleCardNumber-$brandNameInMessage-localization-handback-$handBackDate-$BUILD_NUMBER"
echo "gitCommitMessage "$gitCommitMessage
echo "gitBranchName "$gitBranchName

source tools/setup_python_env.sh "github3.py==1.0.0.a4" slackclient "lxml==3.5.0" python-dateutil
python ./tools/loc_handback_create_pr_update_mingle.py $brand "$gitBranchName" "$gitCommitMessage" "$baseBranch" $mingleCardNumber "$PWD/$handbackFileLocation"
if [ $? != 0 ]; then
    echo "Loc handback failed."
    exit 1
else
    echo "Finished Loc handback successfully."
fi