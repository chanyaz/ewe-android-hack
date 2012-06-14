package com.expedia.bookings.widget;


import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DoubleKnobSlider extends LinearLayout {

	private double knobOnePercent = 0.2;
	private double knobTwoPercent = 0.8;
	
	float dragAlpha = 0.5f;
	
	ViewGroup container;
	View knobOne;
	View knobTwo;
	
	int containerWidth;
	int knobOneWidth;
	int knobTwoWidth;
	int knobOneHalfWidth;
	int knobTwoHalfWidth;
	
	Handler knobOneChangeHandler;
	Handler knobTwoChangeHandler;
	
	public void setKnobOneChangeHandler(Handler handler){
		knobOneChangeHandler = handler;
	}
	
	public void notifyKnobOneChange(){
		if(knobOneChangeHandler != null){
			knobOneChangeHandler.sendEmptyMessage(0);
		}
	}
	
	public void setKnobTwoChangeHandler(Handler handler){
		knobTwoChangeHandler = handler;
	}
	
	public void notifyKnobTwoChange(){
		if(knobTwoChangeHandler != null){
			knobTwoChangeHandler.sendEmptyMessage(0);
		}
	}
	
	public double getKnobOnePercentage(){
		return knobOnePercent;
	}
	public double getKnobTwoPercentage(){
		return knobTwoPercent;
	}

	public DoubleKnobSlider(Context context, AttributeSet attr) {
		super(context, attr);
		init(context);
	}

	public DoubleKnobSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	
	
	boolean measured = false;
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if(!measured){
			containerWidth = container.getWidth();
			knobOneWidth = knobOne.getWidth();
			knobTwoWidth = knobTwo.getWidth();
			knobOneHalfWidth = knobOneWidth/2;
			knobTwoHalfWidth = knobTwoWidth/2;
			if(containerWidth > 0){
				measured = true;	
				setKnobPositions();
			}
		}
	}
	
	
	public void printStats(){
		Log.i("Container Width:" + containerWidth + " k1w:" + knobOneWidth + " k2w:" + knobTwoWidth + " k1hw:" + knobOneHalfWidth + " k2hw:" + knobTwoHalfWidth);
	}
	
	public void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View doubleKnobSlider = inflater.inflate(R.layout.widget_double_knob_slider, this);

		container = Ui.findView(doubleKnobSlider, R.id.slider_container);
		knobOne = Ui.findView(doubleKnobSlider, R.id.knob_one);
		knobTwo = Ui.findView(doubleKnobSlider, R.id.knob_two);
		
		knobOne.setOnTouchListener(new KnobOneTouchListener());
		knobTwo.setOnTouchListener(new KnobTwoTouchListener());
		
		measured = false;

	}
	
	
	private void updateKnobOnePercentage(){
		double fullsize = containerWidth - knobOneWidth;
		knobOnePercent = knobOne.getLeft()/fullsize;
		notifyKnobOneChange();
		Log.i("KnobOnePercent:"+ knobOnePercent);
	}
	
	private void updateKnobTwoPercentage(){
		double fullsize = containerWidth - knobTwoWidth;
		knobTwoPercent = knobTwo.getLeft()/fullsize;
		notifyKnobTwoChange();
		Log.i("knobTwoPercent:"+ knobTwoPercent);
	}
	
	
	private void setKnobPositions(){
		
		LayoutParams k1lp = (LayoutParams) knobOne.getLayoutParams();
		LayoutParams k2lp = (LayoutParams) knobTwo.getLayoutParams();
		
		int fullsize = containerWidth - knobTwoWidth;
	
		int k1left = (int) Math.round(knobOnePercent * fullsize);
		int k2left = (int) Math.round(knobTwoPercent * fullsize);
		
		k1lp.leftMargin = k1left;
		k2lp.leftMargin = k2left - knobOneWidth - k1left;
		
		knobOne.setLayoutParams(k1lp);
		knobTwo.setLayoutParams(k2lp);
	}
	
	private class KnobOneTouchListener implements OnTouchListener{
		
		int touchOffset = 0;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			LayoutParams k1lp = (LayoutParams) knobOne.getLayoutParams();
			LayoutParams k2lp = (LayoutParams) knobTwo.getLayoutParams();
			
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				knobOne.setAlpha(dragAlpha);
				touchOffset = Math.round(event.getX());
				break;
			case MotionEvent.ACTION_MOVE:
				int change = Math.round(event.getX()) - touchOffset;
				int oldK1lpMargin = k1lp.leftMargin;
				
				k1lp.leftMargin += change;
				
				if(k1lp.leftMargin < 0){
					k1lp.leftMargin = 0;
					if(oldK1lpMargin > 0){
						k2lp.leftMargin += oldK1lpMargin;
					}
				}else{
					k2lp.leftMargin -= change;
				}
				if(k1lp.leftMargin + knobOneWidth + knobTwoWidth > containerWidth){
					k1lp.leftMargin = containerWidth - knobOneWidth - knobTwoWidth;
					k2lp.leftMargin = 0;
				}
					
				if(k2lp.leftMargin < 0)
					k2lp.leftMargin = 0;
				
				knobOne.setLayoutParams(k1lp);
				knobTwo.setLayoutParams(k2lp);
				updateKnobOnePercentage();
				updateKnobTwoPercentage();
				break;
			case MotionEvent.ACTION_UP:
				knobOne.setAlpha(1f);
				break;
			default:
				return false;
			}
			return true;
		}
	}
	
	private class KnobTwoTouchListener implements OnTouchListener{

		int touchOffset = 0;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			LayoutParams k1lp = (LayoutParams) knobOne.getLayoutParams();
			LayoutParams k2lp = (LayoutParams) knobTwo.getLayoutParams();
			
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				touchOffset = Math.round(event.getX());
				knobTwo.setAlpha(dragAlpha);
				break;
			case MotionEvent.ACTION_MOVE:
				Log.i("MOVE KNOB2");
				int change = Math.round(event.getX()) - touchOffset;
				
				while(change != 0){
					
					k2lp.leftMargin += change;
					
					if(k2lp.leftMargin < 0){
						k1lp.leftMargin += k2lp.leftMargin;
						k2lp.leftMargin = 0;
					}
					
					if(k1lp.leftMargin < 0){
						k1lp.leftMargin = 0;
					}
					
					if(k1lp.leftMargin + knobOneWidth + k2lp.leftMargin + knobTwoWidth > containerWidth){
						change = containerWidth -  (k1lp.leftMargin + knobOneWidth + k2lp.leftMargin + knobTwoWidth);
					}else{
						change = 0;
					}
				}
				
				knobOne.setLayoutParams(k1lp);
				knobTwo.setLayoutParams(k2lp);
				
				updateKnobOnePercentage();
				updateKnobTwoPercentage();
				
				break;
			case MotionEvent.ACTION_UP:
				knobTwo.setAlpha(1f);
				break;
			default:
				return false;
			}
			return true;
		}
		
	}
}
