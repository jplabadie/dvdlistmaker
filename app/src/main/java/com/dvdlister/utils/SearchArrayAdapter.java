package com.dvdlister.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dvdlister.R;

import java.util.ArrayList;

/**
 * Created by Jean-Paul on 12/3/2017.
 */

public class SearchArrayAdapter extends ArrayAdapter<ArrayList<String>> {
    private final Context context;
    private final ArrayList<String> values;
    private final ViewGroup root;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<ArrayList<String>> saa;

    public SearchArrayAdapter(Context context, ArrayList<String> values, ViewGroup root) {
        super(context, -1, new ArrayList[]{values});
        Log.e("FUCK","REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE   "+ values.size());
        this.context = context;
        this.values = values;
        this.root = root;
        dbHelper = new DatabaseHelper(context);
        saa = this;
    }

    public String[] getDetails(int position){
        String item = values.get(position);
        int first_space = item.indexOf(' ');
        int last_space = item.lastIndexOf(' ');
        String qrcode = item.substring(0,first_space);
        String title = item.substring(first_space,last_space);
        String location = item.substring(last_space,item.length());

        return new String[]{qrcode,title,location};
    }

    @Override
    public View getView(int position, final View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final LinearLayout listView = new LinearLayout(context);
        listView.setOrientation(LinearLayout.VERTICAL);
        listView.setVerticalScrollBarEnabled(true);

        for(final String item: values) {
            int first_space = item.indexOf(' ');
            int last_space = item.lastIndexOf(' ');
            final String qcode = item.substring(0,first_space);
            final String tit = item.substring(first_space+1,last_space);
            final String loc = item.substring(last_space+1,item.length());

            View itemView = inflater.inflate(R.layout.list_item, parent, false);
            TextView title_text = (TextView) itemView.findViewById(R.id.title_text);
            title_text.setText(tit);
            final TextView location_text = (TextView) itemView.findViewById(R.id.location_text);
            location_text.setText(loc);
            TextView qrcode_text = (TextView) itemView.findViewById(R.id.qrcode_text);
            qrcode_text.setText(qcode);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    final LayoutInflater inflater = (LayoutInflater)
                            context.getSystemService(getContext().LAYOUT_INFLATER_SERVICE);

                    final PopupMenu pm = new PopupMenu(context,v,Gravity.CENTER_HORIZONTAL);
                    pm.getMenuInflater().inflate(R.menu.edit_db_menu,pm.getMenu());
                    MenuItem details = pm.getMenu().findItem(R.id.details);
                    details.setEnabled(false);
                    MenuItem move = pm.getMenu().findItem(R.id.move);
                    move.setEnabled(false);
                    MenuItem delete = pm.getMenu().findItem(R.id.delete);
                    delete.setEnabled(false);

                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            String clicked = (String) item.getTitle();
                            if (clicked.equalsIgnoreCase("edit")) {
                                pm.dismiss();

                                View popupView = inflater.inflate(R.layout.edit_db_item, root, false);
                                final PopupWindow pw = new PopupWindow(popupView,500,600,true);
                                final TextView qrcode = (TextView) popupView.findViewById(R.id.qrcode);
                                final EditText title = (EditText) popupView.findViewById(R.id.edit_title);
                                final EditText location = (EditText) popupView.findViewById(R.id.edit_location);
                                final Button save = (Button) popupView.findViewById(R.id.save);
                                final Button cancel = (Button) popupView.findViewById(R.id.cancel);

                                pw.showAtLocation(root,Gravity.CENTER,0,0);

                                qrcode.setText((CharSequence) qcode);
                                title.setText((CharSequence) tit);
                                location.setText((CharSequence) loc);

                                save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dbHelper.updateDvd(""+qcode,title.getText().toString(),
                                                location.getText().toString());
                                        saa.notifyDataSetChanged();
                                        pw.dismiss();
                                    }
                                });
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pw.dismiss();
                                    }
                                });
                                return false;
                            }
                            else if(clicked.equalsIgnoreCase("delete")){
                                return true;
                            }
                            else if(clicked.equalsIgnoreCase("move")){
                                return true;
                            }
                            else if(clicked.equalsIgnoreCase("details")){
                                return true;
                            }
                            return false;
                        }
                    });
                    pm.show();
                }
            });
            listView.addView(itemView);
        }
        return listView;
    }
}
