#!/usr/bin/env python3

import sys
import json
import time
from subprocess import Popen
from subprocess import PIPE
from multiprocessing import Queue, Pool

#Used for testing purposes, otherwise it is overwritten by input params
jsonParam = {
    "_comment":"THIS IS TEST DATA",
    "flavor":"Expedia",
    "package":"com.expedia.bookings",
    "debug":"false",
    "features":[
        "Features/Hotels/hotelSearch.feature",
        "Features/Hotels/RC_HotelSearchByName.feature",
        "Features/Hotels/RC_HotelSearchResults_VIP.feature",
        "Features/Hotels/RC_HotelETPWithoutDeposit.feature",
        "Features/Hotels/RC_HotelResortFee.feature",
        "Features/Hotels/RC_HotelDepositAndResortFeeOnHIS.feature",
        "Features/Checkout/HotelsCheckoutWebView.feature",
        "Features/Trips/RC_TripDetails.feature",
        "Features/Trips/RC_Trips.feature"
    ],
    "tags":[
        "~@manual","~@deprecated","~@todo","~@wip","~@ignore"
    ]
}

deviceQueue=Queue()


def runShell(command):
    return Popen(command, stdout=PIPE).communicate()


def getDevices():
    out = str(runShell(["adb", "devices"])[0])
    if out.__contains__("\\n"):
        list=out.split("\\n")
    else:
        list=out.split("\n")
    devices=[]
    list.pop(0)

    for line in list:
        if line.__contains__("device"):
            device=[]
            if line.__contains__("\\t"):
                device=line.split("\\t")
            else:
                device=line.split("\t")

            if len(device) == 2:
                devices.append(device[0])

    return devices


def deviceRunner(feature, tags, device):
    removeDummyFiles(device)
    createDummyFiles(device)

    command=["adb", "-s", device, "shell", "am", "instrument", "-w", "-r","-e","debug",jsonParam['debug']]
    instrumentation="com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner"

    if feature != "":
        command.append("-e")
        command.append("features")
        command.append(feature)

    if len(tags)>0:
        for tagSet in tags:
            command.append("-e")
            command.append("tags")
            command.append(tagSet)

    command.append(instrumentation)
    if jsonParam["debug"] == "true":
        print("LAUNCHING TEST IN DEBUG MODE. AWAITING DEBUGGER ATTACHMENT")

    print("Executing '"+feature+"' on "+device)
    startTime = time.time()
    out=runShell(command)
    print(str(int(time.time()-startTime))+" seconds runtime for "+feature+"' on "+device)
    downloadReport(device,feature)
    return out


def setDeviceQueue():
    devices=getDevices()
    for device in devices:
        deviceQueue.put(device)


def thread(feature):
    device=deviceQueue.get(True,5)
    deviceRunner(feature,jsonParam['tags'],device)
    deviceQueue.put(device,True,5)


def runMultiThreaded():
    pool = Pool(processes=len(getDevices()))
    pool.imap(thread, jsonParam['features'])

    deviceQueue.close()
    pool.close()
    deviceQueue.join_thread()
    pool.join()


def createDummyFiles(device):
    runShell(["adb","-s",device,"shell","mkdir","/data/local/tmp/cucumber-htmlreport"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/cucumber.json"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/formatter.js"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/index.html"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/jquery-1.8.2.min.js"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/report.js"])
    runShell(["adb","-s",device,"shell","touch","/data/local/tmp/cucumber-htmlreport/style.css"])


def removeDummyFiles(device):
    runShell(["adb","-s",device,"shell","rm","-rf","/data/local/tmp/cucumber-htmlreport"])
    runShell(["adb","-s",device,"shell","rm","-rf","/sdcard/cucumber-images"])


def downloadReport(device, feature):
    systemReportDir="project/build/outputs/report/"+feature+"/"
    deviceReportDir="/data/local/tmp/cucumber-htmlreport/"
    runShell(["mkdir","-p",systemReportDir])
    runShell(["adb","-s",device,"pull",deviceReportDir,systemReportDir])


def main():
    global jsonParam

    setDeviceQueue()
    if len(sys.argv) > 1:
        jsonParam = json.loads(sys.argv[1])

    #Keep for debug use.
    #print deviceRunner("Features/Trips/RC_Trips.feature",
    #                        jsonParam['tags'],
    #                        "emulator-5554")[0]
    runMultiThreaded()

if __name__ == "__main__":
    main()
