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

package com.sample.amazon.asbuilibrary.details;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sample.amazon.uiwidgetssample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Fragment} shown on the Details page which gives a list of options for the user.
 */
public class DetailsMenuListFragment extends ListFragment
{
    private List<DetailsTabInfo> mTabList = new ArrayList<DetailsTabInfo>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Make sure that the inflated view contains a view with R.id.list!
        return inflater.inflate(R.layout.details_menu_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        // Called after onCreateView
        super.onActivityCreated(savedInstanceState);

        // Set our list's adapter
        setListAdapter(new DetailsTabAdapter(getActivity(), mTabList));
    }

    /**
     * @return The coverview for this fragment
     */
    ImageView getCoverView()
    {
        final View rootView = getView();

        if (rootView != null)
        {
            return (ImageView) rootView.findViewById(R.id.cover);
        }
        else
        {
            return null;
        }
    }

    /**
     * @param menuOptions The tabs you with to add to the menu options
     */
    void setMenuOptions(List<DetailsTabInfo> menuOptions)
    {
        if (getActivity() == null)
        {
            // If the activity is not created yet, just set this
            mTabList = menuOptions;
        }
        else
        {
            // If this has been set, then we need to create a new Adapter (it gets glitchy otherwise).
            mTabList = menuOptions;
            setListAdapter(new DetailsTabAdapter(getActivity(), mTabList));
        }
    }

    /**
     * @return The current List of {@link DetailsTabInfo} options in the menu.
     */
    List<DetailsTabInfo> getCurrentMenuOptions()
    {
        return mTabList;
    }
}
