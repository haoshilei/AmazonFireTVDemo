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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.view.FontableTextView;

public class ChannelData
{
    private ItemCarouselView mCarousel;
    private String mTitle;

    public static ChannelData create(Context context, String title)
    {
        View container = LayoutInflater.from(context).inflate(R.layout.carousel_movies_and_tv, null, false);
        ItemCarouselView carousel = (ItemCarouselView) container.findViewById(R.id.cover_list);
        carousel.setName(title);
        carousel.setTranslationX((int) carousel.getResources().getDimension(R.dimen.tombstone_width)
                - carousel.getSelectionOffset() + carousel.getSpacing());
        View tombstone = container.findViewById(R.id.tombstone);
        tombstone.setVisibility(View.VISIBLE);
        FontableTextView tombstoneTitle = (FontableTextView) tombstone.findViewById(R.id.tombstone_text);
        tombstoneTitle.setText(title);
        return new ChannelData(carousel, title);
    }

    private ChannelData(ItemCarouselView carousel, String title)
    {
        mCarousel = carousel;
        mTitle = title;
    }

    public ItemCarouselView getCarousel()
    {
        return mCarousel;
    }

    public String getTitle()
    {
        return mTitle;
    }
}
