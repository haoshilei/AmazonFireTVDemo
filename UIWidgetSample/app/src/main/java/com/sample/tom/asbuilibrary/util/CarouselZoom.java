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

package com.sample.tom.asbuilibrary.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.list.CarouselList;
import com.sample.tom.asbuilibrary.list.CarouselView;
import com.sample.tom.asbuilibrary.list.CoverItemPagingCarouselView;

import java.util.ArrayList;

/**
 * Utility class to transition from a CarouselList screen to a 1D list
 * view of the selected CarouselList item, and vice versa.
 */

public class CarouselZoom
{
    public enum TargetZoomState { CAROUSEL_LIST, ONE_D_LIST }
    private static final String TAG = CarouselZoom.class.getName();
    //private final int HERO_TILE_ID;
    private final int TOMBSTONE_ID;
    private final Context mContext;
    private final CarouselList mCarouselList;
    private final View mOneDListView;
    private final boolean mShowHeroImages;
    private int mZoomListYOffsetHero;
    private int mZoomListYOffsetTombstone;
    private int mSelectedCarouselItemOffsetAdjustmentHero;
    private int mSelectedCarouselItemOffsetAdjustmentTombstone;
    private int mCategoryListTransitionDuration;

    public CarouselZoom(Context context,
                        final CarouselList carouselList,
                        final View oneDListView,
                        final boolean showHeroImages,
                        /*int heroTileId,*/
                        int tombstoneId)
    {
        mContext = context;
        mCarouselList = carouselList;
        mOneDListView = oneDListView;
        //HERO_TILE_ID = heroTileId;
        TOMBSTONE_ID = tombstoneId;

        mShowHeroImages = showHeroImages;
        Resources resources = mContext.getResources();
        mCategoryListTransitionDuration = resources.getInteger(R.integer.category_list_transition_duration_ms);
        mZoomListYOffsetHero = resources.getInteger(R.integer.zoom_list_y_offset_hero);
        mZoomListYOffsetTombstone = resources.getInteger(R.integer.zoom_list_y_offset_tombstone);
        mSelectedCarouselItemOffsetAdjustmentHero = resources.getInteger(R.integer.selected_carousel_item_offset_adjustment_hero);
        mSelectedCarouselItemOffsetAdjustmentTombstone = resources.getInteger(R.integer.selected_carousel_item_offset_adjustment_tombstone);
    }

    /**
     * Get the currently set value for the duration of the animations.
     * @return offset in DP.
     */
    public int getCategoryListTransitionDuration()
    {
        return mCategoryListTransitionDuration;
    }

    /**
     * Adjust the duration of the animations.
     * @param duration
     */
    public void setCategoryListTransitionDuration(int duration)
    {
        mCategoryListTransitionDuration = duration;
    }

    /**
     * Get the currently set value for the Y offset for a Hero image used in the animations.
     * @return offset in DP.
     */
    public int getZoomListYOffsetHero()
    {
        return mZoomListYOffsetHero;
    }

    /**
     * Adjust the Y offset for a Hero image used in the animations.
     * @param offset
     */
    public void setZoomListYOffsetHero(int offset)
    {
        mZoomListYOffsetHero = offset;
    }


    /**
     * Get the currently set value for the Y offset for a tombstone used in the animations.
     * @return offset in DP.
     */
    public int getZoomListYOffsetTombstone()
    {
        return mZoomListYOffsetTombstone;
    }

    /**
     * Adjust the Y offset for a Tombstone used in the animations.
     * @param yOffset
     */
    public void setZoomListYOffsetTombstone(int yOffset)
    {
        mZoomListYOffsetTombstone = yOffset;
    }

    /**
     * Get the currently set value for the offset used
     * in displaying the selected CarouselList item when using Hero images
     * @return offset in DP.
     */
    public int getSelectedCarouselItemOffsetAdjustmentHero()
    {
        return mSelectedCarouselItemOffsetAdjustmentHero;
    }

    /**
     * Adjust the offset used in displaying the selected CarouselList item when using Hero images.
     * @param offset
     */
    public void setSelectedCarouselItemOffsetAdjustmentHero(int offset)
    {
        mSelectedCarouselItemOffsetAdjustmentHero = offset;
    }

    /**
     * Get the currently set value for the offset used
     * in displaying the selected CarouselList item when using Tombstones.
     * @return offset in DP.
     */
    public int getSelectedCarouselItemOffsetAdjustmentTombstone()
    {
        return mSelectedCarouselItemOffsetAdjustmentTombstone;
    }

