#!/usr/bin/env bash

export PYTHONIOENCODING=utf-8

if [ ! -d 'virtualenv' ]; then
  virtualenv -p python2.7 virtualenv
fi

source ./virtualenv/bin/activate

pip install --upgrade "pip"

for lib in $@
do
  pip install $lib
done
