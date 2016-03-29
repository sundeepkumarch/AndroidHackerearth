package com.hackerearth.mrsun.cube26;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private RecyclerView mRecyclerView;
    private MainRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "MainActivity";
    private List<PGObject> listData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new JSONDownloadTask(this).execute("http://hackerearth.0x10.info/api/payment_portals?type=json&query=list_gateway");


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }



    @Override
    public boolean onQueryTextChange(String query) {
        List<PGObject> filteredModelList = filter(listData, query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<PGObject> filter(List<PGObject> models, String query) {
        query = query.toLowerCase();

        final List<PGObject> filteredModelList = new ArrayList<>();
        for (PGObject model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }
    private class JSONDownloadTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        public JSONDownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            InputStream is = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                is = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                parseJSON(builder.toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return false;
        }

        private void parseJSON(String data) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("payment_gateways");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    PGObject pgObject = new PGObject();
                    pgObject.setId(object.getString("id"));
                    pgObject.setName(object.getString("name"));
                    pgObject.setImage(object.getString("image"));
                    pgObject.setDescription(object.getString("description"));
                    pgObject.setBranding(object.getString("branding"));
                    pgObject.setRating(object.getString("rating"));
                    pgObject.setCurrencies(object.getString("currencies"));
                    pgObject.setSetup_fee(object.getString("setup_fee"));
                    pgObject.setTransaction_fees(object.getString("transaction_fees"));
                    pgObject.setHow_to_document(object.getString("how_to_document"));

                    Log.d(LOG_TAG, "Added ID:" + object.get("id") +"-"+object.getString("name"));
                    listData.add(pgObject);
                }
                Log.d(LOG_TAG, "ListSize:" + listData.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            if(flag){
                mAdapter = new MainRecyclerViewAdapter(listData, context);
                Log.d(LOG_TAG,"Setting Adapter");
                mRecyclerView.setAdapter(mAdapter);
//                mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).color(Color.RED).sizeResId(R.dimen.divider).marginResId(R.dimen.leftmargin, R.dimen.rightmargin).build());
            }else{
                Log.d(LOG_TAG,"Downloading/parsing data failed");
            }
        }
    }
}
