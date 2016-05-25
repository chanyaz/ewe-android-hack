#!/usr/bin/env python
'''
This wil remove some skin attr from codebase. It basically removes 2 types of skin attributes:
1. Stale skin attrs: Those which are present in whitelabel.xml but never referred anywhere in codebase.
2. Directly refereced attrs: It will remove skin attr and rename all names equivalent to main, so that code can directly reference those.
    main/abc_layout.xml -> android:background="?attr/skin_bgSelectedFlightCard"
    main/whitelabel.xml -> <attr name="skin_bgSelectedFlightCard" format="reference" />
    main/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card</item>
    tvly/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card_tvly</item>
    wotif/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card_wotif</item>
    
    |-----------------After running script-----------------------|
    
    main/abc_layout.xml -> android:background="@drawable/bg_selected_flight_card"
    
    Deleted-> main/whitelabel.xml -> <attr name="skin_bgSelectedFlightCard" format="reference" />
    Deleted-> main/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card</item>
    Deleted-> tvly/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card_tvly</item>
    Deleted-> wotif/themes.xml -> <item name="skin_bgSelectedFlightCard">@drawable/bg_selected_flight_card_wotif</item>
    
    Renamed -> tvly/bg_selected_flight_card_tvly -> tvly/@drawable/bg_selected_flight_card
    Renamed -> wotif/bg_selected_flight_card_wotif -> wotif/@drawable/bg_selected_flight_card
    
'''
import os
from xml.dom import minidom
import re
from glob import glob
from os import rename

MAIN_DIR = 'project/src'
WHITELABELS_XML_PATH = 'project/src/main/res/values/whitelabels.xml'

#FINAL
def grepStringKey(stringKey):
    output = os.popen('grep -w -r ' + stringKey + ' /Users/pkothari/Expedia/ewe-android-eb/project/src').read().strip().split("\n")
    files = []
    for line in output:
        parts = line.split(":")
        filePath = parts[0]
        if (filePath != "") and (not filePath.isspace()):
            #print "Appending <%s>" % filePath
            files.append(filePath)
    return files


def get_all_skin_attributes():
    xml_doc = minidom.parse(WHITELABELS_XML_PATH)
    item_list = xml_doc.getElementsByTagName('attr')
    keyword_list = []
    for item in item_list:
        keyword_list.append(item.attributes['name'].value)
    return keyword_list


def get_skin_attr_values(skin_attr_value_list, fname, skin_attr):
    xml_doc = minidom.parse(fname)
    item_list = xml_doc.getElementsByTagName('item')
    name_of_item_in_main = ''
    for item in item_list:
        if 'whitelabels.xml' not in fname and item.hasAttribute('name') and skin_attr == item.attributes['name'].value:
            skin_attr_value_list.append(item.firstChild.nodeValue)
            if '/main/' in fname or '/expedia/' in fname:
                name_of_item_in_main = item.firstChild.nodeValue
    return name_of_item_in_main


def check_if_drawable_is_overridden(skin_attr_value_list, name_of_item_in_main):
    for skin_attr_value in skin_attr_value_list:
        if name_of_item_in_main not in skin_attr_value:
            return False
    return True


def remove_stale_attr(stale_skin_attr_list):
    file = open(WHITELABELS_XML_PATH, "r")
    lines = file.readlines()
    file.close()

    file = open(WHITELABELS_XML_PATH, "w")
    for line in lines:
        line_has_skin_attr = False
        for stale_skin_attr in stale_skin_attr_list:
            if stale_skin_attr in line:
                line_has_skin_attr = True
                continue
        if not line_has_skin_attr:
            file.write(line)
    file.close()

def replace_overriden_drawables(overridden_skin_attr_list):
    for overridden_skin_attr in overridden_skin_attr_list:
        skin_attr = overridden_skin_attr[0]
        name_of_item_in_main = overridden_skin_attr[1]
        skin_attr_value_list = overridden_skin_attr[2]

        rename_actual_files(skin_attr_value_list, name_of_item_in_main)

        replace_keyword("?attr/" + skin_attr, name_of_item_in_main)

        remove_attr_from_xml(skin_attr)

        remove_attr_from_java(skin_attr, name_of_item_in_main)

        for skin_attr_value in skin_attr_value_list:
            replace_keyword(skin_attr_value, name_of_item_in_main)

