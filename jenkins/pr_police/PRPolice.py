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
                self.clauseProcessor = ClauseProcessor()
        	
        def police(self, githubToken, prId):
                github = login(token=githubToken)
                repo = github.repository('ExpediaInc', 'ewe-android-eb')
                pullRequest = PullRequest(repo, prId)
                filepathList = pullRequest.filesInPullRequest()
                if not self.anyFileRelevantForAnyClause(filepathList):
                        print "All safe! No Clause wants to scan any file in the PR - %s." % (pullRequest.prId)
                        return

                # TODO: cache data from responses so that we don't have to limit PR police to running only on small PRs
                if not pullRequest.isOkayToProcess(5):
                        pullRequest.addIssueList([Issue(None, None, 0, None, "Warning: PR is too big for PR Police to process")])
                        return 0
                else:
                        pullRequest.scanPullRequest()
                        self.executeClausesAgainstPullRequest(pullRequest)
                if len(pullRequest.issueList) > 0:
                        ghIssue = repo.issue(prId)
                        self.displayIssues(pullRequest)
                        self.deleteCommentsInPRForIssuesFound(pullRequest, ghIssue)
                        self.injectCommentsInPRForIssuesFound(pullRequest, ghIssue)
                else:
                        print "All safe! No issues in the PR - %s." % (prId)

                return len([issue for issue in pullRequest.issueList if issue.issueType==IssueType.error ])

        def anyFileRelevantForAnyClause(self, filepathList):
                return self.clauseProcessor.anyFileRelevantForAnyClause(filepathList)

        def executeClausesAgainstPullRequest(self, pullRequest):
                allIssueList = self.clauseProcessor.runClauses(pullRequest)
                pullRequest.addIssueList(allIssueList)

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
                for comment in ghIssue.comments():
                        if self.isCommentByPRPolice(comment):
                                try:
                                        comment.delete()
                                        print "Deleted an existing Issue Comment - %s" % (comment.body)
                                except:
                                        print "Exception encounted while trying to delete existing comment - %s" % (traceback.format_exc())

        def injectCommentsInPRForIssuesFound(self, pullRequest, ghIssue):
                for issue in pullRequest.issueList:
                        fileCommitId = pullRequest.ensureCommitIdOfFileIsFilledIn(issue.filename)
                        try:
                                print "%s, %s, %s, %d" % (issue.message, fileCommitId, issue.filename, issue.linePositionInDiff)
                                if issue.filename is None:
                                        ghIssue.create_comment(issue.message)
                                else:
                                        pullRequest.pr.create_review_comment(issue.message, fileCommitId, issue.filename, issue.linePositionInDiff)
                        except:
                                print "Exception encountered while trying to create comment - %s" % (traceback.format_exc())

        def displayIssues(self, pullRequest):
                for issue in pullRequest.issueList:
                        print("File: %s\nLine: %d: \'%s\'\nIssue: %s\n\n" %(issue.filename, issue.linePositionInDiff, issue.codeLineContent, issue.message))

def main():
        issuesCount = 0
        githubToken = sys.argv[1]
        prId = sys.argv[2]
        issuesCount = PRPolice().police(githubToken, prId)
        return issuesCount

if __name__ == "__main__":
        sys.exit(main())
