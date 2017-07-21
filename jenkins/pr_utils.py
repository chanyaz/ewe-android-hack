import re
import sys
import traceback
import subprocess
from mingle_utils import murmurInProject, fetchCardTransitionId, transitionCard, fetchCardProperty, updateCardProperty
from slack_send_message import send_private_slack_message

mingleCardRegex = r'^[0-9]{4}$'

def prUrl(pr):
	return "https://github.com/ExpediaInc/{github_repo}/pull/{pr_number}".format(pr_number=pr.number, github_repo=pr.repository[1])

def scanPartsAndAppendCardNumbersToList(branchNameParts, partIndex, storiesList):
	global mingleCardRegex
	while partIndex < len(branchNameParts):
		branchNamePart = branchNameParts[partIndex]
		if re.search(mingleCardRegex, branchNamePart):
			storiesList.append(int(branchNamePart))
		else:
			break
		partIndex = partIndex + 1
	return partIndex

def mingleCardNumbersCategorizedByNewStatusHints(pr):
	global mingleCardRegex
	cardsWithNoMingleHint, cardsToBeMovedToRFT, cardsToBeMovedToSignOff, cardsToStayAsWIP = [], [], [], []

	branchName = pr.head.ref
	branchNameParts = re.split('-|/', branchName)

	partIndex = 0
	while partIndex < len(branchNameParts):
		branchNamePart = branchNameParts[partIndex].lower()
		partIndex = partIndex + 1

		#handle mingle hint WIP - any 4 digit hyphenated number list are WIP cards
		if branchNamePart in ["wip"]:
			partIndex = scanPartsAndAppendCardNumbersToList(branchNameParts, partIndex, cardsToStayAsWIP)
		#handle mingle hint RFT - any 4 digit hyphenated number list are RFT cards
		elif branchNamePart in ["rft", "fix", "fixes", "fixed"]:
			partIndex = scanPartsAndAppendCardNumbersToList(branchNameParts, partIndex, cardsToBeMovedToRFT)
		#handle mingle hint SignOff - any 4 digit hyphenated number list are SignOff cards
		elif branchNamePart in ["signoff"]:
			partIndex = scanPartsAndAppendCardNumbersToList(branchNameParts, partIndex, cardsToBeMovedToSignOff)
		#handle mingle hint None
		elif re.search(mingleCardRegex, branchNamePart):
			cardsWithNoMingleHint.append(int(branchNamePart))

	return (cardsWithNoMingleHint, cardsToStayAsWIP, cardsToBeMovedToRFT, cardsToBeMovedToSignOff)

def mingleCardNumbers(pr):
	cardsWithNoMingleHint, cardsToStayAsWIP, cardsToBeMovedToRFT, cardsToBeMovedToSignOff = mingleCardNumbersCategorizedByNewStatusHints(pr)
	return cardsWithNoMingleHint + cardsToStayAsWIP + cardsToBeMovedToRFT + cardsToBeMovedToSignOff

def mingleCardLink(mingleProject, cardNumber):
	mingleCardLinkTemplate = "https://eiwork.mingle.thoughtworks.com/projects/{mingle_project}/cards/{card_number}"
	return mingleCardLinkTemplate.format(card_number=str(cardNumber), mingle_project=mingleProject)

def transitionStatusMessage(transitionStatus, newState):
	return ("Moved to {new_state}." if transitionStatus == True else "Oops... Failed in moving to {new_state}.").format(new_state=newState)

def transitionCardForPR(mingleProject, mingleAccessId, mingleAccessSecret, cardNumber, transitionHint, pr):
	currentlyAssignedTo = fetchCardProperty(mingleProject, mingleAccessId, mingleAccessSecret, cardNumber, "Assigned")
	print "{cardNumber} is currently assigned to {currentlyAssignedTo}".format(cardNumber=cardNumber, currentlyAssignedTo=currentlyAssignedTo)

	transitionId = fetchCardTransitionId(mingleProject, mingleAccessId, mingleAccessSecret, cardNumber, transitionHint)
	transitionStatus = False if transitionId == None else transitionCard(mingleProject, mingleAccessId, mingleAccessSecret, transitionId, cardNumber)

	#if transitioning to Pull Request Stage, murmur in the card and ensure the 'Assigned' field stays intact
	if "to pr" in transitionHint.lower():
		murmurStatus = murmurInProject(mingleProject, mingleAccessId, mingleAccessSecret, "Raised PR for #{card_number} - {pr_url}".format(card_number=cardNumber, pr_url=prUrl(pr)))
		if currentlyAssignedTo != None:
			updateCardAssignedStatus = updateCardProperty(mingleProject, mingleAccessId, mingleAccessSecret, cardNumber, "Assigned", currentlyAssignedTo)
		
	return transitionStatus

def transitionCardsForPR(mingleProject, mingleAccessId, mingleAccessSecret, cardNumbers, transitionHint, pr):
	cardsTransitionSuccessStatus = {}

	for cardNumber in cardNumbers:
		transitionStatus = transitionCardForPR(mingleProject, mingleAccessId, mingleAccessSecret, cardNumber, transitionHint, pr)
		cardsTransitionSuccessStatus[cardNumber] = transitionStatus

	return cardsTransitionSuccessStatus

