package com.icecold.sleepbandtest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.icecold.sleepbandtest.R;

/**
 *
 * Created by icecold_laptop_2 on 2018/7/25.
 */

public class CircleProgressbar extends View {

    private static final float CIRCLE_LINE_WIDTH = 10F;
    private static final float TEXT_SIZE = 20f;
    private static final float CIRCLE_RADIUS = 100F;
    private Paint mCirclePaint;
    private Paint mCircleInnerPaint;
    private TextPaint mTextPaint;
    private int halfWidth;
    private int halfHeight;
    private String mProgressText;
    private boolean mStartProgress;
    private float mProgress;

    public CircleProgressbar(Context context) {
        super(context);
        initParameter(context);
    }

    public CircleProgressbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParameter(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfWidth = w / 2;
        halfHeight = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mStartProgress){
            RectF mCircleRectf = new RectF(halfWidth - CIRCLE_RADIUS,halfHeight - CIRCLE_RADIUS,halfWidth + CIRCLE_RADIUS,halfHeight + CIRCLE_RADIUS);
            float swipeProgress = (100- mProgress) / 100f * 360;
            canvas.drawArc(mCircleRectf,-90,-swipeProgress,false,mCirclePaint);
        }else {
            //绘制外圆
            canvas.drawCircle(halfWidth, halfHeight, CIRCLE_RADIUS,mCirclePaint);
        }
        //绘制内圆
        canvas.drawCircle(halfWidth,halfHeight,90,mCircleInnerPaint);
        //绘制文字
        float textWidth = mTextPaint.measureText(mProgressText, 0, mProgressText.length());

        canvas.drawText(mProgressText,halfWidth - textWidth / 2,halfHeight - (mTextPaint.ascent() + mTextPaint.descent()) / 2,mTextPaint);
    }

    private void initParameter(Context context) {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(CIRCLE_LINE_WIDTH);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(ContextCompat.getColor(context, R.color.circle_color));

        mCircleInnerPaint = new Paint();
        mCircleInnerPaint.setAntiAlias(true);
        mCircleInnerPaint.setStyle(Paint.Style.FILL);
        mCircleInnerPaint.setColor(ContextCompat.getColor(context, R.color.circle_inner_color));

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setColor(ContextCompat.getColor(context, R.color.circle_text_color));
        mTextPaint.setTextSize(TEXT_SIZE);
    }
    public void updateProgress(float progress){
        if (progress > 100){
            progress = 100;
        }
        if (progress < 0){
            progress = 0;
        }
        mProgress = progress;
        postInvalidate();
    }

    public String getmProgressText() {
        return mProgressText;
    }

    public void setmProgressText(String mProgressText) {
        this.mProgressText = mProgressText;
    }

    public boolean isStartProgress() {
        return mStartProgress;
    }

    public void setStartProgress(boolean mStartProgress) {
        this.mStartProgress = mStartProgress;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float mProgress) {
        this.mProgress = mProgress;
    }
}