    /**
     * Adjust the offset used in displaying the selected CarouselList item when using Tombstones.
     * @param offset
     */
    public void setSelectedCarouselItemOffsetAdjustmentTombstone(int offset)
    {
        mSelectedCarouselItemOffsetAdjustmentTombstone = offset;
    }


    private void zoomToCarouselList(ViewGroup carouselHolder,
                                    final CarouselZoomAnimationCallback callback,
                                    int coverListID,
                                    int heroLargeWidth)
    {
        final CarouselView<?> carousel = (CarouselView<?>) carouselHolder.findViewById(coverListID);
        if (carousel instanceof CoverItemPagingCarouselView)
        {
            ((CoverItemPagingCarouselView) carousel).logLoadStatus(false);
        }

        // if we have a zoom_list we are not in FullScreen, so call the Service to adjust
        // navbar's visibility
        ArrayList<ViewPropertyAnimator> animations = new ArrayList<ViewPropertyAnimator>();

        // Set up our carousel and list properly
        if (callback != null)
        {
            callback.onCarouselZoomOutAnimationStart(carousel);
        }
        carousel.setOnKeyListener(null);
        carousel.setOnItemClickListener(null);
        carousel.setSelection(0);
        if (mShowHeroImages)
        {
            carousel.setTranslationY(mZoomListYOffsetHero);
        }
        else
        {
            carousel.setTranslationY(mZoomListYOffsetTombstone);
        }

        carouselHolder.removeView(carousel);
        mCarouselList.setVisibility(View.VISIBLE);
        ViewGroup listItem = ((ViewGroup) mCarouselList.getSelectedView());
        listItem.addView(carousel);

        // Set up the hero animation
        /*View headerTile = null;
        if (mShowHeroImages)
        {
            headerTile = listItem.findViewById(HERO_TILE_ID);
        }
        else
        {
            headerTile = listItem.findViewById(TOMBSTONE_ID);
        }
        final View hero = headerTile;
        animations.add(hero.animate().scaleX(1f).scaleY(1f).alpha(1f));*/

        // Set up the carousel animation

        heroLargeWidth = heroLargeWidth - (int) carousel.getSelectionOffset() + (int) carousel.getSpacing();

        ViewPropertyAnimator carouselAnimation = carousel.animate().translationX(heroLargeWidth);
        animations.add(carouselAnimation);

        int count = mCarouselList.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = mCarouselList.getChildAt(i);
            animations.add(child.animate().translationY(0));
        }

        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        for (ViewPropertyAnimator animation : animations)
        {
            animation.setInterpolator(interpolator);
            animation.setDuration(mCategoryListTransitionDuration);
        }

