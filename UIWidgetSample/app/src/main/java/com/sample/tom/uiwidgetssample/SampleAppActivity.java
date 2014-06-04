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

package com.sample.tom.uiwidgetssample;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * This is the activity to launch the samples
 */
public class SampleAppActivity extends Activity {
	private static final String TAG = SampleAppActivity.class.getSimpleName();

	private SharedPreferences mPrefs;
	private boolean mHasControl = false;
	private int mExpandedGutterSize;

	// This is the maximum number of cover images that can be loaded in memory
	// at once. When transitioning between two activities, e.g. going from A1 to
	// A2, A2's onStart can be called before A1's onStop is called. So A2 can
	// load up all its images before A1 has dumped its images, which means we
	// need to account for that in the cache size.
	private static final int COVER_IMAGE_CACHE_SIZE = 84;

	private MainFragment mMainFragment;

    /**
     * The Intent Param corresponding to the LeftMargin Width w.r.to Global nav passed by the GlobalNavService
     */
    private static final String LEFT_MARGIN_WIDTH_IN_DP = "left_margin_width_in_dp";

    // View mMainContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set the Content View here with the Navigator width
		setContentView(R.layout.activity_main);

		// Init our singletons
		Resources res = getResources();

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mExpandedGutterSize = (int) Math.floor(res
				.getDimension(R.dimen.left_nav_expanded_width));

		File imageDownloadDir = getCacheDir();

		int carouselCoverWidth = (int) res
				.getDimension(R.dimen.carousel_cover_max_width);
		int carouselCoverHeight = (int) res
				.getDimension(R.dimen.carousel_cover_max_height);

		mMainFragment = (MainFragment) getFragmentManager().findFragmentById(
				R.id.carousel_list);

		onNewIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMainFragment != null) {
			if (mHasControl) {
				mMainFragment.fragmentEntered(mMainFragment);
			} else {
				mMainFragment.fragmentReset();
			}
		}
	}

	/**
	 * onNewIntent
	 * 
	 * The Navigator will send an Intent to the Activity when the size of the
	 * left margin is updated. When this Intent is received, the Activity should
	 * resize itself accordingly.
	 * 
	 * @param intent
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);

		// As an activity we're responsible to track if we're in control of the
		// application.
		// A dirty but simple way to do it, is to track the gutter width. As
		// soon as it's
		// less than what is defined on ASBUILibrary for expanded gutter, we
		// know that
		// our activiby is the one in control.
		int newSize = intent.getIntExtra(LEFT_MARGIN_WIDTH_IN_DP, -1);
		mHasControl = newSize != -1 && newSize < mExpandedGutterSize;
		Log.i(TAG, (mHasControl ? "Taking" : "Giving up")
				+ " controll of activity");
	}

	/**
	 * Let the Activity handle key events first, if desired.
	 * 
	 * @param keyCode
	 *            The value in event.getKeyCode().
	 * @param event
	 *            Description of the key event.
	 * @return true if event was handled
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// First we let our main fragment to handle the key events, if it
		// doesn't want it
		// than we can do our logic

		if (mMainFragment.keyDown(keyCode, event)) {
			return true;
		}

		if (!mMainFragment.isInFullControl()) {
			// override handling of KEYCODE_DPAD_LEFT, KEYCODE_BACK to indicate
			// that we'd like
			// to return focus back to the Navigator.
			if (KeyEvent.KEYCODE_BACK == keyCode
					|| KeyEvent.KEYCODE_DPAD_LEFT == keyCode
					|| KeyEvent.KEYCODE_ESCAPE == keyCode) {
				// return back to Global Nav Bar
				mHasControl = false;
				Log.i(TAG, "Giving up controll of activity");
				this.finish();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Let the Activity handle key events first, if desired.
	 * 
	 * @param keyCode
	 *            The value in event.getKeyCode().
	 * @param event
	 *            Description of the key event.
	 * @return true if event was handled
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// First we let our main fragment to handle the key events, if it
		// doesn't want it
		// than we can do our logic
		if (mMainFragment.keyUp(keyCode, event)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Handle the back button. 
	 */
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	}

	public boolean hasControl() {
		return mHasControl;
	}
}
