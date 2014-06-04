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

import java.util.Locale;

import com.sample.tom.uiwidgetssample.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
// import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class is used to manage the bread crumb UI element that appears along
 * the top of the category list screen and any other fragment that needs the
 * bread crumb ribbon. The bread crumb UI widget layout is specified using an
 * instance of this class in bread_crumb_ribbon_layout.xml. Other layouts can
 * include the Bread Crumb Ribbon UI widget by including this layout in their
 * layout XML file and calling methods of this this class to update the bread
 * crumb ribbon UI to show the appropriate icon, title text and item count.
 */
public class BreadCrumbRibbon extends RelativeLayout {
	// disable Eclipse auto-formatter
	// @formatter:off

	// private static members
	private static final String TAG = "BreadCrumbRibbon";
	private static int sAnimationInMS;

	// private members
	private final String mTitleCountFormatString;
	private Drawable mIconDrawable; // icon to show on the ribbon
	private String mCategoryName; // category name to show on the ribbon
	private int mTotalItemsInCategory; // total number of items to show on the
										// ribbon
	private int mCurrentItemNumInCategory; // current item number (out of total
											// items) to show on the ribbon
	private ImageView mIconView;

	SlideAnimatorListener mSlideAnimatorListener = new SlideAnimatorListener();

	// This AnimatorListener makes the icon appear when the bread crumb slide
	// animation ends
	private class SlideAnimatorListener implements AnimatorListener {
		@Override
		public void onAnimationCancel(Animator animator) {
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			// Show the menu icon
			if (mIconDrawable != null) {
				// Show the icon that indicates whether we're currently
				// in the movies, TV, music, apps, etc. part of the UI
				if (mIconView != null) {
					mIconView.setImageDrawable(mIconDrawable);
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animator animator) {
		}

		@Override
		public void onAnimationStart(Animator animator) {
		}
	}

	/**
	 * Get the View where the icon image will be drawn.
	 */
	private ImageView getIconView() {
		// get the View once and hold on to it for better performance
		if (mIconView == null) {
			mIconView = (ImageView) findViewById(R.id.ribbon_icon_view);
		}

		return mIconView;
	}

	/**
	 * Constructor
	 * 
	 * @param titleRibbonView
	 *            - View corresponding to the breaf_crumb_ribbon_layout.xml.
	 * @param resources
	 *            - Application resources.
	 */
	public BreadCrumbRibbon(Context context, AttributeSet attrs) {
		super(context, attrs);

		Resources res = context.getResources();
		if (res != null) {
			mTitleCountFormatString = res
					.getString(R.string.title_ribbon_num_titles_format_string);
			sAnimationInMS = res
					.getInteger(R.integer.bread_crumb_icon_flyout_duration_ms);
		} else {
			mTitleCountFormatString = null;
			Log.e(TAG, "Unable to load resources");
		}
	}

	/**
	 * Sets the category name property, e.g. "New Releases", "Recently Watched"
	 * etc.
	 * 
	 * @param categoryName
	 */
	public void setCategoryName(String categoryName) {
		mCategoryName = categoryName.toUpperCase(Locale.US);
	}

	/**
	 * Sets the NumTitlesInCategory property. Example: if the current category
	 * name is "New Releases" and there are 100 New Releases movies, the UI code
	 * should call this method and pass 100.
	 * 
	 * @param numTitlesInCategory
	 */
	public void setNumTitlesInCategory(int numTitlesInCategory) {
		mTotalItemsInCategory = numTitlesInCategory;
	}

	/**
	 * Sets the ItemIndexInCategory property. This is a zero-based index.
	 * Example: If the user has set the focus to the first movie in the
	 * "New Releases" 1D list, the UI code should call this method and pass 0.
	 * 
	 * @param itemIndexInCategory
	 */
	public void setItemIndexInCategory(int itemIndexInCategory) {
		// convert from zero-based index to user-friendly one-based value
		mCurrentItemNumInCategory = itemIndexInCategory + 1;
	}

	/**
	 * Set the drawable for the icon that should be displayed on the top left of
	 * the ribbon.
	 * 
	 * @param iconDrawable
	 */
	public void setIconDrawable(Drawable iconDrawable) {
		mIconDrawable = iconDrawable;
	}

	/**
	 * Update the Title Ribbon views to show this bread crumb ribbon's current
	 * UI navigation state.
	 */
	public void displayUiNavigationState() {
		if (mIconDrawable != null) {
			// Prepare to show an icon that indicates whether we're currently in
			// the
			// movies, TV, music, apps, etc. part of the UI
			ImageView iconView = getIconView();
			if (iconView != null) {
				iconView.setImageDrawable(mIconDrawable);
			}
		}

		// Prepare to show the current category string
		TextView categoryNameTextView = (TextView) findViewById(R.id.ribbon_category_name_view);
		categoryNameTextView.setText(mCategoryName);

		if (mTotalItemsInCategory > 0) {
			// Prepare to show a string that says which content item this is in
			// the current category,
			// and how many items there are total in this category.
			TextView categoryItemNumberViewNumerator = (TextView) findViewById(R.id.ribbon_item_number_view_numerator);
			TextView categoryItemNumberViewDenominator = (TextView) findViewById(R.id.ribbon_item_number_view_denominator);
			categoryItemNumberViewNumerator.setText(Integer
					.toString(mCurrentItemNumInCategory));
			categoryItemNumberViewDenominator.setText(Integer
					.toString(mTotalItemsInCategory));
		}

		setVisibility(View.VISIBLE);
	}

	/**
	 * Update the Title Ribbon views to show this bread crumb ribbon's current
	 * UI navigation state. Display the bread crumb using an animation that
	 * causes the icon and title views slide in from the left and causes the
	 * item count view to fade in.
	 */
	public void displayUiNavigationStateWithSlideAnimation() {
		// Make sure the icon isn't shown while the bread crumb ribbon slides in
		if (getIconView() != null) {
			getIconView().setImageDrawable(null);
		}

		// Prepare to show the current category string
		TextView categoryNameTextView = (TextView) findViewById(R.id.ribbon_category_name_view);
		categoryNameTextView.setText(mCategoryName);

		View categoryItemNumberView = findViewById(R.id.ribbon_item_number_view_ll);
		TextView categoryItemNumberViewNumerator = (TextView) findViewById(R.id.ribbon_item_number_view_numerator);
		TextView categoryItemNumberViewDenominator = (TextView) findViewById(R.id.ribbon_item_number_view_denominator);
		if (mTotalItemsInCategory > 0) {
			// Prepare to show a string that says which content item this is in
			// the current category,
			// and how many items there are total in this category.
			categoryItemNumberViewNumerator.setText(Integer
					.toString(mCurrentItemNumInCategory));
			categoryItemNumberViewDenominator.setText(Integer
					.toString(mTotalItemsInCategory));
		} else if (mTotalItemsInCategory == 0) {
			categoryItemNumberViewNumerator.setText("0");
			categoryItemNumberViewDenominator.setText("0");
		}

		LinearLayout iconAndCategoryNameLayout = (LinearLayout) findViewById(R.id.icon_and_category_name);

		// Animate the icon background (without icon shown) and ribbon text
		// sliding in from the left
		// and animate the counter fading in.
		final int layoutXTranslate = 400; // hard-coded since the layout engine
											// apparently doesn't know yet the
											// width
		iconAndCategoryNameLayout.setTranslationX(-layoutXTranslate);

		if (mTitleCountFormatString != null && mTotalItemsInCategory > 0) {
			categoryItemNumberView.setAlpha(0f);
		}

		setVisibility(View.VISIBLE);
		// Use an animator listener to show the icon after the bread crumb
		// ribbon slides in
		iconAndCategoryNameLayout.animate().translationX(0)
				.setDuration(sAnimationInMS)
				.setListener(mSlideAnimatorListener);

		if (mTitleCountFormatString != null && mTotalItemsInCategory > 0) {
			categoryItemNumberView.animate().alpha(1.0f)
					.setDuration(sAnimationInMS);
		}
	}

	// enable Eclipse auto-formatter
	// @formatter:on
}