        carouselAnimation.withEndAction(new Runnable()
        {
            /**
             * Starts executing the active part of the class' code. This method is called when a thread is started that has been
             * created with a class which implements {@code Runnable}.
             */
            @Override
            public void run()
            {
                mCarouselList.setEnabled(true);
                mCarouselList.requestFocus();

                mOneDListView.setVisibility(View.GONE);

                if (!mShowHeroImages)
                {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) carousel.getLayoutParams();
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    carousel.setLayoutParams(params);
                    carousel.setTranslationY(0);
                }
                if (callback != null)
                {
                    callback.onCarouselZoomOutAnimationEnd(carousel);
                }
            }
        });
    }


    private void zoomToOneDList(final CarouselZoomAnimationCallback callback, final ViewGroup carouselHolder,
            int coverListId, boolean animate)
    {
        int count = mCarouselList.getChildCount();
        // find the index of the selected view
        ViewGroup selectedChild = (ViewGroup) mCarouselList.getSelectedView();
        int selected = mCarouselList.indexOfChild(selectedChild);

        if (selected < 0)
        {
            // This should never happen. Check for it just in case.
            Log.e(TAG, "Bad carousel list state when attempting to zoom to 1d list");
            return;
        }

        ArrayList<ViewPropertyAnimator> animations = new ArrayList<ViewPropertyAnimator>();
        float offsetUp = 0;
        float offsetDown = 0;
        if (selected > 0)
        {
            View above = mCarouselList.getChildAt(selected - 1);
            offsetUp = above.getBottom();
        }

        if (selected < mCarouselList.getChildCount() - 1)
        {
            View below = mCarouselList.getChildAt(selected + 1);
            offsetDown = mCarouselList.getHeight() - below.getTop();
        }

        // add how top, bottom and selected carousels are going to animate away
        for (int i = 0; i < count; i++)
        {
            View child = mCarouselList.getChildAt(i);

            float offset = 0;
            if (i < selected)
            {
                // set how those above selected list will animate up
                offset -= offsetUp;
            }
            else if (i > selected)
            {
                // set how those below selected list will animate down
                offset += offsetDown;
            }
            else  // you are looking at the selected view
            {
                // how to animate the selected carousel list
                // TODO - there is a bug here where the first carousel item "jumps" when transitioning into or
                // TODO - out of the 1d list.  It appears that when we're on the first carousel item, the
                // TODO - mCarouselList.getHeight() call is returning 648 instead of 720 (which is the return when
                // TODO - any other rows are selected), and this appears to be causing the jump.  This isn't worth
                // TODO - holding up the whole checkin while I work to fix this bug.
                offset = (mCarouselList.getHeight() / 2) - (child.getHeight() / 2) - child.getTop();
                if (i == 0)
                {
                    // todo - remove
                    offset -= 40;
                }
                if (mShowHeroImages)
                {
                    offset -= mSelectedCarouselItemOffsetAdjustmentHero;
                }
                else
                {
                    offset -= mSelectedCarouselItemOffsetAdjustmentTombstone;
                }

                View childCarousel = child.findViewById(coverListId);
                if (childCarousel != null)
                {
                    if (childCarousel instanceof CoverItemPagingCarouselView)
                    {
                        ((CoverItemPagingCarouselView) childCarousel).logLoadStatus(true);
                    }
                    if (animate)
                    {
                        animations.add(childCarousel.animate().translationX(0));
                    }
                }
                else
                {
                    Log.e(TAG, "ack!! childCarousel is NULL!");
                }
            }

            if (child != null)
            {
                if (animate)
                {
                    animations.add(child.animate().translationY(offset));
                }
            }
            else
            {
                Log.e(TAG, "child is NULL!  That shouldn't happen!!!");
            }
        }

        mCarouselList.setEnabled(false);
        final CarouselView<?> carousel = (CarouselView<?>) selectedChild.findViewById(coverListId);
        if (callback != null)
        {
            callback.onCarouselZoomInAnimationStart(carousel);
        }

        /*View headerTile = null;
        if (mShowHeroImages)
        {
            headerTile = selectedChild.findViewById(HERO_TILE_ID);
        }
        else
        {
            headerTile = selectedChild.findViewById(TOMBSTONE_ID);
        }

        final View hero = headerTile;

        if (hero != null && animate)
        {
            animations.add(hero.animate().scaleX(0).scaleY(0).alpha(0));
        }*/

        //reparent the carousel so that we can move the list out of the way and bring in
        // the rest of the 1d list controls
        selectedChild.removeView(carousel);
        carouselHolder.addView(carousel);

        //give control to the carousel
        carousel.setSelection(0);
        carousel.requestFocus();
        Runnable runnableEnd = new Runnable()
        {
            /**
             * Starts executing the active part of the class' code. This method is called when a
             * thread is started that has been created with a class which implements {@code
             * Runnable}.
             */
            @Override
            public void run()
            {
                mCarouselList.setEnabled(false);
                mCarouselList.setVisibility(View.INVISIBLE);
                carousel.setTranslationY(0);
                if (callback != null)
                {
                    callback.onCarouselZoomInAnimationEnd(carousel);
                }
            }
        };

        if (animate)
        {
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            for (ViewPropertyAnimator animation : animations)
            {
                animation.setInterpolator(interpolator);
                animation.setDuration(mCategoryListTransitionDuration);
            }

            carousel.animate().withEndAction(runnableEnd);
        }
        else
        {
            runnableEnd.run();
        }

        mOneDListView.setVisibility(View.VISIBLE);
    }

    /**
     * Switch the zoom level to the specified target zoom state.
     *
     * @param targetZoomState   The end-goal state of the requested transition.
     * @param heroLargeWidth    The width (in pixels) of the Hero image or tombstone used in the CarouselList
     * @param coverListId       The resource ID of the Carousel portion of the selected CarouselList item.
     * @param carouselHolder    ViewGroup used to hold the 1d list.
     * @param callback          optional CarouselZoomAnimationCallback implementation,
     *                          used if there are necessary operations to be performed before or after the animations.
     * @return the target zoom state.
     */
    public TargetZoomState zoomLevel(TargetZoomState targetZoomState,
                                     int heroLargeWidth,
                                     int coverListId,
                                     ViewGroup carouselHolder,
                                     boolean animate,
                                     CarouselZoomAnimationCallback callback)
    {
        if (targetZoomState == TargetZoomState.CAROUSEL_LIST)
        {
            zoomToCarouselList(carouselHolder, callback, coverListId, heroLargeWidth);
        }
        else if (targetZoomState == TargetZoomState.ONE_D_LIST)
        {
            zoomToOneDList(callback, carouselHolder, coverListId, animate);
        }
        return targetZoomState;
    }
}
