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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sample.tom.uiwidgetssample.R;

import java.util.List;

/**
 * This adapter is used for the tabs on the left side of a details activity
 */
class DetailsTabAdapter extends ArrayAdapter<com.sample.tom.asbuilibrary.details.DetailsTabInfo>
{
    /**
     * @param context A {@link Context}
     * @param tabInfo A list of the tabs to show
     * @param list The listView which will show these tabs
     */
    public DetailsTabAdapter(Context context, List<com.sample.tom.asbuilibrary.details.DetailsTabInfo> tabInfo)
    {
        super(context, 0, tabInfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Grab the view
        TextView view = (TextView) convertView;

        if (view == null)
        {
            // No view to recycle, create a new one
            view = (TextView) View.inflate(getContext(), R.layout.details_action_view, null);
        }

        // Set the text
        view.setText(getItem(position).getTabName());

        return view;
    }
}
