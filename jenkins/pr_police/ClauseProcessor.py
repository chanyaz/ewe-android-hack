import Clauses
from Clauses.StringShouldNotHavePositionalPlaceholders import *
from Clauses.StringShouldNotHaveBrandSpecificTerm import *
from Clauses.TemplatedStringShouldBeSuffixedWithTEMPLATE import *
from PullRequest import PullRequest

stringShouldNotHavePositionalPlaceholders = StringShouldNotHavePositionalPlaceholders()
stringShouldNotHaveBrandSpecificTerm = StringShouldNotHaveBrandSpecificTerm()
templatedStringShouldBeSuffixedWithTEMPLATE = TemplatedStringShouldBeSuffixedWithTEMPLATE()

ClausesList = [stringShouldNotHavePositionalPlaceholders, stringShouldNotHaveBrandSpecificTerm, templatedStringShouldBeSuffixedWithTEMPLATE]

class ClauseProcessor:
        def __init__(self):
                self.issueList = []

        def runClauses(self, pullRequest):
                for file in pullRequest.files:
                        for clause in ClausesList:
                                self.issueList = clause.probableIssues(file, self.issueList)
                return self.issueList