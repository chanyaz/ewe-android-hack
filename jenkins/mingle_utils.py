import re
import sys
import md5
import base64
import requests
import traceback
from HMACAuth import HMACAuth
import xml.etree.ElementTree as ET
from email.utils import formatdate

def httpDateHeader():
	return formatdate(timeval=None, localtime=False, usegmt=True)

def createCard(mingleProjectId, mingleAccessId, mingleAccessSecret, cardName, cardDescription, cardType, cardProperties):
	print("Creating card " + cardName)
	createCardEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingle_project_id}/cards.xml'.format(mingle_project_id=mingleProjectId)
	createCardPostBodyTemplate = "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[name]\"\r\n\r\n{card_name}\r\n" + \
	"-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[description]\"\r\n\r\n{description}\r\n" + \
	"-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[card_type_name]\"\r\n\r\n{card_type}\r\n"

	for key, value in cardProperties.iteritems():
		createCardPostBodyTemplate += "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[properties][][name]\"\r\n\r\n" + key + "\r\n" + \
									  "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[properties][][value]\"\r\n\r\n" + value + "\r\n"
	createCardPostBodyTemplate += "-------------PythonMingleMultipartPost--\r\n\r\n"

	createCardPostBody = createCardPostBodyTemplate.format(card_name=cardName, card_type=cardType, description=cardDescription)

	md5Digest = md5.new()
	md5Digest.update(createCardPostBody)

	try:
		response = requests.post(url=createCardEndpoint, \
			data=createCardPostBody, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Content-Md5': base64.b64encode(md5Digest.digest()), 'Connection':'close', 'Content-Type': 'multipart/form-data; boundary=-----------PythonMingleMultipartPost'})

		print response
		if response.status_code != 201:
			print createCardPostBody
			print response.text
			return -1
		print "Location of card = " + response.headers['Location']
		cardCreatedLink = response.headers['Location']
		cardNumber = re.findall('/(\d+).xml$', cardCreatedLink)[0]
		print "Mingle card Link = https://eiwork.mingle.thoughtworks.com/projects/{mingle_project_id}/cards/{card_number}".format(mingle_project_id=mingleProjectId, card_number=cardNumber)
		return cardNumber
	except:
		print "Exception encountered while creating card '{card_name}'. Stack Trace: \n{stack_trace}".format(card_name=cardName, stack_trace=traceback.format_exc())
		return -1

def getCard(mingleProjectId, mingleAccessId, mingleAccessSecret, cardNumber):
	getCardEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingle_project_id}/cards/{card_number}.xml'.format(mingle_project_id=mingleProjectId, card_number=cardNumber)

	try:
		response = requests.get(url=getCardEndpoint, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Connection':'close'})
		print response.text
	except:
		print "Exception getting card"
		return None

def uploadAttachment(mingleProjectId, mingleAccessId, mingleAccessSecret, cardNumber, fileLocation):

	uploadAttachmentPostEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingle_project_id}/cards/{card_number}/attachments.xml'.format(mingle_project_id=mingleProjectId, card_number=cardNumber)

	try:
		response = requests.post(url=uploadAttachmentPostEndpoint, \
								 auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
								 files={'file': open(fileLocation, 'rb')},\
								 headers={'Date': httpDateHeader(), \
										  })

		return 0 if response.status_code == 201 else -1
	except:
		print "Exception encountered while trying to upload attachment to {card_number} from location {file_location}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, stack_trace=traceback.format_exc(), file_location=fileLocation)
		return -1

