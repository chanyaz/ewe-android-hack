#!/usr/bin/python
import json
import sys
import os.path

def generateTestcasesHTML(featureTestcasesJson, failedTestcaseImageDirectoryPath):
    testCaseReport = []
    for testcase in featureTestcasesJson:
        testcaseId = testcase['id']
        testcaseName = testcase['name']
        testcaseBefore = testcase['before']
        testcaseAfter = testcase['after']
        testcaseTags = testcase['tags']
        testcaseType = testcase['type']
        testcaseStepsJson = testcase['steps']
        print testcaseName
        print "---------------------"
        testCaseStepsBeforeHTML, testCaseStepsAfterHTML, testCaseStepsHTML, testExecutionTime, testcaseStatus = generateTestcaseStepsHTML(testcaseStepsJson, testcaseBefore, testcaseAfter, testcaseTags)
        testExecutionTime = testExecutionTime/1e9
        print "Total time for execution in seconds - {testExecutionTime}".format(testExecutionTime=testExecutionTime)
        print "Test case status - {testcaseStatus}".format(testcaseStatus=testcaseStatus)
        print "---------------------"
        if testcaseStatus=="failed":
            testcaseStatusFormatted='<a target="_blank" href="' + failedTestcaseImageDirectoryPath + '/' + testcaseId + '.png">failed</a>'
        else:
            testcaseStatusFormatted=testcaseStatus
        testCaseReport.append("""\n
                <div class="test" style="width:100%;"">
                    <div class="rTableRow {testcaseStatus} testcase" id="{testcaseId}">
                        <div class="rTableCell width-66">{testcaseName}</div>
                        <div class="rTableCell width-15">{testExecutionTime:.2f}</div>
                        <div class="rTableCell width-15">{testcaseStatusFormatted}</div>
                    </div>
                    <div class="steps">
                        <div class="rTable teststeps dp-none">
                            <div class="rTableHeading">
                                <div class="rTableHead width-45">
                                    Step
                                </div>
                                <div class="rTableHead width-10">
                                    Execution Time (in seconds)
                                </div>
                                <div class="rTableHead width-10">
                                    Status
                                </div>
                                <div class="rTableHead width-20">
                                    Comment
                                </div>
                            </div>
                            <div class="rTableBody">
                                {testCaseStepsBeforeHTML}
                                {testCaseStepsHTML}
                                {testCaseStepsAfterHTML}
                            </div>
                        </div>
                    </div>
                </div>
			""".format(testcaseId=testcaseId, testcaseStatus=testcaseStatus,testcaseName=testcaseName, testExecutionTime=testExecutionTime, testCaseStepsBeforeHTML=testCaseStepsBeforeHTML, testCaseStepsAfterHTML=testCaseStepsAfterHTML, testCaseStepsHTML=testCaseStepsHTML, testcaseStatusFormatted=testcaseStatusFormatted))

    testCaseHTML=''.join(testCaseReport)
    return testCaseHTML

def generateTestcaseStepsHTML(testcaseStepsJson, testcaseBeforeJson, testcaseAfterJson, testcaseTags):
    testcaseStatus = "noresult"
    testExecutionTime = 0
    testCaseStepsHTML = []
    testCaseStepsBeforeHTML = []
    testCaseStepsAfterHTML = []

    testCaseStepsBeforeHTML, testExecutionTime, testcaseStatus=generateStepsHTML(testcaseBeforeJson, testExecutionTime, testcaseStatus, True)
    testCaseStepsAfterHTML, testExecutionTime, testcaseStatus=generateStepsHTML(testcaseAfterJson, testExecutionTime, testcaseStatus, True)
    testCaseStepsHTML, testExecutionTime, testcaseStatus=generateStepsHTML(testcaseStepsJson, testExecutionTime, testcaseStatus, False)

    return testCaseStepsBeforeHTML, testCaseStepsAfterHTML, testCaseStepsHTML, testExecutionTime, testcaseStatus


