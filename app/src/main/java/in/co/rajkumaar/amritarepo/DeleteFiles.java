/*
 * MIT License
 *
 * Copyright (c) 2018  RAJKUMAR S
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package in.co.rajkumaar.amritarepo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeleteFiles extends AppCompatActivity {

    final String dirPath= Environment.getExternalStorageDirectory() + "/AmritaRepo";
    File dir;
    File[] files;
    ListView listView;
    private ArrayList<DownloadsItem> fileList;
    ArrayAdapter<DownloadsItem> fileAdapter;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);
        dir = new File(dirPath);
        fileList = new ArrayList<DownloadsItem>();
        listView=findViewById(R.id.list);
        retrieveFiles();
        listFiles();

        Button delete=findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count=0;
                for(int i=0;i<fileList.size();++i){
                    if(fileList.get(i).getCheckBox()){
                      count++;
                    }
                }
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DeleteFiles.this);
                alertDialog.setMessage("Are you sure you want to delete these "+count+" files? ");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        if(!fileList.isEmpty()){
                            for(int i=0;i<fileList.size();++i){
                                if(fileList.get(i).getCheckBox()){
                                    fileList.get(i).getTitle().delete();
                                }
                            }
                            Toast.makeText(DeleteFiles.this,count+" files deleted",Toast.LENGTH_SHORT).show();
                            retrieveFiles();
                            listFiles();
                        }
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();
            }
        });


    }
    void retrieveFiles(){
        files = dir.listFiles();
        fileList.clear();
        if(files!=null) {
            for (File file : files) {
                    fileList.add(new DownloadsItem(file,(file.length()/1024)+ " kb",false));
            }
        }
    }
    void listFiles(){
        if(!fileList.isEmpty()) {
            fileAdapter = new DownloadsItemAdapter(this, fileList);
            final ListView downloads = listView;
            downloads.setAdapter(fileAdapter);
        }else{
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
