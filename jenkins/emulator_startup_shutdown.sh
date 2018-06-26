#!/usr/bin/env bash

if [[ $(basename `pwd`) != "jenkins" ]]; then
    baseDir="jenkins/"
fi

source $baseDir""emulator_shutdown.sh
source $baseDir""emulator_startup.sh
echo `pwd`
