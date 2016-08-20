package com.chinamobile.hejiaqin.business.ui.main;

import android.content.Intent;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.chinamobile.hejiaqin.business.ui.basic.FocusManager;
import com.chinamobile.hejiaqin.business.ui.basic.FragmentMgr;
import com.chinamobile.hejiaqin.tv.R;
import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.logic.setting.ISettingLogic;
import com.chinamobile.hejiaqin.business.logic.voip.IVoipLogic;
import com.chinamobile.hejiaqin.business.ui.basic.BasicFragment;
import com.chinamobile.hejiaqin.business.ui.basic.BasicFragmentActivity;
import com.chinamobile.hejiaqin.business.ui.basic.dialog.DelCallRecordDialog;
import com.chinamobile.hejiaqin.business.ui.login.LoginActivity;
import com.customer.framework.component.log.Logger;
import com.customer.framework.utils.LogUtil;

/**
 * desc: main
 * project:hejiaqin
 * version 001
 * author:
 * Created: 2016/4/22.
 */
public class MainFragmentActivity extends BasicFragmentActivity {

    private static final int DIAL_STATUS_NORMAL = 1;

    private static final int DIAL_STATUS_SHOW_KEYBORD = 2;

    private static final int DIAL_STATUS_CALL = 3;

    View mNavigatorLay;

    BasicFragment[] mLeftFragments = new BasicFragment[4];

    BasicFragment[] mRightFragments = new BasicFragment[1];

//    TextView[] mTextViews = new TextView[3];

    View[] mMenuViews = new View[4];

    ImageView mDialCallImage;

    int mCurrentIndex;
    final int mRecentIndex = 0;

    final int mContactsIndex = 1;

    final int mDialIndex = 2;

    final int mSettingIndex = 3;

    private boolean mDialSeleted = false;

    private int mDialStatus = DIAL_STATUS_NORMAL;

    private long exitTime = 0;

    private IVoipLogic mVoipLogic;

    private ISettingLogic settingLogic;

