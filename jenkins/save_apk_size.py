import os
import sys

brand = sys.argv[1]
size = round(os.stat('project/build/outputs/apk/{brand}/debug/project-{brand}-debug.apk'.format(brand=brand)).st_size / float(1024*1024), 2)
apk_size_file = open("apkSize.txt", "w")
apk_size_file.write(str(size))
apk_size_file.close()
