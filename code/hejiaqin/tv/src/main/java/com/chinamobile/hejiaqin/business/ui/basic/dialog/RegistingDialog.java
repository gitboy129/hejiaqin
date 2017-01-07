package com.chinamobile.hejiaqin.business.ui.basic.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.chinamobile.hejiaqin.business.ui.basic.view.MyToast;
import com.chinamobile.hejiaqin.tv.R;

/**
 * Created by eshaohu on 17/1/4.
 */
public class RegistingDialog extends Dialog {
    public static final String TAG = RegistingDialog.class.getSimpleName();

    private MyToast myToast;
    private Context mContext;
    private Handler handler = new Handler();

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mHandler != null) {
                RegistingDialog.this.handleStateMessage(msg);
            }
        }
    };

    public RegistingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow_registing);

    }

    private void handleStateMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
        }
    }

    protected void showToast(int resId, int duration, MyToast.Position pos) {
        myToast.showToast(resId, duration, pos);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public static void show(Activity activity)
    {
        RegistingDialog videoInComingDialog = new RegistingDialog(activity, R.style.CalendarDialog);
        Window window = videoInComingDialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        videoInComingDialog.setCancelable(false);
        videoInComingDialog.show();
    }
}
