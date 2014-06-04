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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.net.URL;
import java.util.*;
import java.nio.charset.Charset;
import java.lang.Object;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

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
public class SampleCarouselActivity extends Activity  {

 	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		setContentView(R.layout.carousel_activity);

            new DownloadFilesTask().execute();

        }

    private class DownloadFilesTask extends AsyncTask<Void, Integer, JSONObject> {
        protected JSONObject doInBackground(Void... avoid) {
            JSONObject json = null;
            try {
                json = readJsonFromUrl("http://10.0.1.8:9292/pastries.json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return json;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        protected void onPostExecute(JSONObject json) {
            try {
                BoxesCarouselAdapter adapter = new BoxesCarouselAdapter(SampleCarouselActivity.this);
                JSONArray jran = json.getJSONArray("Cakes");
             //   JSONArray jrad = json.getJSONArray("description");
                for( int i = 0; i < jran.length(); i++) {
                    try {

                        String jName = jran.getJSONObject(i).getString("name");
                        String jDescription = jran.getJSONObject(i).getString("description");
                        Log.v(jName.toString(), "here");
                        adapter.add(new Cake(jName, jDescription, "url"));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @SuppressWarnings("unchecked")
                CarouselView<BoxesCarouselAdapter> boxesCarousel = (CarouselView<BoxesCarouselAdapter>) findViewById(R.id.boxes_carousel);
                boxesCarousel.setAdapter(adapter);
            } catch(JSONException e) { e.printStackTrace(); }


            // Set our adapter on our carousel

        }
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
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
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
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(getItem(position).getName());
            TextView description = (TextView) convertView.findViewById(R.id.description);
            description.setText(getItem(position).getDescription());
           // TextView url = (TextView) convertView.findViewById(R.id.url);
            //url.setText(getItem(position).getUrl());
			return convertView;
		}
	}

}
