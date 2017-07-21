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
MINGLE_PROJECT=eb_ad_app
MINGLE_ACCESS_ID=mingler
MINGLE_ACCESS_SECRET="+94zjsneYF6iwS1lqdLdKmvAyx0ilt8o1RuV71fKU+E="

./on_pr_raised.py ${GITHUB_ACCESS_TOKEN} ${SLACK_ACCESS_TOKEN} ${MINGLE_PROJECT} ${MINGLE_ACCESS_ID} ${MINGLE_ACCESS_SECRET} $ghprbPullId
