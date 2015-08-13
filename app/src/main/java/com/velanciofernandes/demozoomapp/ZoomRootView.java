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
public class ZoomRootView{


    private static final boolean DEBUG = true;

    //default zoom values
    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 2f;
    private static final float DOUBLE_TAP_ZOOM = 1.5f;


    private static final float FLOAT_ZERO = 0.0f;

    private static final float PAN_SENSITIVITY = 15f;

    private static final int INVALID_POINTER_ID = 1;

    //Modes for Zoom and Pan
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private static int SCROLL = 3;


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
    private Rect bbox;
    private RectF bboxF;
    private RectF prevBboxF;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;



    private ZoomRootView(Context context, View rootView) {
        mMainView = (ViewGroup)rootView;
        scaleGestureDetector = new ScaleGestureDetector(context,new ScaleListener());
        gestureDetector = new GestureDetector(context,new GestureListener());
        bbox = new Rect();
    }

    public static ZoomRootView createZoomRootView(Context context, View rootView) {

        if (context == null || rootView == null)
            return null;



        return new ZoomRootView(context,rootView);
    }


    public void setPanSensitivity(final float panSensitivity){
        if(panSensitivity > FLOAT_ZERO)
            this.panSensitivity = panSensitivity;
    }


    public float getPanSensitivity(){
        if(panSensitivity == FLOAT_ZERO){
            return PAN_SENSITIVITY;
        }
        else
            return panSensitivity;
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor *= detector.getScaleFactor();


            scaleFactor = (scaleFactor < MIN_ZOOM ? MIN_ZOOM : scaleFactor); // prevent our view from becoming too small //

            if(scaleFactor == MIN_ZOOM){
                posY = FLOAT_ZERO;
                posX = FLOAT_ZERO;
                mMainView.scrollTo(0,0);
            }

            scaleFactor = (scaleFactor > MAX_ZOOM ? MAX_ZOOM : scaleFactor); // prevent our view from becoming too large //
            scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //

            mMainView.setScaleX(scaleFactor);
            mMainView.setScaleY(scaleFactor);

            return true;
        }

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if(scaleFactor > MIN_ZOOM)
                setPredefinedScale(MIN_ZOOM);

