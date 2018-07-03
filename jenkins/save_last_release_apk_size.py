import os
import sys

target_name = sys.argv[1]


def get_build_size():
    return round(os.stat('project/build/outputs/apk/{target}/release/project-{target}-release.apk'
                         .format(target=target_name)).st_size / float(1024 * 1024), 2)


def save_build_size_to_file(buildSize):
    print("File size is : " + str(buildSize))
    release_apk_size_file = open("releaseApkSize.txt", "w")
    release_apk_size_file.write(str(buildSize))
    release_apk_size_file.close()


try:
    save_build_size_to_file(get_build_size())
except Exception as e:
    print "Exception encountered while trying to fetch latest Release APK size. Exception Trace below : "
    print e
