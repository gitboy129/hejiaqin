package com.chinamobile.hejiaqin.business.ui.basic;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.Const;
import com.chinamobile.hejiaqin.business.logic.LogicBuilder;
import com.chinamobile.hejiaqin.business.logic.contacts.IContactsLogic;
import com.chinamobile.hejiaqin.business.logic.login.ILoginLogic;
import com.chinamobile.hejiaqin.business.logic.setting.ISettingLogic;
import com.chinamobile.hejiaqin.business.manager.UserInfoCacheManager;
import com.chinamobile.hejiaqin.business.ui.basic.view.MyToast;
import com.chinamobile.hejiaqin.business.ui.dial.StbNurseCallActivity;
import com.chinamobile.hejiaqin.business.ui.dial.VtNurseCallActivity;
import com.chinamobile.hejiaqin.business.ui.setting.dialog.BindRequestDialog;
import com.chinamobile.hejiaqin.business.utils.CaaSUtil;
import com.chinamobile.hejiaqin.business.utils.DirUtil;
import com.chinamobile.hejiaqin.tv.BuildConfig;
import com.chinamobile.hejiaqin.tv.R;
import com.customer.framework.logic.BuilderImp;
import com.customer.framework.ui.BaseFragmentActivity;
import com.customer.framework.utils.LogUtil;
import com.customer.framework.utils.PermissionsChecker;
import com.customer.framework.utils.XmlParseUtil;
import com.huawei.rcs.message.TextMessage;

/**
 * desc:
 * project:hejiaqin
 * version 001
 * author:
 * Created: 2016/4/8.
 */
public abstract class BasicFragmentActivity extends BaseFragmentActivity {

    private MyToast myToast;

    private Dialog waitDialog;

    private boolean mIsNecessaryPermission;

    protected boolean networkConnected = true;

