package com.expedia.layouttestandroid.runner

import android.os.Bundle
import android.support.test.runner.AndroidJUnitRunner

class LayoutTestRunner : AndroidJUnitRunner() {
    override fun onCreate(args: Bundle) {
        //    ScreenshotRunner.onCreate(this, args);
        super.onCreate(args)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        //    ScreenshotRunner.onDestroy();
        super.finish(resultCode, results)
    }
}
