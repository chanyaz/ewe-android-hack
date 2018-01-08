package com.mobiata.android.util;

import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.mobiata.android.Log;
import com.mobiata.android.Params;
import com.mobiata.android.R;

public class AndroidUtils {
	public static boolean isTablet(Context context) {
		return context.getResources().getBoolean(R.bool.tablet);
	}

	/**
	 * Returns the code of the app.  Returns 0 if it cannot be determined.
	 *
	 * @param context the context of the app
	 * @return the version code for the app, or 0 if there is no versionCode
	 */
	public static int getAppCode(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		}
		catch (Exception e) {
			// PackageManager is traditionally wonky, need to accept all exceptions here.
			Log.w(Params.LOGGING_TAG, "Couldn't get package info in order to show version code #!", e);
			return 0;
		}
	}

	/**
	 * Projects that integrate with HockeyApp may define their buildNumber in a meta-data tag. If
	 * that is the case, we want to display this number to QA on the About page.
	 * @param context
	 * @return
	 */
	public static String getAlphaBuildNumber(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return Integer.toString(ai.metaData.getInt("buildNumber"));
		}
		catch (Exception e) {
			Log.w("Unable to retrieve \"buildNumber\" meta-data from AndroidManifest", e);
			return "";
		}
	}

	private static final Signature SIG_RELEASE = new Signature(
			"30820253308201bca00302010202044ae85721300d06092a864886f70d0101050500306d310b30090603550406130255533111300f060355040813084d6963686967616e3112301006035504071309416e6e204172626f723110300e060355040a13074d6f62696174613110300e060355040b1307436f6d70616e79311330110603550403130a44616e69656c204c65773020170d3039313032383134333732315a180f32303931313231373134333732315a306d310b30090603550406130255533111300f060355040813084d6963686967616e3112301006035504071309416e6e204172626f723110300e060355040a13074d6f62696174613110300e060355040b1307436f6d70616e79311330110603550403130a44616e69656c204c657730819f300d06092a864886f70d010101050003818d0030818902818100b857f2951c2e4105084e8ee9aacfdfa964a9ee1d5e56d6146df081e766f9ebaa0ae7f2a19c22a034cccc8ac915da8a2e9ee878676db4df67fa4f199cd33ea117e389f062f2e8c31467ca632a8c9832d8e9335796f38ece378e0a273e62b02847678c4f494f29b9c59daa268565f860cdd64e042b404fb8e9c3b5e8c48ec149010203010001300d06092a864886f70d0101050500038181004b07fdf8bd9998daac19baad5b139409d453a0e1a11511afd05dcbb61b0a214f970244aba5deeeeee06beff11624bf5b6eca95ba04009d79cf0cf6986be7dc269cb9e211c7d4c706721dc21090efc98a4cd968459a1254d9ac061f9b1c3abe17bfd289ea93ccff8022104672c9292957db4876270b7408ac51e35edc8800b912");

	private static final Signature SIG_RELEASE_VSC = new Signature(
			"308203423082022aa003020102020451f692b2300d06092a864886f70d01010505003062310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653110300e060355040a130745787065646961310c300a060355040b1303565343311330110603550403130a44616e69656c204c65773020170d3133303732393136303530365a180f32303638303530313136303530365a3062310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653110300e060355040a130745787065646961310c300a060355040b1303565343311330110603550403130a44616e69656c204c657730820122300d06092a864886f70d01010105000382010f003082010a0282010100a716a3aee9586395b3b66338ccbf684174054352c2a26e8e702bd33d52fac31f92519a5becf37b1c57d36f935d59088753d3ba46070859379df39f8765a232a4f7e6f0f4a31a093e78cbf35375d4696d11a52e01dbd9799b0f9757303f25fc10631c0f0bfa71081f8565b05319cb3ae6daa31866511626e73ae6e85537089069c5f53d980a085279aaccf7f150aa23b2eb9863462611dbc2e3a895d80bf77c67928c7ba03d406cda8c07c6b67a955f29f8a8170b083b642a4ef220bb229c833d9b7b581b38e14c88c9a256c95fa46e47580850a3882129da57aa8055935ae2837d7d83ff5accca9004e0f08b453ce20d2a5e3f6b538a46238658b57cb46d21a10203010001300d06092a864886f70d010105050003820101006684e360839f0677a75d2cccfff58897c165fac023af88c066bc5c6b19c3f092e529c291418dfc576cfd7ad3a0b4795c277a50701cb71b39a0c7bc427b3fc387453e40119263f209aed746ec52deca14fa1dbe84d899b38145d81d601ea6126a6448baa5d8e619e9b37312c2e7922123034a6cba08b3501c42fe09318db5ba62813d846b6fad5d4311b6783b70ff9094e86d08e6812f03031915935693ddca7ec18a3491eb35e68a5cdf388ed96177e61c201c260314aee4195f54beb4b529ee8633c852a5ffce0e7229a87cb253c4f756ae88dfc161aed46250ffcded1955f54dcb2d06881c87b93529e901a646253f842056b2ce91239eb22e9357ca91687a");

	private static final Signature SIG_RELEASE_TRAVELOCITY = new Signature(
			"3082035e30820246a00302010202044e0cca7a300d06092a864886f70d01010505003071310b3009060355040613025553310b30090603550408130254583112301006035504071309536f7574686c616b65310e300c060355040a1305536162726531143012060355040b130b54726176656c6f63697479311b30190603550403131254726176656c6f63697479204d6f62696c65301e170d3131303633303139313135345a170d3338313131353139313135345a3071310b3009060355040613025553310b30090603550408130254583112301006035504071309536f7574686c616b65310e300c060355040a1305536162726531143012060355040b130b54726176656c6f63697479311b30190603550403131254726176656c6f63697479204d6f62696c6530820122300d06092a864886f70d01010105000382010f003082010a02820101009a8c80a1a76e61f35de43a1c4b77d2d0cc2045b4bc6b684c3142832331443c6d65251479f3f768e141bdaf550f0aea6483a123fe80a2e0d0d346bca866e29a30bd23c51f8ad48f618d2c847c6a5164a30d58842974a77f6b5499952b9b941ffcfb191725f70ecef2f46a2b653900e583656e4cc9323aca1c74ce9fcfd2266033e30e95238290c12d3cbe92077fab66c0bfc67d82be14cba2eb32bfcc103d82fe7420e6ed003cd626cd4533a77f9150cdc9ba824e3ec7931f05c540aefd20a4e91553bb0bf8e14cd6754887ea0444b8ac687e7eb895826b5cfba82c0310c2427cb70cfbfaeaf627f60de4de394881090e38e25316ead62dd0ffa925d6068a31190203010001300d06092a864886f70d0101050500038201010068c971418bcd91e3ef1a45c3e92a9109bfa4eea98674eacc24bff587b9c60424cb583836aac707fd9d22669c694684e6caba56fdfd79b6a6618ed21d6756dedfa54dbcce8c3216d9c0744145b339a63df30d754e5ed5e9c27a5c16ef575f26bee4307019caee324259220bc3b7fbe9ce67d79c1477a90e2e7bfecd266c7ae6da41a44d79f46c59ef37f84848f3b169e839a3bf8e8132fb9625dc1a734c82cb9d399f5000dd02b44277689014d09bf06e3a06c3f679a52fe0626f42a17a47a589548e7836571834442ae086378fe34b5ff535d5fac9c925cac06fc96cedd8acb1202d1ba49bcbd58042c741e6bc186a2708aaeb2d46b6d9c0c671783118bec96f");

	private static final Signature SIG_RELEASE_AIR_ASIA_GO = new Signature(
			"30820263308201cca00302010202045423aa68300d06092a864886f70d01010505003075310b300906035504061302494e3110300e0603550408130748617279616e613110300e0603550407130747757267616f6e31123010060355040a130941697241736961476f31123010060355040b13094147474d6f62696c65311a3018060355040313117777772e61697261736961676f2e636f6d3020170d3134303932353035333834385a180f32303634303931323035333834385a3075310b300906035504061302494e3110300e0603550408130748617279616e613110300e0603550407130747757267616f6e31123010060355040a130941697241736961476f31123010060355040b13094147474d6f62696c65311a3018060355040313117777772e61697261736961676f2e636f6d30819f300d06092a864886f70d010101050003818d00308189028181008340dc024519536976b8a1c60f646226e85f415c94862599755a53608f9e111f3d33bd9439df9e36befc2ac8e6b52e7118afc68ec6359f80d373f72d386aac1f44c4930ef06b998886b44ac14cfe8f0bd54966c733189cdf8f0c145ee86b1534b5d461b8bd6c23a03894141153f160e8ffa7d6c30b17fadb494e5431eb585f950203010001300d06092a864886f70d0101050500038181001855e634eea8ab2d2ca0bd1b1cf7d3fd2c981e4bc819a82d30bcc9077f085a17fe98ed374ae1236fce27e365fd63bad836f68a5c0a946be0f8a71bd198729d0ae1c6ab2d4ab74011ba67c7a966407750a45325cb5599089dcb874b89b3b8d6c7366306e50c5da2e4006a11175368dc27b1175638ecf6e491e63fabd535c262ec");

	private static Boolean sIsRelease = null;

	/**
	 * Determines if the app was signed using our debug or release keystore.
	 *
	 * If there's an error during checking (since this uses PackageManager), it will
	 * default to returning "true" for safety's sake.
	 *
	 * @param context the app context
	 * @return true if signed using our release keystore, false for debug.
	 */
	public static boolean isRelease(Context context) {
		if (sIsRelease == null) {
			try {
				PackageManager pm = context.getPackageManager();
				PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
				Signature checkSignature = new Signature("");
				if (context.getPackageName().contains("vsc")) {
					checkSignature = SIG_RELEASE_VSC;
				}
				else if (context.getPackageName().contains("travelocity")) {
					checkSignature = SIG_RELEASE_TRAVELOCITY;
				}
				else if (context.getPackageName().contains("airasiago")) {
					checkSignature = SIG_RELEASE_AIR_ASIA_GO;
				}
				else {
					checkSignature = SIG_RELEASE;
				}
				for (Signature sig : pi.signatures) {
					if (sig.equals(checkSignature)) {
						Log.i("Determined that this is a RELEASE build.");
						sIsRelease = true;
					}
				}
			}
			catch (Exception e) {
				Log.w("Exception thrown when detecting if app is signed by a release keystore, assuming this is a release build.",
						e);

				// Return true if we can't figure it out
				sIsRelease = true;
			}

			if (sIsRelease == null) {
				Log.i("Determined that this is a DEBUG build.");
				sIsRelease = false;
			}
		}

		return sIsRelease;
	}

	public static Calendar getAppBuildDate(Context context) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(time);
			return cal;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the screen width/height, as a Point (x is width, y is height).
	 */
	@TargetApi(13)
	@SuppressWarnings("deprecation")
	public static Point getScreenSize(Context context) {
		Point size = new Point();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		size.x = metrics.widthPixels;
		size.y = metrics.heightPixels;
		return size;
	}


	/**
	 * Similar to AndroidUtils.getScreenSize(), but takes the notification and nav bar in to account to
	 * provide a consistent experience across different orientations, API levels, and hardware.
	 * @param context
	 * @return
	 */
	public static Point getDisplaySize(Context context) {
		Point size = new Point();

		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		size.x = metrics.widthPixels;
		size.y = metrics.heightPixels;

		try {
			Point realSize = new Point();
			Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
			size.x = realSize.x;
			size.y = realSize.y;
		}
		catch (Exception ignored) {
		}

		return size;
	}

	/**
	 * Returns the true if package with packageName exists on the system, false otherwise
	 */
	public static boolean isPackageInstalled(Context context, final String packageName) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			// We would have blown up if the package didn't exist
			return true;
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * @param packageName PackageName of the app in the current market
	 * @return market link to the app.
	 */
	public static String getMarketAppLink(Context context, String packageName) {
		String marketPrefix = context.getString(R.string.market_prefix);
		return marketPrefix + packageName;
	}

	/**
	 * Are we currently using a restricted profile?
	 * https://developer.android.com/about/versions/android-4.3.html#RestrictedProfiles
	 */
	public static boolean isRestrictedProfile(Context context) {
		UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
		Bundle restrictions = um.getUserRestrictions();
		return restrictions.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
	}

	public static int getScreenDpi(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.densityDpi;
	}

	public static String getScreenDensityClass(Context context) {

		String densityClass;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float density = metrics.density;

		if (density >= 4.0) {
			densityClass = "xxxhdpi";
		}
		else if (density >= 3.0) {
			densityClass = "xxhdpi";
		}
		else if (density >= 2.0) {
			densityClass = "xhdpi";
		}
		else if (density >= 1.5) {
			densityClass = "hdpi";
		}
		else if (density >= 1.0) {
			densityClass = "mdpi";
		}
		else {
			densityClass = "ldpi";
		}

		return densityClass;
	}
}
