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

import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.sample.amazon.asbuilibrary.list.CarouselView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

/**
 * Superclass for adapters of Carousels that allow for asynchronous loading of data
 * 
 * This allows for only a page of data will be loaded at a time.  Each piece of data must have a unique identifier, as
 * the user scrolls, the adapter will request for the next page of data to be loaded.  This allows your implementation
 * to be smart about loading data and preload some of the data before showing the user.
 * 
 * Type IdentifierType is the datatype to be used as the individual data item identifiers.
 * Type DataType is the datatype to be used as the individual data items.
 */
abstract public class BasePagingCarouselAdapter<IdentifierType, DataType> extends BaseAdapter implements
        AdapterView.OnItemSelectedListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "BasePagingCarouselAdapter";

    protected CarouselView<BasePagingCarouselAdapter<IdentifierType, DataType>> mCarousel;

    // The list of all the ids in the carousel
    protected List<IdentifierType> mList;

    // Map used for mapping an id to an item
    protected Map<IdentifierType, DataType> mItemMap;

    // List of observers that need to be notified when the adapter's contents changed
    protected List<ListObserver> mObservers = new ArrayList<ListObserver>();

    // Quick way to determine if a page has been requested
    private ArrayList<Boolean> mRequestedPages = new ArrayList<Boolean>();

    // Current page size
    private int mPageSize;

    // Current prefetch amount
    private int mPrefetchPages;

    // TODO(steveT): There's no longer any reason to have our own observer
    // interface. We should switch back to DataSetObserver.
    public interface ListObserver
    {
        public void onListChanged();
    }

    public BasePagingCarouselAdapter(List<IdentifierType> list)
    {
        mPageSize = 30;
        mPrefetchPages = 1;
        
        mItemMap = new HashMap<IdentifierType, DataType>(list.size());

        mList = list;

        int pages = getNumPages();
        for (int i = 0; i < pages; i++)
        {
            mRequestedPages.add(false);
        }
    }

    @Override
    public int getCount()
    {
        return mList.size();
    }

    @Override
    public DataType getItem(int position)
    {
        return mItemMap.get(getItemIdAtPosition(position));
    }

    /**
     * @param position The position within the adapter
     * @return the Identifier for the item at the desired position
     */
    public IdentifierType getItemIdAtPosition(int position)
    {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Remove the item at the given position. This triggers an update of the
     * carousel.
     */
    public void removeItem(int position)
    {
        // We need to carefully update all our paging data structures. When we
        // delete item N, all the items shift down one page. If item N was in
        // page P, page P now has one new item at the end that used to be part
        // of page P+1. If page P has been requested but page P+1 hasn't, we
        // need to make sure to request the new item at the end of page P. This
        // goes for all pages from page P to the last page. Earlier pages
        // haven't been modified.

        List<IdentifierType> listOfIdsToRequest = new ArrayList<IdentifierType>();
        int pages = getNumPages();
        int startPage = getPage(position);
        for (int p = startPage; p+1 < pages; p++)
        {
            if (mRequestedPages.get(p) && !mRequestedPages.get(p+1))
            {
                // Request the first item from page i+1, which is about to
                // become the last item of page i
                listOfIdsToRequest.add(getItemIdAtPosition(getPageStartIndex(p+1)));
            }
        }
        requestItemData(listOfIdsToRequest);

        // Now we're ready to actually remove the item
        IdentifierType id = mList.remove(position);
        // It's possible the list has duplicates. Only remove the item from the
        // map if it's no longer in the list.
        if (!mList.contains(id))
        {
            mItemMap.remove(id);
        }

        if (mRequestedPages.size() > getNumPages())
        {
            // The last page is now gone. Remove it from mRequestedPages.
            mRequestedPages.remove(mRequestedPages.size()-1);
        }

        mCarousel.onAdapterItemRemoved(position);
        notifyListChanged();
    }

    /**
     * Remove the item with the given ID. If the same ID shows up multiple times
     * in the list, all instances are removed.
     */
    public void removeItemById(IdentifierType id)
    {
        for (int pos = mList.indexOf(id); pos != -1; pos = mList.indexOf(id))
        {
            removeItem(pos);
        }
    }

    private int getNumPages()
    {
        return getCount() / mPageSize + (getCount() % mPageSize == 0 ? 0 : 1);
    }

    private int getPage(int pos)
    {
        return pos / mPageSize;
    }

    private int getPageLength(int page)
    {
        if (page == getNumPages() - 1)
        {
            return getCount() - page * mPageSize;
        }
        else
        {
            return mPageSize;
        }
    }

    private int getPageStartIndex(int page)
    {
        return page * mPageSize;
    }

    private int getPageEndIndex(int page)
    {
        return page * mPageSize + getPageLength(page);
    }

    public void setPageSize(int size)
    {
        mPageSize = size;
    }

    public void setPagePrefetchAmount(int amount)
    {
        mPrefetchPages = amount;
    }

    /**
     * Sets the current position of the carousel adapter
     */
    public void setPosition(int pos)
    {
        // Allow the client to setPosition(0) even if we're empty.
        if (pos == 0 && getCount() == 0)
        {
            return;
        }

        if (pos < 0 || pos >= getCount())
        {
            throw new IllegalArgumentException("Invalid position given to ItemListCarouselAdapter.setPosition");
        }

        int currentPage = getPage(pos);
        int pages = getNumPages();

        List<IdentifierType> listOfIdsToRequest = new ArrayList<IdentifierType>();

        // If wrapping is disabled, clamp the start/end pages
        int startPage = currentPage - mPrefetchPages;
        int endPage = currentPage + mPrefetchPages;
        if (!mCarousel.getWrap())
        {
            startPage = Math.max(0, startPage);
            endPage = Math.min(pages-1, endPage);
        }

        for (int i = startPage; i <= endPage; i++)
        {
            // Make sure to handle negative modulus results properly. See
            // http://stackoverflow.com/questions/4403542/how-does-java-do-modulus-calculations-with-negative-numbers
            int page = i % pages;
            if (page < 0)
            {
                page += pages;
            }

            // do we already have the data for this page?
            if (!mRequestedPages.get(page))
            {
                // Grab all the ids for this page and add them
                listOfIdsToRequest.addAll(mList.subList(getPageStartIndex(page), getPageEndIndex(page)));
                mRequestedPages.set(page, true);
            }
        }

        if (listOfIdsToRequest.isEmpty())
        {
            // nothing to request
            return;
        }

        requestItemData(listOfIdsToRequest);
    }

    /**
     * Called when this adapter is being added to a carousel
     */
    public void attachToCarousel(CarouselView<BasePagingCarouselAdapter<IdentifierType, DataType>> carousel)
    {
        if (mCarousel != null)
        {
            mCarousel.removeItemSelectedListener(this);
        }
        mCarousel = carousel;
        mCarousel.addItemSelectedListener(this);
    }

    public void detachFromCarousel()
    {
        if (mCarousel != null)
        {
            mCarousel.removeItemSelectedListener(this);
            mCarousel = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        setPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    /**
     * Add a listener to listen when the list of data has been changed
     */
    public void addListObserver(ListObserver observer)
    {
        if (!mObservers.contains(observer))
        {
            mObservers.add(observer);
        }
    }

    /**
     * removeListObserver
     *
     * @param observer The observer to remove
     */
    public void removeListObserver(ListObserver observer)
    {
        mObservers.remove(observer);
    }

    protected void notifyListChanged()
    {
        notifyDataSetChanged();
        for (ListObserver observer : mObservers)
        {
            observer.onListChanged();
        }
    }

    /**
     * This is called when a new list of data should be loaded
     */
    abstract protected void requestItemData(List<IdentifierType> listOfIdsToRequest);
}
