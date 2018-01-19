package com.expedia.account;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.expedia.account.data.AccountResponse;
import com.expedia.account.data.Db;
import com.expedia.account.data.PartialUser;
import com.expedia.account.presenter.BufferedPresenter;
import com.expedia.account.presenter.CompoundTransition;
import com.expedia.account.presenter.FadeTransition;
import com.expedia.account.presenter.LeftToRightTransition;
import com.expedia.account.presenter.OffScreenBottomTransition;
import com.expedia.account.presenter.RightToLeftTransition;
import com.expedia.account.presenter.SlideInBottomTransition;
import com.expedia.account.presenter.SlideUpTransition;
import com.expedia.account.recaptcha.Recaptcha;
import com.expedia.account.recaptcha.RecaptchaHandler;
import com.expedia.account.singlepage.SinglePageSignUpLayout;
import com.expedia.account.util.AccessibilityUtil;
import com.expedia.account.util.Events;
import com.expedia.account.util.FacebookViewHelper;
import com.expedia.account.util.PresenterUtils;
import com.expedia.account.util.SmartPasswordViewHelper;
import com.expedia.account.util.Utils;
import com.expedia.account.view.AnimatedIconToolbar;
import com.expedia.account.view.EmailNameLayout;
import com.expedia.account.view.FacebookAPIHostLayout;
import com.expedia.account.view.FacebookLayout;
import com.expedia.account.view.HeaderLayout;
import com.expedia.account.view.PasswordLayout;
import com.expedia.account.view.SignInLayout;
import com.expedia.account.view.TOSLayout;
import com.expedia.account.view.WelcomeLayout;
import com.squareup.otto.Subscribe;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import com.mobiata.android.Log;

public class AccountView extends BufferedPresenter {

	private SignInLayout vSignInLayout;
	private EmailNameLayout vEmailNameLayout;
	private SinglePageSignUpLayout vSinglePageSignUpLayout;
	private FacebookLayout vFacebookLayout;
	private FacebookAPIHostLayout vFacebookAPIHostLayout;
	private PasswordLayout vPasswordLayout;
	private TOSLayout vTOSLayout;
	private HeaderLayout vHeaderLayout;
	private WelcomeLayout vWelcomeLayout;
	private AnimatedIconToolbar vToolbar;
	private Toolbar vSinglePageToolbar;
	public View vSinglePageWhiteBackground;

	private Config mConfig;
	private String mBrand;

	private FacebookViewHelper mFacebookHelper;
	private SmartPasswordViewHelper mSmartLockHelper;
	private Disposable mCurrentDownload;
	private static final Boolean isMinimumAccessibilityAPI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

	// Flags for specifying whether to direct focus to these fields
	// after transition (as a result of a server-side error message)
	private boolean mFocusEmailAddress = false;
	private boolean mFocusFirstName = false;
	private boolean mFocusLastName = false;
	private boolean handleKeyBoardVisibilityChanges = true;

	private static final String TAG = "AccountView";

	private Observer<Boolean> mNextButtonController = new DisposableObserver<Boolean>() {
		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(Boolean enabled) {
			setNextEnabled(enabled);
		}
	};

	private Observer<Boolean> mLinkButtonController = new DisposableObserver<Boolean>() {
		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(Boolean enabled) {
			setLinkEnabled(enabled);
		}
	};

	public void configure(@NonNull Config config) {
		mConfig = config;

		vTOSLayout.configurePOS(
			mConfig.showSpamOptIn,
			mConfig.enableSpamByDefault,
			mConfig.hasUserRewardsEnrollmentCheck,
			mConfig.shouldAutoEnrollUserInRewards,
			mConfig.tosText,
			mConfig.marketingText,
			mConfig.rewardsText);

		vSignInLayout.configurePOS(
			mConfig.enableFacebookButton);

		if (mConfig.signupString != null) {
			vSignInLayout.configureAccountCreationString(mConfig.signupString);
		}

		vHeaderLayout.configurePOS(mConfig.enableSignInMessaging, mConfig.signInMessagingText);

		vSinglePageSignUpLayout.configurePOS(
			mConfig.showSpamOptIn,
			mConfig.enableSpamByDefault,
			mConfig.hasUserRewardsEnrollmentCheck,
			mConfig.shouldAutoEnrollUserInRewards,
			mConfig.tosText,
			mConfig.marketingText,
			mConfig.rewardsText);

		if (mConfig.facebookAppId != null) {
			mFacebookHelper = createFacebookViewHelper();
		}

		if (!STATE_SIGN_IN.equals(mConfig.initialState)) {
			show(mConfig.initialState, FLAG_CLEAR_BACKSTACK | FLAG_SKIP_ANIMATION_TIME);
		}

		if (mConfig.parentActivity != null && STATE_SIGN_IN.equals(mConfig.initialState)) {
			mSmartLockHelper = createSmartPasswordViewHelper(mConfig.getAnalyticsListener(), config.parentActivity);
		}
	}

	public FacebookViewHelper createFacebookViewHelper() {
		return new FacebookViewHelper(this);
	}

	public SmartPasswordViewHelper createSmartPasswordViewHelper(AnalyticsListener analyticsListener,
		FragmentActivity currentActivity) {
		return new SmartPasswordViewHelper(analyticsListener, currentActivity);
	}


	public AccountService getService() {
		return mConfig == null ? null : mConfig.getService();
	}

	public void setAnalyticsListener(AnalyticsListener analyticsListener) {
		mConfig.setAnalyticsListener(analyticsListener);
	}

	public void setListener(Listener listener) {
		mConfig.setListener(listener);
	}