def generateStepsHTML(testcaseStepsJson, testExecutionTime, testcaseStatus, isAfterBeforeStep):
    testCaseStepsGenerated = []

    for testcaseStep in testcaseStepsJson:
        if isAfterBeforeStep:
            stepName=testcaseStep['match']['location']
        else:
            stepName = testcaseStep['name']
        stepKeyword=""
        if not isAfterBeforeStep:
            stepKeyword = testcaseStep['keyword']

        errorMessage = ""
        stepDuration = 0
        stepDataHTML=""
        if 'rows' in testcaseStep:
            step_rows_json = testcaseStep['rows']
            stepDataHTML=generateStepDataHTML(step_rows_json)
        stepResult = testcaseStep['result']
        stepStatus = stepResult['status']
        if stepStatus=="failed":
            testcaseStatus = "failed"
            errorMessage = stepResult['error_message']
        if stepStatus=="skipped":
            if testcaseStatus!="failed":
                testcaseStatus="skipped"
        if stepStatus=="undefined":
            testcaseStatus = "failed"
            errorMessage = "Missing or Ambiguous step definition"
        if stepStatus=="passed":
            if testcaseStatus!="failed" and testcaseStatus!="skipped":
                testcaseStatus="passed"

        if 'duration' in stepResult:
            stepDuration = stepResult['duration']

        testExecutionTime = testExecutionTime + stepDuration
        testCaseStepsGenerated.append("""\n
                    <div class="rTableRow {stepStatus} teststep">
                        <div class="rTableCell width-45">{stepKeyword} {stepName}<br>{stepDataHTML}</div>
                        <div class="rTableCell width-10">{stepDuration:.4f}</div>
                        <div class="rTableCell width-10">{stepStatus}</div>
                        <div class="rTableCell width-20">{errorMessage}</div>
                    </div>
			""".format(stepStatus=stepStatus,stepKeyword=stepKeyword,stepDataHTML=stepDataHTML.encode('utf-8'), stepName=stepName.encode('utf-8'), stepDuration=stepDuration/1e9, errorMessage=errorMessage.encode('utf-8')))

    testCaseStepsGeneratedHTML=''.join(testCaseStepsGenerated)
    return testCaseStepsGeneratedHTML, testExecutionTime, testcaseStatus

def generateStepDataHTML(step_rows_json):
    stepRowData=[]
    for step_row in step_rows_json:
        cell_json = step_row['cells']
        singleRow = ""
        for row in cell_json:
            singleRow = singleRow + " - " +row
        print "{singleRow}".format(singleRow=singleRow)
        stepRowData.append("""\n
                    <br>{singleRow}</b>
			""".format(singleRow=singleRow))
    stepRowDataHTML=''.join(stepRowData)
    return stepRowDataHTML

