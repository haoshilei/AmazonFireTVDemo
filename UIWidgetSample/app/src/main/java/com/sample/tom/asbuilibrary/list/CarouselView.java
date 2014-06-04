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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.list.handler.ItemListListener;

/**
 * Our base view for our Carousels. All 1d lists using this carousel view will
 * have the same feel when scrolling.
 */
public class CarouselView<AdapterType extends Adapter> extends
		AdapterView<AdapterType> implements ViewGroup.OnHierarchyChangeListener {
	protected static final int JUMP_SIZE = 5;
	protected AdapterType mAdapter;
	protected Hashtable<Integer, LinkedList<View>> mViewPools;
	protected View mSelectedView;
	protected float mSelectedViewMidpointXPos = Float.NaN; // NaN means 'not
															// set'
	protected int mLastSelectedPos = -1;
	protected int mLastItemCount = -1;
	// The index of the last view to be scaled up to mSelectedItemScale, or -1
	// if no view is currently scaled up. Note that a view can be the "selected"
	// view but *not* be scaled up. This variable is necessary to initialize the
	// scale of the selected view correctly when repopulating our child view
	// list.
	protected int mLastScaledPos = -1;
	protected String mName; // For debugging purposes only

	protected LayoutInitParams mLayoutInitParams;
	protected int mHeightMeasureSpec;
	protected boolean mLayoutRequested = false;
	protected boolean mInLayout = false;

	protected AdapterView.OnItemClickListener mItemClickListener;
	protected Set<OnItemSelectedListener> mItemSelectedListeners = new HashSet<OnItemSelectedListener>();
	protected Set<RegionOfInterestListener> mRegionOfInterestListeners = new HashSet<RegionOfInterestListener>();
	protected Set<ItemListListener> mItemListListeners = new HashSet<ItemListListener>();
	protected DataSetObserver mDataSetObserver = new CarouselDataSetObserver();

	protected int mRegionOfInterestTransactions = 0;
	protected int mRegionOfInterestStart = -1;
	protected int mRegionOfInterestEnd = -1;

	// CarouselView can support multiple levels of nesting. For this we store
	// the
	// state of current carousel on the following variables and stack them up
	// when
	// we need more nested levels.
	protected Stack<AdapterType> mNestedListAdapters;
	protected Stack<Integer> mNestedListSelectedPositions;
	protected Stack<AdapterView<AdapterType>> mNestedListContainerViews;

	// We need this flag to know that a back "press" unstack a nested level
	// instead of simple return to the previous screen.
	protected boolean mNestedListRestored = false;

	// Whether or not we should wrap when displaying the children. For example
	// if I have 100 children, and child 0 is in the center of the screen,
	// should child 99 be shown to the left of it? This isn't actually supported
	// for now, but there are certain uses of the carousel that may want this in
	// the future, e.g. the letter picker.
	protected boolean mWrap = false;

	// These should only be used in onKeyDown/onKeyUp
	protected boolean mLeftDown = false;
	protected boolean mRightDown = false;

	protected enum ScrollDir {
		None, Left, Right
	}

	/**
	 * SelectionAlignment controls how to align the selected item. When set to
	 * Left, the carousel aligns the left side of the selected item to the left
	 * side of the carousel. For Center, it aligns the center of the selected
	 * item to the center of the carousel. For Flex, it aligns the first item to
	 * the left, the last item to the right, and the middle elements to the
	 * center. The selection position can be further tweaked using the selection
	 * offset parameter.
	 */
	public enum SelectionAlignment {
		// Make sure the order of these values stays in sync with
		// ASBUiLibrary/res/values/attrs.xml
		Left, Center, Flex
	}

	protected Animation mAnim;
	protected LongScrollSelectorAnimation mLongScrollSelectorAnim;

	// TODO(steveT): This is redundant now that we have a getScrollDir
	// function. Remove.
	protected ScrollDir mScrollDir = ScrollDir.None;

	// TODO: Should be 0.2f according to UX
	protected float mTapScrollDuration = 0.25f;
	protected boolean mEnableLongScroll = true;
	protected float mLongScrollSpeed = 700;
	protected float mLongScrollTransitionDuration = 1.0f;
	protected float mScaleDuration = 0.1f;

	// TODO(steveT): You should be able to set the long scroll threshold to
	// cause an extra delay on the start of the long scroll selector anim, but
	// it currently doesn't do anything.
	protected float mLongScrollThreshold = 0.2f;
	protected float mSpacing = 20;
	protected float mMinSpacing = Float.NEGATIVE_INFINITY; // Setting a value <
															// 0 allows for
															// overlap
	protected float mSelectedItemScale = 1f;
	protected float mUnselectedItemScale = 1f;
	protected SelectionAlignment mSelectionAlignment = SelectionAlignment.Left;
	protected float mSelectionOffset = 0;
	protected float mListStartPadding = 0;
	protected float mListEndPadding = 0;
	/**
	 * The offset (in percent of width) that the "selected" position is
	 * considered at compared to the midpoint
	 */
	protected float mLongScrollSelectorTargetOffset = 0;
	protected boolean mResizingChildren = false;
	protected boolean mIgnoreWindowFocusForScale = false;
	protected boolean mJumpingAllowed = true;
	protected boolean mScaleAnimated = false;
	/**
	 * Maintain constant spacing when we shrink or enlarge items to indicate
	 * selected status
	 */
	protected boolean mConstantSpacing = false;

	protected boolean mDisableRecycling; // Temporarily disable
											// removal/recycling of non-visible
											// views

	protected HashMap<Integer, ScaleAnim> mScaleAnims = new HashMap<Integer, ScaleAnim>();

	private static final String TAG = "PastryView";

	protected boolean mDebugLongScroll = false;
	protected boolean mDebugChildPositioning = false;
	private Paint mDebugLongScrollPaint = new Paint();

	// Variables for framerate test
	public double mFramerateStartTime = -1;
	public double mFramerateLastFrameTime = -1;
	public int mFramerateNumFrames = 0;
	public int mNumLongestFramesToTrack = 10;
	public PriorityQueue<Integer> mLongestFrames = new PriorityQueue<Integer>();

	/**
	 * The LayoutInitParams can be used to control the carousel's behavior when
	 * initializing the layout
	 */
	public static class LayoutInitParams {
		// The new selected view. A negative value means don't change the
		// selected view.
		public int selectedPos = -1;
		public boolean resetScroll = true;
		public boolean forceItemSelectedNotification = false;
		public boolean startFramerateTest = false;

		public LayoutInitParams() {
		}

		public LayoutInitParams(int selectedPos) {
			this.selectedPos = selectedPos;
		}

		public LayoutInitParams(boolean resetScroll) {
			this.resetScroll = resetScroll;
		}

		public LayoutInitParams(int selectedPos, boolean resetScroll) {
			this.selectedPos = selectedPos;
			this.resetScroll = resetScroll;
		}
	}

	// The "region of interest" is a subrange of the item list that is either
	// currently visible or will soon become visible.
	public interface RegionOfInterestListener {
		// End is one past the last item (i.e. not inclusive)
		void onRegionOfInterestChanged(int start, int end);
	}

	public CarouselView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CarouselView(Context context, AttributeSet _attrs, int defStyle) {
		super(context, _attrs, defStyle);

		mViewPools = new Hashtable<Integer, LinkedList<View>>();
		mNestedListAdapters = new Stack<AdapterType>();
		mNestedListSelectedPositions = new Stack<Integer>();
		mNestedListContainerViews = new Stack<AdapterView<AdapterType>>();

		setDefaultParams();

		TypedArray attrs = context.obtainStyledAttributes(_attrs,
				R.styleable.CarouselView);
		mTapScrollDuration = attrs.getFloat(
				R.styleable.CarouselView_tap_scroll_duration,
				mTapScrollDuration);
		mEnableLongScroll = attrs.getBoolean(
				R.styleable.CarouselView_enable_long_scroll, mEnableLongScroll);
		mLongScrollSpeed = attrs.getFloat(
				R.styleable.CarouselView_long_scroll_speed, mLongScrollSpeed);
		mLongScrollTransitionDuration = attrs.getFloat(
				R.styleable.CarouselView_long_scroll_transition_duration,
				mLongScrollTransitionDuration);
		mLongScrollThreshold = attrs.getFloat(
				R.styleable.CarouselView_long_scroll_threshold,
				mLongScrollThreshold);
		mSpacing = attrs.getDimension(R.styleable.CarouselView_spacing,
				mSpacing);
		mMinSpacing = attrs.getDimension(R.styleable.CarouselView_minSpacing,
				mMinSpacing);
		mSelectedItemScale = attrs.getFloat(
				R.styleable.CarouselView_selected_item_scale,
				mSelectedItemScale);
		mUnselectedItemScale = attrs.getFloat(
				R.styleable.CarouselView_unselected_item_scale,
				mUnselectedItemScale);
		mSelectionAlignment = SelectionAlignment.values()[attrs.getInteger(
				R.styleable.CarouselView_selection_alignment,
				mSelectionAlignment.ordinal())];
		mListStartPadding = attrs.getDimension(
				R.styleable.CarouselView_list_start_padding, mListStartPadding);
		mListEndPadding = attrs.getDimension(
				R.styleable.CarouselView_list_end_padding, mListEndPadding);
		mLongScrollSelectorTargetOffset = attrs.getFloat(
				R.styleable.CarouselView_long_scroll_selector_target_offset,
				mLongScrollSelectorTargetOffset);
		mResizingChildren = attrs.getBoolean(
				R.styleable.CarouselView_resizing_children, mResizingChildren);
		mSelectionOffset = attrs.getDimension(
				R.styleable.CarouselView_selection_offset, mSelectionOffset);
		mJumpingAllowed = attrs.getBoolean(
				R.styleable.CarouselView_jumping_allowed, mJumpingAllowed);
		mScaleDuration = attrs.getFloat(
				R.styleable.CarouselView_scale_duration, mScaleDuration);
		mScaleAnimated = attrs.getBoolean(
				R.styleable.CarouselView_scale_animated, mScaleAnimated);
		mConstantSpacing = attrs.getBoolean(
				R.styleable.CarouselView_constant_spacing, mConstantSpacing);

		setWillNotDraw(false); // onDraw doesn't get called without this
		setChildrenDrawingOrderEnabled(true);

		super.setOnHierarchyChangeListener(this);
	}

	/**
	 * Subclasses can change the default parameters here and still allow
	 * overriding via xml
	 */
	protected void setDefaultParams() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatchSetSelected(boolean selected) {
		// We manage selection of our children, they shouldn't get selected when
		// we do.
	}

	@Override
	public AdapterType getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(AdapterType adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		mAdapter = adapter;

		// Changing the adapter represents a complete reset of the carousel
		// state. We need to clear out our children (and therefore notify region
		// of interest listeners), reset the selected view, notify item list
		// listeners that the item list changed, etc etc, and finally request a
		// layout pass.

		startRegionOfInterestTransaction();
		removeAllViewsInLayout();
		endRegionOfInterestTransaction();

		// We can't assume views can be recycled across different
		// adapters. Reset the view pool.
		mViewPools = new Hashtable<Integer, LinkedList<View>>();

		if (mAdapter != null) {
			for (int i = 0; i < mAdapter.getViewTypeCount(); i++) {
				mViewPools.put(i, new LinkedList<View>());
			}

			mAdapter.registerDataSetObserver(mDataSetObserver);
		}

		setSelectedView(null);

		for (ItemListListener listener : mItemListListeners) {
			listener.onItemListChanged(this);
		}

		clearScaleAnims();

		resetLayout(new LayoutInitParams(0));
	}

	// The base class getCount() always returns 0 for some reason
	@Override
	public int getCount() {
		return mAdapter == null ? 0 : mAdapter.getCount();
	}

	public String getName() {
		return mName == null ? Integer.toString(System.identityHashCode(this))
				: mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setWrap(boolean wrap) {
		mWrap = wrap;
	}

	public boolean getWrap() {
		return mWrap;
	}

	@Override
	public View getSelectedView() {
		return mSelectedView;
	}

	@Override
	public void setSelection(int position) {
		resetLayout(new LayoutInitParams(clampPosition(position)));
	}

	@Override
	public int getSelectedItemPosition() {
		return mSelectedView == null ? INVALID_POSITION
				: getViewPos(mSelectedView);
	}

	protected int clampPosition(int pos) {
		int max = mAdapter == null ? 0 : mAdapter.getCount() - 1;
		return Math.max(0, Math.min(max, pos));
	}

	/**
	 * Stores the listener to call when an item in the Carousel has been clicked
	 */
	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
		super.setOnItemClickListener(listener);
		mItemClickListener = listener;
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		throw new RuntimeException(
				"setOnItemSelectedListener not supported. Use addItemSelectedListener instead.");
	}

	public void addItemSelectedListener(OnItemSelectedListener listener) {
		mItemSelectedListeners.add(listener);
	}

	public void removeItemSelectedListener(OnItemSelectedListener listener) {
		mItemSelectedListeners.remove(listener);
	}

	// Returns an array with two integers representing the region of interest.
	public Pair<Integer, Integer> getRegionOfInterest() {
		if (mRegionOfInterestStart != -1) {
			// If mRegionOfInterestStart is set to a valid value, it means we're
			// currently in the middle of updating the region of
			// interest. Return the value from when we started the update.
			return new Pair<Integer, Integer>(mRegionOfInterestStart,
					mRegionOfInterestEnd);
		} else if (isEmpty()) {
			return new Pair<Integer, Integer>(0, 0);
		} else {
			return new Pair<Integer, Integer>(getViewPos(getFirstChild()),
					getViewPos(getLastChild()) + 1);
		}
	}

	public void addRegionOfInterestListener(RegionOfInterestListener listener) {
		mRegionOfInterestListeners.add(listener);
	}

	public void removeRegionOfInterestListener(RegionOfInterestListener listener) {
		mRegionOfInterestListeners.remove(listener);
	}

	public void addItemListListener(ItemListListener listener) {
		mItemListListeners.add(listener);
	}

	public void removeItemListListener(ItemListListener listener) {
		mItemListListeners.remove(listener);
	}

	@Override
	public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
		throw new RuntimeException("setOnHierarchyChangeListener not supported");
	}

	/**
	 * Call this when an item is removed from the adapter
	 */
	public void onAdapterItemRemoved(int pos) {
		// By calling resetLayout we're going to stop the position animations,
		// including scrolling. For now this is ok, but we'll need to fix this
		// in the future.
		resetLayout();

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (getViewPos(child) == pos) {
				removeViewInLayout(child);
				break;
			}
		}

		// Update the pos values attached to our child views
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int childPos = getViewPos(child);
			if (childPos > pos) {
				setViewPos(child, childPos - 1);
			}
		}

		// Update the scale anims, which we track by adapter position
		ArrayList<Integer> scalePositions = new ArrayList<Integer>(
				mScaleAnims.keySet());
		Collections.sort(scalePositions);
		for (Integer scaleAnimPos : scalePositions) {
			if (scaleAnimPos == pos) {
				// This item was deleted. Cancel the scale anim, which will
				// remove it from mScaleAnims.
				mScaleAnims.get(scaleAnimPos).cancel();
			} else if (scaleAnimPos > pos) {
				// This item's index has changed. Update its key in mScaleAnims.
				ScaleAnim anim = mScaleAnims.remove(scaleAnimPos);
				anim.setPos(scaleAnimPos - 1);
				mScaleAnims.put(scaleAnimPos - 1, anim);
			}
		}

		// Update mLastScaledPos
		if (mLastScaledPos == pos) {
			mLastScaledPos = -1;
		} else if (mLastScaledPos > pos) {
			mLastScaledPos--;
		}

		// If we're deleting the selected view select a new item. If we're not
		// changing the selected view, but its index is changing, reselect the
		// item to trigger the onItemSelected notifications.
		if (mSelectedView != null && getViewPos(mSelectedView) >= pos) {
			if (mAdapter.isEmpty()) {
				setSelectedView(null);
			} else {
				int newSelectedPos = Math.min(getViewPos(mSelectedView),
						mAdapter.getCount() - 1);
				fillViewsToTargetPos(newSelectedPos);
				setSelectedView(findView(newSelectedPos));
			}
		}
	}

	protected boolean isEmpty() {
		return getChildCount() == 0;
	}

	protected View getFirstChild() {
		if (isEmpty()) {
			throw new RuntimeException("No first child in empty PastryView");
		}
		return getChildAt(0);
	}

	protected View getLastChild() {
		if (isEmpty()) {
			throw new RuntimeException("No last child in empty PastryView");
		}
		return getChildAt(getChildCount() - 1);
	}

	public float getTapScrollDuration() {
		return mTapScrollDuration;
	}

	public void setTapScrollDuration(float tapScrollDuration) {
		mTapScrollDuration = tapScrollDuration;
	}

	public boolean getEnableLongScroll() {
		return mEnableLongScroll;
	}

	public void setEnableLongScroll(boolean enableLongScroll) {
		mEnableLongScroll = enableLongScroll;
	}

	public float getLongScrollSpeed() {
		return mLongScrollSpeed;
	}

	public void setLongScrollSpeed(float longScrollSpeed) {
		mLongScrollSpeed = longScrollSpeed;
	}

	public float getLongScrollTransitionDuration() {
		return mLongScrollTransitionDuration;
	}

	public void setLongScrollTransitionDuration(
			float longScrollTransitionDuration) {
		mLongScrollTransitionDuration = longScrollTransitionDuration;
	}

	public float getLongScrollThreshold() {
		return mLongScrollThreshold;
	}

	public void setLongScrollThreshold(float longScrollThreshold) {
		mLongScrollThreshold = longScrollThreshold;
	}

	public float getSpacing() {
		return mSpacing;
	}

	public void setSpacing(float spacing) {
		mSpacing = spacing;
	}

	/**
	 * Get the min spacing
	 */
	public float getMinSpacing() {
		return mMinSpacing;
	}

	/**
	 * Set the min spacing. When constant spacing is off, the min spacing value
	 * determines the minimum amount of spacing between two views when one is
	 * selected and scaled up.
	 */
	public void setMinSpacing(float minSpacing) {
		mMinSpacing = minSpacing;
	}

	/**
	 * Returns the scale selected items are set to
	 * 
	 * @return scale
	 */
	public float getSelectedItemScale() {
		return mSelectedItemScale;
	}

	/**
	 * Sets the scale unselected items are set to
	 */
	public void setSelectedItemScale(float selectedItemScale) {
		mSelectedItemScale = selectedItemScale;
	}

	/**
	 * Returns the scale unselected items are set to
	 * 
	 * @return scale
	 */
	public float getUnselectedItemScale() {
		return mUnselectedItemScale;
	}

	/**
	 * Sets the scale unselected items are set to
	 */
	public void setUnselectedItemScale(float unselectedItemScale) {
		mUnselectedItemScale = unselectedItemScale;
	}

	public SelectionAlignment getSelectionAlignment() {
		return mSelectionAlignment;
	}

	public void setSelectionAlignment(SelectionAlignment selectionAlignment) {
		mSelectionAlignment = selectionAlignment;
	}

	public float getListStartPadding() {
		return mListStartPadding;
	}

	public void setListStartPadding(float listStartPadding) {
		mListStartPadding = listStartPadding;
	}

	public float getListEndPadding() {
		return mListEndPadding;
	}

	public void setListEndPadding(float listEndPadding) {
		mListEndPadding = listEndPadding;
	}

	public float getLongScrollSelectorTargetOffset() {
		return mLongScrollSelectorTargetOffset;
	}

	public void setLongScrollSelectorTargetOffset(
			float longScrollSelectorTargetOffset) {
		mLongScrollSelectorTargetOffset = longScrollSelectorTargetOffset;
	}

	public boolean getResizingChildren() {
		return mResizingChildren;
	}

	public void setResizingChildren(boolean resizingChildren) {
		mResizingChildren = resizingChildren;
	}

	public void setIgnoreWindowFocusForScale(boolean ignoreWindowFocusForScale) {
		mIgnoreWindowFocusForScale = ignoreWindowFocusForScale;
	}

	/**
	 * Get the slected item offset
	 */
	public float getSelectionOffset() {
		return mSelectionOffset;
	}

	/**
	 * Set the selected item offset. This only applies when the carousel is
	 * center or left aligned. When left aligned the base selection position is
	 * at the start of the carousel, and when center aligned the base selection
	 * position is at the center of the carousel. So if the selection offset is
	 * 20 and the carousel is left aligned, the selected item will start at
	 * pixel 20 within the carousel. If the selection offset is 20 and the
	 * carousel is center aligned, the center of the selected item will be
	 * positioned 20 pixels to the right of the center of the carousel.
	 */
	public void setSelectionOffset(float selectionOffset) {
		mSelectionOffset = selectionOffset;
	}

	public boolean isJumpingAllowed() {
		return mJumpingAllowed;
	}

	public void setJumpingAllowed(boolean jumpingAllowed) {
		mJumpingAllowed = jumpingAllowed;
	}

	/**
	 * Returns the length of the scale animation in seconds
	 * 
	 * @return duration
	 */
	public float getScaleDuration() {
		return mScaleDuration;
	}

	/**
	 * Sets the length of the scale animation
	 * 
	 * @param scaleDuration
	 *            Duration in seconds
	 */
	public void setScaleDuration(float scaleDuration) {
		mScaleDuration = scaleDuration;
	}

	/**
	 * Returns if the carousel animates scale changes
	 * 
	 * @return true when animated
	 */
	public boolean isScaleAnimated() {
		return mScaleAnimated;
	}

	/**
	 * Animates scale changes when set to true
	 * 
	 * @param scaleAnimated
	 *            whether to animate scale changes
	 */
	public void setScaleAnimated(boolean scaleAnimated) {
		mScaleAnimated = scaleAnimated;
	}

	/**
	 * Returns whether or not constant spacing is enabled. Constant spacing
	 * means that we'll maintain the same spacing when we shrink or enlarge
	 * items to indicate selected status. We maintain constant spacing by moving
	 * other carousel items out of the way.
	 */
	public boolean getConstantSpacing() {
		return mConstantSpacing;
	}

	/**
	 * Enable or disable constant spacing
	 * 
	 * @param constantSpacing
	 *            Whether or not to enable constant spacing
	 */
	public void setConstantSpacing(boolean constantSpacing) {
		mConstantSpacing = constantSpacing;
	}

	/**
	 * Stack the current state and adapter of CarouselView and replace them by
	 * the adapter of the given view.
	 * 
	 * @param nestedListView
	 *            The current view that holds the next adapter to be used by the
	 *            CarouselView.
	 */
	@SuppressLint("UseValueOf")
	@SuppressWarnings("unchecked")
	protected void stackNestedListAdaptor(
			AdapterView<AdapterType> nestedListView) {
		int selectedPos = getViewPos(mSelectedView);
		AdapterType currentAdapter = getAdapter();
		AdapterType newAdapter = (AdapterType) nestedListView.getAdapter();
		mNestedListContainerViews.push(nestedListView);
		mNestedListAdapters.push(currentAdapter);
		mNestedListSelectedPositions.push(new Integer(selectedPos));
		nestedListView.setAdapter(null);
		setAdapter(newAdapter);
		setSelection(0);
	}

	/**
	 * Check if is there is nested levels to be unstacked and if it is, restore
	 * the previous stacked state and discard the current one.
	 * 
	 * @return True if the unstack happened, false otherwise.
	 */
	protected boolean unstackNestedListAdaptor() {
		boolean restored = false;
		if (!mNestedListAdapters.isEmpty()) {
			AdapterView<AdapterType> nestedListView = mNestedListContainerViews
					.pop();
			AdapterType adapter = mNestedListAdapters.pop();
			nestedListView.setAdapter(getAdapter());
			setAdapter(adapter);
			setSelection(mNestedListSelectedPositions.pop());
			restored = true;
		}
		return restored;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (unstackNestedListAdaptor()) {
				mNestedListRestored = true;
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (isEnabled()) {
				if (event.getRepeatCount() == 0) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if (getSelectedItemPosition() == 0 || isEmpty()) {
							if (!unstackNestedListAdaptor()) {
								return super.onKeyDown(keyCode, event);
							}
						}
						mLeftDown = true;
					} else {
						if (getSelectedItemPosition() == getCount() - 1) {
							return super.onKeyDown(keyCode, event);
						}
						mRightDown = true;
					}

					if (mLeftDown ^ mRightDown) {
						mScrollDir = mRightDown ? ScrollDir.Right
								: ScrollDir.Left;
						tapScroll();
					} else {
						finishScroll();
					}
				}
				return true;
			}
			break;

		case KeyEvent.KEYCODE_PAGE_UP:
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
			if (mJumpingAllowed) {
				setSelection(mSelectedView == null ? 0
						: getViewPos(mSelectedView) + JUMP_SIZE);
				return true;
			}
			break;

		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_MEDIA_REWIND:
			if (mJumpingAllowed) {
				setSelection(mSelectedView == null ? 0
						: getViewPos(mSelectedView) - JUMP_SIZE);
				return true;
			}
			break;

		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (mSelectedView != null) {
				View nestedView = mSelectedView
						.findViewById(R.id.carousel_nestedlist_item);
				if (nestedView != null && nestedView instanceof AdapterView<?>
						&& nestedView.getVisibility() == View.VISIBLE) {
					stackNestedListAdaptor((AdapterView<AdapterType>) nestedView);
				} else if (mItemClickListener != null) {
					int selectedPos = getViewPos(mSelectedView);
					mItemClickListener.onItemClick(this, mSelectedView,
							selectedPos, mAdapter.getItemId(selectedPos));
				}
			}
			return true;

		case KeyEvent.KEYCODE_F:
			LayoutInitParams params = new LayoutInitParams(0);
			params.startFramerateTest = true;
			resetLayout(params);
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (!isEnabled()) {
			return super.onKeyUp(keyCode, event);
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mNestedListRestored) {
				mNestedListRestored = false;
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				mLeftDown = false;
			} else {
				mRightDown = false;
			}

			// When we have both direction keys pressed (this is possible
			// on our remotes) and we release one, start scrolling.
			if (mLeftDown || mRightDown) {
				mScrollDir = mRightDown ? ScrollDir.Right : ScrollDir.Left;
				tapScroll();
			} else {
				finishScroll();
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = widthSize;
		int height = 0;
		int maxChildHeight = 0;

		// Measure all the children, and remember the max of all child heights
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			measureChild(child, heightMeasureSpec);
			maxChildHeight = Math.max(height, child.getMeasuredHeight());
		}

		if (heightMode == MeasureSpec.UNSPECIFIED
				|| heightMode == MeasureSpec.AT_MOST) {
			if (mAdapter == null || mAdapter.getCount() == 0) {
				height = 0; // Leave height as 0 if we have no items
			} else if (isEmpty()) {
				// Create a scrap view to measure to get the height
				View view = getView(0);
				height = view.getMeasuredHeight();
			} else {
				height = maxChildHeight;
			}

			float maxScale = Math.max(mSelectedItemScale, mUnselectedItemScale);
			if (maxScale > 1) {
				height *= maxScale;
			}
			height = Math.max(height, getSuggestedMinimumHeight());

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(heightSize, height);
			}
		} else // heightMode == MeasureSpec.EXACTLY
		{
			height = heightSize;
		}

		setMeasuredDimension(width, height);
		mHeightMeasureSpec = heightMeasureSpec;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// If our layout changed our children may have changed size. Force a
		// full layout reset so we don't use mSelectedViewMidpointXPos, which
		// may no longer be correct for resized children. This will break
		// animations.
		if (changed) {
			if (mLayoutInitParams == null) {
				mLayoutInitParams = new LayoutInitParams();
			}
			mLayoutInitParams.resetScroll = true;
		}

		mInLayout = true;
		mLayoutRequested = false;
		startRegionOfInterestTransaction();

		if (mLayoutInitParams != null || mSelectedView == null) {
			// Calls that we make in initLayout may cause a new layout request
			// and set mLayoutInitParams. So save off the current
			// mLayoutInitParams and set it to null before calling initLayout
			// instead of afterward.
			LayoutInitParams params = mLayoutInitParams;
			mLayoutInitParams = null;
			initLayout(params);
		} else {
			layoutChildren();
			incrementalLayout(mSelectedViewMidpointXPos);
			fixLayoutIfInvalid();
		}

		mInLayout = false;
		endRegionOfInterestTransaction();
	}

	// Get a new view, attempting to recycle from a view pool
	protected View getView(int position, boolean selected) {
		int type = mAdapter.getItemViewType(position);

		View recycleView = null;
		if (mViewPools.get(type).size() > 0) {
			recycleView = mViewPools.get(type).pop();
		}

		View child = mAdapter.getView(position, recycleView, this);
		// Remember what type the view is, in case the item itself changes
		child.setTag(R.id.carousel_view_view_type, type);
		setViewPos(child, position);
		child.setX(0);
		measureChild(child, mHeightMeasureSpec);
		layoutChild(child);
		applyViewStyle(child, selected);
		return child;
	}

	private View getView(int position) {
		boolean selected = mSelectedView != null
				&& getViewPos(mSelectedView) == position;
		return getView(position, selected);
	}

	protected float getCurrentViewScale(View child) {
		// If we're animating the scale, use the last anim value. Otherwise use
		// the selected state.
		int pos = getViewPos(child);
		ScaleAnim scaleAnim = mScaleAnims.get(pos);
		if (scaleAnim != null) {
			return scaleAnim.getCurrentScale();
		} else if (mLastScaledPos == pos) {
			return getSelectedScale(child);
		} else {
			return mUnselectedItemScale;
		}
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	protected void measureChild(View child, int heightMeasureSpec) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = generateDefaultLayoutParams();
			child.setLayoutParams(params);
		}

		int childWidthSpec;
		if (params.width > 0) {
			childWidthSpec = MeasureSpec.makeMeasureSpec(params.width,
					MeasureSpec.EXACTLY);
		} else {
			childWidthSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}

		int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0,
				params.height);

		child.measure(childWidthSpec, childHeightSpec);
	}

	protected void layoutChildren() {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			layoutChild(child);
		}
	}

	protected void layoutChild(View child) {
		int width = child.getMeasuredWidth();
		int height = child.getMeasuredHeight();
		int right = width;
		// TODO: Respect gravity
		int top = Math.max(0, (getHeight() - height) / 2);
		int bottom = top + height;
		child.setPivotX(scaleFromLeft() ? 0 : width / 2);
		child.setPivotY(height / 2);

		// When we call layout on the child we'll erase the isLayoutRequested
		// flag, which will prevent us from triggering a full layout pass if we
		// later add this child to the carousel. If we're not currently in a
		// layout pass, reapply the isLayoutRequested flag to the child after we
		// call layout on it.
		boolean layoutRequested = child.isLayoutRequested();
		child.layout(0, top, right, bottom);
		if (!mInLayout && layoutRequested) {
			child.requestLayout();
		}
	}

	protected void addViewInternal(View child, int index) {
		if (isEmpty()
				|| child.isLayoutRequested()
				|| child.getMeasuredHeight() != getChildAt(0)
						.getMeasuredHeight()) {
			postOrRequestLayout();
		}
		addViewInLayout(child, index, child.getLayoutParams());
	}

	public void onChildViewAdded(View parent, View child) {
	}

	public void onChildViewRemoved(View parent, View child) {
		// Recycle the unused view
		if (mAdapter != null) {
			int type = getItemViewType(child);
			mViewPools.get(type).push(child);
		}
	}

	protected int getItemViewType(View child) {
		// Use the remembered type to ensure it goes back in the correct pool
		// even if the item type has changed
		return (Integer) child.getTag(R.id.carousel_view_view_type);
	}

	protected void postOrRequestLayout() {
		if (mLayoutRequested) {
			return;
		}

		mLayoutRequested = true;

		if (mInLayout) {
			// If we're in onLayout a requestLayout call will be ignored. Post
			// it instead.
			post(new Runnable() {
				@Override
				public void run() {
					requestLayout();
				}
			});
		} else {
			requestLayout();
		}
	}

	/**
	 * Force a layout initialization instead of doing an incremental layout
	 */
	public void resetLayout(LayoutInitParams params) {
		mLayoutInitParams = params;
		postOrRequestLayout();
	}

	/**
	 * Force a layout initialization instead of doing an incremental layout
	 */
	public void resetLayout() {
		resetLayout(new LayoutInitParams());
	}

	protected void initLayout(LayoutInitParams layoutInitParams) {
		startRegionOfInterestTransaction();

		if (layoutInitParams == null) {
			layoutInitParams = new LayoutInitParams();
		}

		boolean resetScroll = layoutInitParams.resetScroll
				|| Float.isNaN(mSelectedViewMidpointXPos);

		removeAllViewsInLayout();
		if (resetScroll) {
			mSelectedViewMidpointXPos = Float.NaN;
			mScrollDir = ScrollDir.None;
			mAnim = null;
			mLongScrollSelectorAnim = null;
		}

		if (mAdapter != null && !mAdapter.isEmpty()) {
			int selectedPos = mSelectedView == null ? 0
					: getViewPos(mSelectedView);
			if (layoutInitParams.selectedPos >= 0) {
				selectedPos = layoutInitParams.selectedPos;
			}
			selectedPos = Math.max(0,
					Math.min(mAdapter.getCount() - 1, selectedPos));
			setSelectedView(getView(selectedPos, true), false,
					layoutInitParams.forceItemSelectedNotification);

			if (resetScroll) {
				incrementalLayout(getChildSelectedPos(mSelectedView));
			} else {
				incrementalLayout(mSelectedViewMidpointXPos);
			}

			fixLayoutIfInvalid();
		} else {
			setSelectedView(null, false,
					layoutInitParams.forceItemSelectedNotification);
			mSelectedViewMidpointXPos = Float.NaN;
		}

		if (layoutInitParams.startFramerateTest) {
			Log.d(TAG, "starting framerate test");
			mFramerateStartTime = currentTime();
			mFramerateLastFrameTime = -1;
			mFramerateNumFrames = 0;
			mLongestFrames.clear();
			mScrollDir = ScrollDir.Right;
			startLongScrollAnim();
		}

		invalidate();
		endRegionOfInterestTransaction();
	}

	protected float getChildSelectedPos(View child) {
		if (!isSelectionFlexAligned() || child == null) {
			return getSelectedItemMidpoint(child);
		} else {
			startRegionOfInterestTransaction();
			mDisableRecycling = true;

			View origSelectedView = mSelectedView;
			float origSelectedViewMidpointXPos = mSelectedViewMidpointXPos;

			// Start by positioning the target view in the middle
			mSelectedView = child;
			incrementalLayout(getSelectedItemMidpoint());
			// Align left/right as necessary
			alignRight();
			alignLeft();
			float childSelectedPos = mSelectedViewMidpointXPos;

			mSelectedView = origSelectedView;
			mSelectedViewMidpointXPos = origSelectedViewMidpointXPos;
			incrementalLayout(mSelectedViewMidpointXPos);

			endRegionOfInterestTransaction();
			mDisableRecycling = false;
			return childSelectedPos;
		}
	}

	protected void alignLeft() {
		View firstChild = getFirstChild();
		float startX = firstChild.getX();
		boolean firstItem = getViewPos(firstChild) == 0;
		if (startX > mListStartPadding && firstItem) {
			// Shift left
			float delta = mListStartPadding - startX;
			incrementalLayout(mSelectedViewMidpointXPos + delta);
		}
	}

	protected void alignRight() {
		View lastChild = getLastChild();
		float endX = lastChild.getX() + lastChild.getWidth();
		boolean lastItem = getViewPos(lastChild) == mAdapter.getCount() - 1;
		if (endX < getWidth() - mListEndPadding && lastItem) {
			// Shift right
			float delta = getWidth() - mListEndPadding - endX;
			incrementalLayout(mSelectedViewMidpointXPos + delta);
		}
	}

	protected void incrementalLayout(float selectedViewMidpointXPos) {
		if (mAdapter == null || mAdapter.isEmpty()) {
			return;
		}

		startRegionOfInterestTransaction();

		mSelectedViewMidpointXPos = selectedViewMidpointXPos;
		if (Float.isNaN(mSelectedViewMidpointXPos)) {
			mSelectedViewMidpointXPos = getSelectedItemMidpoint();
		}

		int selectedChildIndex = getSelectedChildIndex();
		if (selectedChildIndex < 0) {
			removeAllViewsInLayout();
			addViewInternal(mSelectedView, 0);
		}

		positionViews();

		recycleStaleViews();
		fillViews();

		endRegionOfInterestTransaction();
	}

	protected boolean isSelectionLeftAligned() {
		return mSelectionAlignment == SelectionAlignment.Left;
	}

	protected boolean isSelectionCenterAligned() {
		return mSelectionAlignment == SelectionAlignment.Center;
	}

	protected boolean isSelectionFlexAligned() {
		return mSelectionAlignment == SelectionAlignment.Flex;
	}

	protected boolean scaleFromCenter() {
		// For now we don't have any scenarios where we scale from the left, but
		// we may want this in the future
		return true;
	}

	protected boolean scaleFromLeft() {
		return !scaleFromCenter();
	}

	protected float getChildMargin(View child) {
		float scale = mConstantSpacing ? child.getScaleX()
				: mUnselectedItemScale;
		float totalMargin = child.getWidth() * (1f - scale);
		return scaleFromLeft() ? totalMargin : totalMargin / 2;
	}

	protected float getChildSpacing(View child, boolean right,
			View adjacentChild) {
		if (scaleFromLeft()) {
			return right ? mSpacing - getChildMargin(child) : mSpacing
					- getChildMargin(adjacentChild);
		} else {
			return mSpacing - getChildMargin(child)
					- getChildMargin(adjacentChild);
		}
	}

	protected float getSelectedScale(View child) {
		if (mConstantSpacing) {
			return mSelectedItemScale;
		} else {
			float unselectedWidth = mUnselectedItemScale * child.getWidth();
			float defaultSelectedWidth = mSelectedItemScale * child.getWidth();
			float widthDelta = (defaultSelectedWidth - unselectedWidth);
			float availableSpace = scaleFromCenter() ? 2 * mSpacing : mSpacing;
			float selectedSpacing = clamp(availableSpace - widthDelta,
					mMinSpacing, availableSpace);
			float selectedWidth = unselectedWidth + availableSpace
					- selectedSpacing;
			float selectedScale = selectedWidth / child.getWidth();
			return selectedScale;
		}
	}

	protected void positionViews() {
		if (mSelectedView == null) {
			return;
		}

		int selectedChildIndex = Math.max(getSelectedChildIndex(), 0);
		mSelectedView.setX(mSelectedViewMidpointXPos - mSelectedView.getWidth()
				/ 2);

		for (int i = selectedChildIndex - 1; i >= 0; i--) {
			View current = getChildAt(i);
			View right = getChildAt(i + 1);
			float spacing = getChildSpacing(current, true, right);
			current.setX(right.getX() - spacing - current.getWidth());
		}

		for (int i = selectedChildIndex + 1; i < getChildCount(); i++) {
			View current = getChildAt(i);
			View left = getChildAt(i - 1);
			float spacing = getChildSpacing(current, false, left);
			current.setX(left.getX() + left.getWidth() + spacing);
		}
	}

	private int getSelectedChildIndex() {
		if (mSelectedView == null) {
			throw new RuntimeException("No selected view");
		}

		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) == mSelectedView) {
				return i;
			}
		}

		return -1;
	}

	private float getStaleLeftBound() {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			View child = getChildAt(i);
			if (child.getX() + child.getWidth() < 0) {
				return child.getX() + child.getWidth();
			}
		}

		return 0;
	}

	private float getStaleRightBound() {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getX() > getWidth()) {
				return child.getX();
			}
		}

		return getWidth();
	}

	private void recycleStaleViews() {
		if (mSelectedView == null || mDisableRecycling) {
			return;
		}

		startRegionOfInterestTransaction();

		// Only recycle a view if it's (a) not in our scroll direction, (b)
		// not in the visible area, and (c) not between the visible area and the
		// selected view
		float leftSelected = mSelectedView.getX();
		float rightSelected = mSelectedView.getX() + mSelectedView.getWidth();
		ScrollDir animScrollDir = mAnim == null ? ScrollDir.None : mAnim
				.getTotalScrollDir();
		float leftBound = Math.min(
				animScrollDir != ScrollDir.Right ? Float.NEGATIVE_INFINITY
						: getStaleLeftBound(), leftSelected);
		float rightBound = Math.max(
				animScrollDir != ScrollDir.Left ? Float.POSITIVE_INFINITY
						: getStaleRightBound(), rightSelected);

		for (int i = 0; i < getChildCount();) {
			View child = getChildAt(i);
			float left = child.getX();
			float right = left + child.getWidth();
			if (right < leftBound || left > rightBound) {
				removeViewsInLayout(i, 1);
			} else {
				i++;
			}
		}

		endRegionOfInterestTransaction();
	}

	// Resizing child views can cause our layout to become invalid. Even without
	// the expanding mini-details child views can get resized e.g. when we're
	// showing a spinner and then we get the cover downloaded. We don't know the
	// cover size until its downloaded so the item changes size.
	private void fixLayoutIfInvalid() {
		if (mAnim != null || mSelectedView == null) {
			// If we're animating let the animation finish before fixing the
			// layout
			return;
		}

		startRegionOfInterestTransaction();
		mDisableRecycling = true;
		float originalPos = mSelectedViewMidpointXPos;

		// If the selected view is partially offscreen, move it
		float left = mSelectedView.getX();
		float right = mSelectedView.getX() + mSelectedView.getWidth();
		float leftBound = getLeftBound();
		float rightBound = getRightBound();
		if (left < leftBound || right > rightBound) {
			float delta = left < leftBound ? leftBound - left : rightBound
					- right;
			if (delta > 0.5f) {
				incrementalLayout(mSelectedViewMidpointXPos + delta);
			}
		}

		if (isSelectionFlexAligned()) {
			// Make sure we're aligned correctly
			alignRight();
			alignLeft();
		}

		endRegionOfInterestTransaction();
		mDisableRecycling = false;
		if (Math.abs(originalPos - mSelectedViewMidpointXPos) > 0.5f) {
			// We moved. Invalidate to make sure we get redrawn.
			invalidate();
		}
	}

	private int getViewPos(View view) {
		return (Integer) view.getTag(R.id.carousel_view_item_tag);
	}

	protected void setViewPos(View view, int pos) {
		view.setTag(R.id.carousel_view_item_tag, pos);
	}

	private void fillViews() {
		if (mSelectedView == null || getChildCount() == 0) {
			return;
		}

		startRegionOfInterestTransaction();

		// Fill left
		View first = getFirstChild();
		while ((first.getX() + first.getWidth() > 0) && getViewPos(first) > 0) {
			View view = getView(getViewPos(first) - 1);
			float spacing = getChildSpacing(view, true, first);
			view.setX(first.getX() - spacing - view.getWidth());
			addViewInternal(view, 0);
			first = view;
		}

		// Fill right
		View last = getLastChild();
		while (last.getX() < getWidth()
				&& getViewPos(last) < (mAdapter.getCount() - 1)) {
			View view = getView(getViewPos(last) + 1);
			float spacing = getChildSpacing(view, false, last);
			view.setX(last.getX() + last.getWidth() + spacing);
			addViewInternal(view, getChildCount());
			last = view;
		}

		endRegionOfInterestTransaction();
	}

	private void fillViewsToTargetPos(int targetPos) {
		if (mSelectedView == null) {
			return;
		}

		startRegionOfInterestTransaction();

		if (getChildCount() == 0) {
			addViewInternal(getView(targetPos), 0);
		}

		int firstPos = getViewPos(getFirstChild());
		int lastPos = getViewPos(getLastChild());
		if (targetPos < firstPos) {
			// Fill left
			for (int i = firstPos - 1; i >= targetPos; i--) {
				View right = getFirstChild();
				View current = getView(i);
				float spacing = getChildSpacing(current, true, right);
				current.setX(right.getX() - spacing - current.getWidth());
				addViewInternal(current, 0);
			}
		} else if (targetPos > lastPos) {
			// Fill right
			for (int i = lastPos + 1; i <= targetPos; i++) {
				View left = getLastChild();
				View current = getView(i);
				float spacing = getChildSpacing(current, false, left);
				current.setX(left.getX() + left.getWidth() + spacing);
				addViewInternal(current, getChildCount());
			}
		}

		endRegionOfInterestTransaction();
	}

	private void startRegionOfInterestTransaction() {
		if (mRegionOfInterestTransactions == 0 && !isEmpty()) {
			mRegionOfInterestStart = getViewPos(getFirstChild());
			mRegionOfInterestEnd = getViewPos(getLastChild()) + 1;
		}
		mRegionOfInterestTransactions++;
	}

	private void endRegionOfInterestTransaction() {
		mRegionOfInterestTransactions--;
		if (mRegionOfInterestTransactions == 0) {
			int newRegionOfInterestStart = 0, newRegionOfInterestEnd = 0;
			if (!isEmpty()) {
				newRegionOfInterestStart = getViewPos(getFirstChild());
				newRegionOfInterestEnd = getViewPos(getLastChild()) + 1;
			}

			if (newRegionOfInterestStart != mRegionOfInterestStart
					|| newRegionOfInterestEnd != mRegionOfInterestEnd) {
				mRegionOfInterestStart = -1;
				mRegionOfInterestEnd = -1;
				for (RegionOfInterestListener listener : mRegionOfInterestListeners) {
					listener.onRegionOfInterestChanged(
							newRegionOfInterestStart, newRegionOfInterestEnd);
				}
			}

			mRegionOfInterestStart = -1;
			mRegionOfInterestEnd = -1;
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (mSelectedView == null) {
			return super.getChildDrawingOrder(childCount, i);
		}

		// It's possible for items to overlap each other. We draw the selected
		// item last so that it shows up on top of the other items.

		int selectedIndex = indexOfChild(mSelectedView);
		if (i < selectedIndex) {
			return i;
		} else if (i < childCount - 1) {
			return childCount - 1 - (i - selectedIndex);
		} else {
			return selectedIndex;
		}
	}

	protected void drawLongScrollSelectorDebugRect(Canvas canvas, float center,
			int color) {
		float rectWidth = 3;
		mDebugLongScrollPaint.setColor(color);
		canvas.drawRect(center - 0.5f * rectWidth, 0,
				center + 0.5f * rectWidth, getHeight(), mDebugLongScrollPaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		updateAnim();

		// We may have animated to an invalid spot. Fix an invalid layout if
		// necessary.
		fixLayoutIfInvalid();

		super.onDraw(canvas);

		if (isAnimating()) {
			invalidate();
		}

		updateFramerateTest();

		if (mDebugChildPositioning) {
			drawChildPositioning(canvas);
		}

		if (mDebugLongScroll) {
			drawLongScrollSelector(canvas);
		}
	}

	protected void drawChildPositioning(Canvas canvas) {
		drawLongScrollSelectorDebugRect(canvas, 0, Color.YELLOW);

		float targetPoint = isSelectionLeftAligned() ? 0 : getWidth() / 2;
		targetPoint += mSelectionOffset;
		drawLongScrollSelectorDebugRect(canvas, targetPoint, Color.BLUE);

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getTag(R.id.carousel_view_debug_color) == null) {
				int randomColor = Color.rgb((int) (Math.random() * 255),
						(int) (Math.random() * 255),
						(int) (Math.random() * 255));
				child.setTag(R.id.carousel_view_debug_color, randomColor);
			}

			int color = (Integer) child.getTag(R.id.carousel_view_debug_color);
			drawLongScrollSelectorDebugRect(canvas, child.getX(), color);
			drawLongScrollSelectorDebugRect(canvas,
					child.getX() + child.getWidth(), color);
		}
	}

	protected void drawLongScrollSelector(Canvas canvas) {
		if (mLongScrollSelectorAnim != null) {
			double time = currentTime();
			drawLongScrollSelectorDebugRect(canvas,
					getLongScrollSelectorTargetPos(getScrollDir()),
					Color.YELLOW);
			if (mLongScrollSelectorAnim.isActive(time)) {
				drawLongScrollSelectorDebugRect(canvas,
						mLongScrollSelectorAnim.getStartPos(), Color.BLUE);
				drawLongScrollSelectorDebugRect(canvas,
						mLongScrollSelectorAnim.getEndPos(), Color.GREEN);
			}
			drawLongScrollSelectorDebugRect(canvas,
					mLongScrollSelectorAnim.getPos(time), Color.RED);
		}
	}

	private void updateFramerateTest() {
		if (mFramerateStartTime > 0) {
			if (mFramerateLastFrameTime > 0) {
				double frameTime = currentTime() - mFramerateLastFrameTime;
				int ms = (int) (frameTime * 1000);
				if (mLongestFrames.size() < mNumLongestFramesToTrack) {
					mLongestFrames.add(ms);
				} else if (ms > mLongestFrames.peek()) {
					mLongestFrames.poll();
					mLongestFrames.add(ms);
				}
			}

			mFramerateLastFrameTime = currentTime();
			mFramerateNumFrames++;

			if (!isAnimating()) {
				// Print stats
				ArrayList<Integer> framesSorted = new ArrayList<Integer>(
						mLongestFrames);
				Collections.sort(framesSorted);
				Collections.reverse(framesSorted);

				String longFramesStr = "";
				float longFramesTotal = 0;
				for (int i = 0; i < framesSorted.size(); i++) {
					longFramesStr += framesSorted.get(i);
					if (i != framesSorted.size() - 1) {
						longFramesStr += " ";
					}
					longFramesTotal += framesSorted.get(i);
				}
				float longFramesAvg = longFramesTotal / framesSorted.size();

				double elapsedTime = currentTime() - mFramerateStartTime;
				Log.d(TAG, String.format("time=%f num_frames=%d avg_fps=%f",
						elapsedTime, mFramerateNumFrames, (mFramerateNumFrames)
								/ elapsedTime));

				String longFramesMsg = String.format(
						"%d longest frames (ms): %s, Avg: %.1f",
						mNumLongestFramesToTrack, longFramesStr, longFramesAvg);
				Log.d(TAG, longFramesMsg);
				Toast.makeText(getContext(), longFramesMsg, Toast.LENGTH_LONG)
						.show();
				mFramerateStartTime = -1;
			}
		}
	}

	private View findView(int pos) {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (getViewPos(child) == pos) {
				return child;
			}
		}
		return null;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		if (!gainFocus) {
			finishScroll();

			// Be sure to clear our key state, otherwise when we regain focus
			// we'll think keys are still down when they're not
			mLeftDown = false;
			mRightDown = false;
		}

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			applyViewStyle(child, child == mSelectedView);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		if (!hasWindowFocus) {
			finishScroll();

			// Be sure to clear our key state, otherwise when we regain focus
			// we'll think keys are still down when they're not
			mLeftDown = false;
			mRightDown = false;
		}

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			applyViewStyle(child, child == mSelectedView);
		}
	}

	protected boolean isLongScrollAnim() {
		return mAnim != null && mAnim.getType() == Animation.Type.LONG_SCROLL;
	}

	protected float getLongScrollSelectorTargetPos(ScrollDir scrollDir) {
		float targetOffset = scrollDir == ScrollDir.Left ? (-getLongScrollSelectorTargetOffset())
				: getLongScrollSelectorTargetOffset();
		return getSelectedItemMidpoint() + targetOffset * getWidth();
	}

	private void startAnim(Animation anim) {
		double time = anim.getStartTime();

		// Finish the previous animation
		finishAnim(time);
		mAnim = anim;
		invalidate();
	}

	// Plays out the current animation up to the given time, then sets mAnim to
	// null so it doesn't apply anymore.
	//
	// Normally the animations are updated on every rendered frame, but the
	// animations change according to user input events, which happen in between
	// frames. Before starting a new animation we call finishAnim to make sure
	// we apply the time that's passed since the last frame.
	private void finishAnim(double time) {
		if (mAnim != null) {
			float newPos = mAnim.eval(time);
			incrementalLayout(newPos);
		}
		mAnim = null;
	}

	public void cancelAnim() {
		if (mAnim == null) {
			return;
		}

		finishScroll();
		finishAnim(mAnim.getStartTime() + mAnim.getDuration());
	}

	protected void finishScroll() {
		mScrollDir = ScrollDir.None;
		if (mLongScrollSelectorAnim != null) {
			boolean selectorAnimAhead = false;
			if (mAnim != null
					&& mAnim.getType() == Animation.Type.APPROACH_TARGET) {
				float endPos = mAnim.eval(mAnim.getStartTime()
						+ mAnim.getDuration());
				ScrollDir scrollDir = getScrollDir();
				selectorAnimAhead = (scrollDir == ScrollDir.Right && endPos > getSelectedItemMidpoint() + 0.5f)
						|| (scrollDir == ScrollDir.Left && endPos < getSelectedItemMidpoint() - 0.5f);
			}
			if (mAnim == null || isLongScrollAnim() || selectorAnimAhead) {
				longScrollApproachTargetAnim();
			}

			mLongScrollSelectorAnim = null;
		}
	}

	ScrollDir getScrollDir() {
		// Make sure to check the selector anim first. It'll be moving in the
		// user specified direction before the pos animation, which can lag
		// behind a bit as it transitions directions.
		if (mLongScrollSelectorAnim != null) {
			return mLongScrollSelectorAnim.getScrollDir();
		} else if (mAnim != null) {
			return mAnim.getTotalScrollDir();
		}
		return ScrollDir.None;
	}

	public boolean scrollingRight() {
		return getScrollDir() == ScrollDir.Right;
	}

	public boolean scrollingLeft() {
		return getScrollDir() == ScrollDir.Left;
	}

	// Returns a numeric value indicating the scroll direction. No scrolling -->
	// 0, right --> 1, and left --> -1. This is useful for certain math
	// operations based on the scroll direction.
	private int getScrollDirFactor(ScrollDir scrollDir) {
		if (scrollDir == ScrollDir.None) {
			return 0;
		} else if (scrollDir == ScrollDir.Right) {
			return 1;
		} else {
			return -1;
		}
	}

	private void tapScroll() {
		if (mSelectedView == null || mAdapter == null) {
			// We're not setup properly
			return;
		}

		View previousSelectedView = mSelectedView;

		boolean right = mScrollDir == ScrollDir.Right;
		int pos = getViewPos(mSelectedView);
		if ((pos == 0 && !right) || (pos == mAdapter.getCount() - 1 && right)) {
			// Already at the boundary
			return;
		}

		int target = pos + getScrollDirFactor(mScrollDir);
		fillViewsToTargetPos(target);
		setSelectedView(findView(target));

		float carouselMid = getSelectedItemMidpoint();
		float previousSelectedViewTargetPos = getChildSelectedPos(previousSelectedView);
		float newSelectedViewTargetPos = getChildSelectedPos(mSelectedView);
		boolean approachingEnd = right ? newSelectedViewTargetPos > carouselMid
				: newSelectedViewTargetPos < carouselMid;

		float selectorStartPos = getViewMidpoint(previousSelectedView);
		float selectorEndPos;
		if (approachingEnd) {
			selectorEndPos = getScrollEndViewMidpoint(mScrollDir);
		} else {
			float selectorTargetDistance = getLongScrollSelectorTargetPos(mScrollDir)
					- newSelectedViewTargetPos;
			float selectorTargetPos = mSelectedViewMidpointXPos
					+ selectorTargetDistance;
			selectorEndPos = right ? Math.max(mSelectedViewMidpointXPos,
					selectorTargetPos) : Math.min(mSelectedViewMidpointXPos,
					selectorTargetPos);
		}

		mLongScrollSelectorAnim = new LongScrollSelectorAnimation(
				!approachingEnd, currentTime(), selectorStartPos,
				selectorEndPos, getScrollDirFactor(mScrollDir)
						* mLongScrollSpeed);
		invalidate();

		float offset = getViewMidpoint(mSelectedView)
				- getViewMidpoint(previousSelectedView);
		if (Math.abs((previousSelectedViewTargetPos + offset)
				- newSelectedViewTargetPos) < 1) {
			// If the new animation will move us to the same position as the
			// current animation, don't reset the animation
			return;
		}

		approachTargetAnim(newSelectedViewTargetPos,
				getAdjustedTapScrollDuration(newSelectedViewTargetPos),
				mTapScrollDuration);
	}

	private Animation getLongScrollStartAnim(double startTime,
			float initialVelocity) {
		float vs = initialVelocity;
		float ve = -getScrollDirFactor(getScrollDir()) * mLongScrollSpeed;
		float T = mLongScrollTransitionDuration;
		QuarticPolynomial poly = QuarticPolynomial.approachVelocity(vs, ve, T);
		return new Animation(poly, Animation.Type.LONG_SCROLL, startTime, T,
				mSelectedViewMidpointXPos, false);
	}

	private void startLongScrollAnim() {
		double time = currentTime();
		startAnim(getLongScrollStartAnim(time, velocityAtTime(time)));
	}

	private void approachTargetAnim(float targetPos, float maxDuration,
			float switchDirectionDuration) {
		double time = currentTime();
		float d = targetPos - mSelectedViewMidpointXPos;
		if (Math.abs(d) < 1) {
			// If we're moving less than a pixel, don't do an animation.
			finishAnim(time);
			return;
		}

		ScrollDir currentScrollDir = mAnim == null ? ScrollDir.None : mAnim
				.getScrollDirAtTime(time);
		ScrollDir newScrollDir = d > 0 ? ScrollDir.Left : ScrollDir.Right;

		if (currentScrollDir == ScrollDir.None) {
			// If the current velocity is 0 the QuarticPolynomial.approachTarget
			// math doesn't work. Instead use the max duration.
			approachTargetFixedDurationAnim(targetPos, maxDuration);
		} else if (currentScrollDir != newScrollDir) {
			// If we're changing directions the QuarticPolynomial.approachTarget
			// math doesn't work. Instead use the switch direction duration.
			approachTargetFixedDurationAnim(targetPos, switchDirectionDuration);
		} else {
			QuarticPolynomial.PolynomialAndDuration p = QuarticPolynomial
					.approachTarget(d, velocityAtTime(time));
			if (p.duration < 0) {
				// With certain inputs the QuarticPolynomial.approachTarget math
				// breaks down and we get a negative duration. In that case fall
				// back to the switchDirectionDuration. This should never happen
				// as long as our conditionals above are working correctly.
				Log.e(TAG,
						"Invalid inputs to QuarticPolynomial.approachTarget()");
				approachTargetFixedDurationAnim(targetPos,
						switchDirectionDuration);
			} else if (p.duration <= maxDuration) {
				Animation anim = new Animation(p.poly,
						Animation.Type.APPROACH_TARGET, time, p.duration,
						mSelectedViewMidpointXPos, true);
				startAnim(anim);
			} else {
				approachTargetFixedDurationAnim(targetPos, maxDuration);
			}
		}
	}

	private float getLongScrollApproachTargetMaxDuration() {
		Animation anim = getLongScrollStartAnim(currentTime(), 0);
		double endTime = anim.getStartTime() + anim.getDuration();
		float longScrollStartDistance = anim.eval(endTime)
				- anim.eval(anim.getStartTime());
		float approachTargetDistance = mSelectedViewMidpointXPos
				- getSelectedItemMidpoint();
		return Math.abs(approachTargetDistance / longScrollStartDistance)
				* anim.getDuration();
	}

	private void longScrollApproachTargetAnim() {
		float maxDuration = Math.max(getLongScrollApproachTargetMaxDuration(),
				mTapScrollDuration);
		approachTargetAnim(getChildSelectedPos(mSelectedView), maxDuration,
				maxDuration);
	}

	private float getAdjustedTapScrollDuration(float targetPos) {
		int i = getSelectedChildIndex();
		View adjacentView = mScrollDir == ScrollDir.Right ? getChildAt(i - 1)
				: getChildAt(i + 1);
		float adjustFactor = Math
				.abs((targetPos - mSelectedViewMidpointXPos)
						/ (getViewMidpoint(mSelectedView) - getViewMidpoint(adjacentView)));
		return Math.min(mTapScrollDuration, mTapScrollDuration * adjustFactor);
	}

	private void approachTargetFixedDurationAnim(float targetPos, float duration) {
		double time = currentTime();
		float d = targetPos - mSelectedViewMidpointXPos;
		float v = velocityAtTime(time);
		float T = duration;
		QuarticPolynomial poly = QuarticPolynomial.approachTargetWithDuration(
				d, T, v);
		Animation anim = new Animation(poly, Animation.Type.APPROACH_TARGET,
				time, T, mSelectedViewMidpointXPos, true);
		startAnim(anim);
	}

	// A NaN return value indicates we're not sure of the long scroll end pos
	// yet (we're not near the end)
	protected float longScrollEndPos() {
		View firstChild = getFirstChild();
		View lastChild = getLastChild();
		boolean nearStart = getViewPos(firstChild) == 0;
		boolean nearEnd = getViewPos(lastChild) == getCount() - 1;
		float endPos = Float.NaN;
		if (scrollingLeft() && nearStart) {
			float childSelectedPos = getChildSelectedPos(firstChild);
			float delta = childSelectedPos - getViewMidpoint(firstChild);
			if (delta < 0) {
				endPos = mSelectedViewMidpointXPos + delta;
			}
		} else if (scrollingRight() && nearEnd) {
			float childSelectedPos = getChildSelectedPos(lastChild);
			float delta = childSelectedPos - getViewMidpoint(lastChild);
			if (delta > 0) {
				endPos = mSelectedViewMidpointXPos + delta;
			}
		}
		return endPos;
	}

	public float getScrollEndViewMidpoint(ScrollDir scrollDir) {
		int endChildIndex = scrollDir == ScrollDir.Right ? getChildCount() - 1
				: 0;
		return getViewMidpoint(getChildAt(endChildIndex));
	}

	protected boolean isAnimating() {
		return mAnim != null || mLongScrollSelectorAnim != null;
	}

	private void updateAnim() {
		double time = currentTime();

		if (mAnim != null) {
			if (!mAnim.active(time)) {
				finishAnim(time);
			} else {
				float newPos = mAnim.eval(time);
				incrementalLayout(newPos);
			}

			if (isLongScrollAnim()) {
				float endPos = longScrollEndPos();

				boolean atEnd = !Float.isNaN(endPos)
						&& ((scrollingRight() && mSelectedViewMidpointXPos <= endPos) || (scrollingLeft() && mSelectedViewMidpointXPos >= endPos));

				if (atEnd) {
					mAnim = null;
					incrementalLayout(endPos);

					float selectorEndPos = getScrollEndViewMidpoint(getScrollDir());
					mLongScrollSelectorAnim = new LongScrollSelectorAnimation(
							false, time, mLongScrollSelectorAnim.getPos(time),
							selectorEndPos, getScrollDirFactor(mScrollDir)
									* mLongScrollSpeed);
				}
			}
		}

		if (mLongScrollSelectorAnim != null) {
			setSelectedView(calcSelectedViewDuringLongScroll(mLongScrollSelectorAnim
					.getPos(time)));

			if (!mLongScrollSelectorAnim.isActive(time)) {
				if (mLongScrollSelectorAnim.requiresLongScrollAnimAtEnd()) {
					if (!isLongScrollAnim()) {
						startLongScrollAnim();
					}
				} else {
					mLongScrollSelectorAnim = null;
				}
			}
		}
	}

	protected float getSelectedItemMidpoint(View view) {
		if (isSelectionLeftAligned()) {
			return mSelectionOffset
					+ (view == null ? 0 : (getSelectedScale(view)
							* view.getWidth() / 2));
		}
		return getWidth() / 2;
	}

	protected float getSelectedItemMidpoint() {
		return getSelectedItemMidpoint(mSelectedView);
	}

	protected float getViewMidpoint(View view) {
		return view.getX() + view.getWidth() / 2;
	}

	protected float getLeftBound() {
		if (isSelectionFlexAligned()) {
			return mListStartPadding;
		} else if (isSelectionLeftAligned()) {
			return mSelectionOffset;
		}
		return Float.NEGATIVE_INFINITY;
	}

	protected float getRightBound() {
		return isSelectionFlexAligned() ? getWidth() - mListEndPadding
				: Float.POSITIVE_INFINITY;
	}

	private View calcSelectedViewDuringLongScroll(float selectorPos) {
		float selectedMid = getViewMidpoint(mSelectedView);

		if (mScrollDir == ScrollDir.Right) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				float childMid = getViewMidpoint(child);
				if (childMid >= selectorPos) {
					return childMid > selectedMid ? child : mSelectedView;
				}
			}
			return getChildAt(getChildCount() - 1);
		} else {
			for (int i = getChildCount() - 1; i >= 0; i--) {
				View child = getChildAt(i);
				float childMid = getViewMidpoint(child);
				if (childMid <= selectorPos) {
					return childMid < selectedMid ? child : mSelectedView;
				}
			}
			return getChildAt(0);
		}
	}

	protected void applyViewStyle(View view, boolean selected) {
		boolean focused = isFocused()
				&& (mIgnoreWindowFocusForScale || hasWindowFocus());

		float desiredScale;
		if (selected && focused) {
			view.setSelected(true);
			desiredScale = getSelectedScale(view);
		} else {
			view.setSelected(false);
			desiredScale = mUnselectedItemScale;
		}

		if (mScaleAnimated) {
			startScaleAnim(view, desiredScale);
		} else {
			view.setScaleX(desiredScale);
			view.setScaleY(desiredScale);
		}

		// If this is the selected view (selected will be true) record whether
		// or not we're actually calling setSelected on it. This is necessary to
		// initialize the scale of the selected view correctly when repopulating
		// our child view list.
		if (selected) {
			mLastScaledPos = view.isSelected() ? getViewPos(view) : -1;
		}
	}

	protected void startScaleAnim(View child, float targetScale) {
		// Scale the duration by the amount we need to scale
		float startScale = getCurrentViewScale(child);
		float selectedScale = getSelectedScale(child);
		float fullDuration = mScaleDuration;
		float duration = fullDuration;
		if (Math.abs(selectedScale - mUnselectedItemScale) >= .0001f) // Guard
																		// against
																		// divide
																		// by 0
		{
			duration = fullDuration
					* Math.abs((targetScale - startScale)
							/ (selectedScale - mUnselectedItemScale));
			duration = Math.min(fullDuration, duration);
		}

		child.setScaleX(startScale);
		child.setScaleY(startScale);

		// If we're currently animating this item, update the anim. Otherwise
		// create a new one.
		int pos = getViewPos(child);
		ScaleAnim previousAnim = mScaleAnims.get(pos);
		if (previousAnim != null) {
			previousAnim.restart(startScale, targetScale, duration);
		} else {
			mScaleAnims.put(pos, new ScaleAnim(pos, startScale, targetScale,
					duration));
		}
	}

	protected void clearScaleAnims() {
		// Copy the collection to avoid ConcurrentModificationExceptions since
		// ScaleAnim.cancel() will remove the ScaleAnim from mScaleAnims
		Collection<ScaleAnim> scaleAnims = new ArrayList(mScaleAnims.values());
		for (ScaleAnim scaleAnim : scaleAnims) {
			scaleAnim.cancel();
		}
		mScaleAnims.clear();
	}

	protected void setSelectedView(View view) {
		setSelectedView(view, true, false);
	}

	protected void setSelectedView(View view, boolean applyOffset,
			boolean forceItemSelectedNotification) {
		if (applyOffset && mSelectedView != null && view != null) {
			float offset = getViewMidpoint(view)
					- getViewMidpoint(mSelectedView);
			if (mAnim != null) {
				mAnim.applyOffset(offset);
			}
			if (mLongScrollSelectorAnim != null) {
				mLongScrollSelectorAnim.applyOffset(offset);
			}
			if (!Float.isNaN(mSelectedViewMidpointXPos)) {
				mSelectedViewMidpointXPos += offset;
			}
		}

		// Ensure mSelectedView is up to date before applying view styles
		// in case they rely on knowing the selected view eg: for layout calcs
		View oldView = mSelectedView;
		mSelectedView = view;
		if (oldView != mSelectedView) {
			if (oldView != null) {
				applyViewStyle(oldView, false);
			}
			if (mSelectedView != null) {
				applyViewStyle(mSelectedView, true);
			}
		}

		if (mSelectedView == null) {
			mLastScaledPos = -1;
		}

		int newSelectedPos = mSelectedView == null ? -1
				: getViewPos(mSelectedView);
		int newItemCount = getCount();
		boolean notifyListeners = mLastSelectedPos != newSelectedPos
				|| mLastItemCount != newItemCount
				|| forceItemSelectedNotification;
		mLastSelectedPos = newSelectedPos;
		mLastItemCount = newItemCount;

		if (notifyListeners) {
			for (OnItemSelectedListener listener : mItemSelectedListeners) {
				if (mSelectedView != null) {
					listener.onItemSelected(this, mSelectedView,
							mLastSelectedPos,
							mAdapter.getItemId(mLastSelectedPos));
				} else {
					listener.onNothingSelected(this);
				}
			}
		}
	}

	protected float velocityAtTime(double time) {
		return mAnim != null ? mAnim.velocity(time) : 0;
	}

	// Get the current time in seconds
	static private double currentTime() {
		return (System.nanoTime()) / 1000000000.0;
	}

	// We need a Util class in ASBUiLibrary
	public static float clamp(float val, float min, float max) {
		return val < min ? min : val > max ? max : val;
	}

	// Interlude: Scrolling mechanics
	//
	// In the carousel we have a lot of different scrolling motions to
	// perform. Here are some examples:
	//
	// - When the user press and holds right, we want to ramp up to a target
	// velocity smoothly. We call this a long scroll.
	// - When the user lets go after a long scroll, we want to smoothly approach
	// the selected item.
	// - When the user taps to the right, we want to smoothly approach the newly
	// selected item in a fixed amount of time.
	// - As the user approaches the last item in the list, we want to smoothly
	// slow down to a stop.
	//
	// It turns out the mechanics of scrolling are mathematically interesting,
	// which leads to the quartic polynomials and matrix inversion code we use
	// for the scrolling animations. Let's take one of our scrolling motions and
	// analyze it. Below are some links with relevant math background. You don't
	// need to fully understand all of this material, we just use a few basic
	// concepts.
	//
	// Polynomials (in particular derivatives and integrals of polynomials):
	// http://en.wikipedia.org/wiki/Polynomial
	// http://en.wikipedia.org/wiki/Power_rule
	// http://hyperphysics.phy-astr.gsu.edu/hbase/intpol.html
	// Systems of linear equations:
	// http://en.wikipedia.org/wiki/System_of_linear_equations
	// Matrix inversion:
	// http://en.wikipedia.org/wiki/Invertible_matrix
	//
	// When the user taps right, we want to smoothly approach the newly selected
	// item in a fixed amount of time. Our approach to solve this is going to be
	// to formally state our constraints then construct a polynomial that
	// satisfies the constraints. In plain English, we can describe the motion
	// we want as follows:
	//
	// 1. The item isn't moving initially.
	// 2. As we approach the target item, we stop moving gradually.
	// 3. The total distance we travel is the difference between our current
	// position and the item's position.
	// 4. The motion should take some fixed amount of time to complete that we
	// can easily control.
	//
	// Now we need to decide on some mathematical model for our position. Most
	// of our constraints are related to the velocity, so we'll choose a model
	// for the velocity, and the position can be derived from that. In our case
	// we'll use a polynomial, which is mathematically simple and fast to
	// compute. Another option would be a differential equation, which is a
	// better model for physical forces (like friction) that we're simulating,
	// but is more complex mathematically, and more computationally intensive at
	// runtime. Here's our polynomial:
	//
	// v(t) = at^3 + bt^2 + ct + d
	//
	// We choose a cubic polynomial for our velocity because a lower degree
	// polynomial doesn't have enough degrees of freedom to satisfy our
	// constraints. You can try going through the process below with a quadratic
	// polynomial and you'll see that it doesn't work.
	//
	// Taking our velocity as the cubic polynomial above, we can get the
	// position function by integrating and the acceleration function by taking
	// the derivative. That gives us
	//
	// p(t) = at^4/4 + bt^3/3 + ct^2/2 + dt + e
	// v(t) = at^3 + bt^2 + ct + d
	// a(t) = 3at^2 + 2bt + c
	//
	// Now we can restate our constraints in terms of our polynomials. Take the
	// the total distance traveled (from constraint 3) as D, and the total
	// duration (from constraint 4) as T.
	//
	// 1. v(0) = 0 --> a(0)^3 + b(0)^2 + c(0) + d = 0 --> d = 0
	// 2. v(T) = 0 --> aT^3 + bT^2 + cT = 0
	// a(T) = 0 --> 3aT^2 + 2bT + c = 0
	// 3. p(T) = D --> aT^4/4 + bt^3/3 + cT^2/2 = D
	//
	// In formulating the constraints, we assume that e = 0 (starting position
	// of 0... this doesn't affect the math), and we take advantage of deriving
	// d = 0 as a result of the first constraint to reduce the number of
	// unknowns in the other equations.
	//
	// This leaves us with the following equations:
	//
	// aT^3 + bT^2 + cT = 0
	// 3aT^2 + 2bT + c = 0
	// aT^4/4 + bT^3/3 + cT^2/2 = D
	//
	// Here we have a system of linear equations for the variables a, b, and
	// c. We can represent this in matrix form:
	//
	// / T^3 T^2 T \ / a \ / 0 \
	// | 3T^2 2T 1 | | b | = | 0 |
	// \ T^4/4 T^3/3 T^2/D / \ c / \ D /
	//
	// To solve for the vector (a b c), we multiply (0 0 D) by the inverse of
	// the given matrix, which we can easily compute at runtime.
	//
	// Once we have the coefficients a, b, and c, we can plug them into the p(t)
	// function to get the position as a funciton of time in the range [0, T]:
	//
	// p(t) = at^4/4 + bt^3/3 + ct^2/2 + dt + e
	//
	// In our case d was calculated to be 0, and e is just the starting position
	// (assume 0).
	//
	// This shows how the math works out for one particular scrolling
	// motion. The others have different constraints, but the math works the
	// same: model the position (or velocity) as a polynomial with unknown
	// coefficients, express the constraints in terms of the polynomial, solve
	// the resulting linear system to get the coefficients.

	private static class QuarticPolynomial {
		// p(t) = at^4 + bt^3 + ct^2 + dt + e
		private float a, b, c, d, e;

		public QuarticPolynomial(float a_, float b_, float c_, float d_,
				float e_) {
			a = a_;
			b = b_;
			c = c_;
			d = d_;
			e = e_;
		}

		public float eval(float t) {
			return a * t * t * t * t + b * t * t * t + c * t * t + d * t + e;
		}

		public float derivative(float t) {
			// p'(t) = 4at^3 + 3bt^2 + 2ct + d
			return 4 * a * t * t * t + 3 * b * t * t + 2 * c * t + d;
		}

		public static QuarticPolynomial approachVelocity(float initialVelocity,
				float targetVelocity, float duration) {
			float VS = initialVelocity;
			float VE = targetVelocity;
			float T = duration;
			Vector v = new Vector(VE - VS, 0, 0);
			v = new Matrix(T * T, T, 0, 2 * T, 1, 0, 0, 0, 1).solve(v);
			return new QuarticPolynomial(0, v.x / 3, v.y / 2, VS, 0);
		}

		public static class PolynomialAndDuration {
			QuarticPolynomial poly;
			float duration;

			PolynomialAndDuration(QuarticPolynomial poly_, float duration_) {
				poly = poly_;
				duration = duration_;
			}
		}

		public String toStr() {
			return String.format(
					"p(t) = %.1fx^4 + %.1fx^3 + %.1fx^2 + %.1fx + %.1f", a, b,
					c, d, e);
		}

		public static PolynomialAndDuration approachTarget(float distance,
				float initialVelocity) {
			float D = distance;
			float V = initialVelocity;
			float b = (V * V * V) / (27 * D * D);
			float c = (-V * V) / (3 * D);
			float d = V;
			return new PolynomialAndDuration(new QuarticPolynomial(0, b, c, d,
					0), (3 * D) / V);
		}

		public static QuarticPolynomial approachTargetWithDuration(
				float distance, float duration, float initialVelocity) {
			float D = distance;
			float T = duration;
			float VS = initialVelocity;
			float VE = 0;
			Vector v = new Vector(VE - VS, 0, D - VS * T);
			v = new Matrix(T * T * T, T * T, T, 3 * T * T, 2 * T, 1,
					(T * T * T * T) / 4, (T * T * T) / 3, (T * T) / 2).solve(v);
			return new QuarticPolynomial(v.x / 4, v.y / 3, v.z / 2, VS, 0);
		}
	}

	private static class Animation {
		private QuarticPolynomial mPoly;
		private double mStartTime;
		private float mDuration;
		private float mStartPos;
		private boolean mClamp;
		private float mFinalVelocity;
		private Type mType;

		public enum Type {
			LONG_SCROLL, APPROACH_TARGET
		}

		public Animation(QuarticPolynomial poly, Type type, double startTime,
				float duration, float startPos, boolean clamp) {
			mPoly = poly;
			mType = type;
			mStartTime = startTime;
			mDuration = duration;
			mStartPos = startPos;
			mClamp = clamp;
			mFinalVelocity = mPoly.derivative(mDuration);
		}

		public boolean active(double time) {
			float t = getT(time);
			return !mClamp || t <= mDuration;
		}

		public float eval(double time) {
			float t = getT(time);
			float pos = mPoly.eval(Math.min(t, mDuration)) + mStartPos;
			if (!mClamp && t > mDuration) {
				pos += mFinalVelocity * (t - mDuration);
			}
			return pos;
		}

		public float velocity(double time) {
			float t = getT(time);
			if (t <= mDuration) {
				return mPoly.derivative(t);
			} else if (!mClamp) {
				return mFinalVelocity;
			}
			return 0;
		}

		public float getT(double time) {
			return (float) (time - mStartTime);
		}

		public void setStartTime(double time) {
			mStartTime = time;
		}

		public double getStartTime() {
			return mStartTime;
		}

		public float getDuration() {
			return mDuration;
		}

		public float getStartPos() {
			return mStartPos;
		}

		public void applyOffset(float offset) {
			mStartPos += offset;
		}

		public ScrollDir getTotalScrollDir() {
			float endPos = eval(mStartTime + mDuration);
			return endPos - mStartPos > 0 ? ScrollDir.Left : ScrollDir.Right;
		}

		public ScrollDir getScrollDirAtTime(double time) {
			float v = velocity(time);
			if (Math.abs(v) < 0.01) {
				return ScrollDir.None;
			}
			return v > 0 ? ScrollDir.Left : ScrollDir.Right;
		}

		public QuarticPolynomial getPolynomial() {
			return mPoly;
		}

		public Type getType() {
			return mType;
		}
	}

	private class LongScrollSelectorAnimation {
		private boolean mStartLongScrollAnimAtEnd;
		private double mStartTime;
		private float mStartPos; // Relative to selected view
		private float mEndPos; // Relative to selected view
		private float mVelocity;

		/**
		 * Create a new LongScrollSelectorAnimation
		 */
		public LongScrollSelectorAnimation(boolean startLongScrollAnimAtEnd,
				double startTime, float startPos, float endPos, float velocity) {
			mStartLongScrollAnimAtEnd = startLongScrollAnimAtEnd;
			mStartTime = startTime;
			mStartPos = carouselPosToSelectedViewPos(startPos);
			mEndPos = carouselPosToSelectedViewPos(endPos);
			mVelocity = velocity;
		}

		/**
		 * Returns true if the selector is still approaching its target position
		 * (i.e. it's still animating), false otherwise
		 */
		public boolean isActive(double time) {
			float end = selectedViewPosToCarouselPos(mEndPos);
			return getScrollDir() == ScrollDir.Right ? getUnclampedPos(time) < end
					: getUnclampedPos(time) > end;
		}

		/**
		 * Get the selector position at the given time
		 */
		public float getPos(double time) {
			if (isClampedToSelectorTargetPos() && !isActive(time)) {
				return getLongScrollSelectorTargetPos(getScrollDir());
			} else {
				return getUnclampedPos(time);
			}
		}

		private boolean isClampedToSelectorTargetPos() {
			return mStartLongScrollAnimAtEnd;
		}

		private float getUnclampedPos(double time) {
			float t = (float) (time - mStartTime);
			float current = selectedViewPosToCarouselPos(mStartPos + t
					* mVelocity);
			float end = selectedViewPosToCarouselPos(mEndPos);
			if (getScrollDir() == ScrollDir.Right) {
				return Math.min(current, end);
			} else {
				return Math.max(current, end);
			}
		}

		private float getStartPos() {
			return selectedViewPosToCarouselPos(mStartPos);
		}

		private float getEndPos() {
			return selectedViewPosToCarouselPos(mEndPos);
		}

		private float carouselPosToSelectedViewPos(float pos) {
			return pos - mSelectedViewMidpointXPos;
		}

		private float selectedViewPosToCarouselPos(float pos) {
			return pos + mSelectedViewMidpointXPos;
		}

		/**
		 * Returns true if we should start a long scroll anim when the selector
		 * anim finishes
		 */
		public boolean requiresLongScrollAnimAtEnd() {
			return mStartLongScrollAnimAtEnd;
		}

		/**
		 * We've reset our coordinate system by the given offset. This will
		 * happen when the selected view changes. Update the selector anim
		 * accordingly.
		 */
		public void applyOffset(float offset) {
			mStartPos -= offset;
			mEndPos -= offset;
		}

		/**
		 * Return the scroll direction as determined by the selector anim
		 */
		public ScrollDir getScrollDir() {
			return mVelocity > 0 ? ScrollDir.Right : ScrollDir.Left;
		}

		/**
		 * Get a string representation of the selector anim. Useful for
		 * debugging.
		 */
		public String toStr() {
			return String
					.format("startLongScrollAnimAtEnd=%b startPos=(%.1f %.1f) endPos=(%.1f %.1f) velocity=%.1f",
							mStartLongScrollAnimAtEnd, mStartPos,
							selectedViewPosToCarouselPos(mStartPos), mEndPos,
							selectedViewPosToCarouselPos(mEndPos), mVelocity);
		}
	}

	private static class Vector {
		float x, y, z;

		Vector(float x_, float y_, float z_) {
			x = x_;
			y = y_;
			z = z_;
		}
	}

	// TODO: See if there's some suitable matrix/vector code we can reuse.
	// Ideally it'd be optimized for 3x3 matrices like this code is: unroll
	// loops, use Cramer's rule rather than LU decomposition, and avoid extra
	// memory allocations and array access overhead by not using an array to
	// store the data. Having said that, even a fully general matrix library
	// like the one in Apache Commons might perform perfectly well for our
	// needs. We should test and see.
	//
	// http://commons.apache.org/math/api-2.0/org/apache/commons/math/linear/RealMatrix.html
	private static class Matrix {
		float m11, m12, m13, m21, m22, m23, m31, m32, m33;

		public Matrix(float m11_, float m12_, float m13_, float m21_,
				float m22_, float m23_, float m31_, float m32_, float m33_) {
			m11 = m11_;
			m12 = m12_;
			m13 = m13_;
			m21 = m21_;
			m22 = m22_;
			m23 = m23_;
			m31 = m31_;
			m32 = m32_;
			m33 = m33_;
		}

		public float determinant() {
			return m11 * (m22 * m33 - m23 * m32) - m12
					* (m21 * m33 - m23 * m31) + m13 * (m21 * m32 - m22 * m31);
		}

		public Matrix inverse() {
			float a11 = m22 * m33 - m23 * m32, a12 = m13 * m32 - m12 * m33, a13 = m12
					* m23 - m13 * m22, a21 = m23 * m31 - m21 * m33, a22 = m11
					* m33 - m13 * m31, a23 = m13 * m21 - m11 * m23, a31 = m21
					* m32 - m22 * m31, a32 = m12 * m31 - m11 * m32, a33 = m11
					* m22 - m12 * m21;

			float d = determinant();
			float invD = 1.0f / d;

			return new Matrix(a11 * invD, a12 * invD, a13 * invD, a21 * invD,
					a22 * invD, a23 * invD, a31 * invD, a32 * invD, a33 * invD);
		}

		public Vector multVec(Vector v) {
			return new Vector(m11 * v.x + m12 * v.y + m13 * v.z, m21 * v.x
					+ m22 * v.y + m23 * v.z, m31 * v.x + m32 * v.y + m33 * v.z);
		}

		public Vector solve(Vector v) {
			return inverse().multVec(v);
		}
	}

	/**
	 * Scales a view while ensuring the layout is updated.
	 */
	protected class ScaleAnim implements Animator.AnimatorListener,
			ValueAnimator.AnimatorUpdateListener {
		private int mPos;
		private ValueAnimator mAnim;

		public ScaleAnim(int pos, float startScale, float endScale,
				float duration) {
			mPos = pos;
			mAnim = ValueAnimator.ofFloat(startScale, endScale);
			mAnim.setDuration((long) (duration * 1000));
			mAnim.setInterpolator(null);
			mAnim.addListener(this);
			mAnim.addUpdateListener(this);
			mAnim.start();
		}

		public void cancel() {
			mAnim.cancel();
		}

		public void restart(float startScale, float endScale, float duration) {
			mAnim.setFloatValues(startScale, endScale);
			mAnim.setDuration((long) (duration * 1000));
			mAnim.start();
		}

		public void setPos(int pos) {
			mPos = pos;
		}

		public float getCurrentScale() {
			return (Float) mAnim.getAnimatedValue();
		}

		public void onAnimationUpdate(ValueAnimator animation) {
			View view = findView(mPos);
			if (view != null) {
				float scale = getCurrentScale();
				view.setScaleX(scale);
				view.setScaleY(scale);
				positionViews();
			}
		}

		public void onAnimationEnd(Animator animation) {
			mScaleAnims.remove(mPos);
		}

		public void onAnimationCancel(Animator animation) {
		}

		public void onAnimationRepeat(Animator animation) {
		}

		public void onAnimationStart(Animator animation) {
		}
	}

	/**
	 * Responds to changes to the Adapter when the user calls
	 * notifyDataSetChanged
	 */
	private class CarouselDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			LayoutInitParams params = new LayoutInitParams();
			params.forceItemSelectedNotification = true;
			resetLayout(params);
		}

		@Override
		public void onInvalidated() {
			LayoutInitParams params = new LayoutInitParams();
			params.forceItemSelectedNotification = true;
			resetLayout(params);
		}
	}
}
