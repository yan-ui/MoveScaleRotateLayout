package com.example.hasee.movescalerotatelayout.weight;

import android.content.Context;
import android.support.v7.widget.ActionBarContextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.hasee.movescalerotatelayout.R;

/**
 * Created by Hasee on 2018/3/25.
 */
public class ActionModeCustomViewContainer extends RelativeLayout {
    WindowManager wm;

    public void init(Context context) {
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public ActionModeCustomViewContainer(Context context) {
        super(context);
        init(context);
    }

    public ActionModeCustomViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ActionModeCustomViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private static final String TAG = "ActionModeCustomViewCon";
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int customWidthMeasureSpec = widthMeasureSpec;

        if (getParent() instanceof ActionBarContextView) {
            View closeLayout = ((ActionBarContextView) getParent()).findViewById(R.id.action_mode_close_button);
            if (null != closeLayout) {
                Log.e(TAG, "onMeasure: "+closeLayout.getDisplay().getWidth()+"  |   "+closeLayout.getMeasuredWidth() + "   |   "+width );
//                customWidthMeasureSpec = MeasureSpec.makeMeasureSpec(closeLayout.getMeasuredWidth() + width + 48, mode);
                customWidthMeasureSpec = MeasureSpec.makeMeasureSpec(closeLayout.getDisplay().getWidth(), mode);
                closeLayout.setVisibility(GONE);
            }
        }

        super.onMeasure(customWidthMeasureSpec, heightMeasureSpec);
    }
}



