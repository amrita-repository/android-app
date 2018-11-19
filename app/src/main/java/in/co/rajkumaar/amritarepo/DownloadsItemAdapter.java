package in.co.rajkumaar.amritarepo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DownloadsItemAdapter extends ArrayAdapter<DownloadsItem> {

    DownloadsItemAdapter(Context context, ArrayList<DownloadsItem> downloadsItems){
        super(context,0,downloadsItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.delete_list_item, parent, false);
        }

        final DownloadsItem current=getItem(position);

        String name=current.getTitle().getName();
        final boolean checkbox=current.getCheckBox();
        String filesize=current.getSize();

        final CheckBox checkBox=listItemView.findViewById(R.id.checkbox);
        TextView title=listItemView.findViewById(R.id.filename);
        TextView size=listItemView.findViewById(R.id.filesize);

        title.setText(name);
        size.setText(filesize);
        checkBox.setChecked(checkbox);

        LinearLayout container=listItemView.findViewById(R.id.container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
                current.setCheckBox(checkBox.isChecked());
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                current.setCheckBox(checkBox.isChecked());
            }
        });
        return listItemView;
    }
}
