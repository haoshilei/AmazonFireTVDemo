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

package com.sample.tom.asbuilibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.util.FontUtils;

public class FontableTextView extends TextView
{
    protected static final String TAG="fontable";
    protected static final String DEFAULT_FONT = "HelveticaNeue";
    public FontableTextView(Context context)
    {
        super(context);
    }

    public FontableTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setTypefaceFromAttr(attrs);
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setTypefaceFromAttr(attrs);
    }

    private void setTypefaceFromAttr(AttributeSet attrs)
    {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.FontableTextView);
        String font = styledAttributes.getString(R.styleable.FontableTextView_font);
        styledAttributes.recycle();

        setTypeface(font);
    }

    public void setTypeface(String font)
    {
        if (!isInEditMode())
        {
            if (font != null)
            {
                FontUtils.setFont(this, font);
            } else {
                Log.w(TAG, "Fontable without font "+this.toString());
                FontUtils.setFont(this, DEFAULT_FONT);
            }
        }
    }

}
