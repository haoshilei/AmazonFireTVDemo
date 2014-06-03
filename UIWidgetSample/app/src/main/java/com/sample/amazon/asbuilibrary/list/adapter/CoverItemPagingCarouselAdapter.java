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

import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.sample.amazon.asbuilibrary.list.CoverItemProvider;
import com.sample.amazon.asbuilibrary.list.ItemView;

/**
 * CoverItemPagingCarouselAdapter Adapter for use in Carousels where the data is
 * obtained on-demand, and the individual carousel items show a CoverItem (e.g.
 * an image). The data items to be shown must implement the CoverItemProvider
 * interface.
 * 
 * @param <IdentifierType>
 *            The datatype to use as the individual data element identifiers,
 *            i.e. the key type to use in the data element map defined in the
 *            BasePagingCarouselAdapter.
 */
abstract public class CoverItemPagingCarouselAdapter<IdentifierType> extends
		BasePagingCarouselAdapter<IdentifierType, CoverItemProvider<?>> {
	protected final Context mContext;
	protected final Drawable mDefaultImage;
	protected final int mCoverHolderId;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context to act in
	 * @param list
	 *            the set of data element identifiers that will comprise this
	 *            adapter's dataset.
	 */
	public CoverItemPagingCarouselAdapter(Context context,
			List<IdentifierType> list, int noImageDrawableId, int coverHolderId) {
		super(list);

		mContext = context;

		mDefaultImage = new BitmapDrawable(context.getResources(),
				BitmapFactory.decodeResource(context.getResources(),
						noImageDrawableId));
		mCoverHolderId = coverHolderId;
	}

	/**
	 * getView View recycling mechanism for the carousel.
	 * 
	 * @param position
	 *            The position in the list
	 * @param convertView
	 *            The view to re-use if supplied
	 * @param parent
	 *            The parent the view will be attached to
	 * @return View to be displayed on screen.
	 */
	public final View getView(final int position, View convertView,
			ViewGroup parent) {
		View returnView = convertView;

		final CoverItemHolder coverHolder;

		CoverItemProvider<?> item = getItem(position);

		if (returnView == null || !doesViewTypeMatchItem(returnView, item)) {
			ItemView itemView = getNewView(position, item, parent);
			returnView = itemView.getItemViewContainer();
			coverHolder = new CoverItemHolder(itemView, mDefaultImage);
			returnView.setTag(mCoverHolderId, coverHolder);
		} else {
			// We are reusing this view
			coverHolder = (CoverItemHolder) returnView.getTag(mCoverHolderId);
		}

		// Set our position on the holder
		coverHolder.setPosition(position);

		coverHolder.updateUIForItem(item);

		return returnView;
	}

	// public ImageManager getImageManager() {
	// return mImageManager;
	// }

	/**
	 * Says whether a specific view can be used to represent a specific item. It
	 * is up to the implementor of
	 * {@link #getNewView(int, CoverItemProvider, ViewGroup)} to determine if a
	 * view can be recycled or not for the given item. As a rule of thumb, if
	 * the adapter only supports a single type of item/view, this should return
	 * true. <b>If you override this method, make sure you override
	 * {@link #getNewView(int, CoverItemProvider, ViewGroup)} accordingly. </b>
	 */
	abstract protected boolean doesViewTypeMatchItem(View v,
			CoverItemProvider<?> item);

	/**
	 * This is called when a view is not being recycled. Return a new view based
	 * on the position and the item provided. <b>If you override this method,
	 * make sure you override
	 * {@link #doesViewTypeMatchItem(View, CoverItemProvider)} accordingly.</b>
	 */
	abstract protected ItemView getNewView(int position,
			CoverItemProvider<?> item, ViewGroup parent);
}
