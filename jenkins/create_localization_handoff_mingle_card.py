from mingle_utils import createCard, uploadAttachment
import sys
import datetime
import pytz
import traceback
from hipchat_send_message import sendHipchatMessageToRoom

MINGLE_PROJECT='eb_ad_app'
#TODO - update to Mingler's Mingle Token
MINGLE_ACCESS_ID='rkochhar'
MINGLE_ACCESS_SECRET="5ISptP8ZqZDO7YNh0ZbwADo2NYRNLRIDSIFDxT0qS+Q="

UTC = pytz.utc
handoffDate = datetime.datetime.now(tz=UTC).strftime('%A %d, %b %Y, %H:%M %p %Z')

brandName = sys.argv[1]
branchName = sys.argv[2]
fileLocation = sys.argv[3]
hipchatAccessToken = sys.argv[4]
assignTo = sys.argv[5]

cardProperties = {'Team':'PF_US', 'Releases - Release':'(Current Release)', 'Theme':'Localizations', 'Schedule - Iteration':'(Current PF Iteration)', 'Status':'Analysis', 'Assigned':assignTo}
hipchatRoomName = 'Team: Android'

cardName = 'LOC DROP - {handoff_date}'.format(handoff_date=handoffDate)
cardDescription = 'Loc handoff<br><br>Brand : {brand_name}<br>Branch : {branch_name}<br>Date : {date}' \
                  .format(brand_name=brandName, branch_name=branchName, date=handoffDate)

cardType = 'Story'

cardNumber = createCard(MINGLE_PROJECT, MINGLE_ACCESS_ID, MINGLE_ACCESS_SECRET, cardName, cardDescription, cardType, cardProperties)
if cardNumber == -1: sys.exit(-1)

with open("build.properties", "w") as buildPropertiesFile:
    print buildPropertiesFile.name
    buildPropertiesFile.write('\nLOC_HANDOFF_CARD_CREATED='+cardNumber+"\n")

#Upload string file as attachment to the card
uploadStatus = uploadAttachment(MINGLE_PROJECT, MINGLE_ACCESS_ID, MINGLE_ACCESS_SECRET, cardNumber, fileLocation )
if uploadStatus == -1: sys.exit(-1)

hipchatMessage = "Loc Drop sent for {brand_name} from branch {branch_name}."\
    "Mingle card : https://eiwork.mingle.thoughtworks.com/projects/{mingleProject}/cards/{card_number}"\
    .format(brand_name=brandName, branch_name=branchName, card_number=cardNumber, mingleProject=MINGLE_PROJECT)

try:
    sendHipchatMessageToRoom(hipchatAccessToken, hipchatRoomName, hipchatMessage)
except:
    print "Exception encountered while trying to ping {hipchatRoomName}. Stack Trace: \n{stack_trace}".format(hipchatRoomName=hipchatRoomName, stack_trace=traceback.format_exc())
