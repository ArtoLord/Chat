package com.example.root.forhelp.AsuncTask;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.root.forhelp.StaticCl;

/**
 * Created by root on 04.03.18.
 */

public class SwipeDetector {
    private int swipe_distanse;
    private int swipe_vel;
    final int MIN_DIST = 120;
    final int MIN_VEL = 200;

    public SwipeDetector(int swipe_distanse, int swipe_vel) {
        super();
        this.swipe_distanse = swipe_distanse;
        this.swipe_vel = swipe_vel;
    }

    public SwipeDetector() {
        super();
        this.swipe_distanse = MIN_DIST;
        this.swipe_vel = MIN_VEL;
    }
    public boolean isSwipeDown(MotionEvent e1,MotionEvent e2,float velY){
        return isSwipe(e2.getY(),e1.getY(),velY);
    }

    private boolean isSwipe(float y, float y1, float velY) {
        return (y-y1)>this.swipe_distanse && Math.abs(velY)>this.swipe_vel;
    }
}