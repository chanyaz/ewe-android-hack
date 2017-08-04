import os
parentdir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
os.sys.path.insert(0,os.path.join(parentdir, "jenkins"))
import sys
from github3 import login
from mingle_utils import murmurInProject, uploadAttachment
from pr_utils import prUrl
import subprocess
import traceback

def isMultiBrand(brandName):
    if brandName == "main":
        return False
    else:
        return True

brandName = sys.argv[1]
locHandbackChangesBranchName = sys.argv[2]
locHandbackCommitMessage = sys.argv[3]
gitBaseBranchName = sys.argv[4]
mingleCardNumber = sys.argv[5]
zipFileLocation = sys.argv[6]

mingleProjectId = 'ebapp'
mingleAccessId = os.environ['MINGLE_ACCESS_ID']
mingleAccessSecret = os.environ['MINGLE_ACCESS_TOKEN']
githubToken = os.environ['GITHUB_ACCESS_TOKEN']
github = login(token=githubToken)
repo = github.repository('ExpediaInc', 'ewe-android-eb')


print "brandName = " + brandName
print "locHandbackChangesBranchName = " + locHandbackChangesBranchName
print "gitCommitMessage = " + locHandbackCommitMessage
print "gitBaseBranchName = " + gitBaseBranchName
print "mingleCardNumber = " + mingleCardNumber

try:
    subprocess.check_call('git status'.split())
    subprocess.check_call('git checkout -b {gitBranchName}'.format(gitBranchName=locHandbackChangesBranchName).split())
    subprocess.check_call('git add project/src/{brandName}/res'.format(brandName=brandName).split())
    commitCommandWords= 'git -c user.name="ewe-mergebot" -c user.email="mobiataauto@gmail.com" commit -m'.split()
    commitCommandWords.append(locHandbackCommitMessage)
    subprocess.check_call(commitCommandWords)

except:
    subprocess.check_call('git checkout -- .'.split())
    print "Unable to create commit locally. Stack Trace: \n{stack_trace}".format(stack_trace=traceback.format_exc())
    sys.exit(1)

print "Commit created. Pushing changes"

os.system('git push origin {gitBranchName}'.format(gitBranchName=locHandbackChangesBranchName))

print "Creating pull request"
pr = repo.create_pull(locHandbackCommitMessage, gitBaseBranchName.replace("origin/", ""), locHandbackChangesBranchName)
murmurMessageInMingle = '{pr_url} for card #{mingleCardNumber}'.format(pr_url=prUrl(pr), mingleCardNumber=mingleCardNumber)

murmurInProject(mingleProjectId, mingleAccessId, mingleAccessSecret, murmurMessageInMingle)

uploadAttachment(mingleProjectId, mingleAccessId, mingleAccessSecret, mingleCardNumber, zipFileLocation)

print "Deleting local branch."
try:
    subprocess.check_call('git checkout {gitBaseBranchName}'.format(gitBaseBranchName=gitBaseBranchName).split())
    subprocess.check_call('git branch -D {gitBranchName}'.format(gitBranchName=locHandbackChangesBranchName).split())
except:
    # Not exiting with failure from here as the only thing it impacts is cleanup of the just created branch.
    # Since branch names guaranteed to be unique (they are created with Jenkins Build Number in them), we are not getting noisy about this error.
    print "Unable to delete local branch. Stack Trace: \n{stack_trace}".format(stack_trace=traceback.format_exc())