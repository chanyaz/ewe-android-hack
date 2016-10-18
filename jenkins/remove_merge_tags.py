import sys
from github3 import login
def main():
    githubAccessToken = sys.argv[1]
    gh = login(token=githubAccessToken)
    repo = gh.repository('ExpediaInc', 'ewe-android-eb')
    prs = repo.pull_requests()

    for pr in prs:
        prIssue = pr.issue()
        labelsOnPr = prIssue.labels()
        labels = []
        for label in labelsOnPr:
            labels.append(label.name)
        shouldRemoveMergeLabel = ('merge' in labels) and ('Current Iteration' not in labels)
        if (shouldRemoveMergeLabel):
            print 'Removing merge label from PR #{prNumber} : {title}'.format(prNumber=prIssue.number, title=prIssue.title)
            prIssue.remove_label('merge')

if __name__ == "__main__":
    main()