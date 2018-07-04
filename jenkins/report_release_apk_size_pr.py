import os
import sys
import textwrap
from github3 import login

BETTER_COLOR = "00b700"
WORSE_COLOR = "d30000"
NEUTRAL_COLOR = "c0c0c0"

brand = sys.argv[1]


def get_new_apk_size():
    return round(
        os.stat(
            'project/build/outputs/apk/{brand}/release/project-{brand}-release.apk'.format(
                brand=brand)).st_size / float(
            1024 * 1024), 2)


def get_old_apk_size():
    old_apk_size = 0
    try:
        with open("releaseApkSize.txt") as archived_release_apk_size_file:
            old_apk_size = float(archived_release_apk_size_file.read())
    except IOError:
        print "Release APK size archive file not found."
    return old_apk_size


def git_setup():
    pull_request_id = os.environ['ghprbPullId']
    github_access_token = os.environ['GITHUB_ACCESS_TOKEN']
    gh = login(token=github_access_token)
    repo = gh.repository('ExpediaInc', 'ewe-android-eb')
    return repo.pull_request(pull_request_id)


def comment_on_pr():
    pr = git_setup()
    old_apk_size = get_old_apk_size()
    new_apk_size = get_new_apk_size()
    if old_apk_size != 0:
        difference = new_apk_size - old_apk_size
        color = NEUTRAL_COLOR
        if difference > 0:
            color = WORSE_COLOR
        elif difference < 0:
            color = BETTER_COLOR

        pr.create_comment(textwrap.dedent("""
        New Release APK size = {newSize} MB
        Old Release APK size = {oldSize} MB
        Difference = {difference} MB ![{color}](https://placehold.it/15/{color}/000000?text=+)
        **These are for debug app""").format(
            newSize=new_apk_size, oldSize=old_apk_size, difference=difference, color=color))
    else:
        pr.create_comment("APK size analysis not available.")


comment_on_pr()
