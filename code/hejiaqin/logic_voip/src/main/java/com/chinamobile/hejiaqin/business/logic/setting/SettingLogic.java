package com.chinamobile.hejiaqin.business.logic.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.manager.UserInfoCacheManager;
import com.chinamobile.hejiaqin.business.model.login.UserInfo;
import com.chinamobile.hejiaqin.business.model.more.TvSettingInfo;
import com.chinamobile.hejiaqin.business.model.more.UserList;
import com.chinamobile.hejiaqin.business.model.more.VersionInfo;
import com.chinamobile.hejiaqin.business.model.more.req.GetBindListReq;
import com.chinamobile.hejiaqin.business.model.more.req.GetDeviceListReq;
import com.chinamobile.hejiaqin.business.model.more.req.SaveBindRequest;
import com.chinamobile.hejiaqin.business.net.IHttpCallBack;
import com.chinamobile.hejiaqin.business.net.setting.SettingHttpmanager;
import com.chinamobile.hejiaqin.business.utils.CaaSUtil;
import com.chinamobile.hejiaqin.business.utils.CommonUtils;
import com.chinamobile.hejiaqin.business.utils.SysInfoUtil;
import com.customer.framework.component.net.NetResponse;
import com.customer.framework.component.threadpool.ThreadPoolUtil;
import com.customer.framework.component.threadpool.ThreadTask;
import com.customer.framework.logic.LogicImp;
import com.customer.framework.utils.LogUtil;
import com.customer.framework.utils.XmlParseUtil;
import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.log.LogApi;
import com.huawei.rcs.message.Conversation;
import com.huawei.rcs.message.Message;
import com.huawei.rcs.message.MessageConversation;
import com.huawei.rcs.message.MessagingApi;
import com.huawei.rcs.message.TextMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by eshaohu on 16/5/24.
 */
