package com.icecold.sleepbandtest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @Description: 类似于支付宝的雷达图
 * @author: MrWang
 * @date: 2019/8/5
 */
public class RadarView extends View {
    private int count = 6;//多边形的数量
    private float angle = (float) (Math.PI * 2 / count);//每个多边形其中一份对应的角度
    private float radius;//网格最大半径
    private int centerX;//中心点X
    private int centerY;//中心点Y
    private String[] titles = {"历史","行为","履约能力","人脉","身份","行为偏好"};
    private Paint mPaintText;//绘制文字的画笔
    private Paint mPaintRadar;//绘制雷达区域的画笔
    private Paint mPaintData;//绘制内部数据的画笔
    private Path mRadarPath;//绘制雷达区域的路径
    private Path mDataPath;//绘制内部数据的画笔
    private Path mMiddlePath;//绘制网格中线的路径
    public RadarView(Context context) {
        this(context,null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //要绘制网格的最大半径是最大宽高的0.9倍
        radius = (Math.max(w, h) >> 1) * 0.9f;
        //画网格线的中心点是在这个视图的中心点上
        centerX = w/2;
        centerY = h/2;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制蜘蛛网格
        drawPolygon(canvas);
        //绘制网格中线
        drawLines(canvas);
        //画多边形中的标题描述
        drawTitle(canvas);
        //画数据图
        drawRegion(canvas);
    }

    private void drawPolygon(Canvas canvas) {
        float rSpace = radius / count;//每一层的间距
        for (int i = 1; i <= count; i++) {
            float currentRadius = rSpace * i;
            //每次绘制之前都先重置路径
            mRadarPath.reset();
            for (int j = 0; j < count; j++) {
                if (j == 0){
                    //表示开始,移动到开始的位置上
                    mRadarPath.moveTo(centerX + currentRadius,centerY);
                }else {
                    //根据半径，计算出每个蜘蛛丝上每个点的坐标
                    float x = (float) (centerX + currentRadius * Math.cos(angle * j));
                    float y = (float) (centerY + currentRadius * Math.sin(angle * j));
                    mRadarPath.moveTo(x,y);
                }
            }
            //闭合路径
            mRadarPath.close();
            canvas.drawPath(mRadarPath,mPaintRadar);
        }
    }

    private void drawLines(Canvas canvas) {
        for (int i = 0; i < count; i++) {
            //首先重置路径
            mMiddlePath.reset();
            //移动到中心点处
            mMiddlePath.moveTo(centerX,centerY);
            float x = (float) (centerX + radius * Math.cos(angle * i));
            float y = (float) (centerY + radius * Math.sin(angle * i));
            mMiddlePath.lineTo(x,y);
            canvas.drawPath(mMiddlePath,mPaintRadar);
        }
    }

    private void drawRegion(Canvas canvas) {

    }

    private void drawTitle(Canvas canvas) {

    }

    private void init() {
        //初始化雷达区域的画笔
        mPaintRadar = new Paint();
        mPaintRadar.setAntiAlias(true);
        mPaintRadar.setStrokeWidth(3);
        mPaintRadar.setColor(Color.WHITE);
        mPaintRadar.setAlpha(90);
        mPaintRadar.setStyle(Paint.Style.FILL_AND_STROKE);

        //初始化数据区域的画笔
        mPaintData = new Paint();
        mPaintData.setAntiAlias(true);
        mPaintData.setColor(Color.BLUE);
        mPaintData.setStrokeWidth(6);
        mPaintData.setAlpha(150);
        mPaintData.setStyle(Paint.Style.FILL);

        //初始化文字的画笔
        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(20);
        mPaintText.setColor(Color.GRAY);
        mPaintText.setStyle(Paint.Style.FILL);

        //初始化雷达区域的路径
        mRadarPath = new Path();
        //初始化数据的路径
        mDataPath = new Path();
        mMiddlePath = new Path();
    }
}
