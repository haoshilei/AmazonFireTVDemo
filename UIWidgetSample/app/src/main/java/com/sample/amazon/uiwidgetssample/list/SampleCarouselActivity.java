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

package com.sample.amazon.uiwidgetssample.list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.list.CarouselView;

/**
 * Activity for showing a simple carousel
 */
public class SampleCarouselActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.carousel_activity);

		// Create our adapter
		BoxesCarouselAdapter adapter = new BoxesCarouselAdapter(this);
		for (int i = 0; i < 50; i++) {
			adapter.add(Integer.toString(i));
		}

		// Set our adapter on our carousel
		@SuppressWarnings("unchecked")
		CarouselView<BoxesCarouselAdapter> boxesCarousel = (CarouselView<BoxesCarouselAdapter>) findViewById(R.id.boxes_carousel);
		boxesCarousel.setAdapter(adapter);
	}

	/**
	 * Adapter for our Carousel to show simple boxes with text
	 */
	private class BoxesCarouselAdapter extends ArrayAdapter<String> {
		public BoxesCarouselAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Does our view exist?
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.carousel_item, parent, false);
			}

			// Set the text
			TextView text = (TextView) convertView.findViewById(R.id.text);
			text.setText(getItem(position));

			return convertView;
		}
	}

}
