/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.downloads.FolderHelper;
import in.co.rajkumaar.amritarepo.downloads.models.DownloadsItem;

public class DownloadsItemAdapter extends ArrayAdapter<DownloadsItem> {

    private final FolderHelper folderHelper;

    public DownloadsItemAdapter(Context context, ArrayList<DownloadsItem> downloadsItems, FolderHelper folderHelper) {
        super(context, 0, downloadsItems);
        this.folderHelper = folderHelper;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.delete_list_item, parent, false);
        }

        final DownloadsItem current = getItem(position);

        String name = current.getTitle().getName();
        final boolean checkbox = current.getCheckBox();
        final String filesize = current.getSize();

        final CheckBox checkBox = listItemView.findViewById(R.id.checkbox);
        TextView title = listItemView.findViewById(R.id.filename);
        TextView size = listItemView.findViewById(R.id.filesize);

        title.setText(name);
        size.setText(filesize);
        checkBox.setChecked(checkbox);

        RelativeLayout container = listItemView.findViewById(R.id.container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current.getTitle().isDirectory()) {
                    folderHelper.loadFilesFromDir(current.getTitle());
                } else {
                    checkBox.setChecked(!checkBox.isChecked());
                    current.setCheckBox(checkBox.isChecked());
                }
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
