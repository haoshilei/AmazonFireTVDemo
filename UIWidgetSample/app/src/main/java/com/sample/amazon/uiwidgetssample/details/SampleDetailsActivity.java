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

package com.sample.amazon.uiwidgetssample.details;

import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.sample.amazon.uiwidgetssample.R;
import com.sample.amazon.asbuilibrary.details.DetailsBaseActivity;
import com.sample.amazon.asbuilibrary.details.DetailsTabInfo;

public class SampleDetailsActivity extends DetailsBaseActivity
{
    private List<DetailsTabInfo> mMenuOptions = new ArrayList<DetailsTabInfo>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Add our menu options
        mMenuOptions.add(new DetailsTabInfo(getString(R.string.overview_tab), new DetailsOverviewFragment()));
        mMenuOptions.add(new DetailsTabInfo(getString(R.string.extra_tab), new DetailsExtraFragment()));

        // Set our cover
        setCoverImage(BitmapFactory.decodeResource(getResources(), R.drawable.cover1));
    }

    @Override
    protected List<DetailsTabInfo> getMenuOptions()
    {
        return mMenuOptions;
    }

    @Override
    protected int getMaxCoverHeight()
    {
        return getResources().getDimensionPixelSize(R.dimen.detail_cover_height);
    }

    @Override
    protected int getMaxCoverWidth()
    {
        return getResources().getDimensionPixelSize(R.dimen.detail_cover_width);
    }
}
