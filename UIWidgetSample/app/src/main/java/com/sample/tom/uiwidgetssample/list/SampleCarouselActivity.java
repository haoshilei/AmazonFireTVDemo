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

package com.sample.tom.uiwidgetssample.list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.list.CarouselView;

import org.json.JSONException;

import java.io.*;
import java.net.*;
import org.json.*;
import java.nio.charset.MalformedInputException;

/**
 * Activity for showing a simple carousel
 */
public class SampleCarouselActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        BoxesCarouselAdapter adapter = new BoxesCarouselAdapter(this);
		setContentView(R.layout.carousel_activity);

		// Create our adapter
        try {

            URL cakeList = new URL("http://localhost:9292/cakeList.json");
            try {
                StringBuffer json = new StringBuffer();
                BufferedReader in = new BufferedReader(new InputStreamReader(cakeList.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);
                in.close();

                for (int i = 0; i < 50; i++) {
                    adapter.add(new Cake("name", "description", "url"));
                }
                JSONObject jObject = new JSONObject(json.toString());
                JSONArray jArray = jObject.getJSONArray("Cakes");

                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String oneObjectsItem = oneObject.getString("STRINGNAMEinTHEarray");
                        String oneObjectsItem2 = oneObject.getString("anotherSTRINGNAMEINtheARRAY");
                        adapter.add(new Cake(oneObjectsItem, oneObjectsItem2, "url"));

                    } catch (JSONException e) {
                        // Oops
                    }
                }
            } catch(IOException x) { x.printStackTrace(); } catch (JSONException e) {
                e.printStackTrace();
            }


            //JSONObject json = jParser.getJSONFromUrl(url);
        } catch (MalformedURLException e) { e.printStackTrace(); }

//		BoxesCarouselAdapter adapter = new BoxesCarouselAdapter(this);
//		for (int i = 0; i < 50; i++) {
//			adapter.add(new Cake("name", "description", "url"));
//		}

		// Set our adapter on our carousel
		@SuppressWarnings("unchecked")
		CarouselView<BoxesCarouselAdapter> boxesCarousel = (CarouselView<BoxesCarouselAdapter>) findViewById(R.id.boxes_carousel);
		boxesCarousel.setAdapter(adapter);
	}

    private class Cake {
        private String name;
        private String description;
        private String url;

        public Cake(String _name, String _description, String _url) {
            this.name = _name;
            this.description = _description;
            this.url = _url;
        }
        public String getName() {
            return this.name;
        }
        public String getDescription() {
            return this.description;
        }
        public String getUrl() {
            return this.url;
        }
    }

	/**
	 * Adapter for our Carousel to show simple boxes with text
	 */
	private class BoxesCarouselAdapter extends ArrayAdapter<Cake> {
		public BoxesCarouselAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Does our view exist?
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.carousel_item, parent, false);
			}

			// Set the text
			TextView name = (TextView) convertView.findViewById(R.id.text);
			name.setText(getItem(position).getName());
            TextView description = (TextView) convertView.findViewById(R.id.text);
            description.setText(getItem(position).getDescription());
           // TextView url = (TextView) convertView.findViewById(R.id.url);
            //url.setText(getItem(position).getUrl());
			return convertView;
		}
	}

}
