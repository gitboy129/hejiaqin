package com.chinamobile.hejiaqin.business.ui.basic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.chinamobile.hejiaqin.R;

/***/
public class QNumberPicker extends NumberPicker {

    public QNumberPicker(Context context) {
        super(context);
    }

    public QNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    /***/
    public void updateView(View view) {
        if (view instanceof EditText) {
            //这里修改字体的属性
            ((EditText) view).setTextSize(getResources().getDimensionPixelSize(
                    R.dimen.date_picker_text_size));
        }
    }

}
