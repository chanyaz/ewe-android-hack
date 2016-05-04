from hypchat import *

#Access Token can be obtained from https://expedia.hipchat.com/account/api
def sendPrivateHipchatMessage(accessToken, userMailIdToSendMessageTo, message):
	hipchat = HypChat(accessToken)
	userToSendMessageTo = hipchat.get_user(userMailIdToSendMessageTo)
	userToSendMessageTo.message(message, message_format='text', notify=True)

def sendHipchatMessageToRoom(accessToken, roomName, message):
	hipchat = HypChat(accessToken)
	roomToSendMessageTo = hipchat.get_room(roomName)
	roomToSendMessageTo.notification(message, notify=True)