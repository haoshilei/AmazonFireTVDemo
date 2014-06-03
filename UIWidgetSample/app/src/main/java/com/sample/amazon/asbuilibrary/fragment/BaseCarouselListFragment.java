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

package com.sample.amazon.asbuilibrary.fragment;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.list.CarouselList;
import com.sample.amazon.asbuilibrary.list.CarouselView;
import com.sample.amazon.asbuilibrary.list.ChannelData;
import com.sample.amazon.asbuilibrary.list.CoverItemPagingCarouselView;
import com.sample.amazon.asbuilibrary.list.CoverItemProvider;
import com.sample.amazon.asbuilibrary.list.ItemCarouselView;
import com.sample.amazon.asbuilibrary.list.OnCategoryChangeListener;
import com.sample.amazon.asbuilibrary.list.adapter.CarouselListAdapter;
import com.sample.amazon.asbuilibrary.list.adapter.CoverItemPagingCarouselAdapter;
import com.sample.amazon.asbuilibrary.list.handler.BaseMiniDetailsItemListHandler;
import com.sample.amazon.asbuilibrary.list.handler.ItemListHandler;
import com.sample.amazon.asbuilibrary.list.handler.UpdateRibbonItemListHandler;
import com.sample.amazon.asbuilibrary.util.BreadCrumbRibbon;
import com.sample.amazon.asbuilibrary.util.CarouselZoom;
import com.sample.amazon.asbuilibrary.util.CarouselZoomAnimationCallback;

/**
 *
 * Abstract Fragment for any screen that has a list of lists. Example : Movies screen, TV screen.
 *
 * This fragment takes care of loading the screen, the list of items and the item data.
 *
 */
public abstract class BaseCarouselListFragment extends Fragment implements OnItemClickListener,
        OnItemSelectedListener, CarouselZoomAnimationCallback, OnCategoryChangeListener
{
    private static final String TAG = BaseCarouselListFragment.class.getSimpleName();
    private static final String SAVE_ZOOM_LEVEL = TAG + "_ZOOM_LEVEL";
    private static final String SAVE_SELECTION_CAROUSEL_INDEX = TAG + "_SELECTION_CAROUSEL_INDEX";
    private static final String SAVE_SELECTION_CAROUSEL_NAME = TAG + "_SELECTION_CAROUSEL_NAME";
    private static final String SAVE_SELECTION_ITEM_INDEX = TAG + "_SELECTION_ITEM_INDEX";
    private static final String SAVE_SELECTION_ITEM_ID = TAG + "_SELECTION_ITEM_NAME";

    private CarouselListAdapter mListAdapter;
    private CarouselList mList;
    private ViewGroup mCarouselHolder;
    private BreadCrumbRibbon mBreadcrumb;

    private int HERO_LARGE_WIDTH;
    private CarouselZoom.TargetZoomState mZoomLevel = CarouselZoom.TargetZoomState.CAROUSEL_LIST;
    private boolean mZoomChanged;
    private UpdateRibbonItemListHandler mItemSelectedRibbonHandler;
    private BaseMiniDetailsItemListHandler mItemSelectedHandler;
    private CarouselZoom mCarouselZoom;
    private View mSelectedRow = null;
    // TODO: debug flag
    private boolean mShowHeroImages;
    private Bundle mSavedInstanceState = null;
    private ItemListHandler mItemHandler;
    private View mHeaderView;
    protected ViewGroup mMiniDetailsHolder;
    private float mSelectedLeftMargin;
    private float mUnselectedLeftMargin;
    private float mTombUnselectedTopMargin;
    private float mTombSelectedTopMargin;
    private int mCoverUnselectedHeight;
    private int mCoverSelectedHeight;

    /**
     * Implement this method to notify data has been loaded into the carousels.
     * For some reason we corrupt the carousel if we go to 1D before all carousels are loaded.
     * @return true if all channels have been loaded
     */
    abstract protected boolean isLoadComplete();

    /**
     * Implement this to load the screen data
     */
    abstract protected void loadScreenData();

    /**
     * Getting the Main handler from implementations, in order to post operations on it
     * @return Main handler
     */
    abstract protected Handler getMainHandler();

    /**
     * Get the icon that is rendered as part of the 1D list ribbon (breadcrumb)
     *
     * @return Drawable
     */
    abstract public Drawable getFragmentBreadCrumbIconDrawable();

    /**
     * Does this fragment have UI focus/control?
     *
     * @return boolean
     */
    abstract public boolean hasControl();

    /**
     * Callback to allow the derived Fragment class to know that we are changing the zoom level.
     */
    protected void onZoomChanged()
    {
    }

    /**
     * Derived classes that have additional data to save should override onSavedUiState instead of onSaveInstanceState
     * @param outState
     */
    protected void onSavedUiState(Bundle outState)
    {
    }

    /**
     * Derived classes that have additional data to save should override onRestoredUiState instead of restoreUiState
     * @param savedInstanceState
     */
    protected void onRestoredUiState(Bundle savedInstanceState)
    {
    }

    private boolean restoringUiState()
    {
        return mSavedInstanceState != null;
    }

    protected final String getLogTag()
    { // we'll show the derived type in the logs to track the instance better
        return getClass().getSimpleName();
    }

    private void restoreUiState()
    {
        if (mSavedInstanceState == null)
        {
            return;
        }

        final Bundle savedInstanceState = mSavedInstanceState;
        final int indexChannel = savedInstanceState.getInt(SAVE_SELECTION_CAROUSEL_INDEX, -1);
        if (indexChannel > mListAdapter.getCount() - 1)
        { // haven't reached the rehydrated selected carousel yet
            return;
        }

        mSavedInstanceState = null;
        if (isHidden())
        { // the user has moved off this fragment
            return;
        }

        if (indexChannel < mListAdapter.getCount() - 1)
        {
            Log.w(TAG, "restoreUiState: unexpected index: " + indexChannel);
            return;
        }

        final CoverItemPagingCarouselView carousel = mListAdapter.getCarousel(indexChannel - mList.getHeaderViewsCount());
        if (carousel == null)
        {
            Log.w(TAG, "restoreUiState: null carousel");
            return;
        }

        String strCarousel = carousel.getName();
        if (strCarousel == null || !strCarousel.equals(savedInstanceState.getString(SAVE_SELECTION_CAROUSEL_NAME)))
        {
            Log.w(TAG, "restoreUiState: carousel selection mismatch");
            return;
        }

        int zoomState = savedInstanceState
                .getInt(SAVE_ZOOM_LEVEL, CarouselZoom.TargetZoomState.CAROUSEL_LIST.ordinal());
        if (zoomState != CarouselZoom.TargetZoomState.ONE_D_LIST.ordinal())
        { // all done
            Log.v(TAG, "restoreUiState: restored carousel selection");
            onRestoredUiState(savedInstanceState);
            return;
        }

        final Handler handler = getMainHandler();
        handler.post(new Runnable()
        { // the list needs main cycles before it knows about the new carousel entry
            @Override
            public void run()
            {
                if (isHidden())
                {
                    Log.w(TAG, "restoreUiState: fragment hidden before we could select carousel");
                    return;
                }

                if (mZoomLevel != CarouselZoom.TargetZoomState.CAROUSEL_LIST)
                {
                    Log.w(TAG, "restoreUiState: zoomed before we could select carousel");
                    return;
                }

                mList.setSelectionFromTop(indexChannel, mList.getSelectedPositionFromTopPX());
            }
        });

        handler.post(new Runnable()
        { // the list view needs cycles to do relayout and have a selected view
            @Override
            public void run()
            {
                if (isHidden())
                {
                    Log.w(TAG, "restoreUiState: fragment hidden before we could zoom to 1D");
                    return;
                }

                if (mList.getSelectedItemPosition() != indexChannel)
                {
                    Log.w(TAG, "restoreUiState: carousel selection out of sync");
                    return;
                }

                if (mZoomLevel != CarouselZoom.TargetZoomState.CAROUSEL_LIST)
                {
                    Log.w(TAG, "restoreUiState: already zoomed");
                    return;
                }

                if (mList.getSelectedView() == null || !selectedViewInSync())
                { // the selected view isn't laid out yet, so try again a little later
                    handler.postDelayed(this, 20);
                    return;
                }

                zoomTo1D(false);
                showBreadcrumb();

                int indexItem = savedInstanceState.getInt(SAVE_SELECTION_ITEM_INDEX, -1);
                if (indexItem < 0 || indexItem >= carousel.getCount())
                {
                    Log.w(TAG, "restoreUiState: item index mismatch");
                    return;
                }

                if (carousel != mCarouselHolder.findViewById(R.id.cover_list))
                {
                    Log.w(TAG, "restoreUiState: invalid carousel state");
                    return;
                }

                CoverItemPagingCarouselAdapter<?> adapter = carousel.getAdapter();
                if (adapter == null)
                {
                    Log.w(TAG, "restoreUiState: null adapter");
                    return;
                }

                CoverItemProvider<?> item = adapter.getItem(indexItem);
                if (item == null)
                {
                    Log.w(TAG, "restoreUiState: null item");
                    return;
                }

                if (!item.equalsId(savedInstanceState, SAVE_SELECTION_ITEM_ID))
                {
                    Log.w(TAG, "restoreUiState: item selection mismatch");
                    return;
                }

                carousel.setSelection(indexItem);
                // not sure why this only works if we explicitly notify listeners
                mItemSelectedHandler.onItemSelected(carousel, null, indexItem, adapter.getItemId(indexItem));
                mItemSelectedRibbonHandler.onItemSelected(carousel, null, indexItem, adapter.getItemId(indexItem));
                Log.v(TAG, "restoreUiState: restored 1D zoom and selection");
                onRestoredUiState(savedInstanceState);
            }
        });
    }

    /**
     * Selecting one carousel of the list by providing a position
     *
     * @param position Position for the carousel to select
     */
    public void setListSelection(int position)
    {
        mList.setSelection(position);
    }

    final public CarouselListAdapter getListAdapter()
    {
        return mListAdapter;
    }

    final protected CarouselZoom.TargetZoomState getZoomLevel()
    {
        return mZoomLevel;
    }

    public void setItemSelectedHandler(BaseMiniDetailsItemListHandler itemSelectedHandler)
    {
        mItemSelectedHandler = itemSelectedHandler;
    }

    /**
     * If the carousel List is to have a Header view, set it here.
     * This method must be called prior to onActivityCreated(), since
     * that's where the setAdapter call is performed.
     * @param view
     */
    public void setHeaderView(View view)
    {
        mHeaderView = view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_carousel_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Grab our resources
        Resources res = getResources();

        // Set up our list of carousel views
        mList = (CarouselList) getView().findViewById(android.R.id.list);
        mList.setSelectedPositionFromTopPX((int) res.getDimension(
                R.dimen.movies_tv_fragment_selected_list_top_margin));
        mList.setDivider(null);
        mList.setItemsCanFocus(false);
        mList.setName(getLogTag());
        mList.logLoadStatus(true);

        mListAdapter = new CarouselListAdapter(this);
        if (mHeaderView != null)
        {
            mList.addHeaderView(mHeaderView);
        }
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(this);
        mList.setOnItemSelectedListener(this);
        mList.setSelector(android.R.color.transparent);

        mCarouselHolder = (ViewGroup) getView().findViewById(R.id.cover_list_container);
        mCarouselHolder.setVisibility(View.VISIBLE);

        View otherCarousel = mCarouselHolder.findViewById(R.id.cover_list);
        mCarouselHolder.removeView(otherCarousel);

        View fullOneD = getView().findViewById(R.id.oned_layout);
        mBreadcrumb = (BreadCrumbRibbon) fullOneD.findViewById(R.id.title_ribbon_layout);
        mBreadcrumb.setVisibility(View.INVISIBLE);

        /*
         * View numberView = mBreadcrumb.findViewById(R.id.ribbon_item_number_view); params =
         * (RelativeLayout.LayoutParams)numberView.getLayoutParams(); params.leftMargin = 1100;
         * numberView.setLayoutParams(params);
         */

        mMiniDetailsHolder = (ViewGroup) getView().findViewById(R.id.mini_details_frame);
        mItemSelectedRibbonHandler = new UpdateRibbonItemListHandler(mBreadcrumb);
        mItemSelectedHandler = new BaseMiniDetailsItemListHandler(getActivity(), mMiniDetailsHolder);
        mItemHandler = new ItemListHandler(getActivity());
        mCarouselZoom = new CarouselZoom(getActivity(), mList, fullOneD, mShowHeroImages, R.id.tombstone);

        mSelectedLeftMargin =
                res.getDimension(R.dimen.left_nav_expanded_width) - res.getDimension(R.dimen.tombstone_margin_left_unselected);
        mUnselectedLeftMargin = res.getDimension(R.dimen.left_nav_expanded_width);
        mTombUnselectedTopMargin = res.getDimension(R.dimen.tombstone_margin_top);
        mTombSelectedTopMargin = res.getDimension(R.dimen.tombstone_margin_top_selected);
        mCoverUnselectedHeight = Math.round(res.getDimension(R.dimen.carousel_cover_unselected_height));
        mCoverSelectedHeight = Math.round(res.getDimension(R.dimen.carousel_cover_target_height));
    }

    @Override
    public final void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (isHidden())
        { // only the active fragment gets to save/restore its state
            return;
        }

        int position = mList.getSelectedItemPosition();
        // Do we even have any carousels?
        if (position < 0 || position >= mList.getCount() - mList.getHeaderViewsCount())
        {
            return;
        }

        outState.putInt(SAVE_ZOOM_LEVEL, mZoomLevel.ordinal());
        outState.putInt(SAVE_SELECTION_CAROUSEL_INDEX, position);
        int carouselIndex = position;
        if (mList.getHeaderViewsCount() > 0)
        {
            if (carouselIndex == 0)
            {
                carouselIndex += mList.getHeaderViewsCount();
            }
            if (carouselIndex >= getListAdapter().getCount())
            {
                carouselIndex = getListAdapter().getCount() - 1;
            }
        }
        CoverItemPagingCarouselView carousel = mListAdapter.getCarousel(carouselIndex);
        outState.putString(SAVE_SELECTION_CAROUSEL_NAME, carousel.getName());
        if (mZoomLevel != CarouselZoom.TargetZoomState.ONE_D_LIST)
        {
            return;
        }

        int indexItem = carousel.getSelectedItemPosition();
        if (indexItem < 0 || indexItem >= carousel.getCount())
        {
            return;
        }

        CoverItemPagingCarouselAdapter<?> adapter = carousel.getAdapter();
        if (adapter == null)
        {
            Log.w(TAG, "onSaveInstanceState: null adapter");
            return;
        }

        CoverItemProvider<?> item = adapter.getItem(indexItem);
        if (item == null)
        {
            Log.w(TAG, "restoreUiState: null item");
            return;
        }

        outState.putInt(SAVE_SELECTION_ITEM_INDEX, indexItem);
        item.putIdInBundle(outState, SAVE_SELECTION_ITEM_ID);
        Log.v(TAG, "onSaveInstanceState: saving selection index: " + position);
        onSavedUiState(outState);
    }

    public final void fragmentShow()
    {
        Log.d(getLogTag(), "Showing fragment");
        CarouselList list = (CarouselList) getView().findViewById(android.R.id.list);
        list.enableImages();

        loadScreenData();

        View carousel = mCarouselHolder.findViewById(R.id.cover_list);
        if (carousel == null)
        {
            mZoomLevel = CarouselZoom.TargetZoomState.CAROUSEL_LIST;
        }
        else
        {
            mZoomLevel = CarouselZoom.TargetZoomState.ONE_D_LIST;
            // returning to 1D list, so need to update mini details
            if (carousel != null)
            {
                mItemSelectedHandler.onReturnToMiniDetails((CarouselView) carousel);
            }
            // NavigatorUtil.hide(mActivity); if navbar was hidden before, it should still be hidden
        }

        // TODO: debug flag
        mShowHeroImages = false;

        mListAdapter.refreshHeroFlag();
        Resources res = getResources();
        if (mShowHeroImages)
        {
            HERO_LARGE_WIDTH = (int) res.getDimension(R.dimen.hero_large_width);
        }
        else
        {
            HERO_LARGE_WIDTH = (int) res.getDimension(R.dimen.tombstone_width);
        }
    }

    public final void fragmentHide()
    {
        Log.d(getLogTag(), "Hiding fragment");

        // Clean up our image cache
        CarouselList list = (CarouselList) getView().findViewById(android.R.id.list);
        list.disableImages();
        mSavedInstanceState = null; // don't try to restore ui state anymore
    }

    public final void fragmentEntered(Fragment fragment)
    {
        //super.onFragmentEntered(fragment);

        CarouselList list = (CarouselList) getView().findViewById(android.R.id.list);
        Resources res = getResources();

        if (fragment != this)
        {
            // UX says that when we enter a fragment, the other carousel lists
            // should reset their position
            list.resetSelection();
            adjustTopMargin(res.getDimension(R.dimen.carousel_margin_top_unselected), false);
        }
        else
        {
            if (list.getSelectedItem() == null || list.getSelectedItem() == list.getItemAtPosition(0))
            {
                adjustTopMargin(res.getDimension(R.dimen.carousel_margin_top_selected), true);
            }
            setCarouselListSelection(true);
        }
    }

    public void fragmentReset()
    {
        //super.onFragmentReset();

        if (mZoomLevel != CarouselZoom.TargetZoomState.CAROUSEL_LIST)
        {
            // make sure we show lists for next time user visits
            CarouselZoom.TargetZoomState oldLevel = mZoomLevel;
            mZoomLevel = mCarouselZoom.zoomLevel(CarouselZoom.TargetZoomState.CAROUSEL_LIST,
                    HERO_LARGE_WIDTH, R.id.cover_list, mCarouselHolder, false, this);
            mZoomChanged = (oldLevel != mZoomLevel);
            if (mMiniDetailsHolder != null)
            {
                mMiniDetailsHolder.setVisibility(View.GONE);
            }

            if (mZoomChanged)
            {
                onZoomChanged();
            }
        }

        CarouselList list = (CarouselList) getView().findViewById(android.R.id.list);
        list.resetSelection();
        setCarouselListSelection(false);
        mSavedInstanceState = null; // don't try to restore ui state anymore
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        zoomTo1D(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        int count = mList.getCount();
        if (count > 0)
        {
            Resources res = getResources();
            if (position == 0)
            { // first row is selected
                if (hasControl())
                {
                    adjustTopMargin(res.getDimension(R.dimen.carousel_margin_top_selected), false);
                }
                else
                {
                    adjustTopMargin(res.getDimension(R.dimen.carousel_margin_top_unselected), false);
                }
            }
            else
            {
                adjustTopMargin(0, false);
            }

            if (hasControl())
            {
                setCarouselListSelection(true);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    public final boolean keyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            {
                if (!isLoadComplete() || mZoomLevel != CarouselZoom.TargetZoomState.CAROUSEL_LIST
                        || event.getRepeatCount() > 0)
                {
                    break;
                }

                if (mList.getSelectedItem() == null || ((ItemCarouselView) mList.getSelectedItem()).getCount() <= 0)
                {
                    break;
                }

                zoomTo1D(true);
                showBreadcrumb();
                event.startTracking();
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BACK:
                if (mZoomLevel == CarouselZoom.TargetZoomState.ONE_D_LIST)
                {
                    if (!isLoadComplete() || event.getRepeatCount() > 0)
                    {
                        break;
                    }

                    CarouselZoom.TargetZoomState oldLevel = mZoomLevel;
                    mZoomLevel = mCarouselZoom.zoomLevel(CarouselZoom.TargetZoomState.CAROUSEL_LIST, HERO_LARGE_WIDTH,
                            R.id.cover_list, mCarouselHolder, true, this);
                    setCarouselListSelection(true); // we need to guarantee that we have something selected once we move away from one D list
                    mZoomChanged = (oldLevel != mZoomLevel);
                    if (mMiniDetailsHolder != null)
                    {
                        mMiniDetailsHolder.setVisibility(View.GONE);
                    }

                    if (mZoomChanged)
                    {
                        onZoomChanged();
                        event.startTracking();
                    }
                    return true;
                }
                else
                { // in preparation for expanding the navbar (why isn't this in onFragmentExit()?)
                    if (mList.getSelectedItem() == null || mList.getSelectedItem() == mList.getItemAtPosition(0))
                    {
                        Resources res = getResources();
                        adjustTopMargin(res.getDimension(R.dimen.carousel_margin_top_unselected), true);
                    }
                    setCarouselListSelection(false);
                }
        }

        return false;
    }

    public final boolean keyUp(int keyCode, KeyEvent event)
    {
        // Only worry about zoom if we have loaded our data
        if (mZoomChanged)
        {
            mZoomChanged = false;
            return true;
        }

        return false;
    }

    private void showBreadcrumb(String title)
    {
        // TODO re-create our "Channel" concept (something along the lines of a carousel a title
        // We are trying to remove any link to Media Browse right nowm they should be plugged back
        // In another way, in MediaBrowseCarouselListFragment
        mBreadcrumb.setCategoryName(title);
        mBreadcrumb.setNumTitlesInCategory(getListAdapter().getItemSize(mList.getSelectedItemPosition() - mList.getHeaderViewsCount()));
        if (mList.getSelectedItem() != null)
        {
            /*final Channel selectedItem = (Channel) mList.getSelectedItem();
            if (selectedItem.getText() != null)
            {
                mBreadcrumb.setCategoryName(selectedItem.getText());
            }
            else
            {
                Log.e(TAG, "mList.getSelectedItem.getText is null in zoomLevel, breadcrumbs category name couldn't be set properly");
            }

            if (selectedItem.getItemIds() != null)
            {
                mBreadcrumb.setNumTitlesInCategory(selectedItem.getItemIds().size());
            }
            else
            {
                Log.e(TAG, "mList.getSelectedItem.getItemIds is null in zoomLevel, breadcrumbs # titles couldn't be set properly");
            }*/
        }
        else
        {
            Log.e(TAG, "mList.getSelectedItem is null in zoomLevel, breadcrumbs couldn't be set properly");
        }

        mBreadcrumb.setIconDrawable(getFragmentBreadCrumbIconDrawable());
        mBreadcrumb.setItemIndexInCategory(0);
        mBreadcrumb.displayUiNavigationStateWithSlideAnimation();
    }

    private void showBreadcrumb()
    {
        showBreadcrumb(getListAdapter().getItemTitle(mList.getSelectedItemPosition() - mList.getHeaderViewsCount()));
    }

    private void zoomTo1D(boolean animate)
    {
        if (!selectedViewInSync() || mList.getSelectedView() == null)
        {
            // When a user hits the Down key, the selected item position will be updated
            // immediately, but the selected view will not update until after a relayout pass. If
            // the Down key is followed too quickly by the Right key, without the relayout yet, then
            // the selected view will still be pointing to the previous row, and we'll end up
            // corrupting the container and the carousel. This is a rare case, so we will quietly
            // ignore the second key event and keep integrity of our state.
            Log.w(TAG, "zoomTo1D: mList.getSelectedView() out of date, ignoring.");
            return;
        }

        CarouselView<?> carousel = (CarouselView<?>) mList.getSelectedView().findViewById(R.id.cover_list);
        carousel.addItemSelectedListener(mItemSelectedRibbonHandler);
        carousel.addItemListListener(mItemSelectedRibbonHandler);
        carousel.addItemSelectedListener(mItemSelectedHandler);
        carousel.setOnItemClickListener(mItemSelectedHandler);
        carousel.addItemListListener(mItemSelectedHandler);

        CarouselZoom.TargetZoomState oldLevel = mZoomLevel;
        mZoomLevel = mCarouselZoom.zoomLevel(CarouselZoom.TargetZoomState.ONE_D_LIST,
                HERO_LARGE_WIDTH, R.id.cover_list, mCarouselHolder, animate, this);
        mZoomChanged = (oldLevel != mZoomLevel);
        if (mZoomChanged)
        {
            onZoomChanged();
        }

        if (mCarouselHolder.findViewById(R.id.cover_list) != carousel)
        {
            Log.e(getLogTag(), "Carousel for 1D out of sync from holder.");
        }
    }

    /**
     *  Interface implementations for CarouselZoomAnimationCallback
     */
    @Override
    public void onCarouselZoomInAnimationStart(CarouselView<?> carousel)
    {
        // Let's be absolute sure it's the right size and in the right spot.
        doSelection(carousel, 0, mCoverSelectedHeight, mTombSelectedTopMargin);
    }

    @Override
    public void onCarouselZoomInAnimationEnd(CarouselView<?> carousel)
    {
        if (mMiniDetailsHolder != null)
        { // Show the mini details
            mMiniDetailsHolder.setVisibility(View.VISIBLE);
        }

        // There is a "bug" in the carousel view that will not call the
        // item selected listener when calling set selection, so init
        // out item selected listener
        mItemSelectedHandler.onItemListChanged(carousel);
    }

    @Override
    public void onCarouselZoomOutAnimationStart(CarouselView<?> carousel)
    {
        carousel.removeItemListListener(mItemSelectedHandler);
        carousel.removeItemSelectedListener(mItemSelectedRibbonHandler);
        carousel.removeItemSelectedListener(mItemSelectedHandler);
    }

    @Override
    public void onCarouselZoomOutAnimationEnd(CarouselView<?> carousel)
    {
        // NO-OP
    }

    /**
     * Animates the top margin of the Fragment so it gives spaces for a logo when it's not selected
     *
     * @param marginSize - size of the top margin.
     */
    private void adjustTopMargin(float marginSize, boolean forceAnimation)
    {
        Resources res = getResources();
        float tombstoneMarginTop = res.getDimension(R.dimen.tombstone_margin_top);
        int newMargin = Math.max(0, Math.round(marginSize - tombstoneMarginTop));

//        if (hasControl() || forceAnimation)
//        {
//            mList.animate().setDuration(res.getInteger(R.integer.carousel_select_duration_ms))
//                           .setInterpolator(new AccelerateDecelerateInterpolator())
//                           .translationY(newMargin)
//                           .withLayer();
//        }
//        else
//        {
            mList.setTranslationY(newMargin);
//        }
    }

    /**
     * Call all needed animations.
     *
     * @param view - the row to be animated
     * @param leftMargin - space between left most part of the screen until the beginning of the row
     * @param coverHeight - target size of all covers inside the row
     * @param tombTopMargin - top margin of the tombstone for the given row
     */
    private void doSelection(View view, float leftMargin, int coverHeight, float tombTopMargin)
    {
        View lastRow = null;
        int count = mList.getChildCount();
//        int animDuration = getResources().getInteger(R.integer.category_list_transition_duration_ms);
        if (count > 0)
        {
            lastRow = mList.getChildAt(count - 1);
        }

        AdapterView<?> carouselList = (AdapterView<?>) view.findViewById(R.id.cover_list);
        if (carouselList != null)
        {
            LayoutParams params = carouselList.getLayoutParams();
            params.height = coverHeight;
            carouselList.setLayoutParams(params);
            if (view == lastRow)
            {
                mList.recalculateCurrentPosition();
            }
//            int height = carouselList.getLayoutParams().height;
//            ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(carouselList), height, coverHeight);
//            heightAnimator.setDuration(animDuration).setInterpolator(new AccelerateDecelerateInterpolator());
//            if (view == lastRow)
//            {
//                heightAnimator.addUpdateListener(new AnimatorUpdateListener()
//                {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animator)
//                    {
//                        mList.recalculateCurrentPosition();
//                    }
//                });
//            }
//            heightAnimator.start();
        }

        View tombstone = view.findViewById(R.id.tombstone);
        if (tombstone != null)
        {
            tombstone.setTranslationY(tombTopMargin);
//            tombstone.animate().setDuration(animDuration)
//                               .setInterpolator(new AccelerateDecelerateInterpolator())
//                               .translationY(tombTopMargin)
//                               .withLayer();
        }

        view.setTranslationX(leftMargin);
//        view.animate().setDuration(animDuration)
//                      .setInterpolator(new AccelerateDecelerateInterpolator())
//                      .translationX(leftMargin)
//                      .withLayer();
    }

    /**
     * Method to animate the selection of a row.
     *
     * @param hasSelection - inform if there is anything selected. This parameter is false when we
     *                       are returning to the main menu.
     */
    private void setCarouselListSelection(boolean hasSelection)
    {
        if (mSelectedRow != null)
        {
            doSelection(mSelectedRow, mUnselectedLeftMargin, mCoverUnselectedHeight, mTombUnselectedTopMargin);
        }

        if (!selectedViewInSync())
        {
            // When a user hits the Down key, the selected item position will be updated
            // immediately, but the selected view will not update until after a relayout pass. If
            // the Down key is followed too quickly by another Down, without the relayout yet, then
            // the selected view may still be pointing to the previous row, and we can do the
            // wrong thing below. So we will ignore this call and do the update on the next
            // selection change.
            Log.w(TAG, "setCarouselListSelection: mList.getSelectedView() out of date, ignoring.");
            return;
        }

        View selectedView = mList.getSelectedView();
        if (selectedView != null)
        {
            if (hasSelection)
            {
                doSelection(selectedView, mSelectedLeftMargin, mCoverSelectedHeight, mTombSelectedTopMargin);
            }
            else
            {
                doSelection(selectedView, mUnselectedLeftMargin, mCoverUnselectedHeight, mTombUnselectedTopMargin);
                selectedView = null;
            }
        }

        mSelectedRow = selectedView;
    }

    /**
     * When a user hits the Down key, the selected item position will be updated immediately, but
     * the selected view will not update until after a relayout pass. If the Down key is followed
     * too quickly by another key like Right or Down, without the relayout yet done, then the
     * selected view will still be pointing to the previous row, and we'll end up corrupting the
     * container and the carousel. This is a rare case, so we will quietly ignore the second key
     * event and keep integrity of our state.
     *
     * @return boolean true if we may safely use mList.getSelectedView()
     */
    private boolean selectedViewInSync()
    {
        int position = mList.getSelectedItemPosition();
        View selectedView = mList.getSelectedView();
        if (position < 0)
        {
            return selectedView == null;
        }

        if (selectedView == null || !selectedView.isShown())
        {
            return false;
        }

        CoverItemPagingCarouselView carousel = (CoverItemPagingCarouselView) selectedView.findViewById(R.id.cover_list);
        if (carousel == null)
        {
            return false;
        }

        return carousel == mListAdapter.getCarousel(position - mList.getHeaderViewsCount());
    }

    public final void onNewChannel(final ChannelData channelData)
    {
        restoreUiState(); // is the carousel we just loaded the one to select?
        ItemCarouselView carousel = channelData.getCarousel();
        carousel.setOnItemClickListener(mItemHandler);
        carousel.addItemSelectedListener(mItemHandler);
        carousel.setOnCategoryChangeListener(this);
        if (mListAdapter.getCount() == 1)
        { // with the first carousel loaded, we don't need the big spinner any more
            ProgressBar spinner = (ProgressBar) getView().findViewById(R.id.loading_spinner);
            spinner.setVisibility(View.GONE);
        }
    }

    public void onCategoryChanged(String newTitle)
    {
        if (newTitle.isEmpty())
        {
            showBreadcrumb();
        }
        else
        {
            showBreadcrumb(newTitle);
        }
    }
}