def murmurInProject(mingleProjectId, mingleAccessId, mingleAccessSecret, murmur):
	print("Murmuring '{murmur}' in '{mingleProjectId}'.".format(murmur=murmur, mingleProjectId=mingleProjectId))
	murmursPostEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingleProjectId}/murmurs.xml'.format(mingleProjectId=mingleProjectId)

	murmursPostBodyTemplate = "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"murmur[body]\"\r\n\r\n{murmur}\r\n-------------PythonMingleMultipartPost--\r\n\r\n"
	murmursPostBody = murmursPostBodyTemplate.format(murmur=murmur)

	md5Digest = md5.new()
	md5Digest.update(murmursPostBody)

	try:
		response = requests.post(url=murmursPostEndpoint, \
			data=murmursPostBody, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Content-Md5': base64.b64encode(md5Digest.digest()), 'Connection':'close', 'Content-Type': 'multipart/form-data; boundary=-----------PythonMingleMultipartPost'})

		return response.status_code == 200
	except:
		print "Exception encountered while trying to murmur '{murmur}' in '{mingleProjectId}'. Stack Trace: \n{stack_trace}".format(murmur=murmur, mingleProjectId=mingleProjectId, stack_trace=traceback.format_exc())
		return False

def fetchCardProperty(mingleProjectId, mingleAccessId, mingleAccessSecret, cardNumber, propertyName):
	print("Fetching {property_name} for {card_number}.".format(card_number=cardNumber, property_name=propertyName))
	cardDetailsGetEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingleProjectId}/cards/{card_number}.xml'.format(mingleProjectId=mingleProjectId, card_number=cardNumber)

	try:
		response = requests.get(url=cardDetailsGetEndpoint, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Connection':'close'})
	except:
		print "Exception encountered while trying to fetch details for {card_number}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, stack_trace=traceback.format_exc())
		return None

	try:
		xmlRoot = ET.fromstring(response.text)
		for prop in xmlRoot.find("properties").findall('property'):
			propName = prop.find('name').text
			if propName.lower() == propertyName.lower():
				if propertyName.lower() == "assigned":
					return prop.find('value').find('login').text
				#add more property-get handlers here
	except:
		print "Exception encountered while trying to parse fetched card property {property_name} for {card_number}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, property_name=propertyName, stack_trace=traceback.format_exc())

	return None

def updateCardProperty(mingleProjectId, mingleAccessId, mingleAccessSecret, cardNumber, propertyName, propertyValue):
	print("Updating {property_name} to {property_value} for {card_number}.".format(card_number=cardNumber, property_name=propertyName, property_value=propertyValue))
	cardDetailsPutEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingleProjectId}/cards/{card_number}.xml'.format(mingleProjectId=mingleProjectId, card_number=cardNumber)
	
	cardDetailsPutBodyTemplate = "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[properties][][name]\"\r\n\r\n{property_name}\r\n-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"card[properties][][value]\"\r\n\r\n{property_value}\r\n-------------PythonMingleMultipartPost--\r\n\r\n"
	cardDetailsPutBody = cardDetailsPutBodyTemplate.format(property_name=propertyName, property_value=propertyValue)

	md5Digest = md5.new()
	md5Digest.update(cardDetailsPutBody)

	try:
		response = requests.put(url=cardDetailsPutEndpoint, \
			data=cardDetailsPutBody, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Content-Md5': base64.b64encode(md5Digest.digest()), 'Content-Type': 'multipart/form-data; boundary=-----------PythonMingleMultipartPost'})

		return response.status_code == 200
	except:
		print "Exception encountered while trying to update {property_name} to {property_value} for {card_number}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, property_name=propertyName, property_value=propertyValue, stack_trace=traceback.format_exc())
		return False

def fetchCardTransitionId(mingleProjectId, mingleAccessId, mingleAccessSecret, cardNumber, transitionHint):
	print("Fetching transitions for {card_number} and {transition_hint}.".format(card_number=cardNumber, transition_hint=transitionHint))
	cardTransitionsGetEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingleProjectId}/cards/{card_number}/transitions.xml'.format(mingleProjectId=mingleProjectId, card_number=cardNumber)

	try:
		response = requests.get(url=cardTransitionsGetEndpoint, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Connection':'close'})
	except:
		print "Exception encountered while trying to fetch transitions for {card_number} and {transition_hint}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, transition_hint=transitionHint, stack_trace=traceback.format_exc())
		return None

	try:
		xmlRoot = ET.fromstring(response.text)
		for transition in xmlRoot.findall('transition'):
			transitionName = transition.find('name').text
			if transitionHint in transitionName.lower():
				return transition.find('id').text
		print "Could not find transitions for {card_number} and {transition_hint} in the response.".format(card_number=cardNumber, transition_hint=transitionHint)
		return None
	except:
		print "Exception encountered while trying to parse fetched transitions for {card_number} and {transition_hint}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, transition_hint=transitionHint, stack_trace=traceback.format_exc())
		return None

def transitionCard(mingleProjectId, mingleAccessId, mingleAccessSecret, transitionId, cardNumber):
	print("Transitioning {card_number} via {transition_id}.".format(card_number=cardNumber, transition_id=transitionId))
	cardTransitionExecutionsPostEndpoint = 'https://eiwork.mingle-api.thoughtworks.com/api/v2/projects/{mingleProjectId}/transition_executions/{transition_id}.xml'.format(mingleProjectId=mingleProjectId, transition_id=transitionId)
	
	cardTransitionExecutionsPostBodyTemplate = "-------------PythonMingleMultipartPost\r\nContent-Disposition: form-data; name=\"transition_execution[card]\"\r\n\r\n{card_number}\r\n-------------PythonMingleMultipartPost--\r\n\r\n"
	cardTransitionExecutionsPostBody = cardTransitionExecutionsPostBodyTemplate.format(card_number=cardNumber)

	md5Digest = md5.new()
	md5Digest.update(cardTransitionExecutionsPostBody)

	try:
		response = requests.post(url=cardTransitionExecutionsPostEndpoint, \
			data=cardTransitionExecutionsPostBody, \
			auth=HMACAuth(mingleAccessId, mingleAccessSecret), \
			headers={'Date': httpDateHeader(), 'Content-Md5': base64.b64encode(md5Digest.digest()), 'Connection':'close', 'Content-Type': 'multipart/form-data; boundary=-----------PythonMingleMultipartPost'})

		return response.status_code == 200
	except:
		print "Exception encountered while trying to transition '{card_number}' on {transition_id}. Stack Trace: \n{stack_trace}".format(card_number=cardNumber, transition_id=transitionId, stack_trace=traceback.format_exc())
		return False
