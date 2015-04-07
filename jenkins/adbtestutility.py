import os
import sys
import os.path


# Methods

def getPathToScreenshotDir():
    return sys.argv[2]


def sendTestCommand(testPackage, apk, testApk):
    spoon = '../jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar'
    adbTestCommand = \
        'java -jar %(spoon)s --apk %(apk)s --test-apk %(testApk)s --class-name %(testPackage)s --no-animations' \
        % locals()
    print adbTestCommand
    os.system(adbTestCommand)


def pullScreenshots(targetDirectory):
    pullScreenCommand = 'cp -a spoon-output/image/ ' + targetDirectory
    print pullScreenCommand
    os.system(pullScreenCommand)
    
    pullHTMLCommand = 'cp -a spoon-output/ ' + targetDirectory + '/spoon/'
    print pullHTMLCommand
    os.system(pullHTMLCommand)



