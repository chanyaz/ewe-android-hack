#!/bin/bash

set -e

number=$1
shift 1

for i in $(seq $number) ; do
  time ./buildbot/androidtest.sh $*
done

