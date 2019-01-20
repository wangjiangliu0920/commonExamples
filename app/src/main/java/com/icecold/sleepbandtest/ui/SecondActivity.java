package com.icecold.sleepbandtest.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.utils.ValueAnimatorUtil;
import com.icecold.sleepbandtest.widget.CircleProgressbar;
import com.vise.log.ViseLog;

import java.util.ArrayList;

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
    @BindView(R.id.viewFlipper)
    ViewFlipper viewFlipper;
    @BindView(R.id.span_text)
    TextView spanText;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        handler = new Handler();

        ButterKnife.bind(this);
        initViewAndData();
        circleProgressbar.setmProgressText("文字");
        Log.d(TAG, "onCreate: 栈 id = " + getTaskId());
//        ViseLog.i("栈 id = "+getTaskId());
    }

    @OnClick({R.id.start_anim, R.id.skip_movie})
    public void allClickEvent(View view) {
        switch (view.getId()) {
            case R.id.start_anim:
                startProgressAnim();
                break;
            case R.id.skip_movie:
                skipMovieActivity();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewFlipper.stopFlipping();
    }

    private void initViewAndData() {
        ArrayList<String> allAdText = new ArrayList<>();
        allAdText.add("个人所得税");
        allAdText.add("快应用");
        allAdText.add("微信阅读-2018年度金米奖");

        for (String adText : allAdText) {
            View adView = View.inflate(this, R.layout.view_advertisement, null);
            TextView adTextView = adView.findViewById(R.id.ad_text);
            adTextView.setText(adText);
            viewFlipper.addView(adView);
        }

        spanText.setText("这是一个基础的文本,可以查看详情");
        highLight(spanText.getText().length() - 2,spanText.getText().length());
    }

    private void highLight(int start, int end) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(spanText.getText().toString());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                widget.setFocusable(false);
                Toast.makeText(SecondActivity.this,"文字被点击了",Toast.LENGTH_SHORT).show();
            }
        };
        spannable.setSpan(clickableSpan,start,end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setText(spannable);
        spannable.append("新加入的");
        spanText.setText(spannable);
        spanText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void skipMovieActivity() {
        Intent intent = new Intent(this, MovieActivity.class);
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
