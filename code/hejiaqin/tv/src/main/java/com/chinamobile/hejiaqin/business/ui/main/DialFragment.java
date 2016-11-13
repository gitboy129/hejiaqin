package com.chinamobile.hejiaqin.business.ui.main;


import android.content.Intent;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.logic.contacts.IContactsLogic;
import com.chinamobile.hejiaqin.business.logic.voip.IVoipLogic;
import com.chinamobile.hejiaqin.business.ui.basic.BasicFragment;
import com.chinamobile.hejiaqin.business.ui.basic.FocusManager;
import com.chinamobile.hejiaqin.business.ui.basic.FragmentMgr;
import com.chinamobile.hejiaqin.business.ui.basic.dialog.VideoOutDialog;
import com.chinamobile.hejiaqin.business.ui.basic.view.HeaderView;
import com.chinamobile.hejiaqin.business.ui.basic.view.keypad.BaseDigitKeypadView;
import com.chinamobile.hejiaqin.business.ui.basic.view.keypad.DialDigitKeypadView;
import com.chinamobile.hejiaqin.business.ui.basic.view.keypad.DigitsEditText;
import com.chinamobile.hejiaqin.business.ui.contact.fragment.ContactEditFragment;
import com.chinamobile.hejiaqin.tv.R;

/**
 * Created by eshaohu on 16/5/22.
 */
public class DialFragment extends BasicFragment implements View.OnClickListener{

    private static final String TAG = "DialFragment";


    HeaderView headerView;


    LinearLayout dialSaveContactArrowLayout;

    DigitsEditText inputNumber;
    LinearLayout dialNumberDelLayout;
    ImageView dialNumberDelIv;
    DialDigitKeypadView digitKeypad;
    LinearLayout dialVideoLayout;

    private IVoipLogic mVoipLogic;
    private IContactsLogic mContactsLogic;

    @Override
    protected void handleFragmentMsg(Message msg) {

    }

    @Override
    protected void handleLogicMsg(Message msg) {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_dail;
    }

    @Override
    protected void initView(View view) {
        headerView = (HeaderView) view.findViewById(R.id.header_view_id);
        headerView.title.setText(R.string.dial_title);

        dialSaveContactArrowLayout = (LinearLayout)view.findViewById(R.id.dial_save_contact_arrow_layout);
        inputNumber = (DigitsEditText)view.findViewById(R.id.input_number);
        dialNumberDelLayout = (LinearLayout)view.findViewById(R.id.dial_number_del_layout);
        dialNumberDelIv = (ImageView)view.findViewById(R.id.dial_number_del_iv);
        digitKeypad = (DialDigitKeypadView)view.findViewById(R.id.digit_keypad);
        dialVideoLayout =(LinearLayout)view.findViewById(R.id.dial_video_layout);
        dialSaveContactArrowLayout.setOnClickListener(this);
        dialVideoLayout.setOnClickListener(this);
        digitKeypad.setDigitKeyPressEvent(new BaseDigitKeypadView.DigitKeyPressEvent() {

            @Override
            public void onKeyPressed(int keyCode, String keyStr) {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                inputNumber.onKeyDown(keyCode, event);
            }
        });
        inputNumber.setCursorVisible(false);
        inputNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (inputNumber.length() > 0) {
                    inputNumber.setCursorVisible(true);
                }
                return false;
            }
        });
        inputNumber.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (inputNumber.length() > 0) {
                    inputNumber.setCursorVisible(true);
                }
                return false;
            }
        });
        inputNumber.setOnClickListener(this);
        dialNumberDelIv.setClickable(false);
        dialNumberDelLayout.setOnClickListener(this);
        dialNumberDelLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 长按删除键, 清空输入内容
                inputNumber.setText("");
                inputNumber.setCursorVisible(false);
                return true;
            }
        });
        FocusManager.getInstance().addFocusViewInLeftFrag("2", digitKeypad.btnOne);
    }

    @Override
    protected void initLogics() {
        mVoipLogic = (IVoipLogic) super.getLogicByInterfaceClass(IVoipLogic.class);
        mContactsLogic = (IContactsLogic) super.getLogicByInterfaceClass(IContactsLogic.class);
    }

    @Override
    protected void initData() {

    }
    
    @Override
    public void onClick(View v) {
       switch (v.getId())
       {
           case R.id.input_number:
               if (inputNumber.length() > 0)
               {
                   inputNumber.setCursorVisible(true);
               }
               break;
           case R.id.dial_number_del_layout:
               if (inputNumber.length() > 0)
               {
                   inputNumber.setCursorVisible(true);
               }
               inputNumber.onKeyDown(KeyEvent.KEYCODE_DEL, new KeyEvent(
                       KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));

               //如果是输入框中无数字，将光标隐藏
               final int length = inputNumber.length();
               if (length == inputNumber.getSelectionStart()
                       && length == inputNumber.getSelectionEnd())
               {
                   inputNumber.setCursorVisible(false);
               }
               break;
           case R.id.dial_save_contact_arrow_layout:
               // 保存联系人
               if(inputNumber.length()>0) {
                   ContactEditFragment fragment = ContactEditFragment.newInstance(inputNumber.getText().toString());
                   FragmentMgr.getInstance().showDialFragment(fragment);
               }
               break;
           case R.id.dial_video_layout:
               if(inputNumber.length()>0) {
                   VideoOutDialog.show(getActivity(),inputNumber.getText().toString(),mVoipLogic,mContactsLogic);
               }
               break;
       }
    }

}
