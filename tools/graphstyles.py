#!/usr/bin/python

import xml.etree.ElementTree as ET
import sys

edges = list()

def clean(s):
    return s.lstrip('@style/')

for f in sys.argv[1:]:
    tree = ET.parse(f)
    root = tree.getroot()
    for style in root.iter('style'):
        name = clean(style.attrib['name'])
        if 'parent' in style.attrib:
            # Explicit parent
            parent = clean(style.attrib['parent'])
            edges.append((name, parent))
        elif name.rfind('.') > 0:
            # Implicit parent
            edges.append((name, name[:name.rindex('.')]))

# Prints out a graphviz graph
# Can generate a pdf with:
# ./tools/graphstyles.py project/src/main/**/*.xml | dot -Tpdf -o foo.pdf
print "digraph G {"
for edge in edges:
    print " -> ".join('"{0}"'.format(node) for node in edge)
print "}"
