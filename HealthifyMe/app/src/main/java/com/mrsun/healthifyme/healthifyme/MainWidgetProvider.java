package com.mrsun.healthifyme.healthifyme;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

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

public class MainWidgetProvider extends AppWidgetProvider {

    private final String LOG_TAG = "MainWidgetProvider";
    public Context context;

    public List<Challenge> listData = new ArrayList<>();

    private static final String NEXT_CLICKED = "NextButtonClick";
    private static final String PREV_CLICKED = "NextButtonClick";

    RemoteViews remoteViews;
    public static int index = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.context = context;
        Log.d(LOG_TAG, "MainWidgetProvider.onUpdate");

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // register for button event


        JSONDownloadTask task = new JSONDownloadTask(context);
        task.execute("https://www.hackerearth.com/api/events/upcoming/?format=json");
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("Index", index);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int i = intent.getIntExtra("Index",0);
        Log.d(LOG_TAG, "onReceive..." + i);
        if (NEXT_CLICKED.equals(intent.getAction())) {
            Log.d(LOG_TAG,"Index on receive:"+index);
            Log.d(LOG_TAG,"Size:"+getListData().size());
            index++;
            if (index < listData.size())
                index++;
        } else if (PREV_CLICKED.equals(intent.getAction())) {
            if (index > 0)
                index--;
        }
        Log.d(LOG_TAG, "New Index:" + index);

        Log.d(LOG_TAG, "Updating the data...........");
    }


    private class JSONDownloadTask extends AsyncTask<String, Void, Boolean> {
        Context context;

        public JSONDownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            InputStream is;
            StringBuilder builder = new StringBuilder();
            try {
                Log.d(LOG_TAG, "JSONDownloadTask.doInBackground");
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
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return false;
        }

        private void parseJSON(String data) {
            try {
                Log.d(LOG_TAG, "Parsing JSON...");
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    if (object.getString("status").equals("ONGOING") && !object.getBoolean("college")) {
                        Challenge challenge = new Challenge();
                        challenge.setStatus(object.getString("status"));
                        challenge.setChallengeType(object.getString("challenge_type"));
                        challenge.setStartTimeStamp(object.getString("start_timestamp"));
                        challenge.setDescription(object.getString("description"));
                        challenge.setEndDate(object.getString("end_date"));
                        challenge.setTitle(object.getString("title"));
                        challenge.setUrl(object.getString("url"));
                        challenge.setIsHackerEarth(object.getBoolean("is_hackerearth"));
                        challenge.setThumbnail(object.getString("thumbnail") == "" ? object.getString("thumbnail") : "");
                        challenge.setEndTZ(object.getString("end_tz"));
                        challenge.setEndUTCTZ(object.getString("end_utc_tz"));
                        challenge.setSubscribe(object.getString("subscribe"));
                        challenge.setCollege(object.getBoolean("college"));
                        challenge.setEndTime(object.getString("end_time"));
                        challenge.setTime(object.getString("time"));
                        challenge.setDate(object.getString("date"));
                        challenge.setStartTZ(object.getString("start_tz"));
                        challenge.setStartUTCTZ(object.getString("start_utc_tz"));
                        challenge.setCoverImage(object.getString("cover_image"));
                        challenge.setEndTimeStamp(object.getString("end_timestamp"));

                        Log.d(LOG_TAG, "Added:" + object.get("title") + "-" + object.getString("status"));
                        listData.add(challenge);
                    }
                }
                Log.d(LOG_TAG, "ListSize:" + listData.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            if (flag) {
                Log.d(LOG_TAG, "JSONDownloadTask.onPostExecute");
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                ComponentName myWidget = new ComponentName(context, MainWidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);

                Log.d(LOG_TAG, "List data in post execute: " + listData.size());
                Challenge challenge = listData.get(index);
                remoteViews.setOnClickPendingIntent(R.id.rightnav, getPendingSelfIntent(context, NEXT_CLICKED));
                remoteViews.setOnClickPendingIntent(R.id.leftnav, getPendingSelfIntent(context, PREV_CLICKED));
                manager.updateAppWidget(myWidget, populateData(remoteViews,challenge));
            } else {
                Log.d(LOG_TAG, "Downloading/parsing data failed");
            }
        }
    }

    private RemoteViews populateData(RemoteViews remoteViews,Challenge ch) {

        remoteViews.setTextViewText(R.id.title, ch.getTitle());
        //TODO Download thumbnail
        remoteViews.setTextViewText(R.id.description, ch.getDescription());
        remoteViews.setTextViewText(R.id.opensAt, "Opens at\n" + ch.getStartTimeStamp().split(",")[0]);
        Log.d(LOG_TAG, "Opens at\n" + ch.getStartTimeStamp().split(",")[0]);
        String timestamp = (ch.getStartTimeStamp().split(",")[1]).substring(0, 10) + (ch.getEndTimeStamp().split(",")[1]).substring(0, 10);
        Log.d(LOG_TAG, "Timestamp: " + timestamp);
        remoteViews.setTextViewText(R.id.timestamp, timestamp);
        remoteViews.setTextViewText(R.id.endsAt, "Closes on\n" + ch.getEndTimeStamp().split(",")[0]);
        Log.d(LOG_TAG, "Closes on\n" + ch.getEndTimeStamp().split(",")[0]);
        remoteViews.setTextViewText(R.id.challengeType, ch.getChallengeType());

        DownloadImagesTask downloadtask = new DownloadImagesTask(context);
        downloadtask.execute(ch.getThumbnail());

        Log.d(LOG_TAG, "added data...");
        return remoteViews;

    }

    public class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {

        String url = null;
        Context context;

        DownloadImagesTask(Context context){
            this.context = context;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            this.url = urls[0];
            return download_Image(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setImageViewBitmap(R.id.thumbnail, result);
        }

        private Bitmap download_Image(String url) {

            Bitmap bmp =null;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            }catch(Exception e){}
            return bmp;
        }
    }

    public List<Challenge> getListData() {
        return listData;
    }

    public void setListData(List<Challenge> listData) {
        this.listData = listData;
    }
}
