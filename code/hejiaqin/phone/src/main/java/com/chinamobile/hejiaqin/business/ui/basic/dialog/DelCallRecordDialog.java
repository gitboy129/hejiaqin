package com.chinamobile.hejiaqin.business.ui.basic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.chinamobile.hejiaqin.R;

/**
 * Created by  on 2016/6/25.
 */
public class DelCallRecordDialog extends Dialog {
    public LinearLayout delLayout;
    public LinearLayout cancelLayout;

    public DelCallRecordDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow_del_call_record);
        delLayout = (LinearLayout) findViewById(R.id.del_call_record_layout);
        cancelLayout = (LinearLayout) findViewById(R.id.cancel_layout);
    }

}
