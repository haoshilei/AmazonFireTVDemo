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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.amazon.asbuilibrary.list.CarouselView;
import com.sample.amazon.asbuilibrary.list.adapter.BasePagingCarouselAdapter;
import com.sample.amazon.uiwidgetssample.R;

/**
 * A carousel adapter showing how the paging mechanism works
 */
public class SamplePagingCarouselActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.carousel_activity);

        // Create our adapter
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 200; i++)
        {
            list.add(i);
        }

        PagingBoxesCarouselAdapter adapter = new PagingBoxesCarouselAdapter(list);

        // Set our adapter on our carousel
        @SuppressWarnings("unchecked")
        CarouselView<BasePagingCarouselAdapter<Integer, Object>> boxesCarousel = (CarouselView<BasePagingCarouselAdapter<Integer, Object>>) findViewById(R.id.boxes_carousel);
        boxesCarousel.setAdapter(adapter);
        adapter.attachToCarousel(boxesCarousel);
    }

    /**
     * Adapter for our Carousel to show simple boxes with text, and toast when items are requested
     */
    private class PagingBoxesCarouselAdapter extends BasePagingCarouselAdapter<Integer, Object>
    {
        public PagingBoxesCarouselAdapter(List<Integer> list)
        {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Does our view exist?
            if (convertView == null)
            {
                convertView = LayoutInflater.from(SamplePagingCarouselActivity.this).inflate(R.layout.carousel_item,
                        parent, false);
            }

            // Set the text
            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText(Integer.toString(position));

            return convertView;
        }

        @Override
        protected void requestItemData(List<Integer> listOfIdsToRequest)
        {
            if (!listOfIdsToRequest.isEmpty())
            {
                // First sort the data
                Collections.sort(listOfIdsToRequest);

                // Show the largest and smallest item requested
                Toast.makeText(
                        SamplePagingCarouselActivity.this,
                        "IDs: " + listOfIdsToRequest.get(0) + " - "
                                + listOfIdsToRequest.get(listOfIdsToRequest.size() - 1) + " requested!",
                        Toast.LENGTH_LONG).show();

                // You can put the retrieved data in mItemMap and it will be returned on a call to getItem(int)
            }
        }
    }
}