    private BasicFragment.BackListener listener = new BasicFragment.BackListener() {
        public void onAction(int actionId, Object obj) {
            switch (actionId) {
                case BussinessConstants.FragmentActionId.SETTING_FRAGMENT_LOGOUT_ACTION_ID:
                    Intent intent = new Intent(MainFragmentActivity.this, LoginActivity.class);
                    MainFragmentActivity.this.startActivity(intent);
                    MainFragmentActivity.this.finishAllActivity(LoginActivity.class.getName());
                    break;
                case BussinessConstants.FragmentActionId.DAIL_FRAGMENT_SHOW_CALL_ACTION_ID:
                    if (mDialStatus == DIAL_STATUS_CALL) {
                        return;
                    }
                    mDialCallImage.setVisibility(View.VISIBLE);
                    mDialStatus = DIAL_STATUS_CALL;
                    break;
                case BussinessConstants.FragmentActionId.DAIL_FRAGMENT_HIDE_CALL_ACTION_ID:
                    mDialCallImage.setVisibility(View.GONE);
                    mDialStatus = DIAL_STATUS_SHOW_KEYBORD;
                    break;
                // 显示导航栏
                case BussinessConstants.FragmentActionId.CONTACT_FRAGMENT_SHOW_NAVIGATOR_ACTION_ID:
                    mNavigatorLay.setVisibility(View.VISIBLE);
                    break;
                // 隐藏导航栏
                case BussinessConstants.FragmentActionId.CONTACT_FRAGMENT_HIDE_NAVIGATOR_ACTION_ID:
                    mNavigatorLay.setVisibility(View.GONE);
                    break;
                case BussinessConstants.FragmentActionId.DAIL_SHOW_DEL_POP_WINDOW_MSG_ID:
                    showPopupWindow();
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_activity_main;
    }

    @Override
    protected void initView() {
        mNavigatorLay = findViewById(R.id.main_nav);

        mMenuViews[mRecentIndex] = findViewById(R.id.recent_layout);
        mMenuViews[mRecentIndex].setBackgroundColor(getResources().getColor(R.color.navigator_bg));
        mMenuViews[mContactsIndex] = findViewById(R.id.contact_layout);
        mMenuViews[mContactsIndex].setBackgroundColor(getResources().getColor(R.color.navigator_select_bg));
        mMenuViews[mDialIndex] = findViewById(R.id.dial_layout);
        mMenuViews[mDialIndex].setBackgroundColor(getResources().getColor(R.color.navigator_bg));
        mMenuViews[mSettingIndex] = findViewById(R.id.more_layout);
        mMenuViews[mSettingIndex].setBackgroundColor(getResources().getColor(R.color.navigator_bg));

        mDialCallImage = (ImageView) findViewById(R.id.dial_call_image);

        FragmentMgr.getInstance().init(this, R.id.content_left, R.id.content_right);
        mLeftFragments[mContactsIndex] = new ContactListFragment();
        mLeftFragments[mContactsIndex].setActivityListener(listener);
        FragmentMgr.getInstance().showContactFragment(mLeftFragments[mContactsIndex]);

        mRightFragments[0] = new TestFragment();
        mRightFragments[0].setActivityListener(listener);
        FragmentMgr.getInstance().showRightFragment(mRightFragments[0]);

        mCurrentIndex = mContactsIndex;

    }

    @Override
    protected void initDate() {
        settingLogic.checkVersion();
        // mVoipLogic.autoLogin();
    }

    @Override
    protected void initListener() {
        for (int i = 0; i < mMenuViews.length; i++) {
            final int menuIndex = i;
            mMenuViews[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    switchFragment(menuIndex);
                }
            });

            mMenuViews[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    switchFragment(menuIndex);
                }
            });
        }
    }

    private void switchFragment(int toIndex) {
        if (mCurrentIndex == toIndex) {
            return;
        }

        switch (toIndex) {
            case mRecentIndex:
                if (null == mLeftFragments[toIndex]) {
                    mLeftFragments[toIndex] = new TestFragment();
                    mLeftFragments[toIndex].setActivityListener(listener);
                }
                FragmentMgr.getInstance().showRecentFragment(mLeftFragments[toIndex]);
                break;
            case mContactsIndex:
                if (null == mLeftFragments[toIndex]) {
                    mLeftFragments[toIndex] = new ContactListFragment();
                    mLeftFragments[toIndex].setActivityListener(listener);
                }
                FragmentMgr.getInstance().showContactFragment(mLeftFragments[toIndex]);
                break;
            case mDialIndex:
                if (null == mLeftFragments[toIndex]) {
                    mLeftFragments[toIndex] = new DialFragment();
                    mLeftFragments[toIndex].setActivityListener(listener);
                }
                FragmentMgr.getInstance().showDialFragment(mLeftFragments[toIndex]);
                break;
            case mSettingIndex:
                if (null == mLeftFragments[toIndex]) {
                    mLeftFragments[toIndex] = new SettingFragment();
                    mLeftFragments[toIndex].setActivityListener(listener);
                }
                FragmentMgr.getInstance().showSettingFragment(mLeftFragments[toIndex]);
                break;
        }

        LogUtil.d("MainFragmentActivity", "commit:" + mLeftFragments[toIndex].getClass());

        mMenuViews[mCurrentIndex].setBackgroundColor(getResources().getColor(R.color.navigator_bg));
        mMenuViews[toIndex].setBackgroundColor(getResources().getColor(R.color.navigator_select_bg));

        mCurrentIndex = toIndex;
        FocusManager.getInstance().requestFocus(mMenuViews[mCurrentIndex]);
    }


    @Override
    protected void initLogics() {
        mVoipLogic = (IVoipLogic) super.getLogicByInterfaceClass(IVoipLogic.class);
        settingLogic = (ISettingLogic) super.getLogicByInterfaceClass(ISettingLogic.class);
    }

    private void showPopupWindow() {

        if (mDialStatus == DIAL_STATUS_SHOW_KEYBORD) {
            mDialStatus = DIAL_STATUS_NORMAL;
            //收起拨号盘
            Message msg = new Message();
            msg.what = BussinessConstants.FragmentActionId.DAIL_FRAGMENT_HIDE_KEYBORD_MSG_ID;
            mLeftFragments[mDialIndex].recieveMsg(msg);
        }

        final DelCallRecordDialog delCallRecordDialog = new DelCallRecordDialog(this, R.style.CalendarDialog);
        Window window = delCallRecordDialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        delCallRecordDialog.setCancelable(true);
        delCallRecordDialog.show();

        // 设置按钮的点击事件
        delCallRecordDialog.delLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除通话记录
                mVoipLogic.delAllCallRecord();
                delCallRecordDialog.dismiss();

            }
        });
        delCallRecordDialog.cancelLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                delCallRecordDialog.dismiss();
            }
        });

    }


    @Override
    protected void handleStateMessage(Message msg) {
        super.handleStateMessage(msg);
        dismissWaitDailog();
        switch (msg.what) {
        }
    }


    @Override
    public void doNetWorkConnect() {
        //通知Fragment网络已经连接
        if (mLeftFragments[mContactsIndex] != null) {
            mLeftFragments[mContactsIndex].doNetWorkConnect();
        }
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showToast(R.string.quit_app_toast_msg, Toast.LENGTH_SHORT, null);
            exitTime = System.currentTimeMillis();
            return;
        }
        mVoipLogic.logout();
        super.onBackPressed();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Logger.d(TAG, "KeyEvent.KEYCODE_DPAD_LEFT");
                FocusManager.getInstance().requestFocus(mMenuViews[mCurrentIndex]);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Logger.d(TAG, "KeyEvent.KEYCODE_DPAD_RIGHT");
                View leftFocusView = FocusManager.getInstance().getFocusViewInLeftFrag(String.valueOf(mCurrentIndex));
                if (null != leftFocusView) {
                    FocusManager.getInstance().requestFocus(leftFocusView);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                Logger.d(TAG, "KeyEvent.KEYCODE_DPAD_UP");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Logger.d(TAG, "KeyEvent.KEYCODE_DPAD_DOWN");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}