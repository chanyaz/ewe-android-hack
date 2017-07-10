#!/usr/bin/env python
#
# Script for copy and renaming assets according to density.
#
# This script should be used with assetsSorter. AssetAggregator copies files having given name 
# in different density folders, to a common folder. It also renames them according to density. 
# AssetsSorter moves them to respective density folder and renames back to original.
#
# 1. Install python from https://www.python.org/downloads/.
# 2. In console, go to project checkout directory e.g. d:/expediaAndroid/ewe-android-eb
# 3. Copy assets using "py tools/assetAgreegator.py <sourceFlavor> <fileName>"
# 4. e.g. To copy "bg_action_bar_flight_bottom.9.png" from cheaptickets
#    use "py tools/AssetAgreegator.py cheaptickets bg_action_bar_flight_bottom"
# 5. Assets will be copied in "D:/expediaAndroid/Assets/aggregated".

import sys
import os
from shutil import copy

destination = ".."+os.path.sep+"assets"+os.path.sep+"aggregated"

def findSource ():
    source = ".."+os.path.sep+"ewe-android-eb"+os.path.sep+"project"+os.path.sep+"src"+os.path.sep+sys.argv[1]+os.path.sep+"res"
    return source;

if(len(sys.argv) != 3):
    print ("")
    print("--------------------------------------------------------")
    print ("Usage py tools/assetAgreegator.py <sourceFlavor> <fileName>")
    print ("Example py tools/assetAgreegator.py ebookers ic_ab_logo")
    print ("Example py tools/AssetAgreegator.py cheaptickets bg_action_bar_flight_bottom")
    print("---------------------------------------------------------")
    print("")
else:
    source = findSource()
    print ("")
    print ("Assets will be searched from:", source)
    print ("")

    if not os.path.exists(destination):
        os.makedirs(destination)

    fileExtensions = ("png", "jpg", "jpeg", "xml", "9.png", "webp")
    postfixes = ("", "xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi", "ldpi", "xxxhdpi-v21", "xxhdpi-v21", "xhdpi-v21", "hdpi-v21", "mdpi-v21", "ldpi-v21", "sw600dp-xhdpi", "sw600dp-xxhdpi")

    for dpi in postfixes:
        if len(dpi) == 0:  # for drawable
            for extention in fileExtensions:
                sourceFileName = source +os.path.sep +"drawable"+os.path.sep + sys.argv[2] + "." + extention
                if os.path.exists(sourceFileName):
                    newFileName = destination +os.path.sep + sys.argv[2] + "[." + extention
                    copy(sourceFileName, newFileName)
                    print("Got " + newFileName);
        else:
            for extention in fileExtensions:
                sourceFileName = source +os.path.sep+"drawable-" + dpi + os.path.sep + sys.argv[2] + "." + extention
                if os.path.exists(sourceFileName):
                    newFileName = destination + os.path.sep + sys.argv[2] + "[" + dpi + "." + extention
                    copy(sourceFileName, newFileName)
                    print("Got " + newFileName);
