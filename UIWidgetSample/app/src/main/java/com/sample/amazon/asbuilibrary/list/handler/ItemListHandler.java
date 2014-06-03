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

package com.sample.amazon.asbuilibrary.list.handler;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import com.sample.amazon.asbuilibrary.list.ItemView;

/**
 * This class is used to handle interaction events on a ContentItem in a CarouselView. This is
 * designed to work with Carousels which have an {@link com.sample.amazon.asbuilibrary.list.adapter.CoverItemPagingCarouselAdapter}
 */
public class ItemListHandler implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener
{
    protected Context mContext;
    protected ItemView mLastSelected;

    public ItemListHandler(Context context)
    {
        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // We should be getting back an ItemView, otherwise our adapter is messed up.
        ItemView itemView = (ItemView) view;
        
        if (itemView != null)
        {
            itemView.onItemClick();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (view == null)
        {
            return;
        }
        
        if (mLastSelected != null)
        {
            mLastSelected.onCarouselSelectionChanged(false);
        }
        
        mLastSelected = (ItemView) view;
        mLastSelected.onCarouselSelectionChanged(true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // no op
    }
}