def generateCompleteReportHTML(allFeatureTestCases):
    allFeatureTestCasesHTML=''.join(allFeatureTestCases)
    with open('project/build/outputs/UITestReport.html', 'w') as automationTestReport:
        automationTestReport.write("""<!DOCTYPE html>
            <html>
                <head>
                    <title>Cucumber Automation Report</title>
                    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
                    <script type="application/javascript">
                        $(document).ready(function() {{
                            $('.testcase').click(function() {{
                                $(this).siblings().find('.teststeps').toggleClass('dp-none');
                                $(this).toggleClass('selected')
                            }});
                        }});
                    </script>
                    <style>
                        .passed{{
                            color: black;
                            background-color: #c1dec1;
                        }}
                        .failed, .noresult{{
                            color: black;
                            background-color: #ffb3b3;
                        }}
                        .skipped{{
                            color: black;
                            background-color: #b3ffff;
                        }}

                        .feature-title{{
                            float: left;
                            width: 100%;
                            font-weight: bold;
                            margin-bottom: 5px;

                        }}
                        .feature-title-background{{
                            color: #480000;
                            background-color: #a6a2bf;
                        }}
                        .feature-title span{{
                            float: left;
                            padding: 14px;
                        }}

                        .rTable{{
                            display: table;
                            width: 96%;
                            margin-left: 2%;
                        }}
                        .rTableRow{{
                            display: block;
                            width: 100%;
                            float: left;
                            border: 1px solid #999999;
                        }}
                        .rTableHeading{{
                            display: block;
                            background-color: #ddd;
                            font-weight: bold;
                            width: 100%;
                            float: left;
                        }}
                        .rTableCell, .rTableHead{{
                            display: block;
                            word-break:break-word;
                            float: left;
                            padding: 0.5%;
                        }}
                        .rTableHeading{{
                            display: block;
                            background-color: #ddd;
                            font-weight: bold;
                            width: 100%;
                            float: left;
                        }}
                        .rTableFoot{{
                          display: table-footer-group;
                          font-weight: bold;
                          background-color: #ddd;
                        }}
                        .rTableBody{{
                          display: table-row-group;
                        }}
                        .width-66{{
                            width: 66%;
                        }}
                        .width-15{{
                            width: 15%;
                        }}
                        .width-20{{
                            width: 39%;
                        }}
                        .width-45{{
                            width: 36%;
                        }}
                        .width-10{{
                            width: 10%;
                        }}
                        .test{{
                            width: 100%;
                            float: left;
                        }}
                        .testcase:hover{{
                            cursor: pointer;
                        }}
                        .teststeps{{
                            width: 100%;
                            padding: 20px;
                            padding-left: 0px;
                            padding-right: 0px;
                        }}
                        thead{{
                            background-color: #e6e6e6;
                        }}
                        .dp-none{{
                            display: none;
                        }}
                        .steps{{
                            width: 97%;
                            clear: both;
                            float: left;
                            margin-left: 20px;
                            margin-bottom: 3px;
                        }}
                        .selected{{
                            font-weight: bold;
                            border: 2px solid #1000ff;
                        }}
                    </style>
                </head>
                <body>
                    <div class="cucumber-report">
                        {allFeatureTestCasesHTML}
                    </div>
                </body>
            </html>""".format(allFeatureTestCasesHTML=allFeatureTestCasesHTML))

def main():
    allConnectedDevicesStr = sys.argv[1].strip()
    allFeatureResults = []
    for deviceIdentifier in allConnectedDevicesStr.split(','):
        testCasesReportJsonFilePath='project/build/outputs/' + deviceIdentifier + '/cucumber-htmlreport/cucumber.json'
        failedTestcaseImageDirectoryPath = deviceIdentifier + '/cucumber-images'
        if os.path.exists(testCasesReportJsonFilePath):
            try:
                with open(testCasesReportJsonFilePath) as reportJsonFile:
                    jsonReport = json.load(reportJsonFile)
                for features in jsonReport:
                    featureName = features['name']
                    feature_testcases_json = features['elements']
                    allTestCases = []
                    print featureName
                    print "*************"
                    allTestCases.append(generateTestcasesHTML(feature_testcases_json, failedTestcaseImageDirectoryPath))
                    allTestCasesHTML=''.join(allTestCases)
                    allFeatureResults.append("""\n<div class="cucumber-feature">
                            <div class="feature-title feature-title-background"><span>Feature:{featureName} run on Device: {deviceIdentifier}</span></div>
                                <div class="testcases">
                                    <div class="rTable">
                                        <div class="rTableHeading">
                                            <div class="rTableHead width-66">
                                                Testcases
                                            </div>
                                            <div class="rTableHead width-15">
                                                Execution Time (in seconds)
                                            </div>
                                            <div class="rTableHead width-15">
                                                Status
                                            </div>
                                        </div>
                                        <div class="rTableBody">
                                            {allTestCasesHTML}
                                        </div>
                                    </div>
                                </div>
                        </div>
                        """.format(deviceIdentifier=deviceIdentifier, featureName=featureName, allTestCasesHTML=allTestCasesHTML ))
            except Exception, e:
                allFeatureResults.append("""\n
                    <div class="cucumber-feature">
                        <div class="feature-title failed"><span>Something went wrong on device : {deviceIdentifier}</span></div>
                    </div>
                    """.format(deviceIdentifier=deviceIdentifier ))
        else:
            allFeatureResults.append("""\n
                    <div class="cucumber-feature">
                        <div class="feature-title failed"><span>Something went wrong!! Could not find cucumber.json file on device : {deviceIdentifier}</span></div>
                    </div>
                    """.format(deviceIdentifier=deviceIdentifier ))
    generateCompleteReportHTML(allFeatureResults)

if __name__ == "__main__":
    main()
