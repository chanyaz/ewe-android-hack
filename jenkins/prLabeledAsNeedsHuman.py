import sys
from github3 import login

def main():
    githubAccessToken = sys.argv[1]
    prId = sys.argv[2]
    gh = login(token=githubAccessToken)
    repo = gh.repository('ExpediaInc', 'ewe-android-eb')
    issue = repo.issue(prId)
    for label in issue.labels():
        if label.name == 'needs-human':
            return 1
    return 0

if __name__ == "__main__":
    sys.exit(main())
