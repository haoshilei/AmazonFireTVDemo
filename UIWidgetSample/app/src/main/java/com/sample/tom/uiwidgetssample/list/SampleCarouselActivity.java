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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import com.sample.tom.uiwidgetssample.R;
import com.sample.tom.asbuilibrary.list.CarouselView;
import com.sample.tom.uiwidgetssample.details.SampleDetailsActivity;

import java.io.*;

import org.json.*;

/**
 * Activity for showing a simple carousel
 */

public class SampleCarouselActivity extends Activity  {

    static TextView description;
    static TextView name;
    ImageView pic;
    static JSONArray jran = new JSONArray();


 	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		setContentView(R.layout.carousel_activity);
        description = (TextView) findViewById(R.id.description2);
        pic = (ImageView) findViewById(R.id.pic);
        name = (TextView) findViewById(R.id.name2);
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
                final BoxesCarouselAdapter adapter = new BoxesCarouselAdapter(SampleCarouselActivity.this);

                jran = json.getJSONArray("Cakes");
                for( int i = 0; i < jran.length(); i++) {
                    try {

                        String jName = jran.getJSONObject(i).getString("name");
                        String jDescription = jran.getJSONObject(i).getString("description");
                        String jUrl = jran.getJSONObject(i).getString("imageURL");
                        adapter.add(new Cake(jName, jDescription, jUrl ));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @SuppressWarnings("unchecked")
                CarouselView<BoxesCarouselAdapter> boxesCarousel = (CarouselView<BoxesCarouselAdapter>) findViewById(R.id.boxes_carousel);
                boxesCarousel.setAdapter(adapter);
                boxesCarousel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(SampleCarouselActivity.this, SampleDetailsActivity.class);
                        intent.putExtra("cake", adapter.getItem(position));
                        startActivity(intent);
                    }
                });
                boxesCarousel.addItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            Log.v("DEBUG", SampleCarouselActivity.jran.getJSONObject(position).getString("description"));
                            SampleCarouselActivity.this.description.setText(jran.getJSONObject(position).getString("description"));
                            SampleCarouselActivity.this.name.setText(jran.getJSONObject(position).getString("name"));

//                            SampleCarouselActivity.this.pic.setImageBitmap();
                            new DownloadImageTask(new DownloadImageTask.ImageLoadedCallback() {
                                @Override
                                public void onImageLoaded(Bitmap image) {
                                    Log.v("img", image.toString());
                                    Log.v("pic", pic.toString());
                                    SampleCarouselActivity.this.pic.setImageBitmap(image);

                                }

                            }).execute(jran.getJSONObject(position).getString("imageURL"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } catch(JSONException e) { e.printStackTrace(); }


            // Set our adapter on our carousel

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
            final ImageView imageView = (ImageView)convertView.findViewById(R.id.image);

            new DownloadImageTask(new DownloadImageTask.ImageLoadedCallback() {
                @Override
                public void onImageLoaded(Bitmap image) {
                    imageView.setImageBitmap(image);

                }

            }).execute(getItem(position).getUrl());
            return convertView;
		}
    }

}