    private ISettingLogic settingLogic;
    BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = connectMgr.getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isConnected()) {
                doNetWorkConnect();
            } else {
                doNetworkDisConnect();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myToast = new MyToast(this.getApplicationContext());
        setContentView(getLayoutId());
        initView();
        initListener();
        initDate();
        //注册监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
        MyActivityManager.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyActivityManager.getInstance().setCurrentActivityName(this.getClass().getName());
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initDate();

    protected abstract void initListener();

    @Override
    protected void handleStateMessage(Message msg) {
        //只在当前activity处理
        if (MyActivityManager.getInstance().isCurrentActity(this.getClass().getName())) {
            switch (msg.what) {
                case BussinessConstants.CommonMsgId.NETWORK_ERROR_MSG_ID:
                    showToast(R.string.network_error_tip, Toast.LENGTH_SHORT, null);
                    break;
                case BussinessConstants.DialMsgID.VOIP_REGISTER_KICK_OUT_MSG_ID:
                    showToast(R.string.kick_out, Toast.LENGTH_SHORT, null);
                    ((ILoginLogic) super.getLogicByInterfaceClass(ILoginLogic.class)).logout();
                    //                    Intent intent = new Intent(this, LoginActivity.class);
                    //                    this.startActivity(intent);
                    this.finishAllActivity(null);
                    break;
                case BussinessConstants.CommonMsgId.SERVER_SIDE_ERROR:
                    dismissWaitDailog();
                    showToast(R.string.server_side_error, Toast.LENGTH_SHORT, null);
                    break;
                case BussinessConstants.SettingMsgID.BIND_REQUEST:
                    TextMessage message = (TextMessage) msg.obj;
                    showBindRequest(message);
                    break;
                case BussinessConstants.SettingMsgID.SEND_CONTACT_REQUEST:
                    settingLogic = (ISettingLogic) super
                            .getLogicByInterfaceClass(ISettingLogic.class);
                    TextMessage req = (TextMessage) msg.obj;
                    if (!UserInfoCacheManager.isBindedApp(getApplicationContext(),
                            XmlParseUtil.getElemString(req.getContent(), "Param3"))) {
                        settingLogic.sendContact(req.getPeer().getNumber(),
                                CaaSUtil.OpCode.SEND_CONTACT_RESPOND_DENIDE, null);
                        break;
                    }
                    IContactsLogic contactsLogic = (IContactsLogic) super
                            .getLogicByInterfaceClass(IContactsLogic.class);
                    String names = XmlParseUtil.getElemString(req.getContent(), "Param1");
                    String numbers = XmlParseUtil.getElemString(req.getContent(), "Param2");
                    String[] nameList = names.split(";");
                    String[] numList = numbers.split(";");
                    for (int i = 0; i < nameList.length; i++) {
                        contactsLogic.addAppContact(nameList[i], numList[i], null);
                    }
                    settingLogic.sendContact(req.getPeer().getNumber(),
                            CaaSUtil.OpCode.SEND_CONTACT_RESPOND_SUCCESS, null);
                    showToast("保存联系人成功", Toast.LENGTH_SHORT, null);
                    break;
                case BussinessConstants.DialMsgID.NURSE_ON_TV_INCOMING_MSG_ID:
                    LogUtil.d(tagString, "NURSE_ON_TV_INCOMING_MSG_ID");
                    if (msg.obj != null) {
                        if (Const.getDeviceType() == Const.TYPE_OTHER) {
                            Intent intent2 = new Intent(this, VtNurseCallActivity.class);
                            this.startActivity(intent2);
                        } else {
                            Intent intent2 = new Intent(this, StbNurseCallActivity.class);
                            this.startActivity(intent2);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void initSystem(Context context) {
        //根据build.gradle设置日志级别
        LogUtil.setContext(getApplicationContext());
        LogUtil.setLogLevel(BuildConfig.LOG_LEVEL);
        LogUtil.setLogCommonDir(DirUtil.getExternalFileDir(context) + "/log/common/");
        LogUtil.d(tagString, "device:" + Build.DEVICE);
        LogUtil.d(tagString, "model:" + Build.MODEL);
        ((ILoginLogic) super.getLogicByInterfaceClass(ILoginLogic.class)).loadUserFromLocal();
        ((ILoginLogic) super.getLogicByInterfaceClass(ILoginLogic.class)).loadHistoryFromLocal();
    }

    @TargetApi(23)
    protected boolean checkPermissions(String[] needPermissions, boolean isNecessary) {
        mIsNecessaryPermission = isNecessary;
        if (Build.VERSION.SDK_INT >= 23 && needPermissions != null && needPermissions.length != 0) {
            if (PermissionsChecker.lacksPermissions(getApplicationContext(), needPermissions)) {
                startPermissionsActivity(needPermissions);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void startPermissionsActivity(String[] needPermissions) {
        Intent intent = new Intent(BasicFragmentActivity.this, PermissionsActivity.class);
        intent.putExtra(BussinessConstants.CommonInfo.INTENT_EXTRA_PERMISSIONS, needPermissions);
        startActivityForResult(intent,
                BussinessConstants.ActivityRequestCode.PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        switch (requestCode) {
            case BussinessConstants.ActivityRequestCode.PERMISSIONS_REQUEST_CODE:
                if (resultCode == BussinessConstants.CommonInfo.PERMISSIONS_DENIED
                        && mIsNecessaryPermission) {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected BuilderImp createLogicBuilder(Context context) {
        return LogicBuilder.getInstance(context);
    }

    protected void showToast(int resId) {
        myToast.showToast(resId, Toast.LENGTH_SHORT, null);
    }

    protected void showToast(int resId, int duration, MyToast.Position pos) {
        myToast.showToast(resId, duration, pos);
    }

    protected void showToast(String text, int duration, MyToast.Position pos) {
        myToast.showToast(text, duration, pos);
    }

    /***/
    public void showToast(View view, int duration, MyToast.Position pos) {
        myToast.showToast(view, duration, pos);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(connectionReceiver);
        MyActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    protected void finishAllActivity(String exceptActivityName) {
        MyActivityManager.getInstance().finishAllActivity(exceptActivityName);
    }

    protected void showWaitDailog() {
        if (waitDialog != null) {
            waitDialog.cancel();
        }
        waitDialog = new HejiaqinProgressDialog(this, null);
        waitDialog.show();
    }

    protected void dismissWaitDailog() {
        if (waitDialog != null) {
            waitDialog.cancel();
        }
    }

    /***/
    public void doNetWorkConnect() {
        this.networkConnected = true;

    }

    /***/
    public void doNetworkDisConnect() {
        this.networkConnected = false;
    }

    private void showBindRequest(TextMessage message) {
        Intent intent = new Intent(BasicFragmentActivity.this, BindRequestDialog.class);
        intent.putExtra("message", message);
        startActivity(intent);
    }
}
