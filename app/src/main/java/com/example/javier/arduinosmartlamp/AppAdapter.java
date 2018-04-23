package com.example.javier.arduinosmartlamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppAdapter extends BaseAdapter {
// Adaptador empleado en ListView de AppInfo.

    private LayoutInflater layoutInflater;
    private List<AppInfo> listStorage;
    private boolean showColors;
    /* Booleano empleado para distinguir cuándo el adaptador se está empleando para mostrar la lista
       de aplicaciones instaladas en el dispositivo y cuándo para listar las aplicaciones que han sido
       seleccionadas por el usuario (su valor es falso en el primer caso; verdadero en el segundo).
    */

    public AppAdapter(Context context, List<AppInfo> customizedListView, boolean showColors) {

        layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
        this.showColors = showColors;

    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;
        if(convertView == null){
            listViewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.installed_app_list, parent, false);

            listViewHolder.textInListView = convertView.findViewById(R.id.list_app_name);
            listViewHolder.imageInListView = convertView.findViewById(R.id.app_icon);
            convertView.setTag(listViewHolder);
        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        String appName = (listStorage.get(position).getName());

        if (!showColors) { appName+= (listStorage.get(position).isAdded()? " [añadida]" : ""); }
        else {

            if (listStorage.get(position).getColor().equals("Indeterminado")) {

                appName+= " [Pulsa para elegir color]";

            } else {

                String color = listStorage.get(position).getColor();
                appName += " [R, " + color.substring(0,3) +  "; G, " + color.substring(3,6) +  "; B, " + color.substring(6,9) + "]";

            }

        }


        listViewHolder.textInListView.setText(appName);
        listViewHolder.imageInListView.setImageDrawable(listStorage.get(position).getIcon());

        return convertView;
    }

    static class ViewHolder{

        TextView textInListView;
        ImageView imageInListView;

        public TextView getTextInListView() {
            return textInListView;
        }
    }
}

