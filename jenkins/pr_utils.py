import traceback
from hipchat_send_message import sendPrivateHipchatMessage

def prUrl(pr):
        return "https://github.com/ExpediaInc/{github_repo}/pull/{pr_number}".format(pr_number=pr.number, github_repo=pr.repository[1])

def authorsAndCommittersMailIds(pr):
        authorsAndCommittersMailIds = list(set([prCommit.commit.author['email'] for prCommit in pr.commits()] + [prCommit.commit.committer['email'] for prCommit in pr.commits()]))
        return [mailId for mailId in authorsAndCommittersMailIds if mailId != "mobiataauto@gmail.com"]

def pingPRAuthors(pr, hipchatAccessToken, messageToBePinged):
        prAuthorsAndCommittersMailIds = authorsAndCommittersMailIds(pr)
        for mailId in prAuthorsAndCommittersMailIds:
                if not mailId.endswith("@expedia.com"):
                        print "PR Author Email is not an Expedia Mail Id - {pr_url}, {mail_id}".format(pr_url=prUrl(pr), mail_id=mailId)
                        continue
                try:
                        sendPrivateHipchatMessage(hipchatAccessToken, mailId, messageToBePinged)
                except:
                        print "Exception encountered while trying to ping {mail_id} for {pr_url}. Stack Trace: \n{stack_trace}".format(pr_url=prUrl(pr), mail_id=mailId, stack_trace=traceback.format_exc())