	private View.OnClickListener mNavigationClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			up();
		}
	};

	private Toolbar.OnMenuItemClickListener mNextClickMenuListener = new Toolbar.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if (item.getItemId() == R.id.action_next) {
				onNextClicked();
				return true;
			}
			else if (item.getItemId() == R.id.action_link) {
				if (mFacebookHelper != null) {
					mFacebookHelper.onLinkClicked();
				}
			}
			return false;
		}
	};

	public abstract static class Listener {
		public abstract void onSignInSuccessful();

		public abstract void onFacebookSignInSuccess();

		public abstract void onSignInCancelled();

		public abstract void onFacebookRequested();

		public abstract void onFacebookClicked();

		public abstract void onForgotPassword();

		void onOverallProgress(boolean forward, float percent) {
		}

		void onObscureBackgroundDesired(float amount) {
		}
	}

	public static final String STATE_SIGN_IN = "STATE_SIGN_IN";
	public static final String STATE_EMAIL_NAME = "STATE_EMAIL_NAME";
	public static final String STATE_PASSWORD = "STATE_PASSWORD";
	public static final String STATE_TOS = "STATE_TOS";
	public static final String STATE_LOADING_ACCOUNT = "STATE_LOADING_ACCOUNT";
	public static final String STATE_LOADING_SINGLE_PAGE = "STATE_LOADING_SINGLE_PAGE";
	public static final String STATE_LOADING_SIGN_IN = "STATE_LOADING_SIGN_IN";
	public static final String STATE_LOADING_FACEBOOK = "STATE_LOADING_FACEBOOK";
	public static final String STATE_WELCOME = "STATE_WELCOME";
	public static final String STATE_FACEBOOK_API_HOST = "STATE_FACEBOOK_API_HOST";
	public static final String STATE_FACEBOOK = "STATE_FACEBOOK";
	public static final String STATE_SINGLE_PAGE_SIGN_UP = "STATE_SINGLE_PAGE_SIGN_UP";

	public AccountView(Context context, AttributeSet attrs) {
		super(context, attrs);

		inflate(context, R.layout.acct__widget_parent, this);

		setClipChildren(false);
		setClipToPadding(false);

		vSignInLayout = (SignInLayout) findViewById(R.id.parent_sign_in_layout);
		vEmailNameLayout = (EmailNameLayout) findViewById(R.id.parent_create_account_email_name_layout);
		vSinglePageSignUpLayout = (SinglePageSignUpLayout) findViewById(R.id.parent_create_account_single_page_layout);
		vFacebookAPIHostLayout = (FacebookAPIHostLayout) findViewById(R.id.parent_facebook_api_host_layout);
		vFacebookLayout = (FacebookLayout) findViewById(R.id.parent_facebook_layout);
		vPasswordLayout = (PasswordLayout) findViewById(R.id.parent_create_account_password_layout);
		vTOSLayout = (TOSLayout) findViewById(R.id.parent_create_account_tos_layout);
		vHeaderLayout = (HeaderLayout) findViewById(R.id.parent_user_image_presenter);
		vToolbar = (AnimatedIconToolbar) findViewById(R.id.toolbar);
		vWelcomeLayout = (WelcomeLayout) findViewById(R.id.welcome_loading_container);
		vSinglePageToolbar = (Toolbar) findViewById(R.id.single_page_toolbar);

		vToolbar.setNavigationOnClickListener(mNavigationClickListener);
		vToolbar.setOnMenuItemClickListener(mNextClickMenuListener);
		vToolbar.showNavigationIconAsX();
		vToolbar.inflateMenu(R.menu.acct__menu_account_creation);
		vSinglePageToolbar.setNavigationOnClickListener(mNavigationClickListener);
		vSinglePageToolbar.setNavigationContentDescription(R.string.acct__Toolbar_nav_close_icon_cont_desc);
		styleize(context, attrs);
		brandIt();
	}

	private void styleize(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.acct__AccountView);
			verifyRequiredAttrs(a);
			mBrand = a.getString(R.styleable.acct__AccountView_acct__brand);

			vSignInLayout.styleizeFromAccountView(a);
			vEmailNameLayout.styleizeFromAccountView(a);
			vFacebookLayout.styleizeFromAccountView(a);
			vPasswordLayout.styleizeFromAccountView(a);
			vTOSLayout.styleizeFromAccountView(a);
			vHeaderLayout.styleizeFromAccountView(a);
			vToolbar.styleizeFromAccountView(a);
			vWelcomeLayout.styleizeFromAccountView(a);
			vSinglePageSignUpLayout.styleizeFromAccountView(a);
			a.recycle();
		}
	}

	private void brandIt() {
		vSignInLayout.brandIt(mBrand);
		vEmailNameLayout.brandIt(mBrand);
		vFacebookLayout.brandIt(mBrand);
		vPasswordLayout.brandIt(mBrand);
		vHeaderLayout.brandIt(mBrand);
		vWelcomeLayout.brandIt(mBrand);
		vSinglePageSignUpLayout.brandIt(mBrand);
	}

	public String getBrand() {
		return mBrand;
	}

	/**
	 * Specifically makes sure all needed attrs are defined, and throws a
	 * useful Exception if one is missing. Without this error message, tracking
	 * down the resulting Exception proves difficult*.
	 */
	private void verifyRequiredAttrs(TypedArray a) {
		int[] requiredAttributes = new int[] {
			R.styleable.acct__AccountView_acct__logo_small_drawable,
			R.styleable.acct__AccountView_acct__logo_large_drawable,
			R.styleable.acct__AccountView_acct__logo_text_drawable,
			R.styleable.acct__AccountView_acct__brand,
		};
		for (int i = 0, l = requiredAttributes.length; i < l; i++) {
			if (!a.hasValue(i)) {
				String name = getResources().getResourceName(requiredAttributes[i]);
				throw new RuntimeException(name + " is not defined");
			}
		}
	}

	private void anchorUserImage(String state) {
		if (state == null) {
			vHeaderLayout.resetCenter();
		}
		else if (state.equals(STATE_SIGN_IN)) {
			vHeaderLayout.anchorTo(vSignInLayout.getContent(),
				getResources().getDimension(R.dimen.acct__user_image_view_min_height_default));
		}
		else if (state.equals(STATE_EMAIL_NAME)) {
			vHeaderLayout.anchorTo(vEmailNameLayout.getContent(),
				getResources().getDimension(R.dimen.acct__user_image_view_min_height_default));
		}
		else if (state.equals(STATE_PASSWORD)) {
			vHeaderLayout.anchorTo(vPasswordLayout.getContent(),
				getResources().getDimension(R.dimen.acct__user_image_view_min_height_hi_name));
		}
		else if (state.equals(STATE_FACEBOOK)) {
			vHeaderLayout.anchorTo(vFacebookLayout.getContent(),
				getResources().getDimension(R.dimen.acct__user_image_view_min_height_default));
		}
		else {
			vHeaderLayout.resetCenter();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		createAndAddTransitions();
		show(STATE_SIGN_IN);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mFacebookHelper != null) {
			mFacebookHelper.onActivityResult(requestCode, resultCode, data);
		}
		if (mSmartLockHelper != null) {
			mSmartLockHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void createAndAddTransitions() {
		// Default transition
		addDefaultTransition(new DefaultCompoundTransition(
			STATE_SIGN_IN,
			vHeaderLayout.getDefaultTransition(),
			new Transition() {
				@Override
				public void finalizeTransition(boolean forward) {
					vSignInLayout.enableButtons();
					vSignInLayout.setVisibility(View.VISIBLE);
					vEmailNameLayout.setVisibility(View.INVISIBLE);
					vSinglePageSignUpLayout.setVisibility(View.INVISIBLE);
					vSinglePageToolbar.setVisibility(View.INVISIBLE);
					vFacebookAPIHostLayout.setVisibility(View.INVISIBLE);
					vFacebookLayout.setVisibility(View.INVISIBLE);
					vPasswordLayout.setVisibility(View.INVISIBLE);
					vTOSLayout.setVisibility(View.INVISIBLE);
					vToolbar.showNavigationIconAsX();
					anchorUserImage(STATE_SIGN_IN);
					Events.post(new Events.OverallProgress(true, 0f));
					Events.post(new Events.ObscureBackgroundDesired(0f));
				}
			}));

		// SignIn <-> EmailName
		addTransition(new CompoundTransition(
			STATE_SIGN_IN, STATE_EMAIL_NAME,
			new ReportProgressTransition(0f, 0.33f),
			new LeftToRightTransition(this, SignInLayout.class.getName(), EmailNameLayout.class.getName()),
			new FadeTransition(vSignInLayout, vEmailNameLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						vSignInLayout.suppressCurrentErrors();
						anchorUserImage(STATE_EMAIL_NAME);
						if (!STATE_EMAIL_NAME.equals(mConfig.initialState)) {
							vToolbar.showNavigationIconAsBack();
						}
					}
					else {
						vHeaderLayout.showSpecialMessage(true);
						anchorUserImage(STATE_SIGN_IN);
						vToolbar.showNavigationIconAsX();
						hideMenu();
						Utils.hideKeyboard(AccountView.this);
					}
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (forward) {
						anchorUserImage(STATE_EMAIL_NAME);
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedNameEntering();
							}
						}
						showMenuNext();
						vEmailNameLayout.setNextButtonController(mNextButtonController);
						// Since we don't focus until later, make sure to suppress at the end too
						vSignInLayout.suppressCurrentErrors();
						postDelayed(new Runnable() {
							@TargetApi(22)
							@Override
							public void run() {
								if (AccessibilityUtil.isTalkbackEnabled(getContext()) == true) {
									AccessibilityUtil.setFocusToToolBarUpIcon(vToolbar);
									if (isMinimumAccessibilityAPI) {
										vEmailNameLayout.setAccessibilityTraversalAfter(R.id.parent_user_image_presenter);
									}
									vEmailNameLayout.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
									vEmailNameLayout.announceForAccessibility(mConfig.signupString);
								}
								else {
									vEmailNameLayout.focusEmailAddress();
								}
							}
						}, 50L);
					}
					else {
						vSignInLayout.enableButtons();
						anchorUserImage(STATE_SIGN_IN);
					}
				}
			}
		));

		// sign in <-> Single page sign up
		addTransition(new CompoundTransition(
			STATE_SIGN_IN, STATE_SINGLE_PAGE_SIGN_UP, getResources().getInteger(R.integer.acct__single_page_sliding_duration),
			new SlideInBottomTransition(vSinglePageSignUpLayout),
			new SlideInBottomTransition(vSinglePageToolbar),
			new SlideUpTransition(vHeaderLayout),
			new SlideUpTransition(vSignInLayout),
			new FadeTransition(vSignInLayout, null),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						vSignInLayout.suppressCurrentErrors();
						vToolbar.setVisibility(View.INVISIBLE);
					}
					else {
						vHeaderLayout.showSpecialMessage(true);
						Utils.hideKeyboard(AccountView.this);
						vSinglePageWhiteBackground.setVisibility(View.GONE);
						vSinglePageSignUpLayout.removeKeyboardChangeListener();
					}
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (forward) {
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedSinglePage();
							}
						}
						vSignInLayout.suppressCurrentErrors();
						setAccessibilityFocus();
						vSinglePageWhiteBackground.setVisibility(View.VISIBLE);
						vSinglePageSignUpLayout.addKeyboardChangeListener();
						AccessibilityUtil.setFocusToToolBarUpIcon(vSinglePageToolbar);
					} else {
						vToolbar.setVisibility(View.VISIBLE);
						vSignInLayout.enableButtons();
					}
				}
			}
		));

		// Single Page <-> Loading
		addTransition(new CompoundTransition(
			STATE_SINGLE_PAGE_SIGN_UP, STATE_LOADING_SINGLE_PAGE, getResources().getInteger(R.integer.acct__single_page_sliding_duration),
			vHeaderLayout.getLogoToLoadingSignInTransition(),
			new OffScreenBottomTransition(vSinglePageSignUpLayout),
			new OffScreenBottomTransition(vSinglePageToolbar),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(forward ? STATE_LOADING_ACCOUNT : STATE_SINGLE_PAGE_SIGN_UP);
					if (forward) {
						Utils.hideKeyboard(AccountView.this);
						vSinglePageWhiteBackground.setVisibility(View.GONE);
						vSinglePageSignUpLayout.removeKeyboardChangeListener();
					}
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (!forward) {
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedSinglePage();
							}
						}
						vSinglePageWhiteBackground.setVisibility(View.VISIBLE);
						vSinglePageSignUpLayout.addKeyboardChangeListener();
					}
				}
			}
		));

		// SignIn <-> Facebook API Host
		addTransition(new CompoundTransition(
			STATE_SIGN_IN, STATE_FACEBOOK_API_HOST,
			new ReportProgressTransition(0f, 0.33f),
			new LeftToRightTransition(this, SignInLayout.class.getName(), FacebookAPIHostLayout.class.getName()),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						vFacebookAPIHostLayout.setTranslationY(0f);
						vFacebookAPIHostLayout.setMessage(R.string.acct__fb_attempting_sign_in);
						vSignInLayout.suppressCurrentErrors();
						if (!STATE_FACEBOOK_API_HOST.equals(mConfig.initialState)) {
							vToolbar.showNavigationIconAsBack();
						}
					}
					else {
						vToolbar.showNavigationIconAsX();
						hideMenu();
					}
					Utils.hideKeyboard(AccountView.this);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (forward) {
						if(mFacebookHelper != null) {
							mFacebookHelper.doFacebookLogin();
						}
					} else {
						vSignInLayout.enableButtons();
					}
				}
			}
		));

		// Facebook API Host -> Facebook More Info
		addTransition(new CompoundTransition(
			STATE_FACEBOOK_API_HOST, STATE_FACEBOOK,
			new ReportProgressTransition(0.33f, 0.67f),
			new LeftToRightTransition(this, FacebookAPIHostLayout.class.getName(), FacebookLayout.class.getName()),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						anchorUserImage(STATE_FACEBOOK);
					}
					Utils.hideKeyboard(AccountView.this);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (forward) {
						showMenuLink();
						vFacebookLayout.setLinkButtonController(mLinkButtonController);
					}
				}
			}
		));

		// Facebook API Host <-> Facebook Loading
		addTransition(new CompoundTransition(
			STATE_FACEBOOK_API_HOST, STATE_LOADING_FACEBOOK,
			new ReportProgressTransition(0.33f, 0.67f),
			vHeaderLayout.getLogoToLoadingSignInTransition(),
			new OffScreenBottomTransition(vFacebookAPIHostLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(forward ? STATE_LOADING_FACEBOOK : STATE_FACEBOOK_API_HOST);
				}
			}
		));

		// Facebook More Info <-> Facebook Loading
		addTransition(new CompoundTransition(
			STATE_FACEBOOK, STATE_LOADING_FACEBOOK,
			vHeaderLayout.getLogoToLoadingSignInTransition(),
			new OffScreenBottomTransition(vFacebookLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						hideMenu();
					}
					anchorUserImage(forward ? STATE_LOADING_FACEBOOK : STATE_FACEBOOK);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					super.finalizeTransition(forward);
					if (!forward) {
						showMenuLink();
					}
				}
			}
		));

		// Facebook More Info -> Sign In
		addTransition(new CompoundTransition(
			STATE_FACEBOOK, STATE_SIGN_IN,
			new ReportProgressTransition(0.67f, 0f),
			new RightToLeftTransition(this, FacebookLayout.class.getName(), SignInLayout.class.getName()),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					vToolbar.showNavigationIconAsX();
					anchorUserImage(STATE_SIGN_IN);
					hideMenu();
					Utils.hideKeyboard(AccountView.this);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if(forward) {
						vSignInLayout.enableButtons();
					}
				}
			}
		));

		// Facebook Loading -> Sign In
		// Let's define this as sign in -> facebook loading, with the
		// assumption that it will (should) only ever happen in reverse.
		addTransition(new CompoundTransition(
			STATE_SIGN_IN, STATE_LOADING_FACEBOOK,
			new ReportProgressTransition(0f, 0.67f),
			vHeaderLayout.getLogoToLoadingSignInTransition(),
			new OffScreenBottomTransition(vSignInLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(STATE_SIGN_IN);
					vToolbar.showNavigationIconAsX();
					if (!forward) {
						vSignInLayout.enableButtons();
					}
				}
			}
		));

		// EmailName <-> Password
		addTransition(new CompoundTransition(
			STATE_EMAIL_NAME, STATE_PASSWORD,
			new ReportProgressTransition(0.33f, 0.67f),
			new LeftToRightTransition(this, EmailNameLayout.class.getName(), PasswordLayout.class.getName()),
			new FadeTransition(vEmailNameLayout, vPasswordLayout),
			vHeaderLayout.getLogoToSmallTransition(),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						anchorUserImage(STATE_PASSWORD);
						vPasswordLayout.setNextButtonController(mNextButtonController);
						vPasswordLayout.requestFocus(forward);
						if (STATE_EMAIL_NAME.equals(mConfig.initialState)) {
							vToolbar.showNavigationIconAsBack();
						}
					}
					else {
						anchorUserImage(STATE_EMAIL_NAME);
						vEmailNameLayout.setNextButtonController(mNextButtonController);
						vEmailNameLayout.requestFocus(forward);
						if (STATE_EMAIL_NAME.equals(mConfig.initialState)) {
							vToolbar.showNavigationIconAsX();
						}
					}
					AccessibilityUtil.setFocusToToolBarUpIcon(vToolbar);
				}

				@Override
				public void finalizeTransition(boolean forward) {
					super.finalizeTransition(forward);
					if (forward) {
						anchorUserImage(STATE_PASSWORD);
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedPasswordEntering();
							}
						}
					}
					else {
						anchorUserImage(STATE_EMAIL_NAME);
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedNameEntering();
							}
						}
					}
				}
			}

		));

		// Password <-> TOS
		addTransition(new CompoundTransition(
			STATE_PASSWORD, STATE_TOS,
			new ReportProgressTransition(0.67f, 1f),
			new LeftToRightTransition(this, PasswordLayout.class.getName(), TOSLayout.class.getName()),
			new FadeTransition(vPasswordLayout, vTOSLayout),
			vHeaderLayout.getSmallToLargeTransition(),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					if (forward) {
						anchorUserImage(STATE_TOS);
						hideMenu();
						Utils.hideKeyboard(AccountView.this);
						AccessibilityUtil.setFocusToToolBarUpIcon(vToolbar);
						vHeaderLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
					}
					else {
						anchorUserImage(STATE_PASSWORD);
						vPasswordLayout.requestFocus(true);
						AccessibilityUtil.setFocusToToolBarUpIcon(vToolbar);
						vHeaderLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
					}
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (forward) {
						anchorUserImage(STATE_TOS);
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedTosPage();
							}
						}
					}
					else {
						anchorUserImage(STATE_PASSWORD);
						showMenuNext();
						if (mConfig != null) {
							AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
							if (analyticsListener != null) {
								analyticsListener.userViewedPasswordEntering();
							}
						}
					}
				}
			}

		));

		// TOS -> SignIn
		addTransition(new CompoundTransition(
			STATE_TOS, STATE_SIGN_IN,
			new ReportProgressTransition(1f, 0f),
			new RightToLeftTransition(this, TOSLayout.class.getName(), SignInLayout.class.getName()),
			new FadeTransition(vTOSLayout, vSignInLayout),
			vHeaderLayout.getLargeToLogoTransition(),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(forward ? STATE_SIGN_IN : STATE_TOS);
					PartialUser user = Db.getNewUser();
					vSignInLayout.populate(user.email, user.password);
					vSignInLayout.focusPassword();
					vToolbar.showNavigationIconAsX();
				}

				@Override
				public void finalizeTransition(boolean forward) {
					vSignInLayout.enableButtons();
					anchorUserImage(forward ? STATE_SIGN_IN : STATE_TOS);
				}
			}
		));

		// TOS -> EmailName
		addTransition(new CompoundTransition(
			STATE_TOS, STATE_EMAIL_NAME,
			new ReportProgressTransition(1f, 0.33f),
			new RightToLeftTransition(this, TOSLayout.class.getName(), EmailNameLayout.class.getName()),
			new FadeTransition(vTOSLayout, vEmailNameLayout),
			vHeaderLayout.getLargeToLogoTransition(),
			new Transition() {
				@Override
				public void finalizeTransition(boolean forward) {
					anchorUserImage(forward ? STATE_EMAIL_NAME : STATE_TOS);
					if (mFocusEmailAddress) {
						vEmailNameLayout.focusEmailAddress();
					}
					else if (mFocusFirstName) {
						vEmailNameLayout.focusFirstName();
					}
					else if (mFocusLastName) {
						vEmailNameLayout.focusLastName();
					}
					mFocusEmailAddress = false;
					mFocusFirstName = false;
					mFocusLastName = false;
				}
			}
		));

		// Signin <-> Loading
		addTransition(new CompoundTransition(
			STATE_SIGN_IN, STATE_LOADING_SIGN_IN,
			vHeaderLayout.getLogoToLoadingSignInTransition(),
			new OffScreenBottomTransition(vSignInLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(forward ? STATE_LOADING_SIGN_IN : STATE_SIGN_IN);
					if (forward) {
						Utils.hideKeyboard(AccountView.this);
						vToolbar.showNavigationIconAsBack();
					}
					else {
						vToolbar.showNavigationIconAsX();
					}
				}

				@Override
				public void finalizeTransition(boolean forward) {
					if (!forward) {
						vSignInLayout.enableButtons();
					}
				}
			}
		));

		// TOS <-> Loading
		addTransition(new CompoundTransition(
			STATE_TOS, STATE_LOADING_ACCOUNT,
			vHeaderLayout.getLargeToLoadingTransition(),
			new OffScreenBottomTransition(vTOSLayout),
			new Transition() {
				@Override
				public void startTransition(boolean forward) {
					anchorUserImage(forward ? STATE_LOADING_ACCOUNT : STATE_TOS);
				}
			}
		));

		// I'm not worried about reusing this between three transitions right now,
		// since it can never overlap and doesn't rely on any real external state.
		// If that changes, this should be fixed.
		Transition welcomeTransition = new Transition() {

			float welcomeStartAlpha;
			float welcomeEndAlpha;

			@Override
			public void startTransition(boolean forward) {
				welcomeStartAlpha = forward ? 0 : 1;
				welcomeEndAlpha = forward ? 1 : 0;
				vWelcomeLayout.setVisibility(View.VISIBLE);
				vWelcomeLayout.setAlpha(welcomeStartAlpha);
				vWelcomeLayout.setTranslationY(vHeaderLayout.getTrueBottom());
				vToolbar.setVisibility(View.GONE);
				anchorUserImage(getCurrentState());
			}

			@Override
			public void updateTransition(float f, boolean forward) {
				vWelcomeLayout.setAlpha(PresenterUtils.calculateStep(welcomeStartAlpha, welcomeEndAlpha, f));
			}

			@Override
			public void finalizeTransition(boolean forward) {
				vWelcomeLayout.setAlpha(welcomeEndAlpha);
				if (forward) {
					vWelcomeLayout.setVisibility(VISIBLE);
					doSignInSuccessful();
				}
				else {
					vWelcomeLayout.setVisibility(GONE);
				}
			}
		};

		// Loading Signin <-> Welcome
		addTransition(new CompoundTransition(
			STATE_LOADING_SIGN_IN, STATE_WELCOME,
			1000,
			vHeaderLayout.getLoadingSignInToDoneLoadingTransition(),
			welcomeTransition
		));

		// Loading Account <-> Welcome
		addTransition(new CompoundTransition(
			STATE_LOADING_ACCOUNT, STATE_WELCOME,
			1000,
			vHeaderLayout.getLoadingAccountToDoneLoadingTransition(),
			welcomeTransition
		));

		addTransition(new CompoundTransition(
			STATE_LOADING_SINGLE_PAGE, STATE_WELCOME,
			1000,
			vHeaderLayout.getLoadingSinglePageToDoneLoadingTransition(),
			welcomeTransition
		));

		// Loading Facebook <-> Welcome
		addTransition(new CompoundTransition(
			STATE_LOADING_FACEBOOK, STATE_WELCOME,
			1000,
			vHeaderLayout.getLoadingFacebookToDoneLoadingTransition(),
			welcomeTransition
		));
	}

	public void up() {
		back();
	}

	@Override
	public boolean back() {
		String state = getCurrentState();
		if (state != null) {
			if (mCurrentDownload != null && (
				STATE_LOADING_ACCOUNT.equals(state) ||
					STATE_LOADING_SIGN_IN.equals(state))) {
				mCurrentDownload.dispose();
				mCurrentDownload = null;
			}

			// You can not back out of our welcome state, thank you very much.
			else if (STATE_WELCOME.equals(state)) {
				return true;
			}

			// "Back" from Facebook = actually go back to sign in
			else if (STATE_FACEBOOK.equals(state)) {
				// make sure to clear any Facebook tokens first so they don't hang around
				AccountService.facebookLogOut();

				if (mConfig != null && STATE_FACEBOOK_API_HOST.equals(mConfig.initialState)) {
					// user explicitly asked to leave after having arrived directly into facebook; buh-bye!
					clearBackStack();
				}
				else {
					show(STATE_SIGN_IN, FLAG_CLEAR_BACKSTACK);
					return true;
				}
			}
		}

		boolean result = super.back();
		if (!result) {
			if (mConfig != null) {
				Listener listener = mConfig.getListener();
				if (listener != null) {
					listener.onSignInCancelled();
				}
			}
		}
		return result;
	}

	private static class ReportProgressTransition extends Transition {
		private float mFrom;
		private float mTo;

		public ReportProgressTransition(float from, float to) {
			super(null, null);
			mFrom = from;
			mTo = to;
		}

		@Override
		public void startTransition(boolean forward) {
			super.startTransition(forward);
			postProgress(forward, forward ? mFrom : mTo);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			super.updateTransition(f, forward);
			float progress = forward
				? (mTo - mFrom) * f + mFrom
				: (mFrom - mTo) * f + mTo;
			postProgress(forward, progress);
		}

		@Override
		public void endTransition(boolean forward) {
			super.endTransition(forward);
			postProgress(forward, forward ? mTo : mFrom);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			super.finalizeTransition(forward);
			postProgress(forward, forward ? mTo : mFrom);
		}

		private void postProgress(boolean forward, float progress) {
			Events.post(new Events.OverallProgress(forward, progress));
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Errors - Sign In
	///////////////////////////////////////////////////////////////////////////

	// Networking error
	private void showSignInError(Throwable throwable) {
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				analyticsListener.userReceivedErrorOnSignInAttempt("Account:local");
			}
		}
		show(STATE_SIGN_IN, FLAG_CLEAR_TOP);
		showSignInErrorGeneric();
	}

	// API returned !success
	private void showSignInError(AccountResponse response) {
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				try {
					analyticsListener.userReceivedErrorOnSignInAttempt("Account" + response.errors.get(0).errorInfo.cause);
				}
				catch (Exception ignored) {
					analyticsListener.userReceivedErrorOnSignInAttempt("Account:server");
				}
			}
		}
		// We don't care how sign in failed, we just know that it did
		show(STATE_SIGN_IN, FLAG_CLEAR_TOP);
		showSignInErrorGeneric(response.SignInFailureError());
	}

	private void showSignInErrorGeneric() {
		showSignInErrorGeneric(new AccountResponse().SignInFailureError());
	}

	private void showSignInErrorGeneric(AccountResponse.SignInError signInError) {
		int errorMessage;
		int errorTitle;

		if (signInError == AccountResponse.SignInError.ACCOUNT_LOCKED) {
			errorMessage = R.string.acct__Sign_in_locked;
			errorTitle = R.string.acct__Sign_in_locked_TITLE;
		}
		else if (signInError == AccountResponse.SignInError.INVALID_CREDENTIALS) {
			errorMessage = R.string.acct__Sign_in_failed;
			errorTitle = R.string.acct__Sign_in_failed_TITLE;
		}
		else {
			errorMessage = R.string.acct__Sign_in_failed_generic;
			errorTitle = R.string.acct__Sign_in_failed_TITLE;
		}

		new AlertDialog.Builder(getContext())
			.setTitle(errorTitle)
			.setMessage(
				Utils.isOnline(getContext()) ? errorMessage : R.string.acct__no_network_connection)
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	///////////////////////////////////////////////////////////////////////////
	// Errors - Facebook (most handling done in FacebookViewHelper
	///////////////////////////////////////////////////////////////////////////

	public void onFacebookError() {
		if (mConfig != null) {
			mConfig.initialState = STATE_SIGN_IN;
			show(STATE_SIGN_IN, FLAG_CLEAR_BACKSTACK);
		}
	}

	public void onFacebookCancel() {
		if (mConfig != null && STATE_FACEBOOK_API_HOST.equals(mConfig.initialState)) {
			back();
		}
		else {
			show(STATE_SIGN_IN, FLAG_CLEAR_BACKSTACK);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Errors - Create Account
	///////////////////////////////////////////////////////////////////////////

	// Networking error
	private void showCreateAccountError(Throwable throwable) {
		Log.e("ohno " + throwable);
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				analyticsListener.userReceivedErrorOnAccountCreationAttempt("Account:local");
			}
			if (mConfig.enableSinglePageSignUp) {
				show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
			}
			else {
				show(STATE_TOS, FLAG_CLEAR_TOP);
			}

		}
		showCreateAccountErrorGeneric();
	}

	// API returned !success
	private void showCreateAccountError(AccountResponse response) {
		Log.e("ohno " + response);
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				try {
					analyticsListener.userReceivedErrorOnAccountCreationAttempt("Account" + response.errors.get(0).errorInfo.cause);
				}
				catch (Exception ignored) {
					analyticsListener.userReceivedErrorOnAccountCreationAttempt("Account:server");
				}
			}
			if (mConfig.enableSinglePageSignUp) {
				show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
			}
			else {
				show(STATE_TOS, FLAG_CLEAR_TOP);
			}
		}
		if (response != null) {

			if (response.hasError(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR)) {
				showErrorPassword(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR);
				return;
			}

			if (response.hasError(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR)) {
				showErrorPassword(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR);
				return;
			}

			// The account already exists. Try to just sign in with this email address and password
			if (response.hasError(AccountResponse.ErrorCode.USER_SERVICE_DUPLICATE_EMAIL)) {
				showErrorAccountExists();
				return;
			}

			if (response.hasError(AccountResponse.ErrorCode.INVALID_INPUT)) {
				AccountResponse.AccountError error = response.findError(AccountResponse.ErrorCode.INVALID_INPUT);
				switch (error.errorInfo.field) {
				case email:
					showErrorEmail();
					return;
				case password:
					showErrorPassword(AccountResponse.ErrorCode.INVALID_INPUT);
					return;
				case firstName:
					showErrorFirstName();
					return;
				case lastName:
					showErrorLastName();
					return;
				}
			}
		}

		// Catch all for anything else. (E.g. reCaptcha token error, which mAPI returns a response with a null errorCode)
		showCreateAccountErrorGeneric();
	}

	private void showCreateAccountErrorGeneric() {
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Create_account_failed_TITLE)
			.setMessage(Utils.isOnline(getContext()) ? R.string.acct__Create_account_failed
				: R.string.acct__no_network_connection)
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	private void showErrorAccountExists() {
		Resources res = getResources();
		final int SIGN_IN = 0;
		final int CREATE_ACCOUNT = 1;

		// Define the array here, instead of in arrays.xml, so that we
		// can be sure of the index, returned in the listener
		CharSequence[] items = new CharSequence[2];
		items[SIGN_IN] = res.getString(R.string.acct__Sign_in_to_my_existing_account);
		items[CREATE_ACCOUNT] = res.getString(R.string.acct__Create_a_new_account_with_different_email);

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				PartialUser user = Db.getNewUser();
				boolean usedExisting = false;
				boolean createNew = false;
				switch (which) {
				case SIGN_IN:
					usedExisting = true;
					user.password = null;
					user.lastName = null;
					user.firstName = null;
					if (mConfig != null) {
						mConfig.initialState = STATE_SIGN_IN;
					}
					show(STATE_SIGN_IN, FLAG_CLEAR_BACKSTACK);
					break;
				case CREATE_ACCOUNT:
					createNew = true;
					user.email = null;
					if (mConfig.enableSinglePageSignUp) {
						show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
					}
					else {
						show(STATE_EMAIL_NAME, FLAG_CLEAR_TOP);
					}
					break;
				}
				if (mConfig != null) {
					AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
					if (analyticsListener != null) {
						analyticsListener.accountCreationAttemptWithPreexistingEmail(usedExisting, createNew);
					}
				}
				Events.post(new Events.PartialUserDataChanged());
			}
		};

		CharSequence title = Utils.obtainBrandedPhrase(
			getContext(), R.string.acct__Brand_account_already_exists_TITLE, mBrand)
			.format();

		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
		}
		else {
			show(STATE_TOS, FLAG_CLEAR_TOP);
		}
		new AlertDialog.Builder(getContext())
			.setTitle(title)
			.setItems(items, listener)
			.create()
			.show();
	}

	private void showErrorEmail() {
		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
		}
		else {
			show(STATE_TOS, FLAG_CLEAR_TOP);
		}
		mFocusEmailAddress = true;
		DialogInterface.OnClickListener okButton = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mConfig != null && !mConfig.enableSinglePageSignUp) {
					show(STATE_EMAIL_NAME, FLAG_CLEAR_TOP);
				}
			}
		};
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Create_account_failed_TITLE)
			.setMessage(R.string.acct__invalid_email_address)
			.setPositiveButton(android.R.string.ok, okButton)
			.create()
			.show();
	}

	private void showErrorPassword(AccountResponse.ErrorCode errorCode) {
		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
		}
		else {
			show(STATE_TOS, FLAG_CLEAR_TOP);
		}

		DialogInterface.OnClickListener okButton = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mConfig != null && !mConfig.enableSinglePageSignUp) {
					show(STATE_PASSWORD, FLAG_CLEAR_TOP);
				}
			}
		};

		int errorMessageId;

		if (errorCode.equals(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR)) {
			errorMessageId = R.string.acct__email_password_identical;
		}
		else if (errorCode.equals(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR)) {
			errorMessageId = R.string.acct__common_password;
		}
		else {
			errorMessageId = R.string.acct__invalid_password;
		}

		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Create_account_failed_TITLE)
			.setMessage(errorMessageId)
			.setPositiveButton(android.R.string.ok, okButton)
			.create()
			.show();
	}

	private void showErrorFirstName() {
		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
		}
		else {
			show(STATE_TOS, FLAG_CLEAR_TOP);
		}
		mFocusFirstName = true;
		DialogInterface.OnClickListener okButton = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				show(STATE_EMAIL_NAME, FLAG_CLEAR_TOP);
			}
		};

		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Create_account_failed_TITLE)
			.setMessage(R.string.acct__invalid_first_name)
			.setPositiveButton(android.R.string.ok, okButton)
			.create()
			.show();
	}

	private void showErrorLastName() {
		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP, FLAG_CLEAR_TOP);
		}
		else {
			show(STATE_TOS, FLAG_CLEAR_TOP);
		}
		mFocusLastName = true;
		DialogInterface.OnClickListener okButton = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				show(STATE_EMAIL_NAME, FLAG_CLEAR_TOP);
			}
		};

		new AlertDialog.Builder(getContext())
			.setTitle(R.string.acct__Create_account_failed_TITLE)
			.setMessage(R.string.acct__invalid_last_name)
			.setPositiveButton(android.R.string.ok, okButton)
			.create()
			.show();
	}

	///////////////////////////////////////////////////////////////////////////
	// Sign In
	///////////////////////////////////////////////////////////////////////////

	private void doSignIn(final String email, final String password, final String recaptchaResponseToken) {
		show(STATE_LOADING_SIGN_IN);
		getService().signIn(email, password, recaptchaResponseToken)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Observer<AccountResponse>() {
				@Override
				public void onComplete() {
					mCurrentDownload = null;
				}

				@Override
				public void onError(Throwable e) {
					mCurrentDownload = null;
					show(STATE_SIGN_IN, FLAG_CLEAR_BACKSTACK);
					showSignInError(e);
				}

				@Override
				public void onSubscribe(Disposable d) {
					mCurrentDownload = d;
				}

				@Override
				public void onNext(AccountResponse response) {
					if (!response.success) {
						showSignInError(response);
						return;
					}
					if (mConfig != null) {
						AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
						if (analyticsListener != null) {
							analyticsListener.signInSucceeded();
						}
					}
					show(STATE_WELCOME);
					saveCredentials(email, password);
				}
			});
	}

	public void doSignInSuccessful() {
		if (mConfig != null) {
			Listener listener = mConfig.getListener();
			if (listener != null) {
				listener.onSignInSuccessful();
			}
		}
	}

	public void doFacebookSignInSuccessful() {
		if (mConfig != null) {
			Listener listener = mConfig.getListener();
			if (listener != null) {
				listener.onFacebookSignInSuccess();
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Create Account
	///////////////////////////////////////////////////////////////////////////

	private void doCreateAccount(String recaptchaResponseToken) {
		final PartialUser user = Db.getNewUser();
		user.recaptchaResponseToken = recaptchaResponseToken;
		getService().createUser(user)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.map(new Function<AccountResponse, AccountResponse>() {
				@Override
				public AccountResponse apply(AccountResponse accountResponse) throws Exception {
					if (!TextUtils.isEmpty(accountResponse.tuid)) {
						accountResponse.success = true;
					}
					return accountResponse;
				}
			})
			.subscribe(new Observer<AccountResponse>() {
				@Override
				public void onComplete() {
					mCurrentDownload = null;
				}

				@Override
				public void onError(Throwable e) {
					mCurrentDownload = null;
					showCreateAccountError(e);
				}

				@Override
				public void onSubscribe(Disposable d) {
					mCurrentDownload = d;
				}

				@Override
				public void onNext(AccountResponse response) {
					// Error states
					if (!response.success) {
						showCreateAccountError(response);
						return;
					}
					doCreateAccountSuccessful();
					show(STATE_WELCOME);
					saveCredentials(user.email, user.password);

				}
			});
	}

	public void saveCredentials(String email, String password) {
		if (mSmartLockHelper != null) {
			mSmartLockHelper.saveCredentials(email, password);
		}
	}

	// Post successful signin, perhaps, depending on the timing we want
	public void doCreateAccountSuccessful() {
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				analyticsListener.userSucceededInCreatingAccount();
			}
		}
	}

	private class SigninHandler implements RecaptchaHandler {
		private String email;
		private String password;

		private SigninHandler(String email, String password) {
			this.email = email;
			this.password = password;
		}

		@Override
		public void onSuccess(String recaptchaResponseToken) {
			doSignIn(email, password, recaptchaResponseToken);
		}

		@Override
		public void onFailure() {
			doSignIn(email, password, null);
		}
	}

	private class CreateAccountHandler implements RecaptchaHandler {
		@Override
		public void onSuccess(String recaptchaResponseToken) {
			doCreateAccount(recaptchaResponseToken);
		}

		@Override
		public void onFailure() {
			doCreateAccount(null);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Otto
	///////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void otto(Events.SignInButtonClicked e) {
		handleKeyBoardVisibilityChanges = false;
		if (mConfig.enableRecaptcha) {
			SigninHandler handler = new SigninHandler(e.email, e.password);
			Recaptcha.recaptchaCheck((Activity) getContext(), mConfig.recaptchaAPIKey, handler);
		}
		else {
			doSignIn(e.email, e.password, null);
		}
		vHeaderLayout.showSpecialMessage(false);
	}

	@Subscribe
	public void otto(Events.SignInWithFacebookButtonClicked e) {
		if (mConfig == null) {
			return;
		}

		Listener listener = mConfig.getListener();
		if (listener == null) {
			return;
		}

		listener.onFacebookClicked();

		if (mConfig.facebookAppId == null) {
			listener.onFacebookRequested();
		}
		else {
			show(STATE_FACEBOOK_API_HOST);
			vHeaderLayout.showSpecialMessage(false);
		}
	}

	@Subscribe
	public void otto(Events.ForgotPasswordButtonClicked e) {
		if (mConfig != null) {
			Listener listener = mConfig.getListener();
			if (listener != null) {
				listener.onForgotPassword();
				vHeaderLayout.showSpecialMessage(false);
			}
		}
	}

	@Subscribe
	public void otto(Events.CreateAccountButtonClicked e) {
		if (mConfig != null && mConfig.enableSinglePageSignUp) {
			show(STATE_SINGLE_PAGE_SIGN_UP);
		}
		else {
			show(STATE_EMAIL_NAME);
		}
		vHeaderLayout.showSpecialMessage(false);
	}

	@Subscribe
	public void otto(Events.NextFromPasswordFired e) {
		onNextClicked();
	}

	@Subscribe
	public void otto(Events.NextFromLastNameFired e) {
		onNextClicked();
	}

	private void onNextClicked() {
		if (STATE_PASSWORD.equals(getCurrentState())) {
			if (vPasswordLayout.passwordsAreValid()) {
				Db.getNewUser().password = vPasswordLayout.getPassword();
				show(STATE_TOS);
			}
		}
		if (STATE_EMAIL_NAME.equals(getCurrentState())) {
			if (vEmailNameLayout.everythingChecksOut()) {
				vEmailNameLayout.storeDataInNewUser();
				show(STATE_PASSWORD);
			}
		}
	}

	@Subscribe
	public void otto(Events.LinkFromFacebookFired e) {
		if (mFacebookHelper != null) {
			mFacebookHelper.onLinkClicked();
		}
	}

	@Subscribe
	public void otto(Events.KeyBoardVisibilityChanged e) {
		if (handleKeyBoardVisibilityChanges) {
			vHeaderLayout.showSpecialMessage(!e.isVisible);
		}
		handleKeyBoardVisibilityChanges = true;
	}

	@Subscribe
	public void otto(Events.TOSContinueButtonClicked e) {
		if (getCurrentState() == STATE_SINGLE_PAGE_SIGN_UP) {
			vSinglePageSignUpLayout.storeDataInNewUser();
			vHeaderLayout.showSpecialMessage(false);
			show(STATE_LOADING_SINGLE_PAGE);
		} else {
			show(STATE_LOADING_ACCOUNT);
		}

		if (mConfig.enableRecaptcha) {
			CreateAccountHandler handler = new CreateAccountHandler();
			Recaptcha.recaptchaCheck((Activity) getContext(), mConfig.recaptchaAPIKey, handler);
		}
		else {
			doCreateAccount(null);
		}
	}

	@Subscribe
	public void otto(Events.ObscureBackgroundDesired e) {
		if (mConfig != null) {
			Listener listener = mConfig.getListener();
			if (listener != null) {
				listener.onObscureBackgroundDesired(e.amount);
			}
			if (mConfig.background != null && mConfig.background.get() != null) {
				mConfig.background.get().setObscure(e.amount);
			}
		}
	}

	@Subscribe
	public void otto(Events.OverallProgress e) {
		if (mConfig != null) {
			Listener listener = mConfig.getListener();
			if (listener != null) {
				listener.onOverallProgress(e.forward, e.progress);
			}
			if (mConfig.background != null && mConfig.background.get() != null) {
				mConfig.background.get().setPan(e.progress);
			}
		}
	}

	@Subscribe
	public void otto(Events.UserChangedSpamOptin e) {
		if (mConfig != null) {
			AnalyticsListener analyticsListener = mConfig.getAnalyticsListener();
			if (analyticsListener != null) {
				analyticsListener.userExplicitlyModifiedMarketingOptIn(e.wantsSpam);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Utils
	///////////////////////////////////////////////////////////////////////////

	private void hideMenu() {
		Menu menu = vToolbar.getMenu();
		menu.findItem(R.id.action_next).setVisible(false);
		menu.findItem(R.id.action_link).setVisible(false);
	}

	private void showMenuNext() {
		Menu menu = vToolbar.getMenu();
		menu.findItem(R.id.action_next).setVisible(true);
		menu.findItem(R.id.action_link).setVisible(false);
		if (AccessibilityUtil.isTalkbackEnabled(getContext())) {
			menu.findItem(R.id.action_next).setTitle(getResources().getString(R.string.acct__NEXT_a11y_button));
		}
	}

	private void showMenuLink() {
		Menu menu = vToolbar.getMenu();
		menu.findItem(R.id.action_next).setVisible(false);
		menu.findItem(R.id.action_link).setVisible(true);
	}

	private void setNextEnabled(Boolean enabled) {
		Menu menu = vToolbar.getMenu();
		menu.findItem(R.id.action_next).setEnabled(enabled);
	}

	private void setLinkEnabled(Boolean enabled) {
		Menu menu = vToolbar.getMenu();
		menu.findItem(R.id.action_link).setEnabled(enabled);
	}

	public void setWhiteBackgroundFromActivity(View view) {
		vSinglePageWhiteBackground = view;
	}

	private void setAccessibilityFocus() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (AccessibilityUtil.isTalkbackEnabled(getContext()) == true) {
					AccessibilityUtil.setFocusToToolBarUpIcon(vToolbar);
				}
			}
		}, 50L);
	}

}
