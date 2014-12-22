#!/bin/bash

set -e

source snap/common.sh

gradleww assembleExpediaLatest assembleExpediaLatestTest
gradleww assembleTravelocityLatest
gradleww assembleAirAsiaGoLatest
gradleww assembleVoyagesLatest
