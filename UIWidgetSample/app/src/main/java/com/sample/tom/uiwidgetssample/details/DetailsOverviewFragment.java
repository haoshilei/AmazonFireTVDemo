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

package com.sample.tom.uiwidgetssample.details;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.tom.uiwidgetssample.R;

public class DetailsOverviewFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.details_overview_fragment, container, false);
    }
}
