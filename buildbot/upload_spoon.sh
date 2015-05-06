#!/bin/bash

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    OUTPUT_TAR="spoon-results-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

    # Archive to the master
    tar czf "$OUTPUT_TAR" "spoon"
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; for i in *.tar.gz ; do echo Extracting "$i" ; tar xzf "$i" ; rm -f "$i" ; done ; /home/buildbot/fixperms.sh'

    # Cleanup locally
    rm -rf "spoon"
    rm -f spoon-results-*.tar.gz
fi

