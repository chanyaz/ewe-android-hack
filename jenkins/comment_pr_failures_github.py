#!/usr/bin/env python
import traceback
from github3 import login

def createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prId, prBuilderType, errorMsg, formatType):
    gh = login(token=githubAccessToken)
    repo = gh.repository(githubOrganization, githubRepository)
    issue = repo.issue(prId)
    if errorMsg != "" and formatType != "":
        errorMsg = errorMsg + '\n```'
        formatType = "```"+formatType
    automatedFeedbackCommentSignature = "{formatType}\n{prBuilderType}FeedbackBot:\n\n".format(formatType=formatType, prBuilderType=prBuilderType)
    automatedCommentsFromPRBuilder = [comment for comment in issue.comments() if comment.body[:len(automatedFeedbackCommentSignature)] == automatedFeedbackCommentSignature]
    try:
        if len(automatedCommentsFromPRBuilder) > 0:
            #Delete any existing comment added by me!
            automatedCommentsFromPRBuilder[0].delete()
    except:
        print "Exception encountered while trying to delete existing comment. Stack Trace \n{stack_trace}".format(stack_trace=traceback.format_exc())

    try:
        if errorMsg != "":
            #Create a new comment if there is an error message to surface up!
            issue.create_comment(automatedFeedbackCommentSignature + errorMsg)
    except:
        print "Exception encountered while trying to create new comment. Stack Trace \n{stack_trace}".format(stack_trace=traceback.format_exc())