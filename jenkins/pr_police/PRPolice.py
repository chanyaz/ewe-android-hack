#!/usr/bin/env python

import os
import sys
import subprocess
from github3 import login
from PullRequest import *
from ClauseProcessor import *
from Issue import *
import traceback

class PRPolice:
        def __init__(self):
                pass
        	
        def police(self, githubToken, prId):
                github = login(token=githubToken)
                repo = github.repository('ExpediaInc', 'ewe-android-eb')
                pullRequest = self.parsePullRequest(repo, prId)
                issues = self.executeClausesAgainstPullRequest(pullRequest)
                self.displayIssues(pullRequest)
                self.deleteCommentsInPRForIssuesFound(pullRequest)
                self.injectCommentsInPRForIssuesFound(pullRequest)
                return len(issues)

        def parsePullRequest(self, repo, prId):
                pullRequest = PullRequest(repo, prId)
                pullRequest.scanPullRequest()
                return pullRequest

        def executeClausesAgainstPullRequest(self, pullRequest):
                allIssueList = []
                clauseProcessor = ClauseProcessor()
                issues = clauseProcessor.runClauses(pullRequest)
                pullRequest.addIssueList(issues)
                return issues

        def isCommentByPRPolice(self, comment):
                return comment.user.login == 'ewe-mergebot'

        def deleteCommentsInPRForIssuesFound(self, pullRequest):
                for comment in pullRequest.pr.review_comments():
                        if self.isCommentByPRPolice(comment):
                                try:
                                        comment.delete()
                                        print "Deleted an existing Review Comment - %s" % (comment.body)
                                except:
                                        print "Exception encountered while trying to delete existing comment - %s" % (traceback.format_exc())

        def injectCommentsInPRForIssuesFound(self, pullRequest):
                for issue in pullRequest.issueList:
                        try:
                                print "%s, %s, %s, %d" % (issue.message, issue.fileCommitId, issue.filename, issue.codelineNumber)
                                reviewComment = pullRequest.pr.create_review_comment(issue.message, issue.fileCommitId, issue.filename, issue.codelineNumber)
                        except:
                                print "Exception encountered while trying to create comment - %s" % (traceback.format_exc())

        def displayIssues(self, pullRequest):
                for issue in pullRequest.issueList:
                        print("File: %s\nLine: %d: \'%s\'\nIssue: %s\n\n" %(issue.filename, issue.codelineNumber, issue.codeLineContent, issue.message ))

def main():
        prPolice = PRPolice()
        issuesCount = prPolice.police(sys.argv[1], sys.argv[2])
        return issuesCount

if __name__ == "__main__":
	sys.exit(main())
