package com.chinamobile.hejiaqin.business.ui.contact;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.chinamobile.hejiaqin.R;
import com.chinamobile.hejiaqin.business.ui.basic.BasicActivity;
import com.chinamobile.hejiaqin.business.ui.basic.view.HeaderView;

/***/
public class InputInfoActivity extends BasicActivity implements View.OnClickListener {
    private HeaderView titleLayout;

    private EditText input;

    private View cancel;

    private String inputText;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_info;
    }

    @Override
    protected void initView() {
        // title
        titleLayout = (HeaderView) findViewById(R.id.title);

        titleLayout.rightBtn.setImageResource(R.mipmap.title_icon_check_nor);
        titleLayout.backImageView.setImageResource(R.mipmap.title_icon_back_nor);

        input = (EditText) findViewById(R.id.input);
        cancel = findViewById(R.id.delete);
    }

    @Override
    protected void initDate() {
        int requestCode = getRequestCode();
        if (requestCode == ModifyContactActivity.REQUEST_CODE_INPUT_NAME) {
            titleLayout.title.setText(R.string.contact_modify_name_text);
            input.setHint(R.string.contact_input_name_hint_text);
            input.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        } else {
            titleLayout.title.setText(R.string.contact_modify_number_text);
            input.setHint(R.string.contact_input_number_hint_text);
            input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        }
        // String editData = getIntent().getStringExtra(ModifyContactActivity.INTENT_DATA_EDIT_INFO);
    }

    @Override
    protected void initListener() {
        titleLayout.rightBtn.setOnClickListener(this);
        titleLayout.backImageView.setOnClickListener(this);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                cancel.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                inputText = s.toString();
            }
        });

        cancel.setOnClickListener(this);
    }

    /**
     * 初始化logic的方法，由子类实现<BR>
     * 在该方法里通过getLogicByInterfaceClass获取logic对象
     */
    @Override
    protected void initLogics() {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn:
                doClickSubmit();
                break;
            case R.id.back_iv:
                doBack();
                break;
            case R.id.delete:
                doClickDelete();
                break;
            default:
                break;
        }
    }

    private void doClickSubmit() {
        Intent intent = new Intent();
        intent.putExtra(ModifyContactActivity.INTENT_DATA_INPUT_INFO, inputText);
        setResult(getRequestCode(), intent);
        this.finish();
    }

    private void doClickDelete() {
        input.setText("");
    }

}
