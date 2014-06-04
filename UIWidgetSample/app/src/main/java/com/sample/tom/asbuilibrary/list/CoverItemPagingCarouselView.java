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
import java.util.List;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sample.tom.asbuilibrary.list.adapter.CoverItemHolder;
import com.sample.tom.asbuilibrary.list.adapter.CoverItemPagingCarouselAdapter;

/**
 * CoverItemPagingCarouselView Carousels for on-demand fetched data displaying
 * an ImageViewProvider's image as the primary UI element.
 */
public class CoverItemPagingCarouselView extends
		CarouselView<CoverItemPagingCarouselAdapter> {
	private Toast mToast;
	private int mCoverHolderId;

	// The vars below (and related functionality) are for trapz logging
	private boolean mAllImagesLoaded = false;
	private boolean mLogLoadStatus = false;
	private static final String LOG_CAROUSEL_LOADED = "Carousel Loaded";

	private static final String TAG = CoverItemPagingCarouselView.class
			.getSimpleName();

	protected OnCategoryChangeListener mCategoryChangeListener = null;
	private Stack<String> mCategoryTitles = new Stack<String>();

	@SuppressLint("ShowToast")
	public CoverItemPagingCarouselView(Context context, AttributeSet attrs,
			int coverHolderId) {
		super(context, attrs);

		mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		mCoverHolderId = coverHolderId;
	}

	@SuppressWarnings("unchecked")
	public void setAdapter(CoverItemPagingCarouselAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.detachFromCarousel();
		}

		if (adapter != null) {
			adapter.attachToCarousel(this);
		}

		super.setAdapter(adapter);
	}

	public void refresh() {
		List<CoverItemHolder> holders = getCoverItemHolders();
		for (CoverItemHolder holder : holders) {
			CoverItemProvider item = (CoverItemProvider) mAdapter
					.getItem(holder.getPosition());
			holder.updateUIForItem(item);
		}
	}

	public void setOnCategoryChangeListener(OnCategoryChangeListener listener) {
		mCategoryChangeListener = listener;
	}

	public OnCategoryChangeListener getOnCategoryChangeListener() {
		return mCategoryChangeListener;
	}

	private boolean calcAllImagesLoaded() {
		List<CoverItemHolder> holders = getCoverItemHolders();
		for (CoverItemHolder holder : holders) {
			if (!holder.isShowingImage()) {
				return false;
			}
		}
		return true;
	}

	public boolean getAllImagesLoaded() {
		return mAllImagesLoaded;
	}

	public void logLoadStatus(boolean logLoadStatus) {
		mLogLoadStatus = logLoadStatus;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		boolean allImagesLoaded = calcAllImagesLoaded();
		if (mLogLoadStatus) {
			if (!mAllImagesLoaded && allImagesLoaded) {
				Log.d(TAG, LOG_CAROUSEL_LOADED);
				// A toast is useful during debugging to make sure we're
				// correctly detecting when all images are loaded
				// String msg = String.format("Carousel '%s' all images loaded",
				// mName);
				// Toast.makeText(getContext(), msg, 1000).show();
			}
		}
		mAllImagesLoaded = allImagesLoaded;
	}

	private List<CoverItemHolder> getCoverItemHolders() {
		ArrayList<CoverItemHolder> holders = new ArrayList<CoverItemHolder>();
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			Object holder = child.getTag(mCoverHolderId);
			if (holder != null) {
				holders.add((CoverItemHolder) holder);
			}
		}

		// If we're empty then return the CoverItemHolder for the selected view,
		// if there is one. If we're not empty then the selected view will be in
		// our children list, so no need to explicitly add its holder.
		if (isEmpty() && mSelectedView != null) {
			Object holder = mSelectedView.getTag(mCoverHolderId);
			if (holder != null) {
				holders.add((CoverItemHolder) holder);
			}
		}

		return holders;
	}

	@Override
	protected void setViewPos(View view, int pos) {
		super.setViewPos(view, pos);
		CoverItemHolder holder = (CoverItemHolder) view.getTag(mCoverHolderId);
		if (holder != null) {
			holder.setPosition(pos);
		}
	}

	@Override
	protected void stackNestedListAdaptor(
			AdapterView<CoverItemPagingCarouselAdapter> nestedListView) {
		if (mCategoryChangeListener != null) {
			int selectedPos = getSelectedItemPosition();
			CoverItemProvider item = (CoverItemProvider) mAdapter
					.getItem(selectedPos);
			if (item != null) {
				String title = item.getTitle();
				if (title == null) {
					title = "";
				}
				mCategoryTitles.push(title);
				mCategoryChangeListener.onCategoryChanged(title);
			}
		}

		super.stackNestedListAdaptor(nestedListView);
	}

	@Override
	protected boolean unstackNestedListAdaptor() {
		boolean restored = false;
		if (!mNestedListAdapters.isEmpty()) {
			restored = super.unstackNestedListAdaptor();
			mCategoryTitles.pop();
			if (mCategoryTitles.size() > 1) {
				mCategoryChangeListener.onCategoryChanged(mCategoryTitles
						.lastElement());
			} else {
				mCategoryChangeListener.onCategoryChanged("");
			}
		}
		return restored;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO(steveT): Remove this image prefetching code
		if (keyCode == KeyEvent.KEYCODE_P) {
			prefetchImages();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	// TODO(steveT): Remove this image prefetching code
	private void prefetchImages() {
		if (mAdapter == null) {
			return;
		}

		final int maxNumPrefetchImages = 30;
		if (mAdapter.getCount() > maxNumPrefetchImages) {
			mToast.setText(String.format(
					"List too large (%d items). Not prefetching.",
					mAdapter.getCount()));
			mToast.show();
			return;
		}

		final int imagesToPrefetch = Math.min(mAdapter.getCount(),
				maxNumPrefetchImages);
		mToast.setText(String.format("Prefetching %d images", imagesToPrefetch));
		mToast.show();

		final long checkLoadedIntervalMs = 1000;
		final Handler handler = new Handler();
		Runnable checkLoadedRunnable = new Runnable() {
			public void run() {
				handler.postDelayed(this, checkLoadedIntervalMs);
			}
		};
		handler.postDelayed(checkLoadedRunnable, checkLoadedIntervalMs);
	}
}
