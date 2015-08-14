package com.velanciofernandes.demozoomapp;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by VELANCIO.FERNANDES on 8/7/2015.
 */
public class ZoomRootView
{
	//default zoom values
	private static final float MIN_ZOOM = 1f;
	private static final float MAX_ZOOM = 2f;
	private static final float DOUBLE_TAP_ZOOM = 1.5f;

	private static final float FLOAT_ZERO = 0.0f;

	private static final float PAN_SENSITIVITY = 15f;

	private static final int INVALID_POINTER_ID = 1;

	//Modes for Zoom and Pan
	private static final int MODE_NONE = 0;
	private static final int MODE_DRAG = 1;
	private static final int MODE_ZOOM = 2;
	private static final int MODE_SCROLL = 3;

	private float scaleFactor = 1.f;
	private float lastTouchX;
	private float lastTouchY;
	private float posX;
	private float posY;
	private float scaledDiffY;
	private float scaledDiffX;
	private float panSensitivity;
	private int mode;
	private int scroll;
	private boolean setDragClick = false;

	private int activePointerId = INVALID_POINTER_ID;
	private ViewGroup mMainView;
	private Rect originalBox;
	private RectF zoomBox;
	private RectF previousZoomBox;
	private ScaleGestureDetector scaleGestureDetector;
	private GestureDetector gestureDetector;
    private ZoomEventListener zoomEventListener;

	private ZoomRootView(Context context, View rootView, ZoomEventListener zoomEventListener)
	{
		mMainView = (ViewGroup) rootView;
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
		gestureDetector = new GestureDetector(context, new GestureListener());
        this.zoomEventListener = zoomEventListener;
		originalBox = new Rect();
	}

    public ZoomEventListener getZoomEventListener(){
        return zoomEventListener;
    }
	public static ZoomRootView createZoomRootView(Context context, View rootView, ZoomEventListener zoomEventListener)
	{
		if (context == null || rootView == null || zoomEventListener == null) return null;
		return new ZoomRootView(context, rootView,zoomEventListener);
	}

	public void setPanSensitivity(final float panSensitivity)
	{
		if (panSensitivity > FLOAT_ZERO) this.panSensitivity = panSensitivity;
	}

