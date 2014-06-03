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

package com.sample.amazon.uiwidgetssample;

import java.util.ArrayList;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.sample.amazon.asbuilibrary.fragment.BaseCarouselListFragment;
import com.sample.amazon.asbuilibrary.list.ChannelData;
import com.sample.amazon.asbuilibrary.list.ItemCarouselView;
import com.sample.amazon.asbuilibrary.list.handler.MiniDetailsItemListHandler;
import com.sample.amazon.asbuilibrary.util.CarouselZoom;
import com.sample.amazon.uiwidgetssample.details.SampleDetailsActivity;
import com.sample.amazon.uiwidgetssample.keyboard.KeyboardInputActivity;
import com.sample.amazon.uiwidgetssample.list.SampleCarouselActivity;
import com.sample.amazon.uiwidgetssample.list.SamplePagingCarouselActivity;
import com.sample.amazon.uiwidgetssample.R;

public class MainFragment extends BaseCarouselListFragment {
	private static final String TAG = MainFragment.class.getSimpleName();
	private static final int SAMPLES_COUNT = 2;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Create our mini details handler here, using the details holder view.
		// If not using the base carousel list fragment, add the mini details
		// item list handler
		// as the focus, selection, click, and list changed listeners on a
		// CarouselView.
		setItemSelectedHandler(new MiniDetailsItemListHandler(getActivity(),
				mMiniDetailsHolder));
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!isHidden()) {
			super.fragmentShow();
		}

	}

	@Override
	public void onStop() {
		super.fragmentHide();
		super.onStop();
	}

	@Override
	protected boolean isLoadComplete() {
		return getListAdapter().getCount() == SAMPLES_COUNT;
	}

	@Override
	protected void loadScreenData() {
		if (isLoadComplete()) { // already loaded data
			Log.w(TAG, "Trying to load screen data that is already loaded");
			return;
		}

		Log.d(TAG, "Filling up the carousels");
		getListAdapter().addItemList(createCarouselSamplesChannelData());
		getListAdapter().addItemList(createDetailsSamplesChannelData());
	}

	@Override
	protected Handler getMainHandler() {
		return null;
	}

	@Override
	public Drawable getFragmentBreadCrumbIconDrawable() {
		return getResources().getDrawable(R.drawable.breadcrumb_icon);
	}

	@Override
	public boolean hasControl() {
		return getParent().hasControl();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		super.onItemClick(parent, view, position, id);
	}

	SampleAppActivity getParent() {
		SampleAppActivity activity = (SampleAppActivity) getActivity();
		return activity;
	}

	private ChannelData createCarouselSamplesChannelData() {
		Log.d(TAG, "Creating channel data for Carousel Samples");
		String categoryTitle = getString(R.string.category_carousel_samples);
		ChannelData channelData = ChannelData.create(getActivity(),
				categoryTitle);

		ArrayList<SampleItem> itemList = new ArrayList<SampleItem>();
		ArrayList<String> idList = new ArrayList<String>();

		/* Carousel */
		String title = getString(R.string.menu_carousel_sample);
		String id = title.toLowerCase(Locale.US).replace(" ", ".");
		idList.add(id);
		itemList.add(new SampleItem(id, "menu_option", title,
				SampleCarouselActivity.class));
		/* Paging carousel */
		title = getString(R.string.menu_paging_sample);
		id = title.toLowerCase(Locale.US).replace(" ", ".");
		idList.add(id);
		itemList.add(new SampleItem(id, "menu_option", title,
				SamplePagingCarouselActivity.class));

		ItemCarouselView carousel = channelData.getCarousel();
		carousel.setUnselectedItemScale(Float
				.parseFloat(getString(R.string.carousel_unselected_item_scale)));
		carousel.setAdapter(new SampleAdapter(getActivity(), idList, itemList));

		return channelData;
	}

	private ChannelData createDetailsSamplesChannelData() {
		Log.d(TAG, "Creating channel data for Detail Samples");
		String categoryTitle = getString(R.string.category_detail_samples);
		ChannelData channelData = ChannelData.create(getActivity(),
				categoryTitle);

		ArrayList<SampleItem> itemList = new ArrayList<SampleItem>();
		ArrayList<String> idList = new ArrayList<String>();

		/* Detail page */
		String title = getString(R.string.menu_details_sample);
		String id = title.toLowerCase().replace(" ", ".");
		idList.add(id);
		itemList.add(new SampleItem(id, "menu_option", title,
				SampleDetailsActivity.class));

		ItemCarouselView carousel = channelData.getCarousel();
		carousel.setUnselectedItemScale(Float
				.parseFloat(getString(R.string.carousel_unselected_item_scale)));
		carousel.setAdapter(new SampleAdapter(getActivity(), idList, itemList));

		return channelData;
	}

	public boolean isInFullControl() {
		return getZoomLevel() == CarouselZoom.TargetZoomState.ONE_D_LIST;
	}
}
