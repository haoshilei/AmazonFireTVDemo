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

package com.sample.amazon.asbuilibrary.list.handler;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.sample.amazon.asbuilibrary.list.CoverItemProvider;
import com.sample.amazon.asbuilibrary.list.ItemView;

/**
 * Item list handler for the mini details. This takes care of updating the mini details data and
 * handling the click events
 */
public class MiniDetailsItemListHandler extends BaseMiniDetailsItemListHandler
{
    private final SparseArray<View> mMiniDetailsViews;
    private View mPreviousMiniDetails;

    public MiniDetailsItemListHandler(Context context, ViewGroup viewHolder)
    {
        super(context, viewHolder);
        mMiniDetailsViews = new SparseArray<View>();
    }

    @Override
    public void onItemListChanged(AdapterView<?> adapterView)
    {
        if (mViewHolder != null)
        {
            updateMiniDetailsContent(adapterView, adapterView.getSelectedView());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
    {
        super.onItemSelected(adapterView, view, position, id);

        if (mViewHolder != null)
        {
            // Set up the view properly
            updateMiniDetailsContent(adapterView, view);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
        if (mViewHolder != null)
        {
            if (hasFocus)
            {
                // We have focus, show and update our content
                AdapterView<?> adapter = (AdapterView<?>) view;
                updateMiniDetailsContent(adapter, adapter.getSelectedView());
                mViewHolder.setVisibility(View.VISIBLE);
            }
            else
            {
                // We lost focus, hide
                mViewHolder.setVisibility(View.GONE);
            }
        }
    }

    public void onReturnToMiniDetails(AdapterView<?> adapterView)
    {
        if (mViewHolder != null)
        {
            updateMiniDetailsContent(adapterView, adapterView.getSelectedView());
        }
    }

    protected void updateMiniDetailsContent(AdapterView<?> adapterView, View view)
    {
        CoverItemProvider<?> wrapper = (CoverItemProvider<?>) adapterView.getItemAtPosition(adapterView.getSelectedItemPosition());

        if (wrapper == null)
        {
            // Hide me
            mViewHolder.setAlpha(0f);
        }
        else
        {
            // Now set our views up!
            mViewHolder.setAlpha(1.0f);

            // First, get our item view.
            ItemView itemView;

            if (view == null)
            {
                itemView = mLastSelected;
            }
            else
            {
                itemView = (ItemView) view;
            }

            // Now get our mini details view
            View miniDetailsView = null;
            if (itemView != null && itemView.getMiniDetailsLayoutId() > 0)
            {
                miniDetailsView = mMiniDetailsViews.get(itemView.getMiniDetailsLayoutId());

                // Do we need to inflate it?
                if (miniDetailsView == null)
                {
                    miniDetailsView = LayoutInflater.from(mViewHolder.getContext()).inflate(
                            itemView.getMiniDetailsLayoutId(), mViewHolder, false);
                    mViewHolder.addView(miniDetailsView);
                    mMiniDetailsViews.put(itemView.getMiniDetailsLayoutId(), miniDetailsView);
                }
                else
                {
                    miniDetailsView.setVisibility(View.VISIBLE);
                }

                // Tell the item view to update the mini details
                itemView.updateMiniDetailsUi(miniDetailsView);
            }

            // Now hide all other views
            if (mPreviousMiniDetails != null && mPreviousMiniDetails != miniDetailsView)
            {
                mPreviousMiniDetails.setVisibility(View.GONE);
            }

            // Save what one we are showing
            mPreviousMiniDetails = miniDetailsView;
        }
    }
}
