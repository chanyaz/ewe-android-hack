#!/usr/bin/python

import xml.etree.ElementTree as ET
import sys
import re

issues = list()

for f in sys.argv[1:]:
    tree = ET.parse(f)
    root = tree.getroot()
    issueIterator = None
    try:
        iterator = root.iter('issue')
    except AttributeError:
        # use old method on old versions of python
        iterator = root.getiterator('issue')
    for issue in iterator:
        line = issue.attrib['errorLine1']
        issues.append(line)

# styles file must be separate in order to satisfy jenkins security feature (https://wiki.jenkins-ci.org/display/JENKINS/Configuring+Content+Security+Policy)
styleFile = open('project/build/outputs/missing_translations_styles.css', 'w')
styleFile.write('span.tag{color:#aaaaaa} span.key{color:#6699cc} span.value{color:#000000; font-weight:bold}')
styleFile.close()

htmlFile = open('project/build/outputs/missing_translations.html', 'w')
htmlFile.write('<html><head><link rel="stylesheet" href="missing_translations_styles.css"></head><body>')
if len(issues) > 0:
    htmlFile.write('<h1>Strings with missing translations</h1>')
    htmlFile.write('<div style="font-family: monospace">')
    for issue in issues:
        key = re.findall("name=\"(\S+)\"", issue)
        stringValue = re.findall(">(.+)</string", issue)
        htmlFile.write('<span class="tag">&lt;string name="</span>')
        htmlFile.write('<span class="key">{}</span>'.format(key[0]))
        htmlFile.write('<span class="tag">"></span>')
        htmlFile.write('<span class="value">{}</span>'.format(stringValue[0].encode('utf-8')))
        htmlFile.write('<span class="tag">&lt;/string></span><br/>')
else:
    htmlFile.write('<h1>All strings translated!</h1>')
htmlFile.write('</div></body></html>')
htmlFile.close()
