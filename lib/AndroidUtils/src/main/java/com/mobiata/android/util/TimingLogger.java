/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiata.android.util;

import java.util.ArrayList;

import android.os.SystemClock;

import com.mobiata.android.Log;

/**
 * This is a version of the TimingLogger that uses our internal com.mobiata.android.Log class
 * to determine whether to enable dumping or not.  No more having to use adb shell to get
 * TimingLogger to work!
 */
public class TimingLogger {

    /**
     * The Log tag to use for checking Log.isLoggable and for
     * logging the timings.
     */
    private String mTag;

    /** A label to be included in every log. */
    private String mLabel;

    /** Used to track whether Log.isLoggable was enabled at reset time. */
    private boolean mDisabled;

    /** Stores the time of each split. */
    ArrayList<Long> mSplits;

    /** Stores the labels for each split. */
    ArrayList<String> mSplitLabels;

    /**
     * Create and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the addSplit and dumpToLog call will do nothing.
     * @param tag the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    public TimingLogger(String tag, String label) {
        reset(tag, label);
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the addSplit and dumpToLog call will do nothing.
     * @param tag the log tag to use while logging the timings
     * @param label a string to be displayed with each log
     */
    public void reset(String tag, String label) {
        mTag = tag;
        mLabel = label;
        reset();
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the tag and label that was specified previously, either via
     * the constructor or a call to reset(tag, label). If the
     * Log.isLoggable is not enabled to at least the Log.VERBOSE
     * level for that tag at creation time then the addSplit and
     * dumpToLog call will do nothing.
     */
    public void reset() {
        mDisabled = !Log.isLoggingEnabled();
        if (mDisabled) return;
        if (mSplits == null) {
            mSplits = new ArrayList<>();
            mSplitLabels = new ArrayList<>();
        } else {
            mSplits.clear();
            mSplitLabels.clear();
        }
        addSplit(null);
    }

    /**
     * Add a split for the current time, labeled with splitLabel. If
     * Log.isLoggable was not enabled to at least the Log.VERBOSE for
     * the specified tag at construction or reset() time then this
     * call does nothing.
     * @param splitLabel a label to associate with this split.
     */
    public void addSplit(String splitLabel) {
        if (mDisabled) return;
        long now = SystemClock.elapsedRealtime();
        mSplits.add(now);
        mSplitLabels.add(splitLabel);
    }

    /**
     * Dumps the timings to the log using Log.d(). If Log.isLoggable was
     * not enabled to at least the Log.VERBOSE for the specified tag at
     * construction or reset() time then this call does nothing.
     */
    public void dumpToLog() {
        if (mDisabled) return;
        Log.d(mTag, mLabel + ": begin");
        final long first = mSplits.get(0);
        long now = first;
        for (int i = 1; i < mSplits.size(); i++) {
            now = mSplits.get(i);
            final String splitLabel = mSplitLabels.get(i);
            final long prev = mSplits.get(i - 1);

            Log.d(mTag, mLabel + ":      " + (now - prev) + " ms, " + splitLabel);
        }
        Log.d(mTag, mLabel + ": end, " + (now - first) + " ms");
    }
}
