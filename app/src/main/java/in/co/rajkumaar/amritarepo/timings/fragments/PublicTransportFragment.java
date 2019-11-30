/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timings.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.timings.PublicTransportsActivity;
import in.co.rajkumaar.amritarepo.timings.ShuttleBusTimingsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublicTransportFragment extends Fragment {


    private String[] items;

    public PublicTransportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        items = new String[]{"Trains from Coimbatore", "Trains from Palghat", "Trains to Coimbatore", "Trains to Palghat", "Buses from Coimbatore", "Buses to Coimbatore"};
        View rootView = inflater.inflate(R.layout.fragment_transport, container, false);
        ListView listView = rootView.findViewById(R.id.dlist);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.custom_list_item, items);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent trainBusOpen = new Intent(getContext(), PublicTransportsActivity.class);
                trainBusOpen.putExtra("type", items[position]);
                startActivity(trainBusOpen);
            }
        });
        listView.setAdapter(arrayAdapter);
        return rootView;
    }

}
