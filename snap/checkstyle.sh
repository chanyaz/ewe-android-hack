#!/bin/bash

set -e

source snap/common.sh

gradleww "checkstyle"
