#!/usr/bin/python
#
# Breaks down code coverage numbers based on class type. Class type is determined purely on a match against the name
# of the class file. For example, SomeViewModel.kt or SomeViewModel.java would be considered as a ViewModel class type.
#
# Usage:
# python code_coverage_by_class_type.py path/to/jacocoReportFile.xml
#

import xml.etree.ElementTree as ET
import sys

def printCoverageInformation(type, coveredCount, totalCount):
    print "{type}:".format(type=type)
    print "{covered} of {total} ({percent:.2f}%)".format(covered=coveredCount, total=totalCount, percent=100.0*coveredCount/totalCount)


classTypes = ['ViewModel', 'Widget', 'Activity', 'Fragment', 'Presenter', 'Utils', 'Adapter', 'View', 'Button', 'EditText', 'Layout', 'Dialog', 'ResponseHandler']
typeMissed = {}
typeCovered = {}

for type in classTypes:
    typeMissed[type] = 0
    typeCovered[type] = 0
otherMissed = 0
otherCovered = 0

for f in sys.argv[1:]:
    tree = ET.parse(f)
    root = tree.getroot()
    iterator = root.findall('package')
    for packageInfo in iterator:
        for classInfo in packageInfo.findall('sourcefile'):
            className = classInfo.attrib['name']
            if className.endswith('.kt'):
                className = className[:-3]
            elif className.endswith('.java'):
                className = className[:-5]
            for counter in classInfo.iter('counter'):
                if counter.attrib['type'] == "INSTRUCTION":
                    missed = int(counter.attrib['missed'])
                    covered = int(counter.attrib['covered'])
                    counted = False
                    for suffix in typeMissed.keys():
                        if className.endswith(suffix):
                            typeMissed[suffix] += missed
                            typeCovered[suffix] += covered
                            counted = True
                    if not counted:
                        otherMissed += missed
                        otherCovered += covered


for type in classTypes:
    printCoverageInformation(type, typeCovered[type], typeCovered[type] + typeMissed[type])
printCoverageInformation("Other", otherCovered, otherCovered + otherMissed)
