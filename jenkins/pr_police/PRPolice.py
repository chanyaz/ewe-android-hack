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
                ghIssue = repo.issue(prId)
                prTooBig = len(pullRequest.files) == 0
                if prTooBig:
                        pullRequest.addIssueList([Issue(None, None, 0, None, "Warning: PR is too big for PR Police to process")])
                else:
                        issues = self.executeClausesAgainstPullRequest(pullRequest)
                self.displayIssues(pullRequest)
                self.deleteCommentsInPRForIssuesFound(pullRequest, ghIssue)
                self.injectCommentsInPRForIssuesFound(pullRequest, ghIssue)
                if prTooBig:
                        return 0
                else:
                        return len(issues)

        def parsePullRequest(self, repo, prId):
                pullRequest = PullRequest(repo, prId)
                # TODO: cache data from responses so that we don't have to limit PR police to running only on small PRs
                if pullRequest.isOkayToProcess(5):
                        pullRequest.scanPullRequest()
                return pullRequest

        def executeClausesAgainstPullRequest(self, pullRequest):
                clauseProcessor = ClauseProcessor()
                issues = clauseProcessor.runClauses(pullRequest)
                pullRequest.addIssueList(issues)
                return issues

        def isCommentByPRPolice(self, comment):
                return comment.user.login == 'ewe-mergebot'

        def deleteCommentsInPRForIssuesFound(self, pullRequest, ghIssue):
                for comment in pullRequest.pr.review_comments():
                        if self.isCommentByPRPolice(comment):
                                try:
                                        comment.delete()
                                        print "Deleted an existing Review Comment - %s" % (comment.body)
                                except:
                                        print "Exception encountered while trying to delete existing comment - %s" % (traceback.format_exc())
                for comment in ghIssue.iter_comments():
                        if self.isCommentByPRPolice(comment):
                                try:
                                        comment.delete()
                                        print "Deleted an existing Issue Comment - %s" % (comment.body)
                                except:
                                        print "Exception encounted while trying to delete existing comment - %s" % (traceback.format_exc())

        def injectCommentsInPRForIssuesFound(self, pullRequest, ghIssue):
                for issue in pullRequest.issueList:
                        try:
                                print "%s, %s, %s, %d" % (issue.message, issue.fileCommitId, issue.filename, issue.codelineNumber)
                                if issue.filename is None:
                                        ghIssue.create_comment(issue.message)
                                else:
                                        pullRequest.pr.create_review_comment(issue.message, issue.fileCommitId, issue.filename, issue.codelineNumber)
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
