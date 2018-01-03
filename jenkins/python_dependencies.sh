#!/bin/bash

set -e

if [ ! -d 'virtualenv' ] ; then
    virtualenv -p python2.7 virtualenv
fi

source ./virtualenv/bin/activate

pip install --upgrade "pip"
pip install "github3.py==1.0.0a4"
pip install "slackclient==1.0.6"
pip install "requests==2.9.1"
pip install "requests-oauthlib==0.6.1"
pip install "python-dateutil==2.6.1"
pip install "lxml==4.1.1"

