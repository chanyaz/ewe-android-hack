#!/usr/bin/python

import xml.etree.ElementTree as ET
import sys
import re

issues = list()

for f in sys.argv[1:]:
    tree = ET.parse(f)
    root = tree.getroot()
    for issue in root.iter('issue'):
        line = issue.attrib['errorLine1']
        issues.append(line)

print "<html><style>span.tag{color:#aaaaaa} span.key{color:#6699cc} span.value{color:#000000; font-weight:bold}</style><body>"
if len(issues) > 0:
    print "<h1>Strings with missing translations</h1>"
    print "<div style=\"font-family: monospace\">"
    for issue in issues:
        key = re.findall("name=\"(\S+)\"", issue)
        stringValue = re.findall(">(.+)</string", issue)
        print "<span class=\"tag\">&lt;string name=\"</span><span class=\"key\">{}</span><span class=\"tag\">\"></span><span class=\"value\">{}</span><span class=\"tag\">&lt;/string></span><br/>".format(key[0], stringValue[0])
else:
    print "<h1>All strings translated!</h1>"
print "</div></body></html>"
