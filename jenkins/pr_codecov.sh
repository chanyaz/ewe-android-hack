#!/usr/bin/env bash

./gradlew jacocoTestReport
curl -s https://codecov.io/bash | bash
