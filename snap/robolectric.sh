#!/bin/bash

set -e

source snap/common.sh

gradleww "clean" ":robolectric:test"
