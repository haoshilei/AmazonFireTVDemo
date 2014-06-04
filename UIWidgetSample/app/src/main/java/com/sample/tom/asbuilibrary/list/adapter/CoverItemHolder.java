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

package com.sample.tom.asbuilibrary.list.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.sample.tom.asbuilibrary.list.CoverItemProvider;
import com.sample.tom.asbuilibrary.list.ItemView;

/**
 * This class is a "holder" object for the items in the cover lists. It manages
 * the progress spinner and the image view.
 * <p/>
 */
public class CoverItemHolder {
	private static final String TAG = "CoverItemHolder";
	private final ImageView mImageView;
	private final View mProgressSpinner;
	private final Drawable mDefaultImage;

	private final ItemView mHolderView;
	private int mPosition;
	private boolean mDrawableLoaded = true;

	/**
	 * Default constructor.
	 * 
	 * @param coverItem
	 *            The coverItem containing a cover image and spinner
	 * @param defaultImage
	 *            The default image to load when no image can be loaded
	 * @param imageManager
	 *            The {@link ImageManager} handling images for this view
	 */
	public CoverItemHolder(ItemView coverItem, Drawable defaultImage) {
		mHolderView = coverItem;
		mImageView = coverItem.getCoverImageView();
		mProgressSpinner = coverItem.getSpinnerView();
		mDefaultImage = defaultImage;
	}

	/**
	 * @return The position of the item this cover item holder is representing
	 */
	public int getPosition() {
		return mPosition;
	}

	/**
	 * This set's the position of the item this cover item holder is
	 * representing
	 */
	public void setPosition(int position) {
		mPosition = position;
	}

	/**
	 * This will set the image view to the given image and remove the spinner
	 */
	private void showDrawable(Drawable image) {
		if (mImageView != null) {
			mImageView.setVisibility(View.VISIBLE);
		}
		mHolderView.setCoverImage(image);
		mProgressSpinner.setVisibility(View.GONE);
	}

	/**
	 * Sets the image view to the spinner
	 */
	public void showSpinner() {
		if (mImageView != null) {
			mImageView.setVisibility(View.GONE);
		}
		mProgressSpinner.setVisibility(View.VISIBLE);
	}

	protected void showImage() {
		if (!mDrawableLoaded) {
			showDrawable(mDefaultImage);
			mDrawableLoaded = true;
		}
		if (mImageView != null && mImageView.getVisibility() != View.VISIBLE) {
			mProgressSpinner.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
		}
	}

	public boolean isShowingImage() {
		return mImageView != null && mImageView.getVisibility() == View.VISIBLE;
	}

	protected void hideImage() {
		mDrawableLoaded = false;
		mHolderView.setCoverImage(null);
	}

	/**
	 * This will set the image view to the default image
	 */
	public void showDefaultImage() {
		showDrawable(mDefaultImage);
	}

	/**
	 * Update the cover item UI elements for the given item
	 */
	public void updateUIForItem(CoverItemProvider<?> item) {
		mHolderView.showItem(item);
	}
}