	public float getPanSensitivity()
	{
		if (panSensitivity == FLOAT_ZERO)
		{
			return PAN_SENSITIVITY;
		}
		else
			return panSensitivity;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{

		public boolean onScale(ScaleGestureDetector detector)
		{
			scaleFactor *= detector.getScaleFactor();

			scaleFactor = (scaleFactor < MIN_ZOOM ? MIN_ZOOM : scaleFactor); // prevent our view from becoming too small //

			if (scaleFactor == MIN_ZOOM)
			{
				posY = FLOAT_ZERO;
				posX = FLOAT_ZERO;
				mMainView.scrollTo(0, 0);
			}

			scaleFactor = (scaleFactor > MAX_ZOOM ? MAX_ZOOM : scaleFactor); // prevent our view from becoming too large //
			scaleFactor = ((float) ((int) (scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //

			mMainView.setScaleX(scaleFactor);
			mMainView.setScaleY(scaleFactor);

			return true;
		}
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDoubleTapEvent(MotionEvent motionEvent)
		{
			if (scaleFactor > MIN_ZOOM) setPredefinedScale(MIN_ZOOM);
			return true;
		}
	}

	public interface ZoomEventListener
	{
        public boolean onDispatchTouchListener(MotionEvent motionEvent);
	}


    public boolean dispatchTouchWrapper(ZoomEventListener zoomEventListener,MotionEvent motionEvent)
    {
        if (zoomEventListener != null)
        {
            if (!dispatchTouchEvent(motionEvent))
                return zoomEventListener.onDispatchTouchListener(motionEvent);
            else
                return true;
        }
        else
            return zoomEventListener.onDispatchTouchListener(motionEvent);
    }

	public boolean dispatchTouchEvent(MotionEvent motionEvent)
	{

		if ( motionEvent == null)
		{
			return false;
		}

		switch (motionEvent.getActionMasked())
		{
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				mode = MODE_ZOOM;
				scroll = MODE_SCROLL;
			}
				break;
			case MotionEvent.ACTION_DOWN:
			{

				activePointerId = motionEvent.getPointerId(0);
				final float x = motionEvent.getX();
				final float y = motionEvent.getY();

				lastTouchX = x;
				lastTouchY = y;

				if (scroll == MODE_SCROLL) scroll = MODE_NONE;

				mode = MODE_DRAG;

			}
				break;
			case MotionEvent.ACTION_UP:
			{

				final float x = motionEvent.getX();
				final float y = motionEvent.getY();

				if (scroll == MODE_SCROLL) return true;

				if (setDragClick == true && zoomBox != null)
				{
					if (Math.abs(lastTouchX - x) < (zoomBox.width() / 100f) && Math.abs(lastTouchY - y) < (zoomBox.height() / 100f))
					{
						gestureDetector.onTouchEvent(motionEvent);
						return false;
					}
					else
					{
						return true;
					}
				}

			}
				break;
			case MotionEvent.ACTION_POINTER_UP:
			{
				mode = MODE_NONE;
				if (scroll == MODE_SCROLL) return true;
			}
				break;
			case MotionEvent.ACTION_MOVE:
			{

				// Find the index of the active pointer and fetch its position
				final int pointerIndex = motionEvent.findPointerIndex(activePointerId);

				if (pointerIndex < FLOAT_ZERO) break;

				float xx = motionEvent.getX(pointerIndex);
				float yy = motionEvent.getY(pointerIndex);

				if (mode == MODE_ZOOM)
				{

					mMainView.getDrawingRect(originalBox);

					Matrix m = mMainView.getMatrix();

					zoomBox = new RectF(originalBox);

					m.mapRect(zoomBox);

					if (previousZoomBox != null && (zoomBox.width() < previousZoomBox.width()))
					{
						float xLeak = (float) (previousZoomBox.width() - zoomBox.width()) / 2f;
						float yLeak = (float) (previousZoomBox.height() - zoomBox.height()) / 2f;

						if (posX == FLOAT_ZERO)
						{
							xLeak = FLOAT_ZERO;
						}

						if (posY == FLOAT_ZERO)
						{
							yLeak = FLOAT_ZERO;
						}

						if (posX > FLOAT_ZERO)
						{
							xLeak = (-1) * xLeak;
							posX = posX + xLeak;
							if (posX < FLOAT_ZERO)
							{
								xLeak = xLeak - posX;
								posX = FLOAT_ZERO;
							}
						}

						if (posX < FLOAT_ZERO)
						{

							posX = posX + xLeak;
							if (posX > FLOAT_ZERO)
							{
								xLeak = xLeak - posX;
								posX = FLOAT_ZERO;
							}
						}

						if (posY > FLOAT_ZERO)
						{
							yLeak = (-1) * yLeak;
							posY = posY + yLeak;
							if (posY < FLOAT_ZERO)
							{
								yLeak = yLeak - posY;
								posY = FLOAT_ZERO;
							}
						}

						if (posY < FLOAT_ZERO)
						{

							posY = posY + yLeak;
							if (posY > FLOAT_ZERO)
							{
								yLeak = yLeak - posY;
								posY = FLOAT_ZERO;
							}
						}
						mMainView.scrollBy((int) xLeak, (int) yLeak);
					}

					scaledDiffY = (zoomBox.height() - originalBox.height()) / 2;
					scaledDiffX = (zoomBox.width() - originalBox.width()) / 2;

					previousZoomBox = zoomBox;

				}

				// DRAG works
				if (mode == MODE_DRAG)
				{

					if (scaledDiffY == FLOAT_ZERO || scaledDiffX == FLOAT_ZERO) break;

					float dx = (float) (lastTouchX - xx) / PAN_SENSITIVITY;
					float dy = (float) (lastTouchY - yy) / PAN_SENSITIVITY;

					if (Math.abs(posX) < scaledDiffX) posX = posX + dx;
					if (Math.abs(posY) < scaledDiffY) posY = posY + dy;

					if (Math.abs(posX) >= scaledDiffX)
					{

						if ((Math.abs(posX) == scaledDiffX && posX > FLOAT_ZERO && dx < FLOAT_ZERO) || (Math.abs(posX) == scaledDiffX && posX < FLOAT_ZERO && dx > FLOAT_ZERO)) posX += dx;

						if (Math.abs(posX) == scaledDiffX)
						{
							dx = FLOAT_ZERO;
						}

						if (Math.abs(posX) > scaledDiffX)
						{
							if (posX > FLOAT_ZERO)
							{
								float x = dx - (posX - scaledDiffX);
								dx = x;
								posX = scaledDiffX;
							}
							else
							{
								float x = dx - (posX + scaledDiffX);
								dx = x;
								posX = (-1) * scaledDiffX;
							}
						}

					}

					if (Math.abs(posY) >= scaledDiffY)
					{

						if ((Math.abs(posY) == scaledDiffY && posY > FLOAT_ZERO && dy < FLOAT_ZERO) || (Math.abs(posY) == scaledDiffY && posY < FLOAT_ZERO && dy > FLOAT_ZERO)) posY += dy;

						if (Math.abs(posY) == scaledDiffY)
						{
							dy = FLOAT_ZERO;
						}

						if (Math.abs(posY) > scaledDiffY)
						{
							if (posY > FLOAT_ZERO)
							{
								float y = dy - (posY - scaledDiffY);
								dy = y;
								posY = scaledDiffY;
							}
							else
							{
								float y = dy - (posY + scaledDiffY);
								dy = y;
								posY = (-1) * scaledDiffY;
							}
						}
					}

					mMainView.scrollBy((int) dx, (int) dy);

				}
			}
				break;
		}

		if (mode == MODE_ZOOM)
		{
			scaleGestureDetector.onTouchEvent(motionEvent);
			return true;
		}

		gestureDetector.onTouchEvent(motionEvent);
		return false;
	}

	public void setPredefinedScale(float scale)
	{

		if (scale >= MIN_ZOOM && scale <= MAX_ZOOM)
		{

			posY = FLOAT_ZERO;
			posX = FLOAT_ZERO;
			mMainView.scrollTo(0, 0);

			scaleFactor = scale;

			scaleFactor = ((float) ((int) (scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //

			mMainView.setScaleX(scaleFactor);
			mMainView.setScaleY(scaleFactor);
		}
	}

	// //this doesn't work well with Viewpagers and some scrollable views
	public void triggerPreventDragClick(boolean setDragClick)
	{
		this.setDragClick = setDragClick;
	}

	public float getMinZoom()
	{
		return MIN_ZOOM;
	}

	public float getMaxZoom()
	{
		return MAX_ZOOM;
	}

}
