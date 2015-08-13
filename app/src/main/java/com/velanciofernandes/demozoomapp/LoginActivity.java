package com.velanciofernandes.demozoomapp;


import android.app.Activity;

import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup;

import android.widget.AutoCompleteTextView;

import android.widget.EditText;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {


    // UI references.
     private ViewGroup mMainView;


    private ZoomRootView zoomRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mMainView = (ViewGroup)findViewById(R.id.main_view);

        zoomRootView = ZoomRootView.createZoomRootView(getApplicationContext(),mMainView);

        if(zoomRootView != null){
            zoomRootView.triggerPreventDragClick(true);
        }

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(!zoomRootView.dispatchTouchEvent(ev))
            return super.dispatchTouchEvent(ev);
        else
            return true;
    }






}

