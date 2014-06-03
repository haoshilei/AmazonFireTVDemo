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

package com.sample.amazon.asbuilibrary.list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.fragment.BaseCarouselListFragment;
import com.sample.amazon.asbuilibrary.list.ChannelData;
import com.sample.amazon.asbuilibrary.list.CoverItemPagingCarouselView;
import com.sample.amazon.asbuilibrary.view.FontableTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A list adapter for a list of {@link com.sample.amazon.asbuilibrary.list.CoverItemPagingCarouselView}
 */
public class CarouselListAdapter extends BaseAdapter implements CarouselListAdapterProvider
{
    private static final String TAG = CarouselListAdapter.class.getSimpleName();

    private final BaseCarouselListFragment mFragment;

    // TODO: debugging flag:
    private boolean mShowHeroImages = false;

    // The ChannelData item list, one item for each category list.
    private List<ChannelData> mChannelData = new ArrayList<ChannelData>();

    // The last recorded visible carousel range. The range is inclusive. A range
    // of 0-4 means 5 carousels total.
    private int mVisibleStart;
    private int mVisibleEnd;

    // The number of carousels to prefetch. This applies both above and below
    // the visible carousels, so the total number of carousels with loaded
    // images will be visibleCarousels + 2*BUFFER_CAROUSELS.
    private static final int BUFFER_CAROUSELS = 1;
    //private ImageManager mHeroImageManager;

    //TODO not taking the time to make it hero-compatible right now. Keeping this code in case it helps.
    // A simple class for storing the data associated with a channel
    // (alternatively called a category list).

    public CarouselListAdapter(BaseCarouselListFragment fragment)
    {
        mFragment = fragment;
        // TODO: debug flag
    }

    // TODO: debug flag
    public void refreshHeroFlag()
    {
    }

    @Override
    public int getCount()
    {
        return mChannelData.size();
    }

    @Override
    public CoverItemPagingCarouselView getItem(int position)
    {
        if (position >= mChannelData.size())
        {
            position = mChannelData.size() - 1;
        }

        if (position < 0)
        {
            position = 0;
        }

        return mChannelData.get(position).getCarousel();
    }

    @Override
    public long getItemId(int id)
    {
        return id;
    }

    @Override
    public View getView(int position, View recycledView, ViewGroup parent)
    {
        ChannelData channelData = mChannelData.get(position);
        ViewGroup containerNew;
        if (recycledView == null)
        { // if the ListView thinks this went to the recycle pool, it no longer behaves properly
            containerNew = (ViewGroup) LayoutInflater.from(mFragment.getActivity()).inflate(
                    R.layout.carousel_movies_and_tv, null, false);
        }
        else
        {
            containerNew = (ViewGroup) recycledView;
        }

        View oldCarousel = containerNew.findViewById(R.id.cover_list);
        if (oldCarousel != null)
        {
            containerNew.removeView(oldCarousel);
        }

        // we reuse the carousel so that we don't have to recreate and relayout
        ViewGroup oldParent = (ViewGroup) channelData.getCarousel().getParent();
        if (oldParent != null)
        {
            oldParent.removeView(channelData.getCarousel());
        }

        containerNew.addView(channelData.getCarousel());
        updateContainer(containerNew, channelData.getTitle());
        return containerNew;
    }

    /**
     * Adds an itemlist to the list of itemlists for this list of carousel views
     *
     * @param channel The channel to add
     */
    public void addItemList(ChannelData channel)
    {
        /*if (list.getItemIds().size() == 0)
        {
            // Don't show an empty list
            return;
        }*/

        mChannelData.add(channel);
        notifyDataSetChanged();
        mFragment.onNewChannel(channel);
    }

    public CoverItemPagingCarouselView getCarousel(int position)
    {
        return mChannelData.get(position).getCarousel();
    }

    public void clearItemLists()
    {
        disableImages();
        mChannelData.clear();
        notifyDataSetChanged();
    }

    /**
     * Disable all images in the CarouselListAdapter.
     */
    public void disableImages()
    {
    	// do nothing
    }

    /**
     * Enable the images in the CarouselListAdapter.
     */
    public void enableImages()
    {
        updateVisible(mVisibleStart, mVisibleEnd);
    }

    /**
     * Update the loaded carousels based on the visible region
     */
    public void updateVisible(int visibleStart, int visibleEnd)
    {
        if (isEmpty())
        {
            return;
        }

        mVisibleStart = visibleStart;
        mVisibleEnd = visibleEnd;

        // If the visible range is bogus release all images and bail
        if (visibleStart < 0 || visibleStart > visibleEnd || visibleEnd >= getCount())
        {
            disableImages();
            return;
        }

        int availableStart = Math.max(0, visibleStart - BUFFER_CAROUSELS);
        int availableEnd = Math.min(getCount() - 1, visibleEnd + BUFFER_CAROUSELS);

        // Make sure we call measure and layout on the carousels in the
        // BUFFER_CAROUSEL range. These carousels aren't actually visible, so
        // won't yet be laid out by the normal rendering of this list view.
        int visibleCarouselWidth = mChannelData.get(visibleStart).getCarousel().getWidth();
        for (int i = availableStart; i < visibleStart; i++)
        {
            prepareNonVisibleCarousel(i, visibleCarouselWidth);
        }
        for (int i = visibleEnd + 1; i <= availableEnd; i++)
        {
            prepareNonVisibleCarousel(i, visibleCarouselWidth);
        }

        // availableStart and availableEnd represent the range of carousels
        // whose images we need to load. Any carousel outside of that range
        // should release its images.
        //mHeroImageManager.startTransaction("CarouselListAdapter.updateActiveCarousels");
        int size = mChannelData.size();
        for (int i = 0; i < size; i++)
        {
            ChannelData channelData = mChannelData.get(i);
            CoverItemPagingCarouselView carousel = channelData.getCarousel();
        }
        //mHeroImageManager.commitTransaction();
    }




    private void updateContainer(View container, String title)
    {
        View tombstone = container.findViewById(R.id.tombstone);
        tombstone.setVisibility(mShowHeroImages ? View.GONE : View.VISIBLE);

        FontableTextView tombstoneTitle = (FontableTextView) tombstone.findViewById(R.id.tombstone_text);
        tombstoneTitle.setText(title.toUpperCase(Locale.US));
    }

    /**
     * This prepares the carousel. This requires doing a layout of the carousel.
     */
    private void prepareNonVisibleCarousel(int pos, int carouselWidth)
    {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(carouselWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        View view = getView(pos, null, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        // Since the child has been laid out already it may not get laid out
        // again when it's actually attached to the parent ListView. But the
        // layout call we made may use slightly different width and height
        // measure specs than the ListView will use, so we want to force a
        // relayout of the carousel by calling requestLayout on it. This will
        // force the ListView to call layout on it when it gets added to the
        // ListView.
        view.requestLayout();
    }

    public String getItemTitle(int selectedItemPosition)
    {
        return mChannelData.get(selectedItemPosition).getTitle();
    }

    public int getItemSize(int selectedItemPosition)
    {
        return mChannelData.get(selectedItemPosition).getCarousel().getCount();
    }
}
