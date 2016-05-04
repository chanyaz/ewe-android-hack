import requests
from HMACAuth import HMACAuth
from email.utils import formatdate
import md5, base64
import traceback
import re

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

		print "Location of card = " + response.headers['Location']
		cardCreatedLink = response.headers['Location']
		cardNumber = re.findall('/(\d+).xml$', cardCreatedLink)[0]
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
