#!/bin/bash

set -e

if [ ! -d 'virtualenv' ] ; then
    virtualenv -p python2.7 virtualenv
fi

source ./virtualenv/bin/activate

pip install --upgrade "pip"
pip install "github3.py==1.0.0a4"
pip install "hypchat==0.21"
pip install "requests==2.9.1"
pip install "requests-oauthlib==0.6.1"
pip install "lxml==3.5.0"

#Bender's Github Token
GITHUB_TOKEN=7d400f5e78f24dbd24ee60814358aa0ab0cd8a76
#TODO - update to Mingler's Hipchat Token
HIPCHAT_TOKEN=3htGpj4sE9XxUToWvWCWWmISA3op2U1roRufVjpQ
MINGLE_PROJECT=eb_ad_app
#TODO - update to Mingler's Mingle Token
MINGLE_ACCESS_ID=nberi
MINGLE_ACCESS_SECRET="AumtaY8gL/psMpuTOV7E/xLmFKbFVvTxGh9MAgTCk0s="

./on_pr_raised.py ${GITHUB_TOKEN} ${HIPCHAT_TOKEN} ${MINGLE_PROJECT} ${MINGLE_ACCESS_ID} ${MINGLE_ACCESS_SECRET} $ghprbPullId
