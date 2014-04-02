#!/usr/bin/env python2.7

import json
import sys
import os

# Files provided on the command line expect to be buildbot
# generated files from a JSONPropertiesDownload build step

for arg in sys.argv[1:]:
    f = open(arg)
    data = json.load(f)
    if 'sourcestamp' in data and 'changes' in data['sourcestamp']:
        revs = [change['revision'] for change in data['sourcestamp']['changes']]
        for rev in revs:
            os.system('git log -1 %s' % (rev))
