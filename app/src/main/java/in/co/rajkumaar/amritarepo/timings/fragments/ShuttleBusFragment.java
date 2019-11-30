/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timings.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.timings.ShuttleBusTimingsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShuttleBusFragment extends Fragment {


    String[] items = {"Buses from AB1", "Buses from AB3"};
    View rootView;
    ListView listView;

    public ShuttleBusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_transport, container, false);
        listView = rootView.findViewById(R.id.dlist);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_list_item, items);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent trainBusOpen = new Intent(getContext(), ShuttleBusTimingsActivity.class);
                trainBusOpen.putExtra("type", items[position]);
                startActivity(trainBusOpen);
            }
        });
        listView.setAdapter(arrayAdapter);
        return rootView;
    }

}
