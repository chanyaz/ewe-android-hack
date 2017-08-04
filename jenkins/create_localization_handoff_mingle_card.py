from mingle_utils import createCard, uploadAttachment
import sys
import datetime
import pytz
import traceback
import os
from slack_send_message import send_public_slack_message

UTC = pytz.utc
handoffDate = datetime.datetime.now(tz=UTC).strftime('%A %d, %b %Y, %H:%M %p %Z')

brandName = sys.argv[1]
branchName = sys.argv[2]
fileLocation = sys.argv[3]
slack_access_token = sys.argv[4]
assignTo = sys.argv[5]
tpmComments = sys.argv[6]
MINGLE_ACCESS_ID = os.environ['MINGLE_ACCESS_ID']
MINGLE_ACCESS_SECRET = os.environ['MINGLE_ACCESS_TOKEN']
mingleProject = 'ebapp'

if brandName == 'expedia':
    slack_channel = '#bexg-app-android'
    team = 'Platform'
else:
    slack_channel = '#ewe-mobile-india'
    team = 'Multibrand'

cardProperties = {'Platform':'Android', 'Team':team, 'Releases - Release':'(Release *WIP)', 'Theme':'Localizations', 'Schedule - Iteration':'(Current Iteration)',
                  'Status':'Analysis', 'Assigned':assignTo}

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

chat_message = "Loc Drop sent for {brand_name} from branch {branch_name}."\
    "Mingle card : https://eiwork.mingle.thoughtworks.com/projects/{mingleProject}/cards/{card_number}"\
    .format(brand_name=brandName, branch_name=branchName, card_number=cardNumber, mingleProject=mingleProject)

try:
    send_public_slack_message(slack_access_token, slack_channel, chat_message)
except:
    print "Exception encountered while trying to ping {channel}. Stack Trace: \n{stack_trace}".format(channel=slack_channel, stack_trace=traceback.format_exc())
