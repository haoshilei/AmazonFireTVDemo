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

package com.sample.tom.asbuilibrary.list.handler;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * Item list handler for the mini details. This takes care of updating the mini details data and
 * handling the click events
 */
public class BaseMiniDetailsItemListHandler extends ItemListHandler implements ItemListListener,
        AdapterView.OnFocusChangeListener
{
    protected final ViewGroup mViewHolder;

    public BaseMiniDetailsItemListHandler(Context context, ViewGroup holder)
    {
        super(context);
        mContext = context;
        mViewHolder = holder;
    }

    public void onReturnToMiniDetails(AdapterView<?> adapterView)
    {
    }

    @Override
    public void onItemListChanged(AdapterView<?> itemList)
    {
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus)
    {
    }
}