def replace_keyword(keyword, replacement):
    file_paths = grepStringKey(keyword)
    for file_path in file_paths:
        with open(file_path) as f:
            file_data = f.read()

        # file_data = file_data.decode().encode('utf-8')

        try:
            if keyword in file_data:
                file_data = file_data.replace(keyword, replacement)
                with open(file_path, "w") as f:
                    f.write(file_data)
        except:
            print keyword + " : " + file_path

def remove_attr_from_xml(skin_attr):
    file_paths = grepStringKey('\'"' + skin_attr + '"\'')
    for file_path in file_paths:
        with open(file_path) as f:
            file_data = f.read()

        # file_data = file_data.decode('utf-8')
        file_data = re.sub(r'<item.*name="'+skin_attr+'".*<\/item>', "", file_data)

        with open(file_path, "w") as f:
            f.write(file_data)

def remove_attr_from_java(skin_attr, name_of_item_in_main):
    file_paths = grepStringKey('R.attr.' + skin_attr)
    for file_path in file_paths:
        with open(file_path) as f:
            file_data = f.read()

        try:
            # file_data = file_data.decode('utf-8')
            file_data = re.sub(r'Ui\.obtain.*R\.attr\.'+skin_attr+'\)', name_of_item_in_main.replace("@","R.").replace("/","."), file_data)

            with open(file_path, "w") as f:
                f.write(file_data)
        except:
            print skin_attr + " : " + file_path

def rename_actual_files(skin_attr_value_list, name_of_item_in_main):
    for skin_attr_value in skin_attr_value_list:
        find_all_files_with_name(skin_attr_value.split("/")[1], name_of_item_in_main.split("/")[1])

def find_all_files_with_name(name, name_of_item_in_main):
    for root, dirs, filenames in os.walk(MAIN_DIR):
        for filename in filenames:
            if name in filename:
                fullpath = os.path.join(root, filename)
                os.rename(fullpath, fullpath.replace(name, name_of_item_in_main))

def file_contains_keyword(keyword):
    files_paths_containing_keyword = []
    for dname, dirs, files in os.walk(MAIN_DIR):
        for fname in files:
            fpath = os.path.join(dname, fname)
            with open(fpath) as f:
                file_data = f.read()

            if keyword not in file_data.decode("ISO-8859-1"):
                continue
            files_paths_containing_keyword.append(fpath)
    return files_paths_containing_keyword

if __name__ == "__main__":
    skin_attr_list = get_all_skin_attributes()
    stale_skin_attr_list = []
    overridden_skin_attr_list = []
    for skin_attr in skin_attr_list:
        files_paths_containing_keyword = file_contains_keyword(skin_attr)
        skin_attr_value_list = []
        name_of_item_in_main = ''
        for fname in files_paths_containing_keyword:
            if 'xml' in fname:
                name_of_item_in_main_temp = get_skin_attr_values(skin_attr_value_list, fname, skin_attr)
                if len(name_of_item_in_main_temp) > 0:
                    name_of_item_in_main = name_of_item_in_main_temp

        if (name_of_item_in_main == ''):
            stale_skin_attr_list.append(skin_attr)
        else:
            if check_if_drawable_is_overridden(skin_attr_value_list, name_of_item_in_main):
                overridden_skin_attr_list.append([skin_attr, name_of_item_in_main, skin_attr_value_list])

    print "Total skin attr - " + str(len(skin_attr_list)) + " ------>"

    print "Stale skin attr - " + str(len(stale_skin_attr_list)) + " ------>"
    # for stale_skin_attr in stale_skin_attr_list:
    #     print stale_skin_attr

    print "Overridden skin attr - " + str(len(overridden_skin_attr_list)) + " ------>"
    # for overridden_skin_attr in overridden_skin_attr_list:
    #     print overridden_skin_attr[0] + " : " + overridden_skin_attr[1] + " : " + str(overridden_skin_attr[2])

    replace_overriden_drawables(overridden_skin_attr_list)
    remove_stale_attr(stale_skin_attr_list)
