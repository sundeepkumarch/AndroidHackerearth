package com.jio.reliance.jmtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button callLog,sms,battery;
    TextView text;
    String batteryInfo = "";
    private static int SMS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        callLog = (Button) findViewById(R.id.callLog);
        sms = (Button)findViewById(R.id.sms);
        battery = (Button) findViewById(R.id.battery);
        text = (TextView) findViewById(R.id.text);

        callLog.setOnClickListener(this);
        sms.setOnClickListener(this);
        battery.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.callLog:
                text.setText(getCallDetails());
                break;
            case R.id.sms:
                String permission = Manifest.permission.READ_SMS;
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
//                    permissionList.add(permission);
                    Toast.makeText(this,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                        Toast.makeText(this,"Requesting permission",Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{permission}, SMS_PERMISSION);
                    }else{
                        Toast.makeText(this,"Dont ask Permission",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this,"Permission Already Granted",Toast.LENGTH_SHORT).show();
                    text.setText(getSms());
                }

                break;
            case R.id.battery:


                registerBatteryLevelReceiver();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                    text.setText(getSms());

                } else {

                    // permission denied
                }
                return;
            }

        }
    }
    private String getCallDetails(){
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int getLocation = managedCursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION);
        int datausage = managedCursor.getColumnIndex(CallLog.Calls.DATA_USAGE);


        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String geoLocation = managedCursor.getString(getLocation);
            String dataUsage = managedCursor.getString(datausage);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration
            +" \nGeoLocation:-- "+geoLocation +" \nData Usage :-- "+ dataUsage);
            sb.append("\n----------------------------------");
        }
        managedCursor.close();
        return sb.toString();
    }

    private String getSms(){
        StringBuffer sb = new StringBuffer();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = 1000;//c.getCount();
        sb.append("SMS Details:\n");
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

               String id = c.getString(c.getColumnIndex(Telephony.Sms._ID));
                String address = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String msg = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                String readState = c.getString(c.getColumnIndex(Telephony.Sms.READ));
                String time = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                String person = c.getString(c.getColumnIndex(Telephony.Sms.PERSON));
                String subject = c.getString(c.getColumnIndex(Telephony.Sms.SUBJECT));
                String folderName = "";

                if (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).contains("1")) {
                    folderName = "inbox";
                } else {
                    folderName = "sent";
                }
                sb.append("\nID:--- " + id + " \nAddress:--- "
                        + address + " \nMessage:--- " + msg
                        + " \nRead State :--- " + readState
                        +" \nTime :-- "+time +" \nType :-- "+ folderName +"\n Person :-- "+person+"\nSubject :-- "+subject);
                sb.append("\n----------------------------------");

                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();
        return sb.toString();

    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(battery_receiver, filter);
    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            String technology = intent.getStringExtra("technology");
            int plugged = intent.getIntExtra("plugged", -1);
            int scale = intent.getIntExtra("scale", -1);
            int health = intent.getIntExtra("health", 0);
            int status = intent.getIntExtra("status", 0);
            int rawlevel = intent.getIntExtra("level", -1);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            int level = 0;

            Bundle bundle = intent.getExtras();

            Log.i("BatteryLevel", bundle.toString());

            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                String info = "Battery Level: " + level + "%\n";
                info += ("Technology: " + technology + "\n");
                info += ("Plugged: " + getPlugTypeString(plugged) + "\n");
                info += ("Health: " + getHealthString(health) + "\n");
                info += ("Status: " + getStatusString(status) + "\n");
                info += ("Voltage: " + voltage + "\n");
                info += ("Temperature: " + temperature + "\n");

                setBatteryInfo(info + "\n\n" + bundle.toString());
            } else {
                setBatteryInfo("Battery not present!!!");
            }
        }
    };

    private String getPlugTypeString(int plugged) {
        String plugType = "Unknown";

        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugType = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugType = "USB";
                break;
        }

        return plugType;
    }

    private String getHealthString(int health) {
        String healthString = "Unknown";

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Over Heat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Failure";
                break;
        }

        return healthString;
    }

    private String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }

        return statusString;
    }

    public void setBatteryInfo(String info){
        text.setText(info);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
