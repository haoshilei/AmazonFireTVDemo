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

package com.sample.amazon.asbuilibrary.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ListView;
import com.sample.amazon.uiwidgetssample.R;

/**
 * A ListView whose selected item is always positioned at a certain Y offset
 */
public class ConditionalFocusListView extends ListView
{
    // This is filled via XML attributes
    int mSelectedPositionFromTopPX = 0;

    public ConditionalFocusListView(Context context)
    {
        this(context, null);
    }

    public ConditionalFocusListView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ConditionalFocusListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        TypedArray typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.ConditionalFocusListView);
        mSelectedPositionFromTopPX = typedAttrs.getDimensionPixelOffset(
                R.styleable.ConditionalFocusListView_selected_position_from_top, mSelectedPositionFromTopPX);
        typedAttrs.recycle();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            int position = getSelectedItemPosition() + 1;
            if (isEnabled() && position < getCount())
            {
                setSelectionFromTop(position, mSelectedPositionFromTopPX);
            }
            return true;
        }
        else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)
        {
            int position = getSelectedItemPosition() - 1;
            if (isEnabled() && position >= 0)
            {
                setSelectionFromTop(position, mSelectedPositionFromTopPX);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Resets the selection to the first item
     */
    public void resetSelection()
    {
        if (getCount() > 0)
        {
            setSelectionFromTop(0, mSelectedPositionFromTopPX);
        }
    }

    /**
     * Forces a re-selection of the current item. This can be useful if the selected item changed
     * its layout and we need to make ListView aware of this change.
     */
    public void recalculateCurrentPosition()
    {
        if (getCount() > 0)
        {
            setSelectionFromTop(getSelectedItemPosition(), mSelectedPositionFromTopPX);
        }
    }

    /**
     * Returns the selected item's desired Y offset
     * @return
     */
    public int getSelectedPositionFromTopPX()
    {
        return mSelectedPositionFromTopPX;
    }

    /**
     * Sets the selected item's desired Y offset
     * @param selectedPositionFromTopPX The desired offset
     */
    public void setSelectedPositionFromTopPX(int selectedPositionFromTopPX)
    {
        mSelectedPositionFromTopPX = selectedPositionFromTopPX;
    }
}
