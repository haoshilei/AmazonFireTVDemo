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

package com.sample.tom.asbuilibrary.details;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.sample.tom.uiwidgetssample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base activity for our details pages.
 * 
 * <pre>
 * How to use
 * 
 *   1) Create a class which extends {@link DetailsBaseActivity}
 *   2) Override getMenuOptions() to define the tabs for your activity
 *   3) Override getMaxCoverHeight() to define height of the cover (or 0 for no cover)
 *   4) Override getMaxCoverWidth() to define width of the cover
 *   5) Create any fragments you want to show in your activity
 *   6) If your menu options change, call refreshMenuOptions() to reload them
 * </pre>
 */
public abstract class DetailsBaseActivity extends Activity {
	private static final String TAG = "DetailsBaseActivity";
	private static final String EXISTING_FRAGMENTS_KEY = "fragment_tags";

	private boolean mFragmentTransactionActive = false;
	private Fragment mCurrentFragment = null;
	private DetailsMenuListFragment mMenuFragment;
	private ImageView mCoverView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.details_base_activity);

		// If we have a saved instance state, we may need to clear our fragment
		// manager
		if (savedInstanceState != null) {
			ArrayList<String> addedTabs = savedInstanceState
					.getStringArrayList(EXISTING_FRAGMENTS_KEY);

			if (addedTabs != null && !addedTabs.isEmpty()) {
				removeFragmentsWithTags(addedTabs);
			}
		}

		// Grab our menu list fragment
		mMenuFragment = (DetailsMenuListFragment) getFragmentManager()
				.findFragmentById(R.id.list_fragment);

		// Grab the cover view
		mCoverView = mMenuFragment.getCoverView();

		// Set our cover's height
		mCoverView.getLayoutParams().height = getMaxCoverHeight();

		// Set our cover's width
		View leftMenuList = findViewById(R.id.list_fragment);
		leftMenuList.getLayoutParams().width = getMaxCoverWidth();

		// Set our menu options
		mMenuFragment.setMenuOptions(getMenuOptions());

		// Set our onSelected Action
		mMenuFragment.getListView().setOnItemSelectedListener(
				new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// Switch to the fragment for this tab
						DetailsTabInfo tab = (DetailsTabInfo) parent
								.getAdapter().getItem(position);
						switchFragment(tab);

						// Notify the list view, so we can get the colors
						// correct
						((DetailsMenuOptionListView) parent)
								.onItemSelected(view);
					}

					@Override
					public void onNothingSelected(
							AdapterView<?> paramAdapterView) {
						// Do nothing
					}
				});

		// Set the onKeyListener
		mMenuFragment.getListView().setOnKeyListener(
				new AdapterView.OnKeyListener() {
					@Override
					public boolean onKey(View view, int keyCode, KeyEvent event) {
						// If we hit right, focus on the fragment
						if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
								&& event.getAction() == KeyEvent.ACTION_DOWN
								&& mCurrentFragment != null
								&& mCurrentFragment.getView() != null) {
							mCurrentFragment.getView().requestFocus();
							return true;
						}
						return false;
					}
				});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Grab the current menu options
		List<DetailsTabInfo> menuOptions = mMenuFragment
				.getCurrentMenuOptions();

		if (menuOptions != null) {
			ArrayList<String> addedTabs = new ArrayList<String>(
					menuOptions.size());

			// Find out which fragments have been previously added to the
			// fragment manager
			for (DetailsTabInfo tab : menuOptions) {
				if (getFragmentManager().findFragmentByTag(tab.getTabName()) != null) {
					addedTabs.add(tab.getTabName());
				}
			}

			// Put the tags of all the fragments in the bundle
			if (!addedTabs.isEmpty()) {
				outState.putStringArrayList(EXISTING_FRAGMENTS_KEY, addedTabs);
			}
		}
	}

	/**
	 * Return default menu item index for this screen. This index refers to the
	 * list returned by {@link #getMenuOptions()}.
	 * 
	 * Set this function to return what you want the default fragment to be.
	 * This function is used within {@link #switchToDefaultFragment()}. By
	 * default this will return 0.
	 * 
	 * @return default menu option index
	 */
	protected int getDefaultMenuItemIndex() {
		return 0;
	}

	/**
	 * Switches fragment to the default fragment for this screen
	 * 
	 * @return <code>true</code> if the fragment was switched,
	 *         <code>false</code> otherwise
	 */
	protected boolean switchToDefaultFragment() {
		if (!mMenuFragment.getListAdapter().isEmpty()
				&& mMenuFragment.getSelectedItemPosition() != getDefaultMenuItemIndex()) {
			mMenuFragment.getListView().requestFocus();
			mMenuFragment.getListView().setSelection(getDefaultMenuItemIndex());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Switch to the given fragment, if we are not already on that fragment.
	 */
	private void switchFragment(DetailsTabInfo tab) {
		// Do not start a new transaction if the old one is still in-progress or
		// we are finishing
		if (mFragmentTransactionActive || isFinishing()) {
			return;
		}

		Fragment fragment = tab.getFragment();

		// Make sure there is actually work to be done
		if (mCurrentFragment == fragment) {
			Log.d(TAG,
					"Switching to fragment which is already shown? Ignoring.");
			return;
		}

		// If this is the first fragment being shown, we need to give it focus!
		boolean giveFragmentFocus;

		// Now hide the current fragment and show the new one
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.disallowAddToBackStack();

		// Do we have an active fragment
		if (mCurrentFragment != null) {
			giveFragmentFocus = false;

			// Hide the current fragment
			ft.hide(mCurrentFragment);

			// Show the new fragment
			if (fragment.isHidden() || fragment.isAdded()) {
				// We are hidden or already added? Show us.
				ft.show(fragment);
			} else {
				// We have never dealt with this fragment, add it.
				ft.add(R.id.details_fragment_content, fragment,
						tab.getTabName());
			}
		} else {
			giveFragmentFocus = true;

			// We don't have a current fragment. Replace all fragments instead
			// of adding
			// in case the fragment manager is storing some.
			ft.replace(R.id.details_fragment_content, fragment,
					tab.getTabName());
		}

		ft.commitAllowingStateLoss();

		// This call blocks until transactions have completed so waiting until
		// its completion is valid/save
		mFragmentTransactionActive = true;
		mCurrentFragment = fragment;
		getFragmentManager().executePendingTransactions();
		mFragmentTransactionActive = false;

		// Give focus if this is our first fragment
		if (giveFragmentFocus) {
			mCurrentFragment.getView().requestFocus();
		}
	}

	/**
	 * This removes all {@link Fragment}(s) in the {@link FragmentManager} that
	 * have the given tags
	 * 
	 * @param fragmentTags
	 *            The tags of the fragments to remove from the fragment manager
	 */
	private void removeFragmentsWithTags(List<String> fragmentTags) {
		if (fragmentTags != null) {
			// Begin our transaction
			FragmentTransaction ft = getFragmentManager().beginTransaction();

			for (String tag : fragmentTags) {
				// Grab each fragment, and remove them in our transaction
				Fragment fragment = getFragmentManager().findFragmentByTag(tag);

				if (fragment != null) {
					ft.remove(fragment);
				}
			}

			// Commit and run our transaction
			ft.commit();
			getFragmentManager().executePendingTransactions();
		}
	}

	/**
	 * This is called onCreate() and after a refreshMenuOptions() call to gather
	 * the options to show, represented by this list of {@link DetailTabInfo}.
	 * 
	 * @return The list of menu options
	 */
	protected abstract List<DetailsTabInfo> getMenuOptions();

	/**
	 * In order to position the left menu options, we need to know what the
	 * cover height is.
	 * 
	 * @return The height of the cover image
	 */
	protected abstract int getMaxCoverHeight();

	/**
	 * In order to position the left menu options, we need to know what the
	 * cover height is.
	 * 
	 * @return The height of the cover image
	 */
	protected abstract int getMaxCoverWidth();

	/**
	 * @return The currently shown fragment, or null if no fragment has been
	 *         shown
	 */
	protected Fragment getCurrentFragment() {
		return mCurrentFragment;
	}

	/**
	 * Call this when the menu options have been changed and the menu should
	 * update
	 */
	protected void refreshMenuOptions() {
		mMenuFragment.setMenuOptions(getMenuOptions());
	}

	/**
	 * Call this when you have an image you want to set above the menu options
	 * 
	 * @param image
	 *            The image to set as the item's cover
	 */
	protected void setCoverImage(Bitmap image) {
		final ImageView imageView = mMenuFragment.getCoverView();
		if (imageView != null) {
			imageView.setImageBitmap(image);
		}
	}
}