def moveMingleCardsPostPRMerge(mingleProject, mingleAccessId, mingleAccessSecret, pr):
	cardsWithNoMingleHint, cardsToStayAsWIP, cardsToBeMovedToRFT, cardsToBeMovedToSignOff = mingleCardNumbersCategorizedByNewStatusHints(pr)

	rftCardsTransitionStatus = transitionCardsForPR(mingleProject, mingleAccessId, mingleAccessSecret, cardsToBeMovedToRFT, "to ready for testing", pr)
	signoffCardsTransitionStatus = transitionCardsForPR(mingleProject, mingleAccessId, mingleAccessSecret, cardsToBeMovedToSignOff, "to sign-off", pr)

	mergeMessage = ["Merged '{pr_title}' {pr_url}".format(pr_title=pr.title, pr_url=prUrl(pr))]
	cardStatusUpdatesMessages = ["{card_link} - {transition_status_message}".format(card_link=mingleCardLink(mingleProject, cardNumber), transition_status_message=transitionStatusMessage(rftCardsTransitionStatus[cardNumber], "RFT")) for cardNumber in cardsToBeMovedToRFT] + \
		["{card_link} - {transition_status_message}".format(card_link=mingleCardLink(mingleProject, cardNumber), transition_status_message=transitionStatusMessage(signoffCardsTransitionStatus[cardNumber], "Sign-Off")) for cardNumber in cardsToBeMovedToSignOff] + \
		["{card_link} - Status Not Changed".format(card_link=mingleCardLink(mingleProject, cardNumber)) for cardNumber in (cardsWithNoMingleHint + cardsToStayAsWIP)]

	messageListToBePingedUponPRMerge = mergeMessage + (["Mingle cards linked with this PR"] if len(cardStatusUpdatesMessages) > 0 else []) + cardStatusUpdatesMessages
	if len(cardsWithNoMingleHint) > 0:
		messageListToBePingedUponPRMerge.append("\nConsider using Hints in branch names for Automated Movement of Cards on PR Merge")

	messageToBePingedUponPRMerge = "\n".join(messageListToBePingedUponPRMerge)
	print messageToBePingedUponPRMerge
	return messageToBePingedUponPRMerge

def moveMingleCardsOnPRRaise(mingleProject, mingleAccessId, mingleAccessSecret, pr):
	cardsWithNoMingleHint, cardsToStayAsWIP, cardsToBeMovedToRFT, cardsToBeMovedToSignOff = mingleCardNumbersCategorizedByNewStatusHints(pr)
	allCardsToBeMovedToPR = cardsWithNoMingleHint + cardsToBeMovedToRFT + cardsToBeMovedToSignOff

	if len(allCardsToBeMovedToPR) == 0:
		return ""

	cardsTransitionStatus = transitionCardsForPR(mingleProject, mingleAccessId, mingleAccessSecret, allCardsToBeMovedToPR, "to pr", pr)

	mergeMessage = ["Thanks for raising '{pr_title}' {pr_url}".format(pr_title=pr.title, pr_url=prUrl(pr))]
	cardStatusUpdatesMessages = ["{card_link} - {transition_status_message}".format(card_link=mingleCardLink(mingleProject, cardNumber), transition_status_message=transitionStatusMessage(cardsTransitionStatus[cardNumber], "PR")) for cardNumber in allCardsToBeMovedToPR]

	messageListToBePingedOnPRRaise = mergeMessage + (["Mingle cards linked with this PR"] if len(cardStatusUpdatesMessages) > 0 else []) + cardStatusUpdatesMessages

	messageToBePingedOnPRRaise = "\n".join(messageListToBePingedOnPRRaise)
	print messageToBePingedOnPRRaise
	return messageToBePingedOnPRRaise

def authorsAndCommittersMailIds(pr):
	authorsAndCommittersMailIds = list(set([prCommit.commit.author['email'] for prCommit in pr.commits()] + [prCommit.commit.committer['email'] for prCommit in pr.commits()]))
	return [mailId for mailId in authorsAndCommittersMailIds if mailId != "mobiataauto@gmail.com"]

def pingPRAuthors(pr, slack_access_token, message):
	prAuthorsAndCommittersMailIds = authorsAndCommittersMailIds(pr)
	for mailId in prAuthorsAndCommittersMailIds:
		if not mailId.endswith("@expedia.com"):
			print "PR Author Email is not an Expedia Mail Id - {pr_url}, {mail_id}".format(pr_url=prUrl(pr), mail_id=mailId)
			continue
		try:
			send_private_slack_message(slack_access_token, mailId, message)
		except:
			print "Exception encountered while trying to ping {mail_id} for {pr_url}. Stack Trace: \n{stack_trace}".format(pr_url=prUrl(pr), mail_id=mailId, stack_trace=traceback.format_exc())
