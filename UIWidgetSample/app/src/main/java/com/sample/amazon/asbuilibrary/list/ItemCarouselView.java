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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.list.adapter.BasePagingCarouselAdapter;
import com.sample.amazon.asbuilibrary.list.adapter.CoverItemPagingCarouselAdapter;
import com.sample.amazon.asbuilibrary.list.handler.ItemListListener;
import com.sample.amazon.asbuilibrary.view.ViewState;

public class ItemCarouselView extends CoverItemPagingCarouselView implements
        BasePagingCarouselAdapter.ListObserver
{
    @SuppressWarnings("unused")
    private static final String TAG = "NewItemCarouselView";

    private ViewState mViewState = ViewState.COLLAPSED;

    private boolean mExpandable = false;

    public ItemCarouselView(Context context, AttributeSet attrs)
    {
        super(context, attrs, R.id.carousel_cover_holder);
    }

    @Override
    public void setAdapter(CoverItemPagingCarouselAdapter adapter)
    {
        if (mAdapter != null)
        {
            (mAdapter).removeListObserver(this);
        }

        if (adapter != null)
        {
            adapter.addListObserver(this);
        }

        super.setAdapter(adapter);
    }

    @Override
    public void onListChanged()
    {
        // The adapter publishes onListChanged when a page
        // finishes downloading. To update the UI, we can either (a)
        // completely reinitialize the layout, or (b) take advantage of our
        // knowledge of how the UI works to only update the cover images
        // without actually redoing the layout. The code below does (a), but
        // this may cause performance issues.

        for (ItemListListener listener : mItemListListeners)
        {
            listener.onItemListChanged(this);
        }

        // Avoid extra allocation by only requesting a layout if necessary
        if (mLayoutInitParams == null)
        {
            resetLayout(new LayoutInitParams(false));
        }
    }

    public void setViewState(ViewState state)
    {
        if (state == mViewState)
        {
            return;
        }

        mViewState = state;

        for (int i = 0; i < getChildCount(); i++)
        {
            ((ItemView) getChildAt(i)).setViewState(state, true);
        }
    }

    // Get a new view, attempting to recycle from a view pool
    @Override
    protected View getView(int position, boolean selected)
    {
        View view = super.getView(position, selected);
        ((ItemView)view).setViewState(mViewState, false);
        ((ItemView)view).setExpandable(mExpandable);
        view.setEnabled(isEnabled());
        return view;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        dispatchEnabled(enabled);
    }

    private void dispatchEnabled(boolean enabled)
    {
        //Pass it along to all children in case it affects their behavior
        for (int i = 0; i < getChildCount(); i++)
        {
            getChildAt(i).setEnabled(enabled);
        }
    }

    public void setExpandable(boolean expandable)
    {
        mExpandable = expandable;

        for (int i = 0; i < getChildCount(); i++)
        {
            ((ItemView) getChildAt(i)).setExpandable(expandable);
        }
    }

    public boolean isExpandable()
    {
        return mExpandable;
    }

    /**
     * Triggers events when focus is gained or lost
     */
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
    {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        //Temporarily disable us until focus comes back
        dispatchEnabled(isEnabled() && gainFocus);
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        boolean handled = false;

        // Send the key event to our child views, for their potential expanded view
        if (mSelectedView != null && isEnabled())
        {
            handled = mSelectedView.onKeyDown(keyCode, event);
        }

        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event)
    {
        boolean handled = false;

        // Send the key event to our child views, for their potential expanded view
        if (mSelectedView != null && isEnabled())
        {
            handled = mSelectedView.onKeyUp(keyCode, event);
        }

        return handled || super.onKeyUp(keyCode, event);
    }
}
