package com.hackerearth.mrsun.cube26;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    TextView dName;
    TextView dDescription;
    TextView dBranding;
    TextView dCurrencies;
    TextView dSetupFee;
    TextView dTransactionFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dName = (TextView) findViewById(R.id.dName);
        dDescription = (TextView) findViewById(R.id.dDescription);
        dBranding = (TextView) findViewById(R.id.dBranding);
        dCurrencies = (TextView) findViewById(R.id.dCurrencies);
        dSetupFee = (TextView) findViewById(R.id.dSetupFee);
        dTransactionFee = (TextView) findViewById(R.id.dTransactionFee);

        dName.setText("Name: "+getIntent().getExtras().get("name"));
        dDescription.setText("Description:"+getIntent().getExtras().get("description"));
        String branding = (String)getIntent().getExtras().get("branding");
        dBranding.setText("Branding: "+ (branding.equals("1")?"Yes":"No"));
        dCurrencies.setText("Currencies: "+ getIntent().getExtras().get("currencies"));
        dSetupFee.setText("Setup Fee: "+ getIntent().getExtras().get("setupfee"));
        dTransactionFee.setText("Transaction Fee: "+getIntent().getExtras().get("transactionfee"));

    }


}
