package net.woorisys.lighting.control.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.woorisys.lighting.control.user.R;
import net.woorisys.lighting.control.user.domain.Apartment;
import net.woorisys.lighting.control.user.domain.Floor;

import java.util.List;

public class FloorAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    private List<Floor> floors;

    public FloorAdapter(Context context, List<Floor> floors) {
        this.context = context;
        this.floors = floors;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public Object getItem(int position) {
        return floors.get(position);
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

        if (floors != null) {
            String text = floors.get(position).getName();
            ((TextView) convertView.findViewById(R.id.spinnerText)).setText(text);
        }

        return convertView;
    }
}
