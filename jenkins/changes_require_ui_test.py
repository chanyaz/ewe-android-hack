#!/usr/bin/env python

import sys
from github3 import login
import re

def main():
    githubAccessToken = sys.argv[1]
    pullRequestId = int(sys.argv[2])

    gh = login(token=githubAccessToken)
    repo = gh.repository('ExpediaInc', 'ewe-android-eb')
    pr = repo.pull_request(pullRequestId)
    prFiles = [file.filename for file in pr.files()]

    whitelistedDirectoriesRegex = '/Features/|fastlane/'

    hasNonWhitelistedFiles = False

    for file in prFiles:
        if not re.search(whitelistedDirectoriesRegex, file):
            hasNonWhitelistedFiles = True
            break
            
    if hasNonWhitelistedFiles:
        print "yes"
        return 0
    else:
        print "no"
        return 1


if __name__ == "__main__":
    sys.exit(main())