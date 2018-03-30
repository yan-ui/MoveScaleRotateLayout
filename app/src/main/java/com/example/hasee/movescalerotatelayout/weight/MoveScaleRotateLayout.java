package com.example.hasee.movescalerotatelayout.weight;

import android.annotation.TargetApi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.hasee.movescalerotatelayout.R;

/**
 * Created by Hasee on 2018/3/25.
 */

public class MoveScaleRotateLayout extends FrameLayout {

    private ActionMode actionMode;
    private Paint mPaint;

    public MoveScaleRotateLayout(Context context) {
        super(context);
        init(context);
    }

    public MoveScaleRotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MoveScaleRotateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MoveScaleRotateLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

//    private View popup_window_view;

    private void init(Context context) {
//        mContext = context;
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        // 设置画笔为抗锯齿
        mPaint.setAntiAlias(true);
        action_mode_Callback = new MyCallback();
        actionMode = ((AppCompatActivity) context).startSupportActionMode(action_mode_Callback);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private int contentTop;     //标题栏+状态栏 高度
    private int statusBarHeight; //状态栏高度
    private int titleBarHeight; //标题栏高度

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.e(TAG, "onWindowFocusChanged: ");
        Rect rect = new Rect();
            /*
             * getWindow().getDecorView()得到的View是Window中的最顶层View，可以从Window中获取到该View，
             * 然后该View有个getWindowVisibleDisplayFrame()方法可以获取到程序显示的区域，
             * 包括标题栏，但不包括状态栏。
             */
        ((AppCompatActivity) getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        /**
         *
         *
         * 1.获取状态栏高度：
         * 根据上面所述，我们可以通过rect对象得到手机状态栏的高度
         * int statusBarHeight = rect.top;
         *
         * 2.获取标题栏高度：
         * getWindow().findViewById(Window.ID_ANDROID_CONTENT);
         * 该方法获取到的View是程序不包括标题栏的部分，这样我们就可以计算出标题栏的高度了。
         * int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
         * //statusBarHeight是上面所求的状态栏的高度
         * int titleBarHeight = contentTop - statusBarHeight
         */
        statusBarHeight = rect.top;
        contentTop = ((AppCompatActivity) getContext()).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        titleBarHeight = contentTop - statusBarHeight;
        Log.d("debug", "statusBarHeight :" + statusBarHeight + " contentTop :" + contentTop + "  titleHeight:" + titleBarHeight);

    }

    private int left, top, right, bottom;
    private int final_left, final_top, final_right, final_bottom;
    private boolean isIntercept = false;
    //touch_x  touch_y  的功能主要是为了获取到监听事件的触发位置
    // 例如view旋转了， 但是监听事件其实还在原位置没有变化，此时就会根据touch 位置反推出原位置，实现监听
    private int x, y, touch_x, touch_y;
    private long lastClickTime;
    private long currentClickTime;

    @Override
    protected void onLayout(final boolean changed, final int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(TAG, "onLayout: 个数：" + getChildCount());
        View view = getChildAt(0);
        final_left = left = view.getLeft();
        final_top = top = view.getTop();
        final_bottom = bottom = view.getBottom();
        final_right = right = view.getRight();

        Log.i(TAG, "onLayout: " + left + "  |  " + top + "  |  " + right + "  |  " + bottom);

        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                x = (int) event.getRawX();
                y = (int) event.getRawY() - contentTop - statusBarHeight;

                //旋转了视图之后点击事件触发位置还在原位，但此时View摆放位置已发生变化
                // 所以需要根据当前位置及角度推导出原位置，从而判断原位置是否是点击事件的触发位置
                touch_x = -1;
                touch_y = -1;
                if (v.getRotation() != 0) {
                    int[] location = getBeforeRotationTouchLocation(v.getRotation(), x, y);
                    touch_x = location[0];
                    touch_y = location[1];
                }
                Log.e(TAG, "onTouch: button    " + x + "   |  " + y + "   |  " + titleBarHeight + "   |  " + contentTop + "   |  " + statusBarHeight);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        lastClickTime = System.currentTimeMillis();

                        isIntercept = false;
                        touchFlag = getTouchedPath(x, y);
                        if (touch_x != -1 && touch_y != -1) {
                            touchFlag = getTouchedPath(touch_x, touch_y);
                        }

                        currentFlag = touchFlag;

                        v.setAlpha(0.5f);
                        Log.e(TAG, "onTouch:   button  ACTION_DOWN ：" + touchFlag);

                        down_x = x;
                        down_y = y;
                        isMove = false;
                        isScale = false;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        currentClickTime = System.currentTimeMillis();
                        if (currentClickTime - lastClickTime > 300) {
                            //根据ACTION_DOWN与ACTION_UP之间停留的时间来判断用户是点击还是长按 若是长按则让view移动，若是点击则走点击监听

                            Log.e(TAG, "onTouch:   button  ACTION_MOVE ");
                            isIntercept = true;

                            isMove = true;
                            if (touch_x != -1 && touch_y != -1) {
                                currentFlag = getTouchedPath(touch_x, touch_y);
                            } else {
                                currentFlag = getTouchedPath(x, y);
                            }
                            switch (touchFlag) {
                                case CENTER:
                                    //移动
                                    left = final_left = left + (x - down_x);
                                    top = final_top = top + (y - down_y);
                                    right = final_right = right + (x - down_x);
                                    bottom = final_bottom = bottom + (y - down_y);

                                    down_x = x;
                                    down_y = y;
                                    break;

                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        currentClickTime = System.currentTimeMillis();
                        if (currentClickTime - lastClickTime > 300) {
                            //根据ACTION_DOWN与ACTION_UP之间停留的时间来判断用户是点击还是长按 若是长按则让view移动，若是点击则走点击监听
                            isIntercept = true;
                        } else {
                            isIntercept = false;
                        }
                        Log.e(TAG, "onTouch:   button  ACTION_UP ");

                        //重新初始化参数
                        isMove = false;
                        isScale = false;

                        v.setAlpha(1f);
                        v.layout(final_left, final_top, final_right, final_bottom);
                        if (angle != 0) {
                            v.setRotation(angle);
                            angle = 0;
                            isRotate = false;
                        }

                        right = final_right;
                        left = final_left;
                        bottom = final_bottom;
                        top = final_top;
                        touchFlag = currentFlag = -1;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.e(TAG, "onTouch:   button  ACTION_CANCEL ");
                        isIntercept = false;
                        break;
                }
                invalidate();
                Log.e(TAG, "onTouch:   button   " + isIntercept);
                return isIntercept;
            }
        });
    }

    private static final String TAG = "MoveScaleLayout";

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isMove) {
            //当前手指是否正在滑动
            mPaint.setColor(Color.GRAY);
            if (isScale) {
                // 在手指滑动的状态下：  放大缩小手势滑动处理
                float x = (right - left) * scale_x; //现在的宽度
                float y = (bottom - top) * scale_y;  //现在的高度

                switch (touchFlag) {
                    case LEFT_TOP:
                        //左上角缩放滑动事件：
                        final_left = (int) (final_right - x);
                        final_top = (int) (final_bottom - y);

                        if (final_left > final_right - 100 && final_top > final_bottom - 100) {
                            final_left = final_right - 100;
                            final_top = final_bottom - 100;
                        } else if (final_left > final_right - 100 && final_top < final_bottom - 100) {
                            final_left = final_right - 100;
                        } else if (final_left < final_right - 100 && final_top > final_bottom - 100) {
                            final_top = final_bottom - 100;
                        }

                        //判断子View 是否有旋转角度  根据旋转角度绘制相同大小的倾斜背景
                        if (getChildAt(0).getRotation() != 0) {
                            Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                            canvas.save();
                            canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                            canvas.drawRect(rect, mPaint);
                            canvas.restore();
                        } else {
                            canvas.drawRect(final_left, final_top, final_right, final_bottom, mPaint);
                        }
                        break;
                    case LEFT_BOTTOM:
                        //左下角缩放滑动事件：
                        final_left = (int) (final_right - x);
                        final_bottom = (int) (final_top + y);

                        if (final_left > final_right - 100 && final_top > final_bottom - 100) {
                            final_left = final_right - 100;
                            final_bottom = final_top + 100;
                        } else if (final_left > final_right - 100 && final_top < final_bottom - 100) {
                            final_left = final_right - 100;
                        } else if (final_left < final_right - 100 && final_top > final_bottom - 100) {
                            final_bottom = final_top + 100;
                        }

                        //判断子View 是否有旋转角度  根据旋转角度绘制相同大小的倾斜背景
                        if (getChildAt(0).getRotation() != 0) {
                            Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                            canvas.save();
                            canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                            canvas.drawRect(rect, mPaint);
                            canvas.restore();
                        } else {
                            canvas.drawRect(final_left, final_top, final_right, final_bottom, mPaint);
                        }

                        break;
                    case RIGHT_TOP:
                        //右上角缩放滑动事件：
                        final_right = (int) (final_left + x);
                        final_top = (int) (final_bottom - y);

                        if (final_left > final_right - 100 && final_top > final_bottom - 100) {
                            final_right = final_left + 100;
                            final_top = final_bottom - 100;
                        } else if (final_left > final_right - 100 && final_top < final_bottom - 100) {
                            final_right = final_left + 100;
                        } else if (final_left < final_right - 100 && final_top > final_bottom - 100) {
                            final_top = final_bottom - 100;
                        }

                        //判断子View 是否有旋转角度  根据旋转角度绘制相同大小的倾斜背景
                        if (getChildAt(0).getRotation() != 0) {
                            Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                            canvas.save();
                            canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                            canvas.drawRect(rect, mPaint);
                            canvas.restore();
                        } else {
                            canvas.drawRect(final_left, final_top, final_right, final_bottom, mPaint);
                        }
                        break;
                    case RIGHT_BOTTOM:
                        //右下角缩放滑动事件：
                        final_right = (int) (final_left + x);
                        final_bottom = (int) (final_top + y);

                        if (final_left > final_right - 100 && final_top > final_bottom - 100) {
                            final_right = final_left + 100;
                            final_bottom = final_top + 100;
                        } else if (final_left > final_right - 100 && final_top < final_bottom - 100) {
                            final_right = final_left + 100;
                        } else if (final_left < final_right - 100 && final_top > final_bottom - 100) {
                            final_bottom = final_top + 100;
                        }

                        //判断子View 是否有旋转角度  根据旋转角度绘制相同大小的倾斜背景
                        if (getChildAt(0).getRotation() != 0) {
                            Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                            canvas.save();
                            canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                            canvas.drawRect(rect, mPaint);
                            canvas.restore();
                        } else {
                            canvas.drawRect(final_left, final_top, final_right, final_bottom, mPaint);
                        }
                        break;
                }

            } else if (isRotate) {
                //在手指滑动的状态下： 旋转滑动手势处理
                if (angle != 0) {
                    //若检测到旋转角度不为 0 ，则进行旋转处理
                    Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                    canvas.save();
                    canvas.rotate(angle, rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                    canvas.drawRect(rect, mPaint);

                    mPaint.setColor(Color.BLACK);
                    mPaint.setPathEffect(new DashPathEffect(new float[]{20, 5}, 0));
                    setLayerType(LAYER_TYPE_SOFTWARE, null);


                    int show_top;
                    int show_bottom;
                    if (final_top - 250 > 0) {
                        show_top = final_top - 250;
                        show_bottom = final_top - 150;
                    } else {
                        show_top = 0;
                        show_bottom = 100;
                    }

                    canvas.drawLine((final_right - final_left) / 2 + final_left, final_top - 200,
                            (final_right - final_left) / 2 + final_left, (final_bottom - final_top) / 2 + final_top, mPaint);
                    Rect targetRect = new Rect((final_right - final_left) / 2 + final_left - 100, show_top,
                            (final_right - final_left) / 2 + final_left + 100, show_bottom);

                    mPaint.setTextSize(80);
                    String testString = ((int) angle) + "°";
                    mPaint.setColor(Color.BLACK);
                    canvas.drawRect(targetRect, mPaint);
                    mPaint.setColor(Color.WHITE);
                    Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                    int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(testString, targetRect.centerX(), baseline, mPaint);
                    canvas.restore();
                }

            } else {
                //在手指滑动的状态下： 整体移动的处理
                if (getChildAt(0).getRotation() != 0) {
                    Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                    canvas.save();
                    canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                    canvas.drawRect(rect, mPaint);
                    canvas.restore();
                } else {
                    canvas.drawRect(final_left, final_top, final_right, final_bottom, mPaint);
                }
            }

        } else if (isRotate) {
            //当前只是点击了 旋转 按钮 ，并没有其它操作   画出一条平分矩形的虚线 和 一个TEXT 用于显示当前旋转角度


            if (getChildAt(0).getRotation() != 0) {
                //点击 旋转 按钮状态下 ：  在子view已经有了一定的旋转角度的情况下  绘制的处理
                Rect rect = new Rect(final_left, final_top, final_right, final_bottom);
                canvas.save();
                canvas.rotate(getChildAt(0).getRotation(), rect.left + (rect.right - rect.left) / 2, rect.top + (rect.bottom - rect.top) / 2); //以矩形中心点旋转图形
                canvas.drawRect(rect, mPaint);

                mPaint.setColor(Color.BLACK);
                mPaint.setPathEffect(new DashPathEffect(new float[]{20, 5}, 0));
                setLayerType(LAYER_TYPE_SOFTWARE, null);

                int show_top;
                int show_bottom;
                if (final_top - 250 > 0) {
                    show_top = final_top - 250;
                    show_bottom = final_top - 150;
                } else {
                    show_top = 0;
                    show_bottom = 100;
                }

                canvas.drawLine((final_right - final_left) / 2 + final_left, final_top - 200,
                        (final_right - final_left) / 2 + final_left, (final_bottom - final_top) / 2 + final_top, mPaint);
                Rect targetRect = new Rect((final_right - final_left) / 2 + final_left - 100, show_top,
                        (final_right - final_left) / 2 + final_left + 100, show_bottom);

                mPaint.setTextSize(80);
                String testString = ((int) getChildAt(0).getRotation()) + "°";
                mPaint.setColor(Color.BLACK);
                canvas.drawRect(targetRect, mPaint);
                mPaint.setColor(Color.WHITE);
                Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                mPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(testString, targetRect.centerX(), baseline, mPaint);
                canvas.restore();
            } else {
                //点击 旋转 按钮状态下 ：子view旋转角度为0 时的绘制处理
                mPaint.setColor(Color.BLACK);
                mPaint.setPathEffect(new DashPathEffect(new float[]{20, 5}, 0));
                setLayerType(LAYER_TYPE_SOFTWARE, null);


                int show_top;
                int show_bottom;

                if (final_top - 250 > 0) {
                    show_top = final_top - 250;
                    show_bottom = final_top - 150;
                } else {
                    show_top = 0;
                    show_bottom = 100;
                }

                canvas.drawLine((final_right - final_left) / 2 + final_left, final_top - 200,
                        (final_right - final_left) / 2 + final_left, (final_bottom - final_top) / 2 + final_top, mPaint);
                Rect targetRect = new Rect((final_right - final_left) / 2 + final_left - 100, show_top,
                        (final_right - final_left) / 2 + final_left + 100, show_bottom);

                mPaint.setTextSize(80);
                String testString = ((int) angle) + "°";
                mPaint.setColor(Color.BLACK);
                canvas.drawRect(targetRect, mPaint);
                mPaint.setColor(Color.WHITE);
                Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                mPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(testString, targetRect.centerX(), baseline, mPaint);

            }
        } else {
            //当前屏幕没有任何操作的情况下 默认绘制显示的视图
            // 在此状态下会有两种情况发生：1.当检测到了action_down动作时  2.当检测到了action_up动作时
            if (!isRotate) {
                View view = getChildAt(0);
                if (view.getRotation() != 0) {
                    canvas.save();
                    canvas.rotate(view.getRotation(), final_left + (final_right - final_left) / 2, final_top + (final_bottom - final_top) / 2); //以矩形中心点旋转图形
                    Log.e(TAG, "onDraw: " + view.getRotation());
                    mPaint.setColor(Color.CYAN);
                    canvas.drawCircle(final_left, final_top, 20, mPaint);
                    canvas.drawCircle(final_right, final_top, 20, mPaint);
                    canvas.drawCircle(final_left, final_bottom, 20, mPaint);
                    canvas.drawCircle(final_right, final_bottom, 20, mPaint);

                    int show_top;
                    int show_bottom;
                    if (final_top - 150 > 0) {
                        show_top = final_top - 150;
                        show_bottom = final_top - 50;
                    } else {
                        show_top = 0;
                        show_bottom = 100;
                    }

//                mPaint.setColor(Color.BLACK);
//                canvas.drawLine((final_right - final_left) / 2 + final_left, show_top, (final_right - final_left) / 2 + final_left, show_bottom, mPaint);

                    Rect targetRect = new Rect((final_right - final_left) / 2 + final_left - 250, show_top,
                            (final_right - final_left) / 2 + final_left + 250, show_bottom);

                    mPaint.setTextSize(50);
                    String rotateString = "旋转";
                    String copyString = "复制";
                    String splitString = "|";
                    mPaint.setColor(Color.GRAY);
                    canvas.drawRect(targetRect, mPaint);
                    mPaint.setColor(Color.WHITE);
                    Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                    int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(rotateString, targetRect.centerX() - 100, baseline, mPaint);
                    canvas.drawText(splitString, targetRect.centerX(), baseline, mPaint);
                    canvas.drawText(copyString, targetRect.centerX() + 100, baseline, mPaint);

                    canvas.restore();
                } else {
                    mPaint.setColor(Color.CYAN);
                    canvas.drawCircle(final_left, final_top, 20, mPaint);
                    canvas.drawCircle(final_right, final_top, 20, mPaint);
                    canvas.drawCircle(final_left, final_bottom, 20, mPaint);
                    canvas.drawCircle(final_right, final_bottom, 20, mPaint);


                    int show_top;
                    int show_bottom;
                    if (final_top - 150 > 0) {
                        show_top = final_top - 150;
                        show_bottom = final_top - 50;
                    } else {
                        show_top = 0;
                        show_bottom = 100;
                    }
//
//                mPaint.setColor(Color.BLACK);
//                canvas.drawLine((final_right - final_left) / 2 + final_left, show_bottom, (final_right - final_left) / 2 + final_left, show_top, mPaint);
//

                    Rect targetRect = new Rect((final_right - final_left) / 2 + final_left - 200, show_top,
                            (final_right - final_left) / 2 + final_left + 200, show_bottom);

                    mPaint.setTextSize(50);
                    String rotateString = "旋转";
                    String copyString = "复制";
                    String splitString = "|";
                    mPaint.setColor(Color.GRAY);
                    canvas.drawRect(targetRect, mPaint);
                    mPaint.setColor(Color.WHITE);
                    Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                    int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(rotateString, targetRect.centerX() - 100, baseline, mPaint);
                    canvas.drawText(splitString, targetRect.centerX(), baseline, mPaint);
                    canvas.drawText(copyString, targetRect.centerX() + 100, baseline, mPaint);

                }
            }
        }

    }


    private static final int CENTER = 0;
    private static final int LEFT_TOP = 1;
    private static final int RIGHT_TOP = 2;
    private static final int RIGHT_BOTTOM = 3;
    private static final int LEFT_BOTTOM = 4;
    private static final int ROTATE_LINE = 5;
    private static final int BTN_ROTATION = 6;
    private static final int BTN_COPY = 7;
    protected int touchFlag = -1;
    protected int currentFlag = -1;
    private int down_x;
    private int down_y;
    private boolean isMove;
    private boolean isScale;
    private boolean isRotate;
    //记录原始落点的时候两个手指之间的距离
    private float[] oldDist; //0：X轴   1：Y轴
    private float scale_x;//X轴放大倍数
    private float scale_y;//Y轴放大倍数
    private float angle; //旋转角度
    private MyCallback action_mode_Callback;

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        x = (int) event.getX();
        y = (int) event.getY();

        //旋转了视图之后点击事件触发位置还在原位，但此时View摆放位置已发生变化
        // 所以需要根据当前位置及角度推导出原位置，从而判断原位置是否是点击事件的触发位置
        touch_x = -1;
        touch_y = -1;
        if (getChildAt(0).getRotation() != 0) {
            int[] location = getBeforeRotationTouchLocation(getChildAt(0).getRotation(), x, y);
            touch_x = location[0];
            touch_y = location[1];
        }
        Log.e(TAG, "onTouchEvent: " + x + "   |  " + y);


        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                Log.e(TAG, "onTouchEvent:  ViewGroup Down");
                touchFlag = getTouchedPath(x, y);
                if (touch_x != -1 && touch_y != -1) {
                    touchFlag = getTouchedPath(touch_x, touch_y);
                }
                currentFlag = touchFlag;

                getChildAt(0).setAlpha(0.5f);
                Log.i(TAG, "ACTION_DOWN：" + touchFlag);

                down_x = x;
                down_y = y;
                isMove = false;
                isScale = false;


                switch (touchFlag) {
                    case LEFT_TOP:
                        oldDist = spacing(down_x, down_y, final_right, final_bottom);
                        break;
                    case LEFT_BOTTOM:
                        oldDist = spacing(down_x, down_y, final_right, final_top);
                        break;
                    case RIGHT_TOP:
                        oldDist = spacing(down_x, down_y, final_left, final_bottom);
                        break;
                    case RIGHT_BOTTOM:
                        oldDist = spacing(down_x, down_y, final_left, final_top);
                        break;
                    case BTN_ROTATION:
                        isRotate = true;
                        Toast.makeText(getContext(), "旋转", Toast.LENGTH_SHORT).show();
                        break;
                    case BTN_COPY:
                        Toast.makeText(getContext(), "复制", Toast.LENGTH_SHORT).show();
                        break;
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "非第一个触摸点按下");

                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onTouchEvent: ViewGroup Move");
                isMove = true;
                if (touch_x != -1 && touch_y != -1) {
                    currentFlag = getTouchedPath(touch_x, touch_y);
                } else {
                    currentFlag = getTouchedPath(x, y);
                }

                switch (touchFlag) {
                    case CENTER:
                        //移动
                        left = final_left = left + (x - down_x);
                        top = final_top = top + (y - down_y);
                        right = final_right = right + (x - down_x);
                        bottom = final_bottom = bottom + (y - down_y);

                        down_x = x;
                        down_y = y;
                        break;
                    case LEFT_TOP:
                        isScale = true;
                        //处理缩放模块

                        float[] newDist = spacing(x, y, final_right, final_bottom);
                        scale_x = newDist[0] / oldDist[0];
                        scale_y = newDist[1] / oldDist[1];

                        down_x = x;
                        down_y = y;
                        break;
                    case LEFT_BOTTOM:
                        isScale = true;
                        //处理缩放模块
                        newDist = spacing(x, y, final_right, final_top);
                        scale_x = newDist[0] / oldDist[0];
                        scale_y = newDist[1] / oldDist[1];

                        down_x = x;
                        down_y = y;
                        break;
                    case RIGHT_TOP:
                        isScale = true;
                        //处理缩放模块
                        newDist = spacing(x, y, final_left, final_bottom);
                        scale_x = newDist[0] / oldDist[0];
                        scale_y = newDist[1] / oldDist[1];

                        down_x = x;
                        down_y = y;
                        break;
                    case RIGHT_BOTTOM:
                        isScale = true;
                        //处理缩放模块
                        newDist = spacing(x, y, final_left, final_top);
                        scale_x = newDist[0] / oldDist[0];
                        scale_y = newDist[1] / oldDist[1];

                        down_x = x;
                        down_y = y;
                        break;

                    case ROTATE_LINE:
                        //处理旋转模块
                        float rotation = getChildAt(0).getRotation();
                        angle = rotation + angleBetweenLines(down_x, down_y, x, y);
                        Log.e(TAG, "onTouchEvent: 旋转角度：" + angle);

                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");
                if (touch_x != -1 && touch_y != -1) {
                    currentFlag = getTouchedPath(touch_x, touch_y);
                } else {
                    currentFlag = getTouchedPath(x, y);
                }

                //重新初始化参数
                isMove = false;
                isScale = false;

                View view = getChildAt(0);
                view.setAlpha(1f);
                view.layout(final_left, final_top, final_right, final_bottom);
                if (angle != 0) {
                    view.setRotation(angle);
                    angle = 0;
                    isRotate = false;
                }

                right = final_right;
                left = final_left;
                bottom = final_bottom;
                top = final_top;
                touchFlag = currentFlag = -1;

                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "非第一个触摸点抬起");

                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "ACTION_CANCEL");
                touchFlag = currentFlag = -1;
                isMove = false;
                isScale = false;
                break;
        }

        invalidate();

        return true;
    }


    // 获取当前触摸点在哪个区域
    private int getTouchedPath(int x, int y) {

        int show_top;
        int show_bottom;
        if (final_top - 150 > 0) {
            show_top = final_top - 150;
            show_bottom = final_top - 50;
        } else {
            show_top = 0;
            show_bottom = 100;
        }

        int scope = -1;
        if (final_left + 20 > x && final_left - 20 < x && final_top + 20 > y && final_top - 20 < y) {
            //触摸点在左上角范围内
            scope = 1;
        } else if (final_left + 20 > x && final_left - 20 < x && final_bottom + 20 > y && final_bottom - 20 < y) {
            //触摸点在左下角范围内
            scope = 4;
        } else if (final_right + 20 > x && final_right - 20 < x && final_top + 20 > y && final_top - 20 < y) {
            //触摸点在右上角范围内
            scope = 2;
        } else if (final_right + 20 > x && final_right - 20 < x && final_bottom + 20 > y && final_bottom - 20 < y) {
            //触摸点在右下角范围内
            scope = 3;
        } else if (x > final_left + 50 && x < final_right - 50 && y > final_top + 50 && y < final_bottom - 50) {
            //触摸点在中间
            scope = 0;
        } else if ((final_right - final_left) / 2 + final_left + 20 > x && (final_right - final_left) / 2 + final_left - 20 < x
                && final_top - 150 < y && final_top > y) {
            //当触摸点在旋转线上才默认为旋转事件发生
            if (isRotate) {
                scope = 5;
            }
        } else if ((final_right - final_left) / 2 + final_left > x && (final_right - final_left) / 2 + final_left - 200 < x
                && y > show_top && y < show_bottom) {
            //触摸点为 旋转 按钮
            if (!isRotate) {
                //当旋转按钮没有被点击时
                scope = 6;
            }
        } else if ((final_right - final_left) / 2 + final_left + 200 > x && (final_right - final_left) / 2 + final_left < x
                && y > show_top && y < show_bottom) {
            //触摸点为 复制 按钮
            if (!isRotate) {
                //当旋转按钮没有被点击时
                scope = 7;
            }
        }
        Log.e(TAG, "getTouchedPath: " + scope + "   |   " + x + "   |   " + y);
        return scope;
    }


    /**
     * |
     * |               B
     * |
     * |     A
     * |                 C
     * --------------------------->
     * |
     * |
     * |
     * V
     * 已知圆心A，圆上一点坐标B，旋转角度，求旋转后的点C坐标
     * <p>
     * 当View 旋转过后，View的属性并没有发生变化，只是绘制的时候旋转了画布以及View，
     * 点击事件的触发位置也在原来的位置，没有发生变化，此方法是根据   旋转角度、
     * 圆心位置、 当前触摸点位置  三个参数 反过来推导出旋转前的坐标位置
     * 即： 通过当前屏幕点击的位置 B（a,b）  计算出旋转前 B应该在屏幕中的坐标
     *
     * @param angle  旋转角度
     * @param down_x 当前按下的X轴坐标 距离屏幕左边距离
     * @param down_y 当前按下的Y轴坐标 距离屏幕顶部距离
     * @return 旋转前该触摸点的位置
     */
    private int[] getBeforeRotationTouchLocation(float angle, int down_x, int down_y) {
        angle = -angle;  //旋转角度取相反值是为了反推坐标，而不是为了获得旋转后的坐标
        int[] location = new int[2];
        int a = (final_right - final_left) / 2 + final_left; //圆心A点X坐标
        int b = (final_bottom - final_top) / 2 + final_top; //圆心A点Y坐标

        double cos_ab = Math.cos(Math.toRadians(angle));
        double sin_ab = Math.sin(Math.toRadians(angle));

        location[0] = (int) (a + (down_x - a) * cos_ab - (down_y - b) * sin_ab);
        location[1] = (int) (b + (down_x - a) * sin_ab + (down_y - b) * cos_ab);

        return location;
    }

    private int[] getAfterRotationTouchLocation(float angle, int down_x, int down_y) {
        int[] location = new int[2];
        int a = (final_right - final_left) / 2 + final_left; //圆心A点X坐标
        int b = (final_bottom - final_top) / 2 + final_top; //圆心A点Y坐标

        double cos_ab = Math.cos(Math.toRadians(angle));
        double sin_ab = Math.sin(Math.toRadians(angle));

        location[0] = (int) (a + (down_x - a) * cos_ab - (down_y - b) * sin_ab);
        location[1] = (int) (b + (down_x - a) * sin_ab + (down_y - b) * cos_ab);

        return location;
    }

    /**
     * 计算两点之间的距离
     *
     * @return 两点之间的距离
     */
    private float[] spacing(int new_down_x, int new_down_y, int old_down_x, int old_down_y) {
        float[] space = new float[2];
        space[0] = new_down_x - old_down_x;
        space[1] = new_down_y - old_down_y;
        return space;
    }


    /**
     * 计算刚开始触摸的点与滑动停止后的点所构成的滑动角度  计算角度的圆心以View的中心点为准
     *
     * @param startX 初始点x坐标
     * @param startY 初始点y坐标
     * @param endX   终点x坐标
     * @param endY   终点y坐标
     * @return 构成的角度值
     */
    private float angleBetweenLines(float startX, float startY, float endX, float endY) {
        float angle1 = (float) Math.atan2((final_bottom - final_top) / 2 + final_top - startY, (final_right - final_left) / 2 + final_left - startX);
        float angle2 = (float) Math.atan2((final_bottom - final_top) / 2 + final_top - endY, (final_right - final_left) / 2 + final_left - endX);

        float angle = ((float) Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return -angle;
    }


    private class MyCallback implements ActionMode.Callback, OnClickListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.i(TAG, "onCreateActionMode: ");
            //可自定义最上层显示的view
            View v = LayoutInflater.from(getContext()).inflate(
                    R.layout.action_mode_custom_view, null);
            mode.setCustomView(v);
            v.findViewById(R.id.action_mode_cancel).setOnClickListener(this);
            v.findViewById(R.id.action_mode_save).setOnClickListener(this);
            v.findViewById(R.id.action_mode_back).setOnClickListener(this);
            v.findViewById(R.id.action_mode_go).setOnClickListener(this);
            v.findViewById(R.id.action_mode_edit).setOnClickListener(this);
            actionMode = mode;
//            mode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.i(TAG, "onPrepareActionMode: ");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.action_mode_cancel:
                    actionMode.finish();
                    Toast.makeText(getContext(), "取消!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_mode_save:
                    Toast.makeText(getContext(), "保存!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_mode_back:
                    Toast.makeText(getContext(), "后退!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_mode_go:
                    Toast.makeText(getContext(), "前进!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_mode_edit:
                    Toast.makeText(getContext(), "编辑!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }


}

