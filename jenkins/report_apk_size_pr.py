import os
from github3 import login
import sys

brand = sys.argv[1]
github_access_token = os.environ['GITHUB_ACCESS_TOKEN']
pull_request_id = os.environ['ghprbPullId']

new_apk_size = round(
    os.stat('project/build/outputs/apk/project-{brand}-debug.apk'.format(brand=brand)).st_size / float(1024 * 1024), 2)

old_apk_size = 0
try:
    with open("apkSize.txt") as archived_apk_size_file:
        old_apk_size = float(archived_apk_size_file.read())
except IOError:
    print "APK size archive file not found."

gh = login(token=github_access_token)
repo = gh.repository('ExpediaInc', 'ewe-android-eb')
pr = repo.pull_request(pull_request_id)

if old_apk_size != 0:
    difference = new_apk_size - old_apk_size

    pr.create_comment(
        "New APK size = {newSize} MB\nOld APK size = {oldSize} MB\nDifference = {difference} MB\n**These are for debug app".format(
            newSize=new_apk_size, oldSize=old_apk_size, difference=difference))
else:
    pr.create_comment("APK size analysis not available.")
