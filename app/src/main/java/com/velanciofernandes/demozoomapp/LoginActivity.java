package com.velanciofernandes.demozoomapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class LoginActivity extends Activity implements ZoomRootView.ZoomEventListener
{

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
        if (zoomRootView != null)
        {
            return zoomRootView.dispatchTouchWrapper(this, motionEvent);
        }
        else
        {
            return super.dispatchTouchEvent(motionEvent);
        }
	}

	@Override
	public boolean onDispatchTouchListener(MotionEvent motionEvent)
	{
		return super.dispatchTouchEvent(motionEvent);
	}
}
