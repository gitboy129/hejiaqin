package com.chinamobile.hejiaqin.business.net.login;

import android.content.Context;

import com.chinamobile.hejiaqin.business.dbapdater.SystemMessageDbAdapter;
import com.chinamobile.hejiaqin.business.manager.UserInfoCacheManager;
import com.chinamobile.hejiaqin.business.model.login.UserInfo;
import com.customer.framework.utils.LogUtil;
import com.google.gson.Gson;
import com.littlec.sdk.entity.AckMessage;
import com.littlec.sdk.entity.CMGroup;
import com.littlec.sdk.entity.CMMember;
import com.littlec.sdk.entity.CMMessage;
import com.littlec.sdk.entity.SystemMessage;
import com.littlec.sdk.manager.CMIMHelper;
import com.littlec.sdk.utils.CMChatListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by eshaohu on 16/6/27.
 */
public class CMIMHelperManager {
    private static final String TAG = "CMIMHelperManager";

    private Context mContext;

    public CMIMHelperManager(Context mContext) {
        this.mContext = mContext;
    }

    private Context getContext() {
        return this.mContext;
    }

    /***/
    public void doLogin(UserInfo userInfo) {
        CMIMHelper.getCmAccountManager().doLogin(userInfo.getImAccount(), userInfo.getImPassword(),
                new CMChatListener.OnCMListener() {
                    @Override
                    public void onSuccess() {
                        LogUtil.d(TAG, "LittleC login success");
                        addConnectionListener();
                        addMessageReceivedListener();
                    }

                    @Override
                    public void onFailed(String s) {
                        LogUtil.d(TAG, "LittleC login failed");
                    }
                });
    }

    private void addConnectionListener() {
        CMIMHelper.getCmAccountManager().addConnectionListener(
                new CMChatListener.OnConnectionListener() {
                    @Override
                    public void onReConnected() {
                        LogUtil.d(TAG, "小溪推送SDK--重连成功");
                    }

                    @Override
                    public void onDisConnected() {
                        LogUtil.d(TAG, "小溪推送SDK--已断开");
                    }

                    @Override
                    public void onAccountConflict() {
                        LogUtil.d(TAG, "小溪推送SDK--用户冲突");
                    }

                    @Override
                    public void onAccountDestroyed() {
                        LogUtil.d(TAG, "小溪推送SDK--账号已删除");
                    }
                });
    }

    private void addMessageReceivedListener() {
        CMIMHelper.addListeners(null, new CMChatListener.CMMessageReceivedCallBack() {
            @Override
            public void onReceivedChatMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedChatMessage");
            }

            @Override
            public void onReceivedToPullMessages(CMMessage cmMessage, int i) {
                LogUtil.d(TAG, "onReceivedToPullMessages");
            }

            @Override
            public void onReceivedGroupChatMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedGroupChatMessage");
            }

            @Override
            public void onReceivedCreateGroupMessage(CMMessage cmMessage, CMGroup cmGroup) {
                LogUtil.d(TAG, "onReceivedCreateGroupMessage");
            }

            @Override
            public void onReceivedExitGroupMessage(CMMessage cmMessage, String s) {
                LogUtil.d(TAG, "onReceivedExitGroupMessage");
            }

            @Override
            public void onReceivedKickMemberMessage(CMMessage cmMessage, String s, CMMember cmMember) {
                LogUtil.d(TAG, "onReceivedKickMemberMessage");
            }

            @Override
            public void onReceivedSetGroupNameMessage(CMMessage cmMessage, String s, String s1) {
                LogUtil.d(TAG, "onReceivedSetGroupNameMessage");
            }

            @Override
            public void onReceivedMemberNickChangedMessage(CMMessage cmMessage, String s, String s1) {
                LogUtil.d(TAG, "onReceivedMemberNickChangedMessage");
            }

            @Override
            public void onReceivedInvitationMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedInvitationMessage");
            }

            @Override
            public void onReceivedGroupDestoryedMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedGroupDestoryedMessage");
            }

            @Override
            public void onReceivedAddMembersMessage(CMMessage cmMessage, String s,
                                                    List<CMMember> list) {
                LogUtil.d(TAG, "onReceivedAddMembersMessage");
            }

            @Override
            public void onReceivedSystemMessage(SystemMessage systemMessage) {
                LogUtil.d(TAG, "onReceivedSystemMessage");
                try {
                    JSONObject systemMessageJSonObj = new JSONObject(systemMessage.getContent());
                    Gson gson = new Gson();
                    com.chinamobile.hejiaqin.business.model.more.SystemMessage receivedSystemMessage = gson.fromJson(
                            systemMessageJSonObj.getString("data"),
                            com.chinamobile.hejiaqin.business.model.more.SystemMessage.class);
                    SystemMessageDbAdapter.getInstance(getContext(),
                            UserInfoCacheManager.getUserId(getContext()))
                            .add(receivedSystemMessage);
                } catch (JSONException e) {
                    LogUtil.e(TAG, e);
                }
            }

            @Override
            public void onReceivedAckMessage(AckMessage ackMessage) {
                LogUtil.d(TAG, "onReceivedAckMessage");
            }

            @Override
            public void onReceivedOwnerChangedMessage(CMMessage cmMessage, String s,
                                                      CMMember cmMember) {
                LogUtil.d(TAG, "onReceivedOwnerChangedMessage");
            }

            @Override
            public void onReceivedAdminChangedMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedAdminChangedMessage");
            }

            @Override
            public void onReceivedJoinRequestMessage(CMMessage cmMessage) {
                LogUtil.d(TAG, "onReceivedJoinRequestMessage");
            }
        });
    }
}
