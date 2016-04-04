#!/usr/bin/env python
import traceback
from github3 import login

def createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prId, prBuilderType, errorMsg):
    gh = login(token=githubAccessToken)
    repo = gh.repository(githubOrganization, githubRepository)
    issue = repo.issue(prId)
    uitestsPRBuilder = "ui"
    unittestsPRBuilder = "unit"
    uiTestsAutomatedFeedbackCommentSignature = '```java\n' + uitestsPRBuilder + 'FeedbackBot:'
    unitTestsAutomatedFeedbackCommentSignature = '```java\n' + unittestsPRBuilder + 'FeedbackBot:'
    if errorMsg != "":
        errorMsg = errorMsg + '\n```'

    automatedFeedbackCommentSignature = uiTestsAutomatedFeedbackCommentSignature if prBuilderType == uitestsPRBuilder else unitTestsAutomatedFeedbackCommentSignature

    automatedCommentsFromPRBuilder = [comment for comment in issue.comments() if comment.body[:len(automatedFeedbackCommentSignature)] == automatedFeedbackCommentSignature]
    try:
        if errorMsg != "":
            if len(automatedCommentsFromPRBuilder) == 0:
                #No existing comment added by me, so nothing to edit, need to create a new one!
                issue.create_comment(automatedFeedbackCommentSignature + errorMsg)
            else:
                #An existing comment added by me, and an error message to update the PR Author, so edit the existing comment!
                print automatedCommentsFromPRBuilder[0]
                automatedCommentsFromPRBuilder[0].edit(automatedFeedbackCommentSignature + errorMsg)
        else:
            if len(automatedCommentsFromPRBuilder) != 0:
                #An existing comment added by me, but no error message to update the PR Author, so delete the existing comment!
                automatedCommentsFromPRBuilder[0].delete()
    except:
        print "Exception encountered while trying to create, edit or delete a comment. Stack Trace \n{stack_trace}".format(stack_trace=traceback.format_exc())