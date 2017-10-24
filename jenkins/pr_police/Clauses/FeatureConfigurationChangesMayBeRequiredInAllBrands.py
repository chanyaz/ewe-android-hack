from IClause import *
from Line import *
from Issue import *
import re

class FeatureConfigurationChangesMayBeRequiredInAllBrands(IClause):

    def __init__(self):
        self.type = ClauseType.prLevel

    def getBrands(self):
        return ['expedia','airAsiaGo','cheapTickets','ebookers','lastMinute','orbitz','travelocity','voyages','wotif']

    def wantsToScanFile(self, filepath):
        return True if re.search("/FeatureConfiguration\.java", filepath, re.I) else False

    def getTheConfigFilesThatGotAffected(self, pullRequest):
        return [file.filename for file in pullRequest.files if self.wantsToScanFile(file.filename)]

    def probableIssues(self, pullRequest):
        issueList = []
        if len(self.getTheConfigFilesThatGotAffected(pullRequest))>0:
            missingBrands=[ brand for brand in self.getBrands() if not any([True for filePath in self.getTheConfigFilesThatGotAffected(pullRequest) if re.search(brand,filePath,re.I)])]
            if len(missingBrands)>0:
                issueList.append(Issue(None, None, None, -1, None, "Should the changes you did in FeatureConfiguration.java be replicated in {missing_brands} as well?".format(missing_brands=",".join(missingBrands)), IssueType.warning))
        return issueList