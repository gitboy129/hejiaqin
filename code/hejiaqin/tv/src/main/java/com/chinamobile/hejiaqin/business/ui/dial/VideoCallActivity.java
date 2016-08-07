package com.chinamobile.hejiaqin.business.ui.dial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.hejiaqin.tv.R;
import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.logic.contacts.IContactsLogic;
import com.chinamobile.hejiaqin.business.logic.voip.IVoipLogic;
import com.chinamobile.hejiaqin.business.model.contacts.ContactsInfo;
import com.chinamobile.hejiaqin.business.model.contacts.NumberInfo;
import com.chinamobile.hejiaqin.business.ui.basic.BasicActivity;
import com.chinamobile.hejiaqin.business.utils.CommonUtils;
import com.customer.framework.utils.LogUtil;
import com.customer.framework.utils.StringUtil;
import com.huawei.rcs.call.CallApi;
import com.huawei.rcs.call.CallSession;
import com.huawei.rcs.system.SysApi;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallActivity extends BasicActivity implements View.OnClickListener {

    public static final String TAG = VideoCallActivity.class.getSimpleName();

    //视频布局(呼出和通话中)
    private RelativeLayout mVideoLayout;
    private LinearLayout mLargeVideoLayout;
    private LinearLayout mSmallVideoLayout;

    //顶部布局(呼出和通话中)
    private LinearLayout mTopLayout;
    private TextView mContactNameTv;
    private LinearLayout mCallStatusLayout;
    private TextView mTalkingTimeTv;

    //底部布局(呼出和通话中)
    private LinearLayout mBottomLayout;
    private LinearLayout mMuteLayout;
    private ImageView mMuteIv;
    private LinearLayout mHangupLayout;
    private LinearLayout mCameraLayout;

    //来电布局
    private RelativeLayout mIncomingLayout;
    private CircleImageView mCallerIv;
    private TextView mCallerNameTv;
    private Button mComingAnswerCallBtn;
    private Button mComingRejectCallBtn;

    //是否来电
    private boolean mIsInComing;
    //是否通话中
    private boolean mIsTalking;

    //被叫号码
    private String mCalleeNumber;

    //被叫姓名
    private String mCalleeName;

    private IVoipLogic mVoipLogic;

    //通话会话对象
    private CallSession mCallSession = null;

    private Timer timer;
    private int callTime;
    private Handler handler = new Handler();

    /**
     * 如果重力感应变化角度过小，则不处理.
     */
    private static final int ORIENTATION_SENSITIVITY = 45;

    private int lastOrientation = 270;

    private int lastDisplayRotation = Surface.ROTATION_0;

    private SurfaceView localVideoView;
    private SurfaceView remoteVideoView;

    private boolean hasStoped = false;

    private boolean mute = false;

    private boolean speakerState;

    private boolean closed;

    private IContactsLogic mContactsLogic;

    /* display the video stream which arrived from remote. */
    private BroadcastReceiver remoteVideoStreamArrivedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CallSession session = (CallSession) intent.getSerializableExtra(CallApi.PARAM_CALL_SESSION);
            if (!mCallSession.equals(session)) {
                return;
            }
            remoteVideoView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mLargeVideoLayout.updateViewLayout(remoteVideoView, layoutParams);
        }
    };


    private BroadcastReceiver cameraSwitchedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("VOIP", "receive cameraSwitched broadcast");
            if (localVideoView != null) {
                localVideoView.setVisibility(View.VISIBLE);
            }
            mCameraLayout.setEnabled(true);
        }
    };

    @Override
    protected void initLogics() {
        mVoipLogic = (IVoipLogic) super.getLogicByInterfaceClass(IVoipLogic.class);
        mContactsLogic = (IContactsLogic)super.getLogicByInterfaceClass(IContactsLogic.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_call;
    }

    @Override
    protected void initView() {
        //视频布局(呼出和通话中)
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mLargeVideoLayout = (LinearLayout) findViewById(R.id.large_video_layout);
        mSmallVideoLayout = (LinearLayout) findViewById(R.id.small_video_layout);

        //顶部布局(呼出和通话中)
        mTopLayout = (LinearLayout) findViewById(R.id.top_layout);
        mContactNameTv = (TextView) findViewById(R.id.contact_name_tv);
        mCallStatusLayout = (LinearLayout) findViewById(R.id.call_status_layout);
        mTalkingTimeTv = (TextView) findViewById(R.id.talking_time_tv);

        //底部布局(呼出和通话中)
        mBottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        mMuteLayout = (LinearLayout) findViewById(R.id.mute_layout);
        mMuteIv = (ImageView) findViewById(R.id.mute_iv);
        mHangupLayout = (LinearLayout) findViewById(R.id.hangup_layout);
        mCameraLayout = (LinearLayout) findViewById(R.id.camera_layout);

        //来电布局
        mIncomingLayout = (RelativeLayout) findViewById(R.id.incoming_layout);
        mCallerIv = (CircleImageView) findViewById(R.id.caller_iv);
        mCallerNameTv = (TextView) findViewById(R.id.caller_name_tv);
        mComingAnswerCallBtn = (Button) findViewById(R.id.coming_answer_call_btn);
        mComingRejectCallBtn = (Button) findViewById(R.id.coming_reject_call_btn);

        mHangupLayout.setOnClickListener(this);
        mComingAnswerCallBtn.setOnClickListener(this);
        mComingRejectCallBtn.setOnClickListener(this);
        mMuteLayout.setOnClickListener(this);
        mCameraLayout.setOnClickListener(this);
    }

    @Override
    protected void initDate() {
        mIsInComing = getIntent().getBooleanExtra(BussinessConstants.Dial.INTENT_CALL_INCOMING, false);
        if (mIsInComing) {
            long sessionId = getIntent().getLongExtra(BussinessConstants.Dial.INTENT_INCOMING_SESSION_ID, CallSession.INVALID_ID);
            mCallSession = CallApi.getCallSessionById(sessionId);
            if (mCallSession == null) {
                finish();
                return;
            }
            showIncoming();
        } else {
            mCalleeNumber = getIntent().getStringExtra(BussinessConstants.Dial.INTENT_CALLEE_NUMBER);
            mCalleeName = getIntent().getStringExtra(BussinessConstants.Dial.INTENT_CALLEE_NAME);
            outingCall();
            showOuting();
        }
        if (CallApi.getCameraCount() < 2) {
            mCameraLayout.setEnabled(false);
        }
    }

    private void showIncoming() {
        String incomingNumber = CommonUtils.getPhoneNumber(mCallSession.getPeer().getNumber());
        // 查询姓名和头像信息
        ContactsInfo info = searchContactInfo(incomingNumber);
        if(info!=null)
        {
            if(!StringUtil.isNullOrEmpty(info.getName())) {
                mCallerNameTv.setText(info.getName());
                mContactNameTv.setText(info.getName());
            }else{
                mCallerNameTv.setText(incomingNumber);
                mContactNameTv.setText(incomingNumber);
            }
            if(!StringUtil.isNullOrEmpty(info.getPhotoSm())) {
                Picasso.with(this.getApplicationContext())
                        .load(info.getPhotoSm())
                        .placeholder(R.drawable.contact_photo_default)
                        .error(R.drawable.contact_photo_default).into(mCallerIv);
            }

        } else {
            mCallerNameTv.setText(incomingNumber);
            mContactNameTv.setText(incomingNumber);
        }

        mIncomingLayout.setVisibility(View.VISIBLE);
    }

    private void showOuting() {
        //TODO 查询姓名
        if(!StringUtil.isNullOrEmpty(mCalleeName))
        {
            mContactNameTv.setText(mCalleeName);
        }else{
            ContactsInfo info = searchContactInfo(mCalleeNumber);
            if(info!=null &&!StringUtil.isNullOrEmpty(info.getName()))
            {
                mContactNameTv.setText(info.getName());
            }else{
                mContactNameTv.setText(mCalleeNumber);
            }
        }
        mTopLayout.setVisibility(View.VISIBLE);
        mVideoLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
    }


    private ContactsInfo searchContactInfo(String phoneNumber)
    {
        //遍历本地联系人
        boolean isMatch = false;
        List<ContactsInfo> appContactsInfos = mContactsLogic.getCacheAppContactLst();
        for (ContactsInfo contactsInfo : appContactsInfos) {
            if(isMatch)
            {
                break;
            }
            if (contactsInfo.getNumberLst() != null) {
                for (NumberInfo numberInfo : contactsInfo.getNumberLst()) {
                    if (CommonUtils.getPhoneNumber(phoneNumber).equals(numberInfo.getNumberNoCountryCode())) {
                        return contactsInfo;
                    }
                }
            }
        }
        List<ContactsInfo> localContactsInfos = mContactsLogic.getCacheLocalContactLst();
        for (ContactsInfo contactsInfo : localContactsInfos) {
            if(isMatch)
            {
                break;
            }
            if (contactsInfo.getNumberLst() != null) {
                for (NumberInfo numberInfo : contactsInfo.getNumberLst()) {
                    if (CommonUtils.getPhoneNumber(phoneNumber).equals(numberInfo.getNumberNoCountryCode())) {
                        return contactsInfo;
                    }
                }
            }
        }

        return null;
    }

    private void outingCall() {
        mCallSession = mVoipLogic.call(mCalleeNumber, true);
        if (mCallSession.getErrCode() != CallSession.ERRCODE_OK) {
            showToast(R.string.call_outing_error, Toast.LENGTH_SHORT, null);
            closed = true;
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        } else {
            if (mute) {
                mCallSession.mute();
            }
            createLocalPreviewVideo();
        }
    }

    private void createLocalPreviewVideo() {
        OrientationEventListener listener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    orientationChanged(orientation);
                }
            }
        };
        if (listener.canDetectOrientation()) {
            listener.enable();
        } else {
            LogUtil.e("V2OIP", "OrientationEventListener enable failed!!");
        }
        setCameraRotate();
        if (localVideoView == null) {
            localVideoView = CallApi.createLocalVideoView(getApplicationContext());
            localVideoView.setZOrderOnTop(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mLargeVideoLayout.addView(localVideoView, layoutParams);
            mCallSession.prepareVideo();
        }
        registerReceivers();
    }

    private void showTalking() {
        mIsTalking =true;
        AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        speakerState = audioManamger.isSpeakerphoneOn();
        if (!speakerState) {
            audioManamger.setSpeakerphoneOn(!speakerState);
        }

        if (mIsInComing) {
            OrientationEventListener listener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                        orientationChanged(orientation);
                    }
                }
            };
            if (listener.canDetectOrientation()) {
                listener.enable();
            } else {
                LogUtil.e("V2OIP", "OrientationEventListener enable failed!!");
            }
        }
        if (mIsInComing) {
            mIncomingLayout.setVisibility(View.GONE);
            mCallStatusLayout.setVisibility(View.GONE);
            mTopLayout.setVisibility(View.VISIBLE);
            mVideoLayout.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
        } else {
            mCallStatusLayout.setVisibility(View.GONE);
        }
        /* In order to properly display video, Camera rotate should be set. */
        setCameraRotate();
        /* creat local video view and remote video view. */
        createVideoView();
        if (mIsInComing) {
            registerReceivers();
        }
        mCallSession.showVideoWindow();
        startCallTimeTask();
    }

    private void setCameraRotate() {
        Display display = this.getWindowManager().getDefaultDisplay();
        int displayRotation = display.getRotation();
        int cameraRotate = getCameraOrientation(displayRotation);
        CallApi.setCameraRotate(cameraRotate);
    }

    private void createVideoView() {

        if (remoteVideoView == null) {
            remoteVideoView = CallApi.createRemoteVideoView(getApplicationContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            remoteVideoView.setVisibility(View.GONE);
            mLargeVideoLayout.addView(remoteVideoView, layoutParams);
            remoteVideoView.setZOrderOnTop(false);
        }

        if (localVideoView == null) {
            localVideoView = CallApi.createLocalVideoView(getApplicationContext());
            localVideoView.setZOrderOnTop(false);
            localVideoView.setVisibility(View.GONE);
        } else {
            mLargeVideoLayout.removeView(localVideoView);
            localVideoView.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSmallVideoLayout.addView(localVideoView, layoutParams);
        localVideoView.setVisibility(View.VISIBLE);
        mCallSession.showVideoWindow();

    }

    private void orientationChanged(int orientation) {
        int cameraRotate = 270;
        int displayRotation = Surface.ROTATION_0;
        if (Math.abs(orientation - lastOrientation) < ORIENTATION_SENSITIVITY
                || Math.abs(orientation - lastOrientation - 360) < ORIENTATION_SENSITIVITY) {
            return;
        }

        lastOrientation = orientation;

        if (orientation < ORIENTATION_SENSITIVITY
                || 360 - orientation < ORIENTATION_SENSITIVITY) {
            displayRotation = Surface.ROTATION_0;
        } else if (Math.abs(orientation - 90) <= ORIENTATION_SENSITIVITY) {
            displayRotation = Surface.ROTATION_90;
        } else if (Math.abs(orientation - 180) <= ORIENTATION_SENSITIVITY) {
            displayRotation = Surface.ROTATION_180;
        } else if (Math.abs(orientation - 270) <= ORIENTATION_SENSITIVITY) {
            displayRotation = Surface.ROTATION_270;
        } else {
            LogUtil.e("V2OIP", "orientationChanged get wrong orientation:" + orientation + ", getCameraOrientation with default displayRotation " + Surface.ROTATION_0);
            lastDisplayRotation = Surface.ROTATION_0;
        }

        if (lastDisplayRotation == displayRotation) {
            return;
        }

        lastDisplayRotation = displayRotation;
        cameraRotate = getCameraOrientation(lastDisplayRotation);
        CallApi.setCameraRotate(cameraRotate);
    }

    /**
     * get orientation to set camera orientation in this situation
     *
     * @param displayRotation get the displayRotation by Gravity sensor(onOrientationChanged event).
     */
    private int getCameraOrientation(int displayRotation) {

        boolean isFrontCamera = true;
        int cameraOrientation = 0;
        int degrees = 0;
        int result = 0;

        if (CallApi.getCameraCount() < 2) {
            isFrontCamera = false;
            LogUtil.d("V2OIP", "getCameraOrientation getCameraCount " + CallApi.getCameraCount());
        } else {
            if (CallApi.getCamera() == CallApi.CAMERA_TYPE_FRONT) {
                isFrontCamera = true;
            } else {
                isFrontCamera = false;
            }
        }

        cameraOrientation = CallApi.getCameraRotate();

        switch (displayRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 90;
                break;
            default:
                LogUtil.e("V2OIP", "getCameraOrientation wrong displayRotation " + displayRotation);
                break;
        }

        CallApi.setVideoRenderRotate(degrees);

        if (isFrontCamera) {
            result = (cameraOrientation + degrees) % 360;
        } else {
            result = (cameraOrientation - degrees + 360) % 360;
        }

        return result;
    }

    private void stopCallTimeTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startCallTimeTask() {
        timer = new Timer();
        callTime = (int) ((System.currentTimeMillis() - mCallSession.getOccurDate()) / 1000);
        mTalkingTimeTv.setText(SysApi.PhoneUtils.getCallDurationTime(callTime));
        mTalkingTimeTv.setVisibility(View.VISIBLE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callTime++;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTalkingTimeTv.setText(SysApi.PhoneUtils.getCallDurationTime(callTime));
                    }
                });
            }
        }, 1000, 1000);
    }

    private void registerReceivers() {

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                remoteVideoStreamArrivedReceiver,
                new IntentFilter(CallApi.EVENT_CALL_VIDEO_STREAM_ARRIVED));


        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                cameraSwitchedReceiver,
                new IntentFilter(CallApi.EVENT_CALL_CAMERA_SWITCHED));


    }

    private void unRegisterReceivers() {

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(remoteVideoStreamArrivedReceiver);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(cameraSwitchedReceiver);

    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onClick(View v) {
        if (closed) {
            LogUtil.w(TAG, "is closed");
        }
        switch (v.getId()) {
            case R.id.hangup_layout:
                //呼出和接通后的挂断
                mVoipLogic.hangup(mCallSession, mIsInComing, mIsTalking,callTime);
                finish();
                break;
            case R.id.coming_answer_call_btn:
                mVoipLogic.answerVideo(mCallSession);
                break;
            case R.id.coming_reject_call_btn:
                mVoipLogic.hangup(mCallSession, mIsInComing, mIsTalking,callTime);
                finish();
                break;
            case R.id.mute_layout:
                if (mute) {
                    mute = false;
                    if (mCallSession != null) {
                        mMuteIv.setImageResource(R.mipmap.mute_off);
                        mCallSession.unMute();
                    }
                } else {
                    mute = true;
                    if (mCallSession != null) {
                        mMuteIv.setImageResource(R.mipmap.mute_on);
                        mCallSession.mute();
                    }
                }
                break;
            case R.id.camera_layout:
                if (CallApi.getCameraCount() < 2) {
                    return;
                }
                CallApi.switchCamera();
                if (localVideoView != null) {
                    localVideoView.setVisibility(View.GONE);
                }
                LogUtil.d("V2OIP", "onClick_CameraSwitch displayRotation" + lastDisplayRotation);
                int cameraRotate = getCameraOrientation(lastDisplayRotation);
                CallApi.setCameraRotate(cameraRotate);
                break;
        }

    }

    @Override
    protected void handleStateMessage(Message msg) {
        super.handleStateMessage(msg);
        switch (msg.what) {
            case BussinessConstants.DialMsgID.CALL_ON_TALKING_MSG_ID:
                showTalking();
                break;
            case BussinessConstants.DialMsgID.CALL_CLOSED_MSG_ID:
                if (msg.obj != null) {
                    CallSession session = (CallSession) msg.obj;
                    if (mCallSession != null && mCallSession.equals(session)) {
                        mVoipLogic.dealOnClosed(mCallSession, mIsInComing, mIsTalking, callTime);
                        closed = true;
                        if (mCallSession.getSipCause() == BussinessConstants.DictInfo.SIP_TEMPORARILY_UNAVAILABLE) {
                            showToast(R.string.sip_temporarily_unavailable, Toast.LENGTH_SHORT, null);
                        } else if (!mIsInComing && !mIsTalking && (mCallSession.getSipCause() == BussinessConstants.DictInfo.SIP_BUSY_HERE
                                || mCallSession.getSipCause() == BussinessConstants.DictInfo.SIP_DECLINE)) {
                            showToast(R.string.sip_busy_here, Toast.LENGTH_SHORT, null);
                        }
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    } else if (session != null && session.getType() == CallSession.TYPE_VIDEO_INCOMING) {
                        mVoipLogic.dealOnClosed(session, true, false,0);
                    }
                }
                break;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsTalking) {
            return;
        }
        if ((null != localVideoView || null != remoteVideoView)
                && CallSession.INVALID_ID != mCallSession.getSessionId()) {
            if (hasStoped) {
                mCallSession.showVideoWindow();
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if (remoteVideoView != null) {
                    mLargeVideoLayout.addView(remoteVideoView, layoutParams);
                }
                if (localVideoView != null) {
                    mSmallVideoLayout.addView(localVideoView, layoutParams);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mIsTalking) {
            return;
        }
        if ((null != localVideoView || null != remoteVideoView)
                && CallSession.INVALID_ID != mCallSession.getSessionId()) {
            mCallSession.hideVideoWindow();
            mLargeVideoLayout.removeAllViews();
            mSmallVideoLayout.removeAllViews();
            hasStoped = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //还原免提设置
        if (!speakerState) {
            AudioManager audioManamger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManamger.setSpeakerphoneOn(speakerState);
        }
        destroyVideoView();
        stopCallTimeTask();
        if (mIsTalking) {
            unRegisterReceivers();
        }
    }

    private void destroyVideoView() {

        if (localVideoView != null) {
            localVideoView.setVisibility(View.GONE);
            mLargeVideoLayout.removeView(localVideoView);
            mSmallVideoLayout.removeView(localVideoView);
            CallApi.deleteRemoteVideoView(localVideoView);
            localVideoView = null;
        }

        if (remoteVideoView != null) {
            remoteVideoView.setVisibility(View.GONE);
            mLargeVideoLayout.removeView(remoteVideoView);
            CallApi.deleteRemoteVideoView(remoteVideoView);
            remoteVideoView = null;
        }
    }

}