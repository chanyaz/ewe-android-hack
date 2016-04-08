#!/usr/bin/env bash

# install rules that only check MissingTranslation
cp project/lint-translations.xml project/lint.xml

# run lint
./gradlew lintExpediaDebug

# translate XML report into HTML report
python jenkins/build_missing_translations_report.py project/build/outputs/lint-results-expediaDebug.xml

# revert lint config file
git checkout -- .
