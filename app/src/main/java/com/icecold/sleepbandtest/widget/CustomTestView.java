package com.icecold.sleepbandtest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Description: 自定义练习的view
 * @author: MrWang
 * @date: 2019/8/2
 */
public class CustomTestView extends View {

    private Paint mPaint;
    private Path ccwRectpath;
    private Path cwRectpath;
    private String mText;

    public CustomTestView(Context context) {
        this(context,null);
    }

    public CustomTestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomTestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //然后画出两条路径
        canvas.drawPath(ccwRectpath,mPaint);
        canvas.drawPath(cwRectpath,mPaint);

        //对两种路径写文字
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(1);
        mPaint.setTextSize(30);

        canvas.drawTextOnPath(mText,ccwRectpath,0,18,mPaint);
        canvas.drawTextOnPath(mText,cwRectpath,0,18,mPaint);
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);

        //第一条路径是逆向生成的
        ccwRectpath = new Path();
        RectF rect1 = new RectF(50, 50, 250, 250);
//        ccwRectpath.addRect(rect1, Path.Direction.CCW);
        ccwRectpath.addRoundRect(rect1,15,15, Path.Direction.CCW);

        //第二条路径顺向生成的
        cwRectpath = new Path();
        float[] radii = {10,15,20,25,30,35,40,45};
        RectF rect2 = new RectF(290, 50, 480, 200);
//        cwRectpath.addRect(rect2, Path.Direction.CW);
        cwRectpath.addRoundRect(rect2,radii, Path.Direction.CW);

        mText = "苦心人天不负，有志者事竟成";
    }

}
