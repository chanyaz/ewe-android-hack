import sys
import os
import re
import shutil
from optparse import OptionParser

def move_files(in_dir, out_dir):
    # Create output directories
    # Clean out old output dir first
    if os.path.exists(out_dir):
        shutil.rmtree(out_dir)

    os.mkdir(out_dir)

    expedia_dir = os.path.join(out_dir, "project", "res", "values")
    expedia_xml_dir = os.path.join(out_dir, "project", "res", "xml")
    utils_dir = os.path.join(out_dir, "lib", "Utils", "res", "values")
    hotellib_dir = os.path.join(out_dir, "lib", "HotelLib", "res", "values")

    os.makedirs(expedia_dir)
    os.makedirs(expedia_xml_dir)
    os.makedirs(utils_dir)
    os.makedirs(hotellib_dir)

    # Move project files
    src = os.path.join(in_dir, "project", "res", "values", "strings.xml")
    shutil.copy(src, os.path.join(expedia_dir, "strings.xml"))
    
    src = os.path.join(in_dir, "project", "res", "xml", "oosdk_comment_card.xml")
    shutil.copy(src, os.path.join(expedia_xml_dir, "oosdk_comment_card.xml"))
    
    # Move AndroidUtils files
    src = os.path.join(in_dir, "lib", "Utils", "res", "values", "strings.xml")
    shutil.copy(src, os.path.join(utils_dir, "strings.xml"))

    # Move HotelLib files
    src = os.path.join(in_dir, "lib", "HotelLib", "res", "values", "strings.xml")
    shutil.copy(src, os.path.join(hotellib_dir, "strings.xml"))

if __name__ == "__main__":
    usage = "usage: %prog [options]"
    parser = OptionParser(usage=usage)
    parser.add_option('-d', '--dir', action="store", help="Target input directory (should be project base)", default="..")
    parser.add_option('-o', '--out', action="store", help="Target out directory", default="out")
    
    (options, args) = parser.parse_args()

    move_files(options.dir, options.out)
