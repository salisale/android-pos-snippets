package com.example.havensbee.myposapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.havensbee.myposapp.R;
import com.example.havensbee.myposapp.adapter.TableDisplayAdapter;
import com.example.havensbee.myposapp.base.Table;
import com.example.havensbee.myposapp.retriever.TableDataRetriever;


public class TableDisplayFragment extends Fragment {

    private Table[] tables;
    private GridView gridView;
    private TableDisplayAdapter tableDisplayAdapter;
    private TableDataRetriever tableDataRetriever;
    private String hostIP;
    private boolean compInit;

    private OnFragmentInteractionListener mListener;

    public TableDisplayFragment() {
        // Required empty public constructor
    }

    public static TableDisplayFragment newInstance(String hostIP) {
        TableDisplayFragment fragment = new TableDisplayFragment();
        Bundle args = new Bundle();
        args.putString("hostIP", hostIP);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hostIP = getArguments().getString("hostIP");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_table_display, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view_table);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState==null) {
            tableDataRetriever = new TableDataRetriever(hostIP);
            compInit = initComp(); // check whether connection to server is successful
        }
    }
    private boolean initComp() {
        if (tableDataRetriever.DB_CONNECTION_SUCCESSFUL) {
            tables = tableDataRetriever.getTables();
            tableDisplayAdapter = new TableDisplayAdapter(tables, this.getContext());
            gridView.setAdapter(tableDisplayAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    mListener.onTableOrderClicked(tables[position]);
                }
            });
            return true;
        } else {
            Toast.makeText(getContext(), R.string.db_connection_unsuccessful, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private boolean loadTables() {
        if (!tableDataRetriever.DB_CONNECTION_SUCCESSFUL) {
            Toast.makeText(getContext(), R.string.db_connection_unsuccessful, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            tables = tableDataRetriever.getTables();
            return true;
        }
    }
    public void updateData() {
        if (!compInit) {
            initComp();
        } else {
            tableDataRetriever.update();
            boolean loadSuccess = loadTables();
            if (loadSuccess) {
                tableDisplayAdapter.updateData(tables);
            }
        }

    }
    public interface OnFragmentInteractionListener {
        void onTableOrderClicked(Table table);
    }
}