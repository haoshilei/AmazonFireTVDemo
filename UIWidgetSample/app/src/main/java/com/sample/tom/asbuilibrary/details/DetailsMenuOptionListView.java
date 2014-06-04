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

package com.sample.tom.asbuilibrary.details;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sample.tom.uiwidgetssample.R;

/**
 * This is the {@link ListView} for the options on the left side of the details page.
 */
class DetailsMenuOptionListView extends ListView
{
    private static ColorStateList defaultColors = null;
    private static ColorStateList selectedColors = null;
    private TextView mPreviouslySelectedView = null;

    public DetailsMenuOptionListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if (defaultColors == null)
        {
            defaultColors = context.getResources().getColorStateList(R.color.details_option_text_color);
        }

        if (selectedColors == null)
        {
            selectedColors = context.getResources().getColorStateList(R.color.details_option_text_color_selected);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
    {
        // We pass up "null" for the rect so when changing back to this fragment, we don't change
        // the focused menu option
        super.onFocusChanged(gainFocus, direction, null);

        // Now make sure the correct item is bolded as focus changes!
        setTextColorOnView(gainFocus, (TextView) getSelectedView());
    }

    /**
     * Call this in the onItemSelected() listener if you want to make sure the selected view's style
     * is set up correctly.
     * 
     * @param view The newly selected view
     */
    /* package */void onItemSelected(View view)
    {
        // Reset the views to have the correct style
        setTextColorOnView(true, mPreviouslySelectedView);
        setTextColorOnView(hasFocus(), (TextView) view);

        mPreviouslySelectedView = (TextView) view;
    }

    /**
     * Helper method to set the correct style on the textviews.
     */
    private static void setTextColorOnView(boolean defaultStyle, TextView view)
    {
        if (view != null)
        {
            view.setTextColor(defaultStyle ? defaultColors : selectedColors);
        }
    }
}
