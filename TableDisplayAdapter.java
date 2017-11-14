package com.example.havensbee.myposapp.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.havensbee.myposapp.R;
import com.example.havensbee.myposapp.base.Table;

import java.text.DecimalFormat;


public class TableDisplayAdapter extends BaseAdapter {
    private Context mContext; // do we need this?
    private Table[] tables;
    private LayoutInflater inflater = null;

    public TableDisplayAdapter(Table[] tables, Context c) {
        this.mContext = c;
        this.tables = tables;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public int getCount() {
        return tables.length;
    }
    public Object getItem(int position) {
        return null;
    }
    public long getItemId(int position) {
        return 0;
    }

    public void updateData(Table[] updatedTable) {
        tables = updatedTable;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.my_table_view, parent, false);
        }
        Table tb = tables[position];

        // Table number
        TextView no = (TextView) view.findViewById(R.id.table_view_no);
        no.setText(tb.getTEXT());

        // How long this table has been opened
        TextView min = (TextView) view.findViewById(R.id.table_view_min);
        min.setText(tb.getTimeString());

        // Total sales so far
        TextView sale = (TextView) view.findViewById(R.id.table_view_sale);
        sale.setText(getCurrencyFormat(tb.getSALE()));

        // Table status: 0,1,2,3
        int stat = tb.getSTATUS();
        view.setBackgroundColor(getTableColor(stat));

        return view;
    }
    private String getCurrencyFormat(double sale) {
        if (sale==0) {
            return "0";
        }

        DecimalFormat df;
        if (Math.round(sale)!=sale) {
            df = new DecimalFormat("#,###,###.##");
        } else {
            df = new DecimalFormat("#,###,### -");
        }
        return df.format(sale);
    }
    private int getTableColor(int stat) {
        int color;
        if (stat==0 || stat==1) { // Available: green
            color = ContextCompat.getColor(mContext, R.color.color_tablestat_0_1);
        } else if (stat==3) { // Bill Requested: grey
            color = ContextCompat.getColor(mContext, R.color.color_tablestat_3);
        } else if (stat==2) { // Unavailable: red
            color = ContextCompat.getColor(mContext, R.color.color_tablestat_2);
        } else { // This should never happen
            color = ContextCompat.getColor(mContext, R.color.color_tablestat_invalid);
        }
        return color;
    }
}
