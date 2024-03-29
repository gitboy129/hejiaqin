package com.chinamobile.hejiaqin.business.ui.contact.fragment;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.hejiaqin.R;
import com.chinamobile.hejiaqin.business.model.contacts.ContactsInfo;
import com.chinamobile.hejiaqin.business.model.contacts.NumberInfo;
import com.chinamobile.hejiaqin.business.ui.basic.BasicFragment;

import java.util.List;

/***/
public class ContactInfoFragment extends BasicFragment implements View.OnClickListener {
    private static final String TAG = "ContactInfoFragment";

    private LayoutInflater inflater;

    private LinearLayout contactInfoLayout;

    private ContactsInfo mContactsInfo;

    @Override
    protected void handleFragmentMsg(Message msg) {

    }

    @Override
    protected void handleLogicMsg(Message msg) {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_contact_info;
    }

    @Override
    protected void initView(View view) {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contactInfoLayout = (LinearLayout) view.findViewById(R.id.contact_info_layout);
    }

    @Override
    protected void initData() {
        refreshView();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
    }

    public void setContactsInfo(ContactsInfo contactsInfo) {
        mContactsInfo = contactsInfo;
    }

    /***/
    public void refreshView() {
        if (null == mContactsInfo) {
            return;
        }

        List<NumberInfo> numberInfoList = mContactsInfo.getNumberLst();
        if (null == numberInfoList) {
            return;
        }

        contactInfoLayout.removeAllViews();
        for (NumberInfo numberInfo : numberInfoList) {
            View contactNumberView = inflater.inflate(R.layout.layout_contact_number_info_view,
                    null);
            TextView numberDesc = (TextView) contactNumberView.findViewById(R.id.number_desc_text);
            numberDesc.setText(numberInfo.getDesc());
            TextView number = (TextView) contactNumberView.findViewById(R.id.number_text);
            number.setText(numberInfo.getNumber());
            contactInfoLayout.addView(contactNumberView);
        }
    }

}
