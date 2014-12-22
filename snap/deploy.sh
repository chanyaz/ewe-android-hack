#!/bin/bash

set -e

# Skip non master branch
test -z "${SNAP_PULL_REQUEST_NUMBER}" || exit 0

source snap/common.sh

echo """
SNAP_PIPELINE_COUNTER: ${SNAP_PIPELINE_COUNTER}
SNAP_BRANCH: ${SNAP_BRANCH}

$(git log ${SNAP_COMMIT}^..${SNAP_COMMIT})
""" > "changelog.txt"

gradleww crashlyticsUploadDistributionTravelocityLatest
gradleww crashlyticsUploadDistributionAirAsiaGoLatest
gradleww crashlyticsUploadDistributionVoyagesLatest
