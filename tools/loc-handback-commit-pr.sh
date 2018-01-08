#!/usr/bin/env bash

brand=$1
handbackFileLocation=$2
baseBranch=$3
mingleCardNumber=$4

brandNameInMessage=$brand
handBackDate=`date +"%b_%d_%Y"`

rm -rf templocHandback
mkdir templocHandback
stringsZippedFile=templocHandback/strings-$brand-$handBackDate.zip
zip $stringsZippedFile $handbackFileLocation/*.zip

if [[ "$brandNameInMessage" == "main" ]]
then
    brandNameInMessage="expedia"
fi

gitCommitMessage="Localization handback - $handBackDate - $brandNameInMessage"
gitBranchName="s/rft-$mingleCardNumber-$brandNameInMessage-localization-handback-$handBackDate-$BUILD_NUMBER"
echo "gitCommitMessage "$gitCommitMessage
echo "gitBranchName "$gitBranchName

source tools/setup_python_env.sh "github3.py==1.0.0.a4" slackclient "lxml==3.5.0" python-dateutil
python ./tools/loc_handback_create_pr_update_mingle.py $brand "$gitBranchName" "$gitCommitMessage" "$baseBranch" $mingleCardNumber "$PWD/$stringsZippedFile"
if [ $? != 0 ]; then
    echo "Loc handback failed."
    exit 1
else
    echo "Finished Loc handback successfully."
fi
