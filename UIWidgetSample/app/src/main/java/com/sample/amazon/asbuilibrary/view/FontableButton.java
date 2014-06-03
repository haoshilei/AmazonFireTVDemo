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

package com.sample.amazon.asbuilibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.util.FontUtils;

public class FontableButton extends Button
{
    public FontableButton(Context context)
    {
        super(context);
    }

    public FontableButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setTypefaceFromAttr(attrs);
    }

    public FontableButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setTypefaceFromAttr(attrs);
    }

    private void setTypefaceFromAttr(AttributeSet attrs)
    {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.FontableButton);
        String font = styledAttributes.getString(R.styleable.FontableButton_font);
        styledAttributes.recycle();

        if (!isInEditMode())
        {
            if (font != null)
            {
                FontUtils.setFont(this, font);
            } else {
                Log.w(FontableTextView.TAG, "Fontable without font " + this.toString());
                FontUtils.setFont(this, FontableTextView.DEFAULT_FONT);
            }
        }
    }
}
