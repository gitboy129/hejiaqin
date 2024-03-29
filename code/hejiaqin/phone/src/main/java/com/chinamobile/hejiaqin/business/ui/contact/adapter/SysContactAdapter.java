package com.chinamobile.hejiaqin.business.ui.contact.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chinamobile.hejiaqin.R;
import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.model.contacts.ContactsInfo;
import com.chinamobile.hejiaqin.business.ui.basic.view.stickylistview.StickyListHeadersAdapter;
import com.chinamobile.hejiaqin.business.ui.contact.ContactInfoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yupeng on 5/23/16.
 */
public class SysContactAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context mContext;
    private LayoutInflater inflater;

    private List<ContactsInfo> contactsInfoList = new ArrayList<ContactsInfo>();

    private SparseArray<String> positionToLetter = new SparseArray<>();

    private Map<String, Integer> letterToPosition = new HashMap<>();

    public SysContactAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return contactsInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactsInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.adapter_contact_app_view, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.contact_name_text);
            holder.convertView = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        initView(position, holder);

        return convertView;
    }

    private void initView(int position, ViewHolder holder) {
        final ContactsInfo contactsInfo = contactsInfoList.get(position);
        holder.text.setText(contactsInfo.getName());

        holder.convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ContactInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BussinessConstants.Contact.INTENT_CONTACTSINFO_KEY,
                        contactsInfo);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.adapter_contact_head_view, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.contact_head_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        ContactsInfo contactsInfo = contactsInfoList.get(position);
        String headerText = contactsInfo.getGroupName();
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        //        ContactsInfo contactsInfo = contactsInfoList.get(position);
        //        return contactsInfo.getGroupName().charAt(0);

        String groupLetter = this.positionToLetter.get(position);
        return null == groupLetter ? -1 : groupLetter.charAt(0);
    }

    public int getPositionByLetter(String letter) {
        Integer position = this.letterToPosition.get(letter);
        return null == position ? -1 : position;
    }

    public String[] getGroupLetters() {
        return letterToPosition.keySet().toArray(new String[letterToPosition.size()]);
    }

    public void setData(List<ContactsInfo> contactsInfoList) {
        this.contactsInfoList.clear();
        this.positionToLetter.clear();
        this.letterToPosition.clear();
        if (null != contactsInfoList) {
            int index = 0;
            for (ContactsInfo contactsInfo : contactsInfoList) {
                String groupLetter = contactsInfo.getGroupName();
                this.positionToLetter.put(index, groupLetter);
                if (!letterToPosition.containsKey(groupLetter)) {
                    this.letterToPosition.put(groupLetter, index + 1);
                }
                this.contactsInfoList.add(contactsInfo);
                index++;
            }
        }

        notifyDataSetChanged();
    }

    static class HeaderViewHolder {
        TextView text;
    }

    static class ViewHolder {
        TextView text;
        View convertView;
    }

}
