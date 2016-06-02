package com.chinamobile.hejiaqin.business.ui.more.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.RelativeLayout;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.hejiaqin.R;
import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.ui.more.ScanActivity;
import com.chinamobile.hejiaqin.business.utils.CommonUtils;

import java.io.File;

/**
 * Created by eshaohu on 16/5/31.
 */
public class BindTVDialog extends Dialog {
    private Context mContext;
    private ClickListener mClickListener;
    private TextView mByScanTv, mByInputTv, mByContactTv,mByCancleTv;

    public BindTVDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public BindTVDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    protected BindTVDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bind_tv);
        mClickListener = new ClickListener();
        mByScanTv = (TextView) findViewById(R.id.more_bind_tv_dialog_scan);
        mByInputTv = (TextView)findViewById(R.id.more_bind_tv_dialog_input);
        mByContactTv = (TextView) findViewById(R.id.more_bind_tv_dialog_contact);
        mByCancleTv = (TextView)  findViewById(R.id.more_bind_tv_dialog_cancle);

        mByCancleTv.setOnClickListener(mClickListener);
        mByContactTv.setOnClickListener(mClickListener);
        mByInputTv.setOnClickListener(mClickListener);
        mByScanTv.setOnClickListener(mClickListener);

    }

    class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.more_bind_tv_dialog_cancle:
                    //取消键
                    dismiss();
                    break;
                case R.id.more_bind_tv_dialog_scan:
                    dismiss();
                    Intent intent = new Intent(mContext, ScanActivity.class);
                    mContext.startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
