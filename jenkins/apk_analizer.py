import os
import shutil
import sys
import subprocess


class ApkAnalizer:
    def __init__(self, brand, releaseType):
        self.rootPath = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        self.apkArchivePath = os.path.join(self.rootPath, "apkArchive")
        self.apkOutputPath = os.path.join(self.rootPath, "project", "build", "outputs", "apk")
        self.analysisOutputFile = os.path.join(self.apkArchivePath, "output.txt")
        self.apkPatchSizeEstimatorPath = os.path.join(self.rootPath, "lib", "apk-patch-size-estimator")
        self.apkNamePrefix = "project"
        self.extention = ".apk"
        self.apkName = self.apkNamePrefix + "-" + brand + "-" + releaseType + self.extention
        self.srcCopyPath = os.path.join(self.apkOutputPath, self.apkName)

    # copy apk from output folder to archive folder
    def copyApkFromOutputToArchive(self, src, destination):
        if not self.pathContainsGivenApk(self.apkOutputPath, self.apkName):
            print "Output apk not Found"
            exit(1)
        else:
            shutil.copy2(src, destination)

    # path contains given apk
    def pathContainsGivenApk(self, givenPath, apkName):
        apkFiles = os.listdir(givenPath)
        return apkName in apkFiles

    def subprocess_cmd(self, command):
        process = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
        proc_stdout = process.communicate()[0].strip()
        return proc_stdout

    # run analizer
    def runApkAnalizer(self, apkPatchSizeEstimatorPath, oldApk, newApk):
        print "Running apk analizer..."
        cmd = " python apk_patch_size_estimator.py  --old-file " + oldApk + " --new-file " + newApk
        output = self.subprocess_cmd("cd " + apkPatchSizeEstimatorPath + "; "+cmd )
        with open(self.analysisOutputFile, 'wb') as file:
            file.write(output)

    def getStarted(self):
        if not os.path.exists(self.apkArchivePath):
            print "Archive apk not found. Copying..."
            os.makedirs(self.apkArchivePath)
            # coping for first time
            self.copyApkFromOutputToArchive(self.srcCopyPath, self.apkArchivePath)
            exit()
        else:
            self.runApkAnalizer(self.apkPatchSizeEstimatorPath, os.path.join(self.apkArchivePath, self.apkName),
                                os.path.join(self.apkOutputPath, self.apkName))


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print "Please use like : apk_analizer.py brandName releaseType"
        exit(1)
    else:
        analizerObj = ApkAnalizer(sys.argv[1], sys.argv[2])
        analizerObj.getStarted()
