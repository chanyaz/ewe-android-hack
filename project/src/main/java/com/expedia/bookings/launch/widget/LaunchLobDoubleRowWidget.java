package com.expedia.bookings.launch.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioTrack;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AudioUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LaunchLobDoubleRowWidget extends RelativeLayout {

	@InjectView(R.id.hotels_button)
	PhoneLaunchDoubleRowButton hotelsBtn;

	@InjectView(R.id.flights_button)
	PhoneLaunchDoubleRowButton flightsBtn;

	@InjectView(R.id.cars_button)
	PhoneLaunchDoubleRowButton carsBtn;

	@InjectView(R.id.activities_button)
	PhoneLaunchDoubleRowButton lxBtn;

	@InjectView(R.id.lob_bottom_row)
	FrameLayout bottomRow;

	@InjectView(R.id.lob_bottom_row_bg)
	View bottomRowBg;

	@InjectView(R.id.vertical_divider)
	View divider;

	@InjectView(R.id.shadow)
	View shadow;

	float origHeight;

	public LaunchLobDoubleRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.section_phone_launch_double_row_lob, this);
		ButterKnife.inject(this);
	}

	public void transformButtons(float f) {
		hotelsBtn.scaleTo(f);
		flightsBtn.scaleTo(f);
		carsBtn.scaleTo(f);
		lxBtn.scaleTo(f);
		bottomRowBg.setScaleY(f);
		divider.setTranslationY((f * origHeight) - origHeight);
		bottomRow.setTranslationY((f * origHeight) - origHeight);
		shadow.setTranslationY((f * origHeight * 2) - origHeight * 2);
	}

	public void toggleButtonState(boolean enabled) {
		hotelsBtn.setEnabled(enabled);
		flightsBtn.setEnabled(enabled);
		carsBtn.setEnabled(enabled);
		lxBtn.setEnabled(enabled);
		if (!enabled) {
			bottomRowBg.setScaleY(1.0f);
			divider.setTranslationY(0.0f);
			bottomRow.setTranslationY(0.0f);
			shadow.setTranslationY(0.0f);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		bottomRowBg.setPivotY(-bottomRowBg.getTop());
		origHeight = getResources().getDimension(R.dimen.launch_lob_double_row_height);
	}

	@Subscribe
	public void onNetworkAvailable(Events.LaunchOnlineState event) {
		toggleButtonState(true);
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		toggleButtonState(false);
	}

	@Subscribe
	public void onMemoryTestActivated(Events.MemoryTestImpetus event) {
		if (Db.getMemoryTestActive() && !gameStarted) {
			gameStarted = true;
			onNextInput();
		}
	}

	// Memory testing

	List<Integer> enteredInts;
	List<Integer> generatedInts;
	private AudioTrack hotelTrack, flightTrack /* lol */, carTrack, lxTrack, failTrack;
	private boolean replayRunning = false;
	private boolean gameStarted = false;

	@Subscribe
	public void onMemoryTestInput(Events.MemoryTestInput event) {
		if (!replayRunning && gameStarted) {
			switch (event.viewId) {
			case R.id.hotels_button:
				checkNewInput(0);
				break;
			case R.id.flights_button:
				checkNewInput(1);
				break;
			case R.id.cars_button:
				checkNewInput(2);
				break;
			case R.id.activities_button:
				checkNewInput(3);
				break;
			default:
				throw new RuntimeException("UNKNOWN LOB");
			}
		}
	}

	private void fail() {
		generatedInts.clear();
		enteredInts.clear();
		playSoundFor(-1);
		Ui.showToast(getContext(), "YOU LOSE");
		gameStarted = false;
	}

	private void checkNewInput(int newInput) {
		enteredInts.add(newInput);
		for (int i = 0; i < enteredInts.size(); i++) {
			int entered = enteredInts.get(i);
			int gen = generatedInts.get(i);
			if (gen != entered) {
				fail();
				return;
			}
		}
		lightUpButton(newInput);
		playSoundFor(newInput);
		if (enteredInts.size() == generatedInts.size()) {
			onNextInput();
		}
	}

	private void playSoundFor(int i) {
		switch (i) {
		case -1:
			if (failTrack != null) {
				failTrack.release();
			}
			failTrack = AudioUtils.genTone(1.5f, 42);
			failTrack.play();
			break;
		case 0:
			if (hotelTrack != null) {
				hotelTrack.release();
			}
			hotelTrack = AudioUtils.genTone(getSoundDuration(), 209);
			hotelTrack.play();
			break;
		case 1:
			if (flightTrack != null) {
				flightTrack.release();
			}
			flightTrack = AudioUtils.genTone(getSoundDuration(), 415);
			flightTrack.play();
			break;
		case 2:
			if (carTrack != null) {
				carTrack.release();
			}
			carTrack = AudioUtils.genTone(getSoundDuration(), 252);
			carTrack.play();
			break;
		case 3:
			if (lxTrack != null) {
				lxTrack.release();
			}
			lxTrack = AudioUtils.genTone(getSoundDuration(), 310);
			lxTrack.play();
			break;
		default:
			throw new RuntimeException("UNKNOWN LOB");
		}
	}

	private float getSoundDuration() {
		float duration;
		int len = generatedInts.size();
		if (len > 13) {
			duration = 0.22f;
		}
		else if (len > 5) {
			duration = 0.32f;
		}
		else {
			duration = 0.42f;
		}
		return duration;
	}

	private Animator lightUpButton(final int i) {
		Animator a;
		switch (i) {
		case 0:
			a  = ObjectAnimator.ofFloat(hotelsBtn, "alpha", 1, .5f, 1);
			break;
		case 1:
			a  = ObjectAnimator.ofFloat(flightsBtn, "alpha", 1, .5f, 1);
			break;
		case 2:
			a  = ObjectAnimator.ofFloat(carsBtn, "alpha", 1, .5f, 1);
			break;
		case 3:
			a  = ObjectAnimator.ofFloat(lxBtn, "alpha", 1, .5f, 1);
			break;
		default:
			throw new RuntimeException("UNKNOWN LOB");
		}
		a.setStartDelay(50);
		a.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				playSoundFor(i);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		return a;
	}

	private void onNextInput() {
		replayRunning = true;
		if (enteredInts == null) {
			enteredInts = new ArrayList<>();
		}
		if (generatedInts == null) {
			generatedInts = new ArrayList<>();
		}

		Random rand = new Random();
		generatedInts.add(rand.nextInt(4));
		enteredInts.clear();

		List<Animator> anims = new ArrayList<>();
		for (int i : generatedInts) {
			anims.add(lightUpButton(i));
		}

		AnimatorSet animSet = new AnimatorSet();
		animSet.setStartDelay(500);
		animSet.playSequentially(anims);
		animSet.setDuration((long) (getSoundDuration() * 1000));
		animSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				replayRunning = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		animSet.start();
	}
}
