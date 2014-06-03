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

package com.sample.amazon.uiwidgetssample;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sample.amazon.asbuilibrary.list.CoverItemProvider;
import com.sample.amazon.asbuilibrary.list.ItemView;
import com.sample.amazon.asbuilibrary.view.ViewState;
import com.sample.amazon.uiwidgetssample.R;

public class SampleItemView extends RelativeLayout implements ItemView
{
    private SampleItem mItem;

    public SampleItemView(Context context)
    {
        super(context);
        init();
    }

    public SampleItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public SampleItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
    }

    @Override
    public ImageView getCoverImageView()
    {
        return (ImageView) findViewById(R.id.image);
    }

    @Override
    public View getSpinnerView()
    {
        return findViewById(android.R.id.progress);
    }

    @Override
    public void onItemClick()
    {
        if (mItem != null && mItem.getItemClass() != null)
        {
            Intent intent = new Intent(getContext(), mItem.getItemClass());
            getContext().startActivity(intent);
        }
    }

    @Override
    public void showItem(CoverItemProvider<?> coverItem)
    {
        if (mItem == coverItem)
        { // note: this precludes the ability to update and refresh SampleItem
            return;
        }

        mItem = (SampleItem) coverItem;
        TextView textView = (TextView)findViewById(R.id.text);
        if (textView != null)
        {
            textView.setText(mItem.getTitle());
        }
    }

    @Override
    public View getItemViewContainer()
    {
        return this;
    }

    // No-op methods

    @Override
    public void setCoverImage(Drawable image)
    {
    }

    @Override
    public void setViewState(ViewState state, boolean animate)
    {
    }

    @Override
    public void setExpandable(boolean expandable)
    {
    }

    @Override
    public void onCarouselSelectionChanged(boolean selected)
    {
    }

    @Override
    public int getMiniDetailsLayoutId()
    {
        // This is the layout that the mini details should be inflated from
        return R.layout.sample_mini_details_layout;
    }

    @Override
    public void updateMiniDetailsUi(View miniDetailsLayout)
    {
        // The view passed in is the inflated view from getMiniDetailsLayoutId().  This view
        // may be shared between other items in the carousel, so be sure to update the content.
        if (mItem != null)
        {
            // We have an item, populate the mini details!
            miniDetailsLayout.setVisibility(View.VISIBLE);
            ((TextView)miniDetailsLayout).setText(mItem.getTitle());
        }
        else
        {
            // Otherwise, we should hide our views
            miniDetailsLayout.setVisibility(View.GONE);
        }
    }
}
