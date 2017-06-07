#!/usr/bin/env python

import sys
import os
from github3 import login

def reapplyMergeLabels():
	#Bender's Github Token
	githubAccessToken = os.environ['GITHUB_ACCESS_TOKEN']
	gh = login(token=githubAccessToken)
	repo = gh.repository('ExpediaInc', 'ewe-android-eb')

	for prIssue in repo.issues(state='open', labels=['reviewed'], sort='created', direction='asc'):
		prLabels = map(lambda x: str(x), prIssue.labels())

		#All good if already has a 'merge' label
		if 'merge' in prLabels:
			continue

		#Reapply 'merge' label only if 'reviewed' is the only label on the PR
		if len(prLabels) == 1:
			pr = prIssue.pull_request()
			#and all tests have passed on the PR
			if pr.mergeable_state == 'clean':
				print "Re-applying 'merge' label on PR {prNumber}".format(prNumber=prIssue.number)
				prIssue.add_labels('merge')
			else:
				print "Not applying 'merge' label on PR {prNumber} as it is '{prState}'".format(prNumber=prIssue.number, prState=pr.mergeable_state)
		else:
			print "Not applying 'merge' label on PR {prNumber} as 'reviewed' is not the only label on it ({prLabels})".format(prNumber=prIssue.number, prLabels=prLabels)

def removeMergeLabels():
	#Bender's Github Token
	githubAccessToken = os.environ['GITHUB_ACCESS_TOKEN']
	gh = login(token=githubAccessToken)
	repo = gh.repository('ExpediaInc', 'ewe-android-eb')

	for prIssue in repo.issues(state='open', labels=['merge'], sort='created', direction='asc'):
		prLabels = map(lambda x: str(x), prIssue.labels())

		#Ensure that PR is 'reviewed' before we remove 'merge' label from it!
		if not 'reviewed' in prLabels:
			print "Ensuring PR {prNumber} has 'reviewed' label".format(prNumber=prIssue.number)
			prIssue.add_labels('reviewed')
		else:
			print "PR {prNumber} already has 'reviewed' label".format(prNumber=prIssue.number)
		
		#Remove 'merge' label from PR
		print "Removing 'merge' label from PR {prNumber}".format(prNumber=prIssue.number)
		prIssue.remove_label('merge')

def help():
	print
	print "Usage:"
	print "python toggle_merge_labels.py --remove                   to remove  merge labels from PRs"
	print "python toggle_merge_labels.py --reapply                  to reapply merge labels onto PRs"

if __name__ == "__main__":
	if len(sys.argv) == 2:
		if sys.argv[1] == '--remove':
			print "Removing merge labels from PRs..."
			removeMergeLabels()
		elif sys.argv[1] == '--reapply':
			print "Reapplying merge labels onto PRs..."
			reapplyMergeLabels()
		else:
			print "Unrecognized option."
			help()
	else:
		help()
