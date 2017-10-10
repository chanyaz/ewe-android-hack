package com.mobiata.android;

/**
 * A utility class for Android logging.  Allows one to easily shut on/off
 * all logging at once, also you don't have to think about tags.
 * 
 * Future ideas: allowing this to gather all logging messages, and thus 
 * being able to send us logs from a user easily.
 */
public class Log {
	public static final int LEVEL_VERBOSE = 0;
	public static final int LEVEL_DEBUG = 1;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARNING = 3;
	public static final int LEVEL_ERROR = 4;
	public static final int LEVEL_NONE = 5;

	// These are defaults that should be overridden before use
	private static String mTag = "Mobiata";
	private static int mLevel = 0;

	public static void configureLogging(String tag, boolean enabled) {
		mTag = tag;
		mLevel = (enabled) ? LEVEL_VERBOSE : LEVEL_NONE;
	}

	public static void configureLogging(String tag, int level) {
		mTag = tag;
		mLevel = level;
	}

	public static boolean isLoggingEnabled() {
		return mLevel != LEVEL_NONE;
	}

	// All the below methods are just implementations of standard Android logging
	public static int v(String msg) {
		if (mLevel <= LEVEL_VERBOSE) {
			return android.util.Log.v(mTag, msg);
		}
		return 0;
	}

	public static int v(String tag, String msg) {
		if (mLevel <= LEVEL_VERBOSE) {
			return android.util.Log.v(tag, msg);
		}
		return 0;
	}

	public static int v(String msg, Throwable tr) {
		if (mLevel <= LEVEL_VERBOSE) {
			return android.util.Log.v(mTag, msg, tr);
		}
		return 0;
	}

	public static int v(String tag, String msg, Throwable tr) {
		if (mLevel <= LEVEL_VERBOSE) {
			return android.util.Log.v(tag, msg, tr);
		}
		return 0;
	}

	public static int d(String msg) {
		if (mLevel <= LEVEL_DEBUG) {
			return android.util.Log.d(mTag, msg);
		}
		return 0;
	}

	public static int d(String tag, String msg) {
		if (mLevel <= LEVEL_DEBUG) {
			return android.util.Log.d(tag, msg);
		}
		return 0;
	}

	public static int d(String msg, Throwable tr) {
		if (mLevel <= LEVEL_DEBUG) {
			return android.util.Log.d(mTag, msg, tr);
		}
		return 0;
	}

	public static int d(String tag, String msg, Throwable tr) {
		if (mLevel <= LEVEL_DEBUG) {
			return android.util.Log.d(tag, msg, tr);
		}
		return 0;
	}

	public static int i(String msg) {
		if (mLevel <= LEVEL_INFO) {
			return android.util.Log.i(mTag, msg);
		}
		return 0;
	}

	public static int i(String tag, String msg) {
		if (mLevel <= LEVEL_INFO) {
			return android.util.Log.i(tag, msg);
		}
		return 0;
	}

	public static int i(String msg, Throwable tr) {
		if (mLevel <= LEVEL_INFO) {
			return android.util.Log.i(mTag, msg, tr);
		}
		return 0;
	}

	public static int i(String tag, String msg, Throwable tr) {
		if (mLevel <= LEVEL_INFO) {
			return android.util.Log.i(tag, msg, tr);
		}
		return 0;
	}

	public static int w(String msg) {
		if (mLevel <= LEVEL_WARNING) {
			return android.util.Log.w(mTag, msg);
		}
		return 0;
	}

	public static int w(String tag, String msg) {
		if (mLevel <= LEVEL_WARNING) {
			return android.util.Log.w(tag, msg);
		}
		return 0;
	}

	public static int w(String msg, Throwable tr) {
		if (mLevel <= LEVEL_WARNING) {
			return android.util.Log.w(mTag, msg, tr);
		}
		return 0;
	}

	public static int w(String tag, String msg, Throwable tr) {
		if (mLevel <= LEVEL_WARNING) {
			return android.util.Log.w(tag, msg, tr);
		}
		return 0;
	}

	public static int e(String msg) {
		if (mLevel <= LEVEL_ERROR) {
			return android.util.Log.e(mTag, msg);
		}
		return 0;
	}

	public static int e(String tag, String msg) {
		if (mLevel <= LEVEL_ERROR) {
			return android.util.Log.e(tag, msg);
		}
		return 0;
	}

	public static int e(String msg, Throwable tr) {
		if (mLevel <= LEVEL_ERROR) {
			return android.util.Log.e(mTag, msg, tr);
		}
		return 0;
	}

	public static int e(String tag, String msg, Throwable tr) {
		if (mLevel <= LEVEL_ERROR) {
			return android.util.Log.e(tag, msg, tr);
		}
		return 0;
	}

	/**
	 * This logs the current stack trace printing n levels up
	 * @param n The number of levels up the stack to print, 0 for all levels
	 * @param msg msg to print for easier filtering
	 */
	public static void stackTrace(int n, String msg) {
		try {
			throw new RuntimeException();
		}
		catch (RuntimeException e) {
			Log.d("Printing Stack Trace: " + msg);

			StackTraceElement[] elements = e.getStackTrace();
			int length;
			if (n == 0) {
				length = elements.length;
			}
			else {
				length = Math.min(elements.length, n+1);
			}

			for (int i = 1; i < length; i ++) {
				StackTraceElement element = elements[i];
				Log.d("    " + element.toString());
			}
		}
	}

	/**
	 * This is a special test logger.  It enables on LEVEL_VERBOSE, and always spits out
	 * its output to the tag "test".  It can also help by automatically formatting arguments.
	 * @param msg the logging message
	 * @param args arguments for String.format(), to be applied to msg
	 * @return a number!
	 */
	public static int t(String msg, Object... args) {
		if (mLevel <= LEVEL_VERBOSE) {
			return android.util.Log.v("test", String.format(msg, args));
		}
		return 0;
	}

	// The maximum number of characters to dump on each line via dump()
	private static final int DUMP_LENGTH = 4000;

	public static void dump(String longMsg) {
		dump(mTag, longMsg, LEVEL_INFO);
	}

	public static void dump(String longMsg, int level) {
		dump(mTag, longMsg, level);
	}

	public static void dump(String tag, String longMsg) {
		dump(tag, longMsg, LEVEL_INFO);
	}

	/**
	 * This is a developer tool for dumping an extraordinarily long message to logcat.
	 * @param longMsg
	 */
	public static void dump(String tag, String longMsg, int level) {
		int len = longMsg.length();
		String curr;
		for (int a = 0; a < len; a += DUMP_LENGTH) {
			if (a + DUMP_LENGTH < len) {
				curr = longMsg.substring(a, a + DUMP_LENGTH);
			}
			else {
				curr = longMsg.substring(a);
			}

			switch (level) {
			case LEVEL_ERROR:
				Log.e(tag, curr);
				break;
			case LEVEL_WARNING:
				Log.w(tag, curr);
				break;
			case LEVEL_INFO:
				Log.i(tag, curr);
				break;
			case LEVEL_DEBUG:
				Log.d(tag, curr);
				break;
			case LEVEL_VERBOSE:
			default:
				Log.v(tag, curr);
				break;
			}
		}
	}
}
