package com.icecold.sleepbandtest;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.icecold.sleepbandtest.utils.ValueAnimatorUtil;
import com.icecold.sleepbandtest.widget.CircleProgressbar;
import com.vise.log.ViseLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    @BindView(R.id.circle_progress)
    CircleProgressbar circleProgressbar;
    @BindView(R.id.start_anim)
    Button progressAnim;
    @BindView(R.id.skip_movie)
    Button skipMovie;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        handler = new Handler();

        ButterKnife.bind(this);
        circleProgressbar.setmProgressText("文字");
        Log.d(TAG, "onCreate: 栈 id = "+getTaskId());
//        ViseLog.i("栈 id = "+getTaskId());
    }
    @OnClick({R.id.start_anim,R.id.skip_movie})
    public void allClickEvent(View view){
        switch (view.getId()) {
            case R.id.start_anim:
                startProgressAnim();
                break;
            case R.id.skip_movie:
                skipMovieActivity();
                break;
        }

    }

    private void skipMovieActivity() {
        Intent intent = new Intent(this,MovieActivity.class);
        startActivity(intent);
    }

    private void startProgressAnim() {
        //        circleProgressbar.setProgress(0);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                circleProgressbar.setStartProgress(true);
////                ViseLog.i("更新进度条");
//                float progress = circleProgressbar.getProgress();
//                progress ++;
//                circleProgressbar.updateProgress(progress);
//                if (progress <= 99){
//                    handler.postDelayed(this,20);
//                }
//            }
//        },20);
        //利用属性动画来做
        circleProgressbar.setStartProgress(true);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 100f);
        valueAnimator.setRepeatCount(1);
//        valueAnimator.setInterpolator(new LinearInterpolator());
//        valueAnimator.setDuration(20000);
//        valueAnimator.setStartDelay(200);
//        valueAnimator.setRepeatCount(0);
//        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                ViseLog.i("value = " + value);
                circleProgressbar.updateProgress(value);
            }
        });
        ValueAnimatorUtil.resetDurationScaleIfDisable();
        valueAnimator.start();
    }
}
