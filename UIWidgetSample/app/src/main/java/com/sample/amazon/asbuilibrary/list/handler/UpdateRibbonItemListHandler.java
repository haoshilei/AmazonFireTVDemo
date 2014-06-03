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

import android.view.View;
import android.widget.AdapterView;

import com.sample.amazon.asbuilibrary.util.BreadCrumbRibbon;

/**
 * This is an ItemListHandler that updates a Bread Crumb Ribbon to show the index of the currently
 * selected item.
 */
public class UpdateRibbonItemListHandler implements AdapterView.OnItemSelectedListener,
                                                 ItemListListener
{
    private BreadCrumbRibbon mBreadCrumbRibbon;

    public UpdateRibbonItemListHandler(BreadCrumbRibbon breadCrumbRibbon)
    {
        mBreadCrumbRibbon = breadCrumbRibbon;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // Update the title ribbon message to show the index of the selected content item
        mBreadCrumbRibbon.setItemIndexInCategory((int) id);
        mBreadCrumbRibbon.setNumTitlesInCategory(parent.getAdapter().getCount());
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
    }

    @Override
    public void onItemListChanged(AdapterView<?> itemList)
    {
        mBreadCrumbRibbon.setNumTitlesInCategory(itemList.getAdapter().getCount());
        updateUI();
    }

    protected void updateUI()
    {
        if (mBreadCrumbRibbon.getVisibility() == View.VISIBLE)
        {
            mBreadCrumbRibbon.displayUiNavigationState();
        }
    }
}