public class SettingLogic extends LogicImp implements ISettingLogic {
    private static final String TAG = "SettingLogic";
    private static SettingLogic instance;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = (Message) intent.getSerializableExtra(MessagingApi.PARAM_MESSAGE);
            if (msg == null) {
                LogUtil.i(TAG, "Message received.but it is null");
                return;
            }
            msg.read();
            LogUtil.i(TAG, "Message received. Message body: " + msg.getBody());
            int msgType = msg.getType();
            switch (msgType) {
                case Message.MESSAGE_TYPE_TEXT: { // 文本消息
                    TextMessage textMessage = (TextMessage) msg;
                    LogUtil.d(TAG, "receive message: " + msg.getBody() + " Peer name: "
                            + msg.getPeer().getNumber());
                    handleTextMessage(textMessage);
                    break;
                }
                default: { // 其他消息
                }
            }
            if (this.isOrderedBroadcast()) {
                LogUtil.i(TAG, "abort the broadcast");
                this.abortBroadcast();
            }
        }
    };

    private BroadcastReceiver mMessageStatusChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = (Message) intent.getSerializableExtra(MessagingApi.PARAM_MESSAGE);
            if (null != msg) {
                switch (msg.getStatus()) {
                    case Message.STATUS_DELIVERY_OK:
                        LogUtil.i(TAG,
                                "发送消息成功(Message.STATUS_DELIVERY_OK)，Message body：" + msg.getBody());
                        sendEmptyMessage(BussinessConstants.SettingMsgID.STATUS_DELIVERY_OK);
                        break;
                    case Message.STATUS_DISPLAY_OK:
                        LogUtil.i(TAG,
                                "发送消息成功(Message.STATUS_DISPLAY_OK)，Message body：" + msg.getBody());
                        sendEmptyMessage(BussinessConstants.SettingMsgID.STATUS_DISPLAY_OK);
                        break;
                    case Message.STATUS_SEND_FAILED:
                        LogUtil.i(TAG,
                                "发送消息失败(Message.STATUS_SEND_FAILED)，Message body：" + msg.getBody());
                        ThreadPoolUtil.execute(new ThreadTask() {

                            @Override
                            public void run() {
                                LogApi.copyLastLog();
                            }
                        });
                        sendEmptyMessage(BussinessConstants.SettingMsgID.STATUS_SEND_FAILED);
                        break;
                    case Message.STATUS_UNDELIVERED:
                        LogUtil.i(TAG,
                                "发送消息失败(Message.STATUS_UNDELIVERED)，Message body：" + msg.getBody());
                        ThreadPoolUtil.execute(new ThreadTask() {

                            @Override
                            public void run() {
                                LogApi.copyLastLog();
                            }
                        });
                        sendEmptyMessage(BussinessConstants.SettingMsgID.STATUS_UNDELIVERED);
                        break;
                    default:
                        break;
                }
                return;
            }
        }
    };

    private BroadcastReceiver missCallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //            Toast.makeText(context, "receive misscall", Toast.LENGTH_LONG).show();
            LogUtil.i(TAG, "receive misscall");
        }
    };

    private void handleTextMessage(TextMessage msg) {
        String cmdType = getCmdType(msg.getContent());
        switch (cmdType) {
            case CaaSUtil.CmdType.BIND:
                handleBindRequest(msg);
                break;
            case CaaSUtil.CmdType.SEND_CONTACT:
                handleSendContact(msg);
                break;
            default:
                break;

        }
    }

    private void handleSendContact(TextMessage msg) {
        switch (getOpCode(msg.getContent())) {
            case CaaSUtil.OpCode.SEND_CONTACT:
                LogUtil.d(TAG, "Will send the SEND_CONTACT_REQUEST message to UI");
                sendMessage(BussinessConstants.SettingMsgID.SEND_CONTACT_REQUEST, msg);
                break;
            case CaaSUtil.OpCode.SEND_CONTACT_RESPOND_SUCCESS:
                LogUtil.d(TAG, "Will send the SEND_CONTACT_RESPOND_SUCCESS message to UI");
                sendMessage(BussinessConstants.SettingMsgID.SEND_CONTACT_RESPOND_SUCCESS, msg);
                break;
            case CaaSUtil.OpCode.SEND_CONTACT_RESPOND_DENIDE:
                LogUtil.d(TAG, "Will send the SEND_CONTACT_RESPOND_DENIDE message to UI");
                sendMessage(BussinessConstants.SettingMsgID.SEND_CONTACT_RESPOND_DENIED, msg);
                break;
            default:
                break;
        }
    }

    private void handleBindRequest(TextMessage msg) {
        switch (getOpCode(msg.getContent())) {
            case CaaSUtil.OpCode.BIND:
                LogUtil.d(TAG, "Will send the BIND_REQUEST message to UI");
                sendMessage(BussinessConstants.SettingMsgID.BIND_REQUEST, msg);
                break;
            case CaaSUtil.OpCode.BIND_SUCCESS:
                LogUtil.d(TAG, "Will send the BIND_SUCCESS message to UI");
                sendMessage(BussinessConstants.SettingMsgID.BIND_SUCCESS, msg);
                break;
            case CaaSUtil.OpCode.BIND_DENIED:
                LogUtil.d(TAG, "Will send the BIND_DENIED message to UI");
                sendMessage(BussinessConstants.SettingMsgID.BIND_DENIED, msg);
                break;
            default:
                break;
        }
    }

    private SettingLogic(Context context) {
        init(context.getApplicationContext());
    }

    /**
     * 获取单例对象
     *
     * @param context 系统的context对象
     * @return LogicBuilder对象
     */
    public synchronized static SettingLogic getInstance(Context context) {
        if (instance == null) {
            instance = new SettingLogic(context);
        }
        return instance;
    }

    @Override
    public void handleCommit(Context context, String inputNumber, final String id) {
        TvSettingInfo settingInfo = UserInfoCacheManager.getUserSettingInfo(context);
        if (settingInfo != null) {
            switch (id) {
                case "numberOne":
                    settingInfo.setNumberOne(inputNumber);
                    break;
                case "numberTwo":
                    settingInfo.setNumberTwo(inputNumber);
                    break;
                case "numberThree":
                    settingInfo.setNumberThree(inputNumber);
                    break;
                case "numberFour":
                    settingInfo.setNumberFour(inputNumber);
                    break;
                default:
                    break;
            }
            UserInfoCacheManager.updateUserSetting(context, settingInfo);
            SettingLogic.this
                    .sendEmptyMessage(BussinessConstants.SettingMsgID.AUTO_ANSWER_SETTING_COMMIT);
        }
    }

    @Override
    public void getDeviceList() {
        final GetDeviceListReq reqBody = new GetDeviceListReq();
        new SettingHttpmanager(getContext()).getDeviceList(null, reqBody, new IHttpCallBack() {
            @Override
            public void onSuccessful(Object invoker, Object obj) {
                UserList userList = new UserList();
                userList.setUsers((List<UserInfo>) obj);
                LogUtil.i(TAG, "obj size " + ((List<UserInfo>) obj).size());
                UserInfoCacheManager.saveBindDeviceToLoacl(getContext(), userList);
                UserInfoCacheManager.saveBindDeviceToMem(getContext(), userList);
                SettingLogic.this.sendMessage(
                        BussinessConstants.SettingMsgID.GET_DEVICE_LIST_SUCCESSFUL, obj);
            }

            @Override
            public void onFailure(Object invoker, String code, String desc) {

            }

            @Override
            public void onNetWorkError(NetResponse.ResponseCode errorCode) {

            }
        });
    }

    @Override
    public void getBindList() {
        final GetBindListReq reqBody = new GetBindListReq();
        new SettingHttpmanager(getContext()).getBindList(null, reqBody, new IHttpCallBack() {
            @Override
            public void onSuccessful(Object invoker, Object obj) {
                UserList userList = new UserList();
                userList.setUsers((List<UserInfo>) obj);
                UserInfoCacheManager.saveBindAppToLoacl(getContext(), userList);
                UserInfoCacheManager.saveBindAppToMem(getContext(), userList);
                SettingLogic.this.sendMessage(
                        BussinessConstants.SettingMsgID.GET_BIND_LIST_SUCCESSFUL, obj);
            }

            @Override
            public void onFailure(Object invoker, String code, String desc) {

            }

            @Override
            public void onNetWorkError(NetResponse.ResponseCode errorCode) {

            }
        });
    }

    @Override
    public void checkVersion() {
        new SettingHttpmanager(getContext()).checkVersion(null, new IHttpCallBack() {
            @Override
            public void onSuccessful(Object invoker, Object obj) {
                VersionInfo info = (VersionInfo) obj;
                //                //TODO Test
                //                info = new VersionInfo();
                //                info.setVersionCode("1");
                //                info.setVersionName("版本1.0");
                //                info.setUrl("http://downsz.downcc.com/apk/dianxinxiangjia_downcc.apk");
                //                info.setTime("2016-03-01 15:17:42");
                //                info.setByForce(0);
                //                info.setForceVersionCode("1");
                //                //TODO Test End
                if (isNewVersion(info)) {
                    UserInfoCacheManager.saveVersionInfoToLoacl(getContext(), info);
                    if (isForceUpgrade(info)) {
                        LogUtil.i(TAG, "New force version found!");
                        SettingLogic.this.sendMessage(
                                BussinessConstants.SettingMsgID.NEW_FORCE_VERSION_AVAILABLE, info);
                    } else {
                        LogUtil.i(TAG, "New version found!");
                        SettingLogic.this.sendMessage(
                                BussinessConstants.SettingMsgID.NEW_VERSION_AVAILABLE, info);
                    }
                } else {
                    UserInfoCacheManager.clearVersionInfo(getContext());
                    SettingLogic.this
                            .sendEmptyMessage(BussinessConstants.SettingMsgID.NO_NEW_VERSION_AVAILABLE);
                }
            }

            @Override
            public void onFailure(Object invoker, String code, String desc) {
                UserInfoCacheManager.clearVersionInfo(getContext());
            }

            @Override
            public void onNetWorkError(NetResponse.ResponseCode errorCode) {
                UserInfoCacheManager.clearVersionInfo(getContext());
                SettingLogic.this.sendMessage(BussinessConstants.CommonMsgId.NETWORK_ERROR_MSG_ID,
                        errorCode);
            }
        });
    }

    @Override
    public void saveBindRequest(final TextMessage message) {
        SaveBindRequest req = new SaveBindRequest();
        req.setPhone(XmlParseUtil.getElemString(message.getContent(), "Phone"));
        new SettingHttpmanager(getContext()).saveBindRequest(null, req, new IHttpCallBack() {
            @Override
            public void onSuccessful(Object invoker, Object obj) {
                LogUtil.d(TAG, "Save the bind request success");
                sendMessage(BussinessConstants.SettingMsgID.SAVE_BIND_REQUEST_SUCCESS, message);
            }

            @Override
            public void onFailure(Object invoker, String code, String desc) {
                LogUtil.d(TAG, "Save failed");
            }

            @Override
            public void onNetWorkError(NetResponse.ResponseCode errorCode) {

            }
        });
    }

    @Override
    public void sendBindReq(String tvNumber, String phoneNum) {
        sendEmptyMessage(BussinessConstants.SettingMsgID.SENDING_BIND_REQUEST);
        sendTextMessage(tvNumber, CaaSUtil.CmdType.BIND, CaaSUtil.OpCode.BIND, phoneNum);
    }

    @Override
    public void sendBindResult(String toNumber, String opCode) {
        sendTextMessage(toNumber, CaaSUtil.CmdType.BIND, opCode, null);
    }

    @Override
    public void bindSuccNotify() {
        sendEmptyMessage(BussinessConstants.SettingMsgID.UPDATE_DEVICE_LIST_REQUEST);
    }

    @Override
    public void sendContact(String toNumber, String opCode, Map<String, String> params) {
        LogUtil.i(TAG, "send contact to:" + toNumber);
        sendTextMessage(CommonUtils.getPhoneNumber(toNumber), CaaSUtil.CmdType.SEND_CONTACT, "1",
                opCode, null, params);
    }

    private void sendTextMessage(String toNumber, String cmdType, String opCode, String phone) {
        sendTextMessage(toNumber, cmdType, "1", opCode, phone, null);
    }

    private void sendTextMessage(String toNumber, String cmdType, String seq, String opCode,
                                 String phoneNum, Map<String, String> params) {
        MessageConversation mMessageConversation = MessageConversation
                .getConversationByNumber(toNumber);
        LogUtil.d(TAG, "Peer name: " + mMessageConversation.getPeerName());
        Intent intent = new Intent();
        intent.putExtra(MessageConversation.PARAM_SEND_TEXT_PAGE_MODE, true);
        intent.putExtra(Conversation.PARAM_SERVICE_KIND, Conversation.SERVICE_KIND_THROUGH_SEND_MSG);
        mMessageConversation.sendText(
                CaaSUtil.buildMessage(cmdType, seq, opCode, phoneNum, params), intent);
    }

    private boolean isNewVersion(VersionInfo versionInfo) {
        if (versionInfo == null) {
            return false;
        }
        int currentVersioncode;
        int versionCode;
        currentVersioncode = SysInfoUtil.getVersionCode(getContext());
        try {
            versionCode = Integer.parseInt(versionInfo.getVersionCode());
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return false;
        }
        if (currentVersioncode < versionCode) {
            LogUtil.d(TAG, "currentVersioncode: " + currentVersioncode + " , versionCode: "
                    + versionCode);
            return true;
        }
        return false;
    }

    private boolean isForceUpgrade(VersionInfo versionInfo) {
        if (versionInfo.getByForce() == 1) {
            return true;
        }
        int currentVersioncode;
        int forceVersionCode;
        currentVersioncode = SysInfoUtil.getVersionCode(getContext());
        try {
            forceVersionCode = Integer.parseInt(versionInfo.getForceVersionCode());
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return false;
        }
        if (currentVersioncode < forceVersionCode) {
            return true;
        }
        return false;
    }

    /***/
    public void registerMessageReceiver() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(MessagingApi.EVENT_MESSAGE_INCOMING));
        //        LocalBroadcastManager.getInstance(getContext())
        //                .registerReceiver(mMessageReceiver, new IntentFilter("SMS_DELIVER_ACTION"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mMessageStatusChangedReceiver,
                new IntentFilter(MessagingApi.EVENT_MESSAGE_STATUS_CHANGED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(missCallReceiver,
                new IntentFilter(CallApi.EVENT_MISS_CALL));
    }

    /***/
    public void unRegisterMessageReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(
                mMessageStatusChangedReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(missCallReceiver);
    }

    public String getOpCode(String toParse) {
        return XmlParseUtil.getElemString(toParse, "OpCode");
    }

    public String getCmdType(String toParse) {
        return XmlParseUtil.getElemString(toParse, "CmdType");
    }

    public String getPhone(String toParse) {
        return XmlParseUtil.getElemString(toParse, "Phone");
    }
}
