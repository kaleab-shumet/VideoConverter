package com.tnt.videoconverter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class ttest {

    Spinner spinner;
    void go(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
