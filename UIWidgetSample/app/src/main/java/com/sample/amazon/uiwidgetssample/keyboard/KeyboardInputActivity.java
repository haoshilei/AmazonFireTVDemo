/**
 * Amazon Fire TV Development Resources
 *
 * Copyright 2004-2014 Amazon.com, Inc. or its affiliates.  All Rights Reserved.
 
 * These materials are licensed as "Program Materials" under the Program Materials 
 * License Agreement (the "License") of the Amazon Mobile App Distribution program, 
 * which is available at https://developer.amazon.com/sdk/pml.html.  See the License 
 * for the specific language governing permissions and limitations under the License.
 *
 * These materials are distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.sample.amazon.uiwidgetssample.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sample.amazon.uiwidgetssample.R;

@SuppressLint("DefaultLocale")
public class KeyboardInputActivity extends Activity implements OnEditorActionListener, OnClickListener
{
    private TextView mNameLabel;
    private TextView mEmailLabel;
    private TextView mPhoneLabel;
    private TextView mPasswordLabel;
    private TextView mPINLabel;

    private View mFieldsGroup;
    private EditText mNameField;
    private EditText mEmailField;
    private TextView mPhoneField;
    private EditText mPasswordField;
    private EditText mPINField;

    private Button mEditButton;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.keyboard_activity);

        mFieldsGroup = findViewById(R.id.input_fields_group);
        mNameLabel = (TextView) findViewById(R.id.name_field_label);
        mEmailLabel = (TextView) findViewById(R.id.email_field_label);
        mPhoneLabel = (TextView) findViewById(R.id.phone_field_label);
        mPasswordLabel = (TextView) findViewById(R.id.password_field_label);
        mPINLabel = (TextView) findViewById(R.id.pin_field_label);

        mNameField = (EditText) findViewById(R.id.name_field_input);
        mNameField.setOnEditorActionListener(this);
        Bundle extras = mNameField.getInputExtras(true);
        if (extras != null)
        {
            extras.putString(InputExtras.KEY_TYPE, InputExtras.KEY_TYPE_WIFI);
            extras.putString(InputExtras.KEY_LABEL, getString(R.string.kbsample_field_name_label));
            extras.putString(InputExtras.KEY_DESCRIPTION, getString(R.string.kbsample_field_name_description));
            extras.putString(InputExtras.KEY_HINT, getString(R.string.kbsample_field_name_hint));
            extras.putString(InputExtras.KEY_BACK_LABEL, getString(R.string.kbsample_field_name_backLabel).toUpperCase());
            extras.putInt(InputExtras.KEY_FIELD_NUMBER, 1);
            extras.putInt(InputExtras.KEY_FIELD_COUNT, 5);
        }

        mEmailField = (EditText) findViewById(R.id.email_field_input);
        extras = mEmailField.getInputExtras(true);
        if (extras != null)
        {
            extras.putString(InputExtras.KEY_TYPE, InputExtras.KEY_TYPE_EMAIL);
            extras.putString(InputExtras.KEY_LABEL, getString(R.string.kbsample_field_email_label));
            extras.putString(InputExtras.KEY_DESCRIPTION, getString(R.string.kbsample_field_email_description));
            extras.putString(InputExtras.KEY_HINT, getString(R.string.kbsample_field_email_hint));
            extras.putInt(InputExtras.KEY_FIELD_NUMBER, 2);
            extras.putInt(InputExtras.KEY_FIELD_COUNT, 5);
        }

        mPhoneField = (TextView) findViewById(R.id.phone_field_input);
        mPhoneField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        extras = mPhoneField.getInputExtras(true);
        if (extras != null)
        {
            extras.putString(InputExtras.KEY_TYPE, InputExtras.KEY_TYPE_CREDIT_CARD);
            extras.putString(InputExtras.KEY_LABEL, getString(R.string.kbsample_field_phone_label));
            extras.putString(InputExtras.KEY_DESCRIPTION, getString(R.string.kbsample_field_phone_description));
            extras.putString(InputExtras.KEY_HINT, getString(R.string.kbsample_field_phone_hint));
            extras.putInt(InputExtras.KEY_FIELD_NUMBER, 3);
            extras.putInt(InputExtras.KEY_FIELD_COUNT, 5);
        }

        mPasswordField = (EditText) findViewById(R.id.password_field_input);
        extras = mPasswordField.getInputExtras(true);
        if (extras != null)
        {
            extras.putString(InputExtras.KEY_LABEL, getString(R.string.kbsample_field_password_label));
            extras.putString(InputExtras.KEY_DESCRIPTION, getString(R.string.kbsample_field_password_description));
            extras.putString(InputExtras.KEY_HINT, getString(R.string.kbsample_field_password_hint));
            extras.putInt(InputExtras.KEY_FIELD_NUMBER, 4);
            extras.putInt(InputExtras.KEY_FIELD_COUNT, 5);
        }

        mPINField = (EditText) findViewById(R.id.pin_field_input);
        mPINField.setOnEditorActionListener(this);
        extras = mPINField.getInputExtras(true);
        if (extras != null)
        {
            extras.putString(InputExtras.KEY_LABEL, getString(R.string.kbsample_field_pin_label));
            extras.putString(InputExtras.KEY_DESCRIPTION, getString(R.string.kbsample_field_pin_description));
            extras.putString(InputExtras.KEY_HINT, getString(R.string.kbsample_field_pin_hint));
            extras.putString(InputExtras.KEY_NEXT_LABEL, getString(R.string.kbsample_field_pin_nextLabel).toUpperCase());
            extras.putInt(InputExtras.KEY_FIELD_NUMBER, 5);
            extras.putInt(InputExtras.KEY_FIELD_COUNT, 5);
        }

        mEditButton = (Button) findViewById(R.id.edit_button);
        mEditButton.setOnClickListener(this);
    }

    private void initializeInputFields()
    {
        mNameField.setText(mNameLabel.getText());
        mEmailField.setText(mEmailLabel.getText());
        mPhoneField.setText(mPhoneLabel.getText());
        mPasswordField.setText(mPasswordLabel.getText());
        mPINField.setText(mPINLabel.getText());
    }

    private void saveInputFields()
    {
        mNameLabel.setText(mNameField.getText().toString());
        mEmailLabel.setText(mEmailField.getText().toString());
        mPhoneLabel.setText(mPhoneField.getText().toString());
        mPasswordLabel.setText(mPasswordField.getText().toString());
        mPINLabel.setText(mPINField.getText().toString());
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event)
    {
        if (textView == mNameField && actionId == EditorInfo.IME_ACTION_PREVIOUS)
        {
            mFieldsGroup.setVisibility(View.GONE);
            mEditButton.requestFocus();
            return true;
        }
        else if (textView == mPINField && actionId != EditorInfo.IME_ACTION_PREVIOUS)
        {
            saveInputFields();
            mFieldsGroup.setVisibility(View.GONE);
            mEditButton.requestFocus();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v)
    {
        initializeInputFields();
        mFieldsGroup.setVisibility(View.VISIBLE);
        mNameField.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mNameField, 0);
    }
}

