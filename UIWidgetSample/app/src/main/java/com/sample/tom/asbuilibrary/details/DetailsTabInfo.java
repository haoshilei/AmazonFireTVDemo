/**
 * Copyright 2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 *  Licensed under the Amazon Software License (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License
 *  is located at
 *   				http://aws.amazon.com/asl/
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 *  implied. See the License for the specific language governing permissions
 *  and limitations under the License. 
 */

package com.sample.tom.asbuilibrary.details;

import android.app.Fragment;

/**
 * This class contains the information needed to display a tab used on the details activities
 */
public class DetailsTabInfo
{
    private final String mTabName;
    private final Fragment mFragment;

    /**
     * @param tabName The unique user visible name for this tab
     * @param fragment The fragment to show when this tab is selected
     */
    public DetailsTabInfo(String tabName, Fragment fragment)
    {
        mTabName = tabName;
        mFragment = fragment;
    }

    /**
     * @return The unique user visible string for this tab
     */
    public String getTabName()
    {
        return mTabName;
    }

    /**
     * @return The Fragment to show when switching to this tab
     */
    public Fragment getFragment()
    {
        return mFragment;
    }
}
