package com.example.root.forhelp.Table;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.root.forhelp.AsuncTask.SwipeDetector;
import com.example.root.forhelp.R;

import java.io.File;

public class SeeImageActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_see_image);
        getSupportActionBar().hide();
        gestureDetector = new GestureDetector(this,new myGestureDetector());
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        ImageView iv = (ImageView)findViewById(R.id.image);
        View rl = findViewById(R.id.rl);
        rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        iv.setImageBitmap(BitmapFactory.decodeFile(new File(path).getAbsolutePath()));
    }


    private class myGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            SwipeDetector detector = new SwipeDetector();
            if (detector.isSwipeDown(e1,e2,velocityY)){
            onBackPressed();}
            return false;

        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}