            return true;
        }
    }


    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(this == null || ev == null){
            return false;
        }

        switch(ev.getActionMasked())
        {
            case MotionEvent.ACTION_POINTER_DOWN: {
                mode = ZOOM;
                scroll = SCROLL;
            }
            break;
            case MotionEvent.ACTION_DOWN: {

                activePointerId = ev.getPointerId(0);
                final float x = ev.getX();
                final float y = ev.getY();

                lastTouchX  = x;
                lastTouchY  = y;

                if(scroll == SCROLL)
                    scroll = NONE;



                mode = DRAG;

            }
            break;
            case MotionEvent.ACTION_UP: {

                final float x = ev.getX();
                final float y = ev.getY();


                if(scroll == SCROLL )
                    return true;


                if(setDragClick == true && bboxF != null ) {
                    if (Math.abs(lastTouchX - x) < (bboxF.width()/100f) && Math.abs(lastTouchY - y) < (bboxF.height() / 100f)) {
                        gestureDetector.onTouchEvent(ev);
                        return false;
                    } else
                        return true;
                }


            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                mode = NONE;
                if (scroll == SCROLL)
                    return true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {

                // Find the index of the active pointer and fetch its position
                final int pointerIndex = ev.findPointerIndex(activePointerId);

                if (pointerIndex < FLOAT_ZERO)
                    break;

                float xx = ev.getX(pointerIndex);
                float yy = ev.getY(pointerIndex);



                if (mode == ZOOM) {

                    mMainView.getDrawingRect(bbox);

                    Matrix m = mMainView.getMatrix();

                    bboxF = new RectF(bbox);

                    m.mapRect(bboxF);

                    if (prevBboxF != null && (bboxF.width() < prevBboxF.width())) {
                        float xLeak = (float) (prevBboxF.width() - bboxF.width()) / 2f;
                        float yLeak = (float) (prevBboxF.height() - bboxF.height()) / 2f;


                        if (posX == FLOAT_ZERO) {
                            xLeak = FLOAT_ZERO;
                        }

                        if (posY == FLOAT_ZERO) {
                            yLeak = FLOAT_ZERO;
                        }

                        if (posX > FLOAT_ZERO) {
                            xLeak = (-1) * xLeak;
                            posX = posX + xLeak;
                            if (posX < FLOAT_ZERO) {
                                xLeak = xLeak - posX;
                                posX = FLOAT_ZERO;
                            }
                        }

                        if (posX < FLOAT_ZERO) {

                            posX = posX + xLeak;
                            if (posX > FLOAT_ZERO) {
                                xLeak = xLeak - posX;
                                posX = FLOAT_ZERO;
                            }
                        }


                        if (posY > FLOAT_ZERO) {
                            yLeak = (-1) * yLeak;
                            posY = posY + yLeak;
                            if (posY < FLOAT_ZERO) {
                                yLeak = yLeak - posY;
                                posY = FLOAT_ZERO;
                            }
                        }

                        if (posY < FLOAT_ZERO) {

                            posY = posY + yLeak;
                            if (posY > FLOAT_ZERO) {
                                yLeak = yLeak - posY;
                                posY = FLOAT_ZERO;
                            }
                        }



                        mMainView.scrollBy((int) xLeak, (int) yLeak);


                    }

                    scaledDiffY = (bboxF.height() - bbox.height()) / 2;
                    scaledDiffX = (bboxF.width() - bbox.width()) / 2;

                    prevBboxF = bboxF;

                }

                // DRAG works
                if (mode == DRAG) {

                    if (scaledDiffY == FLOAT_ZERO || scaledDiffX == FLOAT_ZERO)
                        break;

                    float dx = (float) (lastTouchX - xx) / PAN_SENSITIVITY;
                    float dy = (float) (lastTouchY - yy) / PAN_SENSITIVITY;



                    if (Math.abs(posX) < scaledDiffX)
                        posX = posX + dx;
                    if (Math.abs(posY) < scaledDiffY)
                        posY = posY + dy;

                    if (Math.abs(posX) >= scaledDiffX) {

                        if ((Math.abs(posX) == scaledDiffX && posX > FLOAT_ZERO && dx < FLOAT_ZERO) || (Math.abs(posX) == scaledDiffX && posX < FLOAT_ZERO && dx > FLOAT_ZERO))
                            posX += dx;

                        if (Math.abs(posX) == scaledDiffX) {
                            dx = FLOAT_ZERO;
                        }

                        if (Math.abs(posX) > scaledDiffX) {
                            if (posX > FLOAT_ZERO) {
                                float x = dx - (posX - scaledDiffX);
                                dx = x;
                                posX = scaledDiffX;
                            } else {
                                float x = dx - (posX + scaledDiffX);
                                dx = x;
                                posX = (-1) * scaledDiffX;
                            }
                        }

                    }

                    if (Math.abs(posY) >= scaledDiffY) {

                        if ((Math.abs(posY) == scaledDiffY && posY > FLOAT_ZERO && dy < FLOAT_ZERO) || (Math.abs(posY) == scaledDiffY && posY < FLOAT_ZERO && dy > FLOAT_ZERO))
                            posY += dy;


                        if (Math.abs(posY) == scaledDiffY) {
                            dy = FLOAT_ZERO;
                        }


                        if (Math.abs(posY) > scaledDiffY) {
                            if (posY > FLOAT_ZERO) {
                                float y = dy - (posY - scaledDiffY);
                                dy = y;
                                posY = scaledDiffY;
                            } else {
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

        if(mode == ZOOM)
        {
            scaleGestureDetector.onTouchEvent(ev);
            return true;
        }

        gestureDetector.onTouchEvent(ev);
        return false;
    }


    public void setPredefinedScale(float scale){

        if(scale>=MIN_ZOOM && scale<=MAX_ZOOM){

            posY = FLOAT_ZERO;
            posX = FLOAT_ZERO;
            mMainView.scrollTo(0,0);

            scaleFactor = scale;

            scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //

            mMainView.setScaleX(scaleFactor);
            mMainView.setScaleY(scaleFactor);
        }
    }

    // Does not work correctly with scrollable view like ViewPager etc
    public void triggerPreventDragClick(boolean setDragClick){
        this.setDragClick = setDragClick;
    }

    public float getMinZoom(){
        return MIN_ZOOM;
    }

    public float getMaxZoom(){
        return MAX_ZOOM;
    }

}
