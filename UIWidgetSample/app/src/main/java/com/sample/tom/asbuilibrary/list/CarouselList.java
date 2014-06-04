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

package com.sample.tom.asbuilibrary.list;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HeaderViewListAdapter;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.list.adapter.CarouselListAdapterProvider;
import com.sample.tom.asbuilibrary.util.Utils;

/**
 * This is a listview which controls the up and down positions
 */
public class CarouselList extends ConditionalFocusListView
{
    // The vars below (and related functionality) are for trapz logging
    private String mName = "undefined";
    private boolean mLogLoadStatus = false;
    private boolean mAllImagesLoaded = false;
	private static final String TAG = CarouselList.class.getName();
    private static final String LOG_CAROUSEL_LIST_LOADED = "Pastry List Loaded";


    public CarouselList(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setItemsCanFocus(true);
    }

    private CarouselListAdapterProvider getCarouselListAdapter()
    {
        if (getHeaderViewsCount() > 0)
        {
            return (CarouselListAdapterProvider) ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter();
        }
        else
        {
            return (CarouselListAdapterProvider)getAdapter();
        }
    }

    /**
     * Disable the images in the CarouselList
     */
    public void disableImages()
    {
        CarouselListAdapterProvider adapterProvider = getCarouselListAdapter();

        if (adapterProvider != null)
        {
            adapterProvider.disableImages();
        }
    }

    /**
     * Enable the images in the CarouselList
     */
    public void enableImages()
    {
        CarouselListAdapterProvider adapterProvider = getCarouselListAdapter();

        if (adapterProvider != null)
        {
            adapterProvider.enableImages();
        }
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public void logLoadStatus(boolean logLoadStatus)
    {
        mLogLoadStatus = logLoadStatus;
    }

    protected boolean allImagesLoaded()
    {
        if (getChildCount() == 0)
        {
            return false;
        }

        for (int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            CoverItemPagingCarouselView carousel =
                    (CoverItemPagingCarouselView) child.findViewById(R.id.cover_list);
            if (carousel != null && !carousel.getAllImagesLoaded())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        CarouselListAdapterProvider adapterProvider = getCarouselListAdapter();

        if (mLogLoadStatus)
        {
            boolean allImagesLoaded = allImagesLoaded();
            if (!mAllImagesLoaded && allImagesLoaded)
            {
                Log.d(TAG , LOG_CAROUSEL_LIST_LOADED);
                // A toast is useful during debugging to make sure we're
                // correctly detecting when all images are loaded
                // String msg = String.format("CarouselList '%s' all images loaded", mName);
                // Toast.makeText(getContext(), msg, 1000).show();
            }
            mAllImagesLoaded = allImagesLoaded;
        }

        if (adapterProvider != null)
        {
            if (getFirstVisiblePosition() > getLastVisiblePosition())
            {
                // This indicates the visible range is empty
                adapterProvider.updateVisible(getFirstVisiblePosition(), getLastVisiblePosition());
            }
            else
            {
                // I see weirdness in the reported visible range at the start of the
                // list. Clamp it to the valid item range.
                int visibleStart = Utils.clamp(getFirstVisiblePosition() - getHeaderViewsCount(), 0, getCount() - 1);
                int visibleEnd = Utils.clamp(getLastVisiblePosition() - getHeaderViewsCount(), 0, getCount() - 1);
                adapterProvider.updateVisible(visibleStart, visibleEnd);
            }
        }
    }
}
