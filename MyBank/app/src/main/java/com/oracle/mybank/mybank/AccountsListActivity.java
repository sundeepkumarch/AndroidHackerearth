package com.oracle.mybank.mybank;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AccountsListActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        LinearLayout savingsLL = (LinearLayout) findViewById(R.id.savings);
        LinearLayout salaryLL = (LinearLayout) findViewById(R.id.salary);
        LinearLayout currentLL = (LinearLayout) findViewById(R.id.current);
        LinearLayout dematLL = (LinearLayout) findViewById(R.id.demat);
        LinearLayout creditLL = (LinearLayout) findViewById(R.id.credit);

        savingsLL.setOnClickListener(this);
        salaryLL.setOnClickListener(this);
        currentLL.setOnClickListener(this);
        dematLL.setOnClickListener(this);
        creditLL.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Any action can be implemented
                //showDialog(null);
            }
        });
    }

    public void showDialog(final String accountType){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //TODO Generating the account number randomly
        long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
        alertDialogBuilder.setMessage("New Account ID: \n"+ number);

        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                Intent intent = new Intent(AccountsListActivity.this,DetailsActivity.class);
                intent.putExtra("AccountType",accountType);
                startActivity(intent);
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
            Intent intent = new Intent(AccountsListActivity.this,LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.savings:
                showDialog("Savings");
                break;
            case R.id.salary:
                showDialog("Salary");
                break;
            case R.id.current:
                showDialog("Current");
                break;
            case R.id.demat:
                showDialog("Demat");
                break;
            case R.id.credit:
                showDialog("Credit");
                break;
        }
    }
}
