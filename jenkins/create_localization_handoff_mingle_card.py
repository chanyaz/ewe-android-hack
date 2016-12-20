from mingle_utils import createCard, uploadAttachment
import sys
import datetime
import pytz
import traceback
from hipchat_send_message import sendHipchatMessageToRoom

MINGLE_ACCESS_ID='mingler'
MINGLE_ACCESS_SECRET="+94zjsneYF6iwS1lqdLdKmvAyx0ilt8o1RuV71fKU+E="

UTC = pytz.utc
handoffDate = datetime.datetime.now(tz=UTC).strftime('%A %d, %b %Y, %H:%M %p %Z')

brandName = sys.argv[1]
branchName = sys.argv[2]
fileLocation = sys.argv[3]
hipchatAccessToken = sys.argv[4]
assignTo = sys.argv[5]
tpmComments = sys.argv[6]

if brandName == 'expedia':
    mingleProject = 'eb_ad_app'
    hipchatRoomName = 'Team: Android'
    cardProperties = {'Team':'PF_US', 'Releases - Release':'(Current Release)', 'Theme':'Localizations', 'Schedule - Iteration':'(Current Iteration)',
    'Status':'Analysis', 'Assigned':assignTo}
else:
    mingleProject = 'india_mobile_team'
    hipchatRoomName = 'India Mobile Team'
    cardProperties = {'Sprint Tree - Sprint':'(Current Sprint)', 'Theme':'Localizations', 'Status':'In analysis', 'Assigned':assignTo, 'OS':'Android', 'LOB':'MB'}

cardName = 'LOC DROP - {handoff_date}'.format(handoff_date=handoffDate)
cardDescription = 'Loc handoff<br><br>Brand : {brand_name}<br>Branch : {branch_name}<br>Date : {date}<br>Comments : {tpm_comments}' \
                  .format(brand_name=brandName, branch_name=branchName, date=handoffDate, tpm_comments=tpmComments)

cardType = 'Story'

cardNumber = createCard(mingleProject, MINGLE_ACCESS_ID, MINGLE_ACCESS_SECRET, cardName, cardDescription, cardType, cardProperties)
if cardNumber == -1: sys.exit(-1)

with open("build.properties", "w") as buildPropertiesFile:
    print buildPropertiesFile.name
    buildPropertiesFile.write('\nLOC_HANDOFF_CARD_CREATED='+cardNumber+"\n")
    buildPropertiesFile.write('\nMINGLE_PROJECT='+mingleProject+"\n")

#Upload string file as attachment to the card
uploadStatus = uploadAttachment(mingleProject, MINGLE_ACCESS_ID, MINGLE_ACCESS_SECRET, cardNumber, fileLocation )
if uploadStatus == -1: sys.exit(-1)

hipchatMessage = "Loc Drop sent for {brand_name} from branch {branch_name}."\
    "Mingle card : https://eiwork.mingle.thoughtworks.com/projects/{mingleProject}/cards/{card_number}"\
    .format(brand_name=brandName, branch_name=branchName, card_number=cardNumber, mingleProject=mingleProject)

try:
    sendHipchatMessageToRoom(hipchatAccessToken, hipchatRoomName, hipchatMessage)
except:
    print "Exception encountered while trying to ping {hipchatRoomName}. Stack Trace: \n{stack_trace}".format(hipchatRoomName=hipchatRoomName, stack_trace=traceback.format_exc())
