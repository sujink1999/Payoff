package com.sujin.payoff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.ArrayList;
import java.util.List;

public class PeopleAdapter extends ArrayAdapter {

    List<String> names = new ArrayList<>();

    Context mContext;


    public PeopleAdapter(Context context,int resource, List<String> names) {

        super(context, resource,names);
        mContext = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            view = new View(mContext);
            view = inflater.inflate(R.layout.peoplecard, null);

            //convertView = ((Activity) this).getLayoutInflater().inflate(R.layout.blogs, parent, false);


            TextView name = (TextView) view.findViewById(R.id.name);


            name.setText(names.get(position));


        }else
        {
            view = (View) convertView;
        }



        return view;
    }

}
