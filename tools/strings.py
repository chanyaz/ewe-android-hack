import sys
import os
import re
import shutil
from optparse import OptionParser

LANGUAGES = {
    1028: "zh-rTW",
    1030: "da",
    1031: "de",
    1036: "fr",
    1040: "it",
    1041: "ja",
    1042: "ko",
    1044: "nb",
    1046: "pt",
    1053: "sv",
    1057: "in",
    1086: "ms",
    1124: "tl",
    2052: "zh",
    2057: "en-rGB",
    3076: "zh-rHK",
    3082: "es",
    3084: "fr-rCA",
}

# Kept for reference
UNUSED_LANGUAGES = {
    1025: "ar-EG",
    1029: "cd-CZ",
    1032: "el-GR",
    1033: "en-US",
    1035: "fi-FI",
    1038: "hu-HU",
    1039: "is-IS",
    1043: "nl-NL",
    1045: "pl-PL",
    1049: "ru-RU",
    1050: "hr-HR",
    1051: "sk-SK",
    1054: "th-TH",
    1055: "tr-TR",
    1058: "uk-UA",
    1061: "et-EE",
    1062: "lv-LV",
    1063: "lt-LT",
    1066: "vi-VN",
    2058: "es-MX",
    2070: "pt-PT",
    3079: "de-AT",
    3081: "en-AU",
    4105: "en-CA",
    5129: "en-NZ",
    6153: "gle-IE",
}

# Prints out langauges from the wiki page
def parse_languages():
    test = """1025    Arabic                      ar-EG
    1028    Chinese(Taiwan)             zh-TW
    1029    Czech                       cd-CZ
    1030    Danish                      da-DK
    1031    German(Germany)             de-DE
    1032    Greek                       el-GR
    1033    English(US)                 en-US
    1035    Finnish                     fi-FI
    1036    French(France)              fr-FR
    1038    Hungarian                   hu-HU
    1039    Iceland                     is-IS
    1040    Italian                     it-IT
    1041    Japanese                    jp-JP
    1042    Korean                      ko-KR
    1043    Dutch                       nl-NL
    1044    Norwegian                   no-NO
    1045    Polish                      pl-PL
    1046    Portuguese(Brazil)          pt-BR
    1049    Russian                     ru-RU
    1050    Croatian                    hr-HR
    1051    Slovak                      sk-SK
    1053    Swedish                     sv-SE
    1054    Thai                        th-TH
    1055    Turkish                     tr-TR
    1057    Indonesian                  id-ID
    1058    Ukraine                     uk-UA
    1061    Estonian                    et-EE
    1062    Latvian                     lv-LV
    1063    Lithuanian                  lt-LT
    1066    Vietnamese                  vi-VN
    1086    Malay                       ms-MY
    2052    Chinese(Simplified)         zh-CN
    2057    English (UK)                en-UK
    2058    Spanish(Mexican)            es-MX
    2070    Portuguese(Portugal)        pt-PT
    3079    German(Austria)             de-AT
    3081    English(Australian)         en-AU
    3082    Spanish(international)      es-ES
    3084    French(Canada)              fr-CA
    3076    Chinese (Hong Kong)         zh-HK
    4105    English(Canada)             en-CA
    5129    English(New Zealand)        en-NZ
    6153    English(Irish)              gle-IE"""

    for row in test.split("\n"):
        items = row.split()
        print "%s: \"%s\"," % (items[0], items[-1])


RE_STRING_FOLDER = re.compile("(\d{4})_.+")

def move_files(in_dir, out_dir):
    # Determine location of all folders
    folders = {}
    for file in os.listdir(in_dir):
        match = RE_STRING_FOLDER.match(file)
        if match:
            folders[int(match.group(1))] = os.path.join(in_dir, file)

    # Sanity check
    for lang_id in folders:
        if lang_id not in LANGUAGES:
            print("ERROR: Could not find language id %d in LANGUAGES" % lang_id)
            sys.exit()

    # Create output directories
    # Clean out old output dir first
    if os.path.exists(out_dir):
        shutil.rmtree(out_dir)

    os.mkdir(out_dir)

    expedia_dir = os.path.join(out_dir, "project", "res")
    utils_dir = os.path.join(out_dir, "lib", "Utils", "res")
    hotellib_dir = os.path.join(out_dir, "lib", "HotelLib", "res")

    os.makedirs(expedia_dir)
    os.makedirs(utils_dir)
    os.makedirs(hotellib_dir)

    # Start moving files
    for lang_id in folders:
        postfix = LANGUAGES[lang_id]
        for dir_info in os.walk(folders[lang_id]):
            dir_path = dir_info[0]
            for filename in dir_info[2]:
                filepath = os.path.join(dir_path, filename)
                if filename == "strings.xml":
                    new_dir = None
                    if "ExpediaBookings" in dir_path:
                        new_dir = os.path.join(expedia_dir, "values-%s" % postfix)
                    elif "AndroidUtils" in dir_path:
                        new_dir = os.path.join(utils_dir, "values-%s" % postfix)
                    elif "HotelLib" in dir_path:
                        new_dir = os.path.join(hotellib_dir, "values-%s" % postfix)
                    else:
                        print("Not sure where this file should go: %s" % filepath)

                    if new_dir is not None:
                        os.mkdir(new_dir)
                        shutil.copy(filepath, os.path.join(new_dir, "strings.xml"))
                elif filename == "oosdk_comment_card.xml":
                    new_dir = None
                    if "ExpediaBookings" in dir_path:
                        new_dir = os.path.join(expedia_dir, "xml-%s" % postfix)
                    else:
                        print("Not sure where this file should go: %s" % filepath)

                    if new_dir is not None:
                        os.mkdir(new_dir)
                        shutil.copy(filepath, os.path.join(new_dir, "oosdk_comment_card.xml"))
                elif filename == ".DS_Store":
                    # Ignore this annoying file
                    pass
                else:
                    print("Saw unknown file: %s" % filename)

if __name__ == "__main__":
    usage = "usage: %prog [options]"
    parser = OptionParser(usage=usage)
    parser.add_option('-d', '--dir', action="store", help="Target input directory", default=".")
    parser.add_option('-o', '--out', action="store", help="Target out directory", default="out")
    
    (options, args) = parser.parse_args()

    move_files(options.dir, options.out)
