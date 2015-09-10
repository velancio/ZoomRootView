package com.velanciofernandes.demozoomapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class LoginActivity extends Activity implements ZoomRootView.ZoomEventListener
{
	float scale = 0.0f;

	// UI references.
	private ViewGroup mMainView;

	private ZoomRootView zoomRootView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mMainView = (ViewGroup) findViewById(R.id.main_view);

		zoomRootView = ZoomRootView.createZoomRootView(getApplicationContext(), mMainView, this);

		//this doesn't work well with Viewpagers and some scrollable views
		if (zoomRootView != null)
		{
			zoomRootView.triggerPreventDragClick(true);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent motionEvent)
	{
       return ZoomRootView.dispatchTouchWrapper(zoomRootView,this, motionEvent);
	}

	@Override
	public boolean onDispatchTouchListener(MotionEvent motionEvent)
	{
		return super.dispatchTouchEvent(motionEvent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
