package net.woorisys.lighting.control.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.domain.Apartment;
import net.woorisys.lighting.control.user.domain.City;

import java.util.List;

public class ApartmentAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    private List<Apartment> apartments;

    public ApartmentAdapter(Context context, List<Apartment> apartments) {
        this.context = context;
        this.apartments = apartments;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return apartments.size();
    }

    @Override
    public Object getItem(int position) {
        return apartments.get(position);
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

        if (apartments != null) {
            String text = apartments.get(position).getName();
            ((TextView) convertView.findViewById(R.id.spinnerText)).setText(text);
        }

        return convertView;
    }
}
