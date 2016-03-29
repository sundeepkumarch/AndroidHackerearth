package com.oracle.mybank.mybank;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FILE_SELECT_CODE = 0;
    CheckBox cb1,cb2,cb3,cb4,cb5,cb6,cb7,cb8;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cb1 = (CheckBox) findViewById(R.id.pancardCB);
        cb2 = (CheckBox) findViewById(R.id.voterIdCB);
        cb3 = (CheckBox) findViewById(R.id.bankStmtCB);
        cb4 = (CheckBox) findViewById(R.id.ebillCB);
        cb5 = (CheckBox) findViewById(R.id.tbillCB);
        cb6 = (CheckBox) findViewById(R.id.pportCB);
        cb7 = (CheckBox) findViewById(R.id.dlCB);
        cb8 = (CheckBox) findViewById(R.id.adcardCB);

        cb1.setOnClickListener(this);
        cb2.setOnClickListener(this);
        cb3.setOnClickListener(this);
        cb4.setOnClickListener(this);
        cb5.setOnClickListener(this);
        cb6.setOnClickListener(this);
        cb7.setOnClickListener(this);
        cb8.setOnClickListener(this);

        saveBtn = (Button) findViewById(R.id.saveBtn);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            Intent intent = new Intent(DetailsActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        new FileChooser(DetailsActivity.this).setFileListener(new FileChooser.FileSelectedListener() {
                                                                  @Override
                                                                  public void fileSelected(final File file) {
                                                                      // do something with the file
                                                                      Toast.makeText(DetailsActivity.this,"FilePath:"+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
                                                                      checkSaveButton();
                                                                  }
                                                              }
        ).showDialog();

    }

    private void checkSaveButton() {
        if(cb1.isChecked()||cb2.isChecked()){
            if(cb3.isChecked()||cb4.isChecked()||cb5.isChecked()){
                if(cb6.isChecked()||cb7.isChecked()||cb8.isChecked()){
                    saveBtn.setEnabled(true);
                }

            }
        }
    }
}
