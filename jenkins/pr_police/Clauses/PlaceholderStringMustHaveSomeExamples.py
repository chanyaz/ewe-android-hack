from IClause import *
from Line import *
from Issue import *
import re
from StringValidationClause import *

class PlaceholderStringMustHaveSomeExamples(StringValidationClause):
    def placeholderPatterns(self):
        return [r'\{[a-z][a-z_]+\}']

    def probableIssuesOnLineInFile(self, file, line, index):
        issueList = []
        if re.match("<string", line.codeLineAdded) and (any(re.search(pattern, line.codeLineAdded) for pattern in self.placeholderPatterns())):
            #extract the string content
            patternStringContent = re.compile(ur'>(.*?)<')
            matchObjStringContent = re.search(patternStringContent, line.codeLineAdded)
            #remove the > & < from start and end
            stringContent = line.codeLineAdded[matchObjStringContent.start() + 1:matchObjStringContent.end() - 1]
            #create the expected string clause
            expectedExampleStringFormat = re.sub(self.placeholderPatterns()[0], '(.*)', stringContent)
            if index == 0:
                #this is the first line
                issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Please include an example string with all parameters filled in to aid in translation."))
            else:
                previousLine = file.patch.fileLines[index-1]
                # validate 1> The comment must have used one example
                if not re.search(expectedExampleStringFormat, previousLine.codeLineAdded):
                    issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Please include an example string with all parameters filled in to aid in translation.", IssueType.warning))
                else:
                    # extract the placeholder values from comments and validate them
                    for placeholderValueTuples in re.finditer(expectedExampleStringFormat, previousLine.codeLineAdded):
                        print placeholderValueTuples.groups()
                        for indiValue in placeholderValueTuples.groups():
                            if indiValue.startswith("{") and indiValue.endswith("}") :
                                issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Please include an example string with all parameters filled in to aid in translation.", IssueType.warning))
                            else:
                                print u'EXAMPLE: ' + previousLine.codeLineAdded + u'\nFOR THE STRING : '+ line.codeLineAdded + u'\nFOUND.\n\n'
        return issueList
