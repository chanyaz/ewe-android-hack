#!/usr/bin/env python

import os
import sys
from github3 import login
from pr_utils import moveMingleCardsOnPRRaise, pingPRAuthors

TAG = "on_pr_raised"

def hasPRBeenProcessed(pr):
	return os.path.exists("./processed_prs/" + str(pr.number))

def markPRHasBeenProcessed(pr):
	return open("./processed_prs/" + str(pr.number), 'a').close()

def transitionMingleCards(mingleProject, mingleAccessId, mingleAccessSecret, hipchatAccessToken, pr):
	print "Processing PR {pr_id}...".format(pr_id=pr.number)
	print "Moving Mingle Cards..."
	messageToBePingedOnPRRaise = moveMingleCardsOnPRRaise(mingleProject, mingleAccessId, mingleAccessSecret, pr)
	if messageToBePingedOnPRRaise != "":
		print "Pinging PR Authors..."
		pingPRAuthors(pr, hipchatAccessToken, messageToBePingedOnPRRaise)
	else:
		print "No cards to be moved to PR..."

def nudgeAuthorsTowardsWritingUnitTests(pr):
	uiTestDirectory = '/androidTest/'
	prFiles = [file.filename for file in pr.files()]

	hasUITestChanges = any(uiTestDirectory in file for file in prFiles)

	if hasUITestChanges:
		print "Found changes in androidTest. Creating a comment."
		pr.create_comment("This PR touches some methods in /androidTest/. Can we do a quick check if these can be implemented via Robolectric?")
		pr.issue().add_labels('question')

def processPR(mingleProject, mingleAccessId, mingleAccessSecret, hipchatAccessToken, pr):
    # Moving mingle card has been disabled until some one finds a solution or workaround that can support multiple mingle projects.
	#transitionMingleCards(mingleProject, mingleAccessId, mingleAccessSecret, hipchatAccessToken, pr)
	nudgeAuthorsTowardsWritingUnitTests(pr)

def main():
	githubAccessToken = sys.argv[1]
	hipchatAccessToken = sys.argv[2]
	mingleProject = sys.argv[3]
	mingleAccessId = sys.argv[4]
	mingleAccessSecret = sys.argv[5]
	pullRequestId = int(sys.argv[6])

	if not os.path.isdir("./processed_prs"):
		os.mkdir("./processed_prs")

	gh = login(token=githubAccessToken)
	repo = gh.repository('ExpediaInc', 'ewe-android-eb')
	pr = repo.pull_request(pullRequestId)

	#Act on the PR if it is open and has not been processed!
	if pr.state == 'open' and not hasPRBeenProcessed(pr):
		markPRHasBeenProcessed(pr)
		processPR(mingleProject, mingleAccessId, mingleAccessSecret, hipchatAccessToken, pr)

if __name__ == "__main__":
	main()
