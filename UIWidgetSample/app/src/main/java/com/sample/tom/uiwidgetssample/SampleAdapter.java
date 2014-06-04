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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.tom.asbuilibrary.list.CoverItemProvider;
import com.sample.tom.asbuilibrary.list.ItemView;
import com.sample.tom.asbuilibrary.list.RollupView;
import com.sample.tom.asbuilibrary.list.RollupView.RollupState;
import com.sample.tom.asbuilibrary.list.adapter.CoverItemPagingCarouselAdapter;
import com.sample.tom.asbuilibrary.view.CoverImageView;

public class SampleAdapter extends CoverItemPagingCarouselAdapter<String>
{
    Drawable[] mCovers = new Drawable[5];

    public SampleAdapter(Context context, List<String> idList, List<SampleItem> itemList)
    {
        super(context, idList, 0, R.id.menu_option);
        mCovers[0] = context.getResources().getDrawable(R.drawable.cover1);
        mCovers[1] = context.getResources().getDrawable(R.drawable.cover5);
        mCovers[2] = context.getResources().getDrawable(R.drawable.cover4);
        mCovers[3] = context.getResources().getDrawable(R.drawable.cover2);
        mCovers[4] = context.getResources().getDrawable(R.drawable.cover3);

        TreeMap<String, List<SampleItem>> allItems = new TreeMap<String, List<SampleItem>>();

        for (SampleItem item : itemList)
        { // we need to explicitly populate the map upfront since we're not doing delay-loading
            List<SampleItem> destin = allItems.get(item.getType());
            if (destin == null)
            {
                destin = new ArrayList<SampleItem>();
                allItems.put(item.getType(), destin);
            }
            destin.add(item);
        }

        if (allItems.size() == 1)
        {
            for (SampleItem item : allItems.firstEntry().getValue())
            {
                mItemMap.put(item.getId(), item);
            }
        }
        else
        {
            List<String> idListChanged = new ArrayList<String>();
            int stackCount = 1;
            for (TreeMap.Entry<String, List<SampleItem>> entry : allItems.entrySet())
            {
                List<SampleItem> items = entry.getValue();
                if (items.size() == 1)
                {
                    mItemMap.put(items.get(0).getId(), items.get(0));
                    idListChanged.add(items.get(0).getId());
                }
                else if (items.size() > 1)
                {
                    mItemMap.put(items.get(0).getType(), new SampleItem(items.get(0).getType(), "Stack " + (stackCount++), items));
                    idListChanged.add(items.get(0).getType());
                }
            }
            mList = idListChanged;
        }
    }

    @Override
    protected void requestItemData(List<String> listOfIdsToRequest)
    {
    }

    @Override
    protected ItemView getNewView(int position, CoverItemProvider<?> item, ViewGroup parent)
    {
        SampleItem realItem = (SampleItem)item;
        SampleItemView newView = (SampleItemView)LayoutInflater.from(mContext).inflate(R.layout.main_option_item, parent, false);
        CoverImageView cover = (CoverImageView)newView.findViewById(R.id.image);
        TextView text = (TextView)newView.findViewById(R.id.text);
        RollupView rollup = (RollupView)newView.findViewById(R.id.carousel_nestedlist_item);

        if (realItem.getChildren() == null)
        {
            cover.setImageDrawable(mCovers[position % 5]);
            rollup.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
        }
        else
        {
            List<String> idList = new ArrayList<String>();
            for (SampleItem child : realItem.getChildren())
            {
                idList.add(child.getId());
            }
            SampleAdapter stackAdapter = new SampleAdapter(mContext, idList, realItem.getChildren());
            rollup.setAdapter(stackAdapter);
            rollup.setState(RollupState.Collapsed);
            rollup.setVisibility(View.VISIBLE);
            text.setVisibility(View.GONE);
        }

        newView.showItem(item);
        return newView;
    }

    @Override
    protected boolean doesViewTypeMatchItem(View v, CoverItemProvider<?> item) {
        return true;
    }
}
