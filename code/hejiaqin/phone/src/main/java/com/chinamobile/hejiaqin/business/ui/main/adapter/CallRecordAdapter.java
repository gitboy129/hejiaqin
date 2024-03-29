package com.chinamobile.hejiaqin.business.ui.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.hejiaqin.R;
import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.logic.contacts.IContactsLogic;
import com.chinamobile.hejiaqin.business.model.contacts.ContactsInfo;
import com.chinamobile.hejiaqin.business.model.contacts.NumberInfo;
import com.chinamobile.hejiaqin.business.model.dial.CallRecord;
import com.chinamobile.hejiaqin.business.ui.contact.ContactInfoActivity;
import com.chinamobile.hejiaqin.business.ui.dial.DialHelper;
import com.customer.framework.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2016/7/20.
 */
public class CallRecordAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private IContactsLogic mContactsLogic;
    private List<CallRecord> mData;

    public CallRecordAdapter(Context context, IContactsLogic contactsLogic) {
        this.mContext = context;
        this.mContactsLogic = contactsLogic;
        mData = new ArrayList<CallRecord>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holer = null;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_call_record, parent, false);
        holer = new HolderView(view);
        return holer;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CallRecord info = mData.get(position);
        if (info != null) {
            HolderView tHolder = (HolderView) holder;
            tHolder.callRecordTypeLayout.setTag(position);
            tHolder.callRecordContentLayout.setTag(position);
            tHolder.callRecordTimeLayout.setTag(position);
            tHolder.callRecordArrowLayout.setTag(position);
            tHolder.callRecordTypeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callVideo(v);
                }
            });
            tHolder.callRecordContentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callVideo(v);
                }
            });
            tHolder.callRecordTimeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetail(v);
                }
            });
            tHolder.callRecordArrowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetail(v);
                }
            });
            if (info.getType() == CallRecord.TYPE_VIDEO_INCOMING) {
                tHolder.callRecordTypeIv.setImageResource(R.mipmap.icon_incoming);
            } else if (info.getType() == CallRecord.TYPE_VIDEO_MISSING) {
                tHolder.callRecordTypeIv.setImageResource(R.mipmap.icon_missed_call);
            } else if (info.getType() == CallRecord.TYPE_VIDEO_REJECT) {
                tHolder.callRecordTypeIv.setImageResource(R.mipmap.icon_reject_call);
            } else {
                tHolder.callRecordTypeIv.setImageResource(R.mipmap.icon_outbound_call);
            }
            if (info.getContactsInfo() == null) {
                //遍历本地联系人
                boolean isMatch = false;
                List<ContactsInfo> localcontactsInfos = mContactsLogic.getCacheLocalContactLst();
                for (ContactsInfo contactsInfo : localcontactsInfos) {
                    if (isMatch) {
                        break;
                    }
                    if (contactsInfo.getNumberLst() != null) {
                        for (NumberInfo numberInfo : contactsInfo.getNumberLst()) {
                            if (info.getNoCountryNumber().equals(
                                    numberInfo.getNumberNoCountryCode())) {
                                info.setPeerName(contactsInfo.getName());
                                info.setContactsInfo(contactsInfo);
                                isMatch = true;
                            }
                        }
                    }
                }
            }
            if (info.getRecordSearchUnit() != null) {
                tHolder.callRecordNameTv.setText(Html.fromHtml(info.getRecordSearchUnit()
                        .getNumberText()));
                tHolder.callRecordNumberTv.setText("");
            } else {
                tHolder.callRecordNameTv
                        .setText(StringUtil.isNullOrEmpty(info.getPeerName()) ? info
                                .getPeerNumber() : info.getPeerName());
                tHolder.callRecordNumberTv
                        .setText(StringUtil.isNullOrEmpty(info.getPeerName()) ? "" : info
                                .getPeerNumber());
            }
            tHolder.callRecordTimeTv.setText(info.getBeginTimeformatter());
        }
    }

    private void callVideo(View v) {
        int position = (int) v.getTag();
        if (mData.get(position).getContactsInfo() != null) {
            //            Intent outingIntent = new Intent(mContext, VideoCallActivity.class);
            //            outingIntent.putExtra(BussinessConstants.Dial.INTENT_CALLEE_NUMBER, mData.get(position).getPeerNumber());
            //            outingIntent.putExtra(BussinessConstants.Dial.INTENT_CALLEE_NAME, mData.get(position).getContactsInfo().getName());
            //            mContext.startActivity(outingIntent);
            DialHelper.getInstance().call(mContext, mData.get(position).getPeerNumber(),
                    mData.get(position).getContactsInfo().getName());
        } else {
            //            Intent outingIntent = new Intent(mContext, VideoCallActivity.class);
            //            outingIntent.putExtra(BussinessConstants.Dial.INTENT_CALLEE_NUMBER, mData.get(position).getPeerNumber());
            //            mContext.startActivity(outingIntent);
            DialHelper.getInstance().call(mContext, mData.get(position).getPeerNumber(), null);
        }
    }

    private void showDetail(View v) {
        int position = (int) v.getTag();
        if (mData.get(position).getContactsInfo() != null) {
            Intent intent = new Intent(mContext, ContactInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(BussinessConstants.Contact.INTENT_CONTACTSINFO_KEY,
                    mData.get(position).getContactsInfo());
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        } else {
            Intent intent = new Intent(mContext, ContactInfoActivity.class);
            intent.putExtra(BussinessConstants.Contact.INTENT_CONTACT_NUMBER_KEY,
                    mData.get(position).getPeerNumber());
            mContext.startActivity(intent);
        }
    }

    /***/
    public void refreshData(List<CallRecord> data) {
        if (data != null) {
            mData = data;
        } else {
            mData.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    /***/
    public class HolderView extends RecyclerView.ViewHolder {

        private ImageView callRecordTypeIv;
        private TextView callRecordNameTv;
        private TextView callRecordNumberTv;
        private TextView callRecordTimeTv;
        private LinearLayout callRecordTypeLayout;
        private LinearLayout callRecordContentLayout;
        private LinearLayout callRecordTimeLayout;
        private LinearLayout callRecordArrowLayout;

        public HolderView(View view) {
            super(view);
            callRecordTypeIv = (ImageView) view.findViewById(R.id.call_record_type_iv);
            callRecordNameTv = (TextView) view.findViewById(R.id.call_record_name_tv);
            callRecordNumberTv = (TextView) view.findViewById(R.id.call_record_number_tv);
            callRecordTimeTv = (TextView) view.findViewById(R.id.call_record_time_tv);
            callRecordTypeLayout = (LinearLayout) view.findViewById(R.id.call_record_type_layout);
            ;
            callRecordContentLayout = (LinearLayout) view
                    .findViewById(R.id.call_record_content_layout);
            ;
            callRecordTimeLayout = (LinearLayout) view.findViewById(R.id.call_record_time_layout);
            callRecordArrowLayout = (LinearLayout) view.findViewById(R.id.call_record_arrow_layout);
        }
    }
}
