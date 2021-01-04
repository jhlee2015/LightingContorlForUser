package net.woorisys.lighting.control.user.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.domain.City;

import java.util.List;

public class CityAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    private List<City> cities;

    public CityAdapter(Context context, List<City> cities) {
        this.context = context;
        this.cities = cities;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public Object getItem(int position) {
        return cities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.spinner_custom, parent, false);
        }

        if (cities != null) {
            String text = cities.get(position).getName();
            ((TextView) convertView.findViewById(R.id.spinnerText)).setText(text);
        }

        return convertView;
    }
}
