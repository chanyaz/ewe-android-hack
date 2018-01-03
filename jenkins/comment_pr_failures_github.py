#!/usr/bin/env python2.7

import traceback
from github3 import login

def createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prId, pr_builder_type, error_message, format_type):
    gh = login(token=githubAccessToken)
    repo = gh.repository(githubOrganization, githubRepository)
    issue = repo.issue(prId)
    if error_message != "" and format_type != "":
        error_message = error_message + '\n```'
        format_type = "```" + format_type
    comment_header = "{format_type}\n{pr_builder_type}FeedbackBot:\n\n".format(format_type=format_type, pr_builder_type=pr_builder_type)
    automated_comments_from_pr_builder = [comment for comment in issue.comments() if comment.body[:len(comment_header)] == comment_header]
    try:
        if len(automated_comments_from_pr_builder) > 0:
            #Delete any existing comment added by me!
            automated_comments_from_pr_builder[0].delete()
    except:
        print "Exception encountered while trying to delete existing comment. Stack Trace \n{stack_trace}".format(stack_trace=traceback.format_exc())

    try:
        if error_message != "":
            #Create a new comment if there is an error message to surface up!
            issue.create_comment(comment_header + error_message)
    except:
        print "Exception encountered while trying to create new comment. Stack Trace \n{stack_trace}".format(stack_trace=traceback.format_exc())