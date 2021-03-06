package com.faith.earthquaketracker.UI;
//Mbatha Faith S1803443

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.faith.earthquaketracker.models.EarthQuake;
import com.faith.earthquaketracker.Adapters.EarthQuakeListAdapter;
import com.faith.earthquaketracker.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //defining variables

    public final String TAG = "MainActivity";


    private List<EarthQuake> mEarthQuakeList;


    EarthQuakeListAdapter earthQuakeListAdapter;

    String filter;

    //defining views

    private SwipeRefreshLayout mSwipeLayout;
    RecyclerView recyclerView;
    EditText searchEt;
    Spinner filterSpin;

    ArrayList<EarthQuake> filteredNames = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing views

        recyclerView = findViewById(R.id.recyclerView);

        mSwipeLayout = findViewById(R.id.swipe);

        searchEt = findViewById(R.id.searchEt);

        filterSpin = findViewById(R.id.filterSpin);


        filterSpin.setVisibility(View.INVISIBLE);


        //setting layout manager to RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //calling method to fetch earthquakes
        new FetchQuakeTask().execute((Void) null);

        // Initializing a String Array
        String[] options = new String[]{
                "Select search option...",
                "Closest to Mauritius",
                "Largest Magnitude",
                "Deepest",
                "Shallowest"
        };

        final List<String> optionsList = new ArrayList<>(Arrays.asList(options));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item,optionsList){
            @Override
            public boolean isEnabled(int position){
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        filterSpin.setAdapter(spinnerArrayAdapter);

        filterSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    filter = selectedItemText;
                    filterMore(filter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
                filterSpin.setVisibility(View.VISIBLE);
            }
        });


    }

    private void filter(String toString) {

        //new array list that will hold the filtered data


        for (EarthQuake earthQuake : mEarthQuakeList) {
            if (earthQuake.getPubDate().toLowerCase().contains(toString.toLowerCase())) {
                filteredNames.add(earthQuake);
            }

        }


        earthQuakeListAdapter.filterList(filteredNames);


    }

    private void filterMore(String filterParams) {
        //new array list that will hold the filtered data
        ArrayList<EarthQuake> filtered = new ArrayList<>();

        //variables
        Double largest_magnitude;
        Double deepest;
        Double shallowest;
        String location;
        String depth;
        String magnitude;
        String[] locations;
        String[] depths;
        String[] magnitudes;
        String locashen;
        String depthdd;
        String magnituded;


        //ArrayLists to Store individual entities

        ArrayList<String> loctn = new ArrayList<>();
        ArrayList<Double> mgtd = new ArrayList<>();
        ArrayList<Double> dpth = new ArrayList<>();


        for (EarthQuake earthQuake : filteredNames) {

            //Getting description from earthquake
            String description = earthQuake.description;

            //splitting the description String
            String[] descriptions = description.split(";");
            location = descriptions[1];
            depth = descriptions[3];
            magnitude = descriptions[4];

            //Splitting the individual entities to get specific values for comparison
            locations = location.split(":");
            depths = depth.split(":");
            magnitudes = magnitude.split(":");
            locashen = locations[1];
            depthdd = depths[1];
            magnituded = magnitudes[1];


            //populating the individual entity arrays for comparison
            loctn.add(locashen);
            mgtd.add(Double.parseDouble(magnituded.trim()));
            dpth.add(Double.parseDouble(depthdd.replaceAll("km", "").trim()));

            //checking for highest values

            largest_magnitude = Collections.max(mgtd);
            deepest = Collections.max(dpth);
            shallowest = Collections.min(dpth);

            //populating new ArrayList based on user choices

            for(int i = 0; i < mgtd.size(); i++) {
                if(filterParams.toLowerCase().matches("Largest Magnitude") && largest_magnitude >= mgtd.get(i)){
                    filtered.add(earthQuake);
                }
            }
            for(int i = 0; i < dpth.size(); i++) {
                if(filterParams.toLowerCase().matches("Deepest") && deepest >= dpth.get(i)){
                    filtered.add(earthQuake);
                }
               else if(filterParams.toLowerCase().matches("Shallowest") && shallowest <= dpth.get(i)){
                    filtered.add(earthQuake);
                }
            }


        }



        earthQuakeListAdapter.filterList(filtered);



    }


    //pulling XML data
    private class FetchQuakeTask extends AsyncTask<Void, Void, Boolean> {

        String urlLink;

        //method used to pull XML data initially
        @Override
        protected void onPreExecute() {
            urlLink = "http://quakes.bgs.ac.uk/feeds/WorldSeismology.xml";
            mSwipeLayout.setRefreshing(false);
        }

        //background task
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mEarthQuakeList = parseFeed(inputStream);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        //parsing the XML
        private List<EarthQuake> parseFeed(InputStream inputStream) throws XmlPullParserException,
                IOException {
            String title = null;
            String link = null;
            String category = null;
            String date = null;
            String lat = null;
            String longtd = null;
            String description = null;
            boolean isItem = false;


            List<EarthQuake> earthQuakes = new ArrayList<>();


            try {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xmlPullParser.setInput(inputStream, null);

                xmlPullParser.nextTag();

                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                    int eventType = xmlPullParser.getEventType();

                    String name = xmlPullParser.getName();

                    if (name == null)
                        continue;


                    if (eventType == XmlPullParser.END_TAG) {
                        if (name.equalsIgnoreCase("item")) {
                            isItem = false;
                        }
                        continue;
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (name.equalsIgnoreCase("item")) {
                            isItem = true;
                            continue;
                        }
                    }

                    Log.d("MyXmlParser", "Parsing name ==> " + name);
                    String result = "";
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.getText();
                        xmlPullParser.nextTag();
                    }

                    if (name.equalsIgnoreCase("title")) {
                        title = result;
                    } else if (name.equalsIgnoreCase("link")) {
                        link = result;
                    } else if (name.equalsIgnoreCase("category")) {
                        category = result;
                    } else if (name.equalsIgnoreCase("pubDate")) {
                        date = result;
                    } else if (name.equalsIgnoreCase("geo:lat")) {
                        lat = result;
                    } else if (name.equalsIgnoreCase("geo:long")) {
                        longtd = result;
                    } else if (name.equalsIgnoreCase("description")) {
                        description = result;
                    }
                    if (title != null && link != null && category != null && date != null && lat != null && longtd != null) {
                        if (isItem) {
                            EarthQuake item = new EarthQuake(title, link, category, date, longtd, lat, description);
                            earthQuakes.add(item);

                        }


                        title = null;
                        link = null;
                        category = null;
                        description = null;
                        longtd = null;
                        lat = null;
                        isItem = false;
                    }
                }
                return earthQuakes;
            } finally {
                inputStream.close();
            }


        }

        //displaying the XML
        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                earthQuakeListAdapter = new EarthQuakeListAdapter(mEarthQuakeList);
                // Fill RecyclerView
                recyclerView.setAdapter(earthQuakeListAdapter);
            }
        }
    }
}
