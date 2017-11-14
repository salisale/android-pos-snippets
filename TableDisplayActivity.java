package com.example.havensbee.myposapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.havensbee.myposapp.base.Table;
import com.example.havensbee.myposapp.fragment.TableDisplayFragment;

public class TableDisplayActivity extends AppCompatActivity
        implements TableDisplayFragment.OnFragmentInteractionListener {

    TableDisplayFragment tableDisplayFragment;
    Table tableSelected;
    String hostIP, emp_id;
    static boolean wifiConnected = true;
    NetworkReceiver receiver;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // To solve NetworkOnMainThreadException
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Detect Connectivity change
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        registerReceiver(receiver, filter);

        // Get arguments
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            hostIP = bundle.getString("hostIP");
            emp_id = bundle.getString("emp_id");
        }

        setContentView(R.layout.activity_table_display);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // same tool bar as OrderAct...okay?
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_table_display_title);


        tableDisplayFragment = TableDisplayFragment.newInstance(
                hostIP
        );

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_table, tableDisplayFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_refresh_button_table);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(tableDisplayFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .commit();

                if (!wifiConnected) {
                    makeToastWifiError(); // User can't do any damage with unrefreshed data, can they?
                } else {
                    tableDisplayFragment.updateData();
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .show(tableDisplayFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wifiConnected) {
            tableDisplayFragment.updateData();
        } else {
            makeToastWifiError(); // User can't do any damage with unrefreshed data, can they?
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onTableOrderClicked(Table table) {
        tableSelected = table;
        int stat = tableSelected.getSTATUS();
        if (stat == 0 || stat == 1) { // Customers arriving at this new table
            confirmDialogNewTable();
        } else { // Opened table: 2 (eating) or 3 (bill has been requested)
            confirmDialogOldTable();
        }

    }

    private void goToOrderActivity() {
        Intent myIntent = new Intent(TableDisplayActivity.this, OrderActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("Table", tableSelected);
        b.putString("hostIP", hostIP);
        b.putString("emp_id", emp_id);
        myIntent.putExtras(b);
        TableDisplayActivity.this.startActivity(myIntent);
    }
    private void goToPaymentActivity() {
        Intent myIntent = new Intent(TableDisplayActivity.this, PaymentActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("Table", tableSelected);
        b.putString("hostIP", hostIP);
        b.putString("emp_id", emp_id);
        myIntent.putExtras(b);
        TableDisplayActivity.this.startActivity(myIntent);
    }

    /**
     * This shows 2 Options: order, cancel dialog
     */
    private void confirmDialogNewTable() {
        String str = getString(R.string.confirm_dialog_table) + " " + tableSelected.getTEXT() + "?";
        AlertDialog.Builder builder = new AlertDialog.Builder(TableDisplayActivity.this);

        builder
                .setMessage(str)
                .setPositiveButton(R.string.confirm_dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.confirm_dialog_table_order, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        goToOrderActivity();
                    }
                })
                .show();
    }

    /**
     * This shows 3 Options: order, payment, cancel dialog
     */
    private void confirmDialogOldTable() {
        String str = getString(R.string.confirm_dialog_table) + " " + tableSelected.getTEXT() + "?";
        AlertDialog.Builder builder = new AlertDialog.Builder(TableDisplayActivity.this);

        builder
                .setMessage(str)
                .setPositiveButton(R.string.confirm_dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNeutralButton(R.string.confirm_dialog_table_payment, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        goToPaymentActivity();
                    }
                })
                .setNegativeButton(R.string.confirm_dialog_table_order, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        goToOrderActivity();
                    }
                })
                .show();
    }

    private void makeToastWifiError() {
        Toast.makeText(this.getApplicationContext(), R.string.wifi_not_connected, Toast.LENGTH_SHORT).show();
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conn = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conn.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                wifiConnected = true;
            } else {
                wifiConnected = false;

                Toast.makeText(context, R.string.wifi_not_connected, Toast.LENGTH_SHORT).show();

            }

        }
    }

}
