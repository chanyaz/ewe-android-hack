#!/bin/bash

echo "Installing git hooks..."

echo " pre-push"
cp tools/githooks/pre-push.sh .git/hooks/pre-push
chmod a+x .git/hooks/pre-push

echo "Done"
