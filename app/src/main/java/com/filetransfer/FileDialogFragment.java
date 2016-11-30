package com.filetransfer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ádám on 2016.10.26..
 */
public class FileDialogFragment extends android.support.v4.app.DialogFragment
{
    public static ArrayList<File> selectedList = new ArrayList<File>();

    ImageButton homeDir, upDir;
    View divider;
    Button addSelected;
    TextView folderpath;
    File root, currFolder;
    ListView fileView;
    ArrayList<FileItem> fileList = new ArrayList<FileItem>();
    FileDialogAdapter adapter;

    public FileDialogFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFullWidth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_layout, container);
        getDialog().setTitle("Choose a file");

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        //root = new File(getContext().getFilesDir().getAbsolutePath());
        currFolder = root;

        initComponents(view);

        return view;
    }

    private void initComponents(View view)
    {
        folderpath = (TextView)view.findViewById(R.id.txt_currentpath);
        divider = (View)view.findViewById(R.id.divider1);

        upDir = (ImageButton)view.findViewById(R.id.btn_parentfolder);
        upDir.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ListDir(currFolder.getParentFile());
            }
        });

        homeDir = (ImageButton)view.findViewById(R.id.btn_home);
        homeDir.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ListDir(root);
            }
        });

        addSelected = (Button)view.findViewById(R.id.btn_addselected);
        addSelected.setOnClickListener(new View.OnClickListener()
        {
            boolean containsSelected = false;

            @Override
            public void onClick(View view)
            {
                for (int i=0; i<selectedList.size(); i++)
                {
                    File f = selectedList.get(i);
                    if (MainActivity.selected.contains(f))
                    {
                        selectedList.remove(f);
                        containsSelected = true;
                    }
                    else
                    {
                        MainActivity.selected.add(f);
                    }
                }
                MainGridAdapter adapter = new MainGridAdapter(getActivity(), MainActivity.selected);
                GridView gw = (GridView)(getActivity().findViewById(R.id.gridView));
                gw.setAdapter(adapter);
                if (containsSelected)
                {
                    Toast.makeText(getContext(), "Some files were already selected", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getContext(), selectedList.size() + " item(s) added", Toast.LENGTH_SHORT).show();
                FileDialogFragment.this.dismiss();
            }
        });

        fileView = (ListView)view.findViewById(R.id.filelist);
        adapter = new FileDialogAdapter(getContext(), R.layout.fileitem_layout, fileList);
        fileView.setAdapter(adapter);
        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                File selected = fileList.get(position).file;
                if (selected.isDirectory())
                {
                    ListDir(selected);
                }
                else
                {
                    FileItem item = fileList.get(position);
                    if (!item.isChecked)
                    {
                        FileDialogFragment.selectedList.add(item.file);
                    }
                    else
                    {
                        FileDialogFragment.selectedList.remove(item.file);
                    }
                    item.isChecked = !item.isChecked;
                    adapter.notifyDataSetChanged();
                }
            }
        });

        ListDir(root);
    }

    private void ListDir(File f)
    {
        FileDialogFragment.selectedList.clear();
        if (f.equals(root))
        {
            upDir.setEnabled(false);
            upDir.setVisibility(View.INVISIBLE);
            homeDir.setEnabled(false);
            homeDir.setVisibility(View.INVISIBLE);
            divider.setVisibility(View.INVISIBLE);
        }
        else
        {
            upDir.setEnabled(true);
            upDir.setVisibility(View.VISIBLE);
            homeDir.setEnabled(true);
            homeDir.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }
        currFolder = f;
        folderpath.setText(f.getAbsolutePath());
        fileList.clear();
        File[] files = f.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                int resId = R.drawable.folder_icon;
                fileList.add(new FileItem(resId, file));
            }
            else
            {
                int resId = R.drawable.file_icon;
                fileList.add(new FileItem(resId, file));
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDismiss(final DialogInterface dialog)
    {
        super.onDismiss(dialog);
        for (FileItem fi : fileList)
        {
            fi.isChecked = false;
        }
        FileDialogFragment.selectedList.clear();
    }
}

class FileItem
{
    boolean isChecked;
    int icon;
    File file;
    String filename;

    FileItem(int icon, File file)
    {
        this.icon = icon;
        this.file = file;
        this.filename = file.getName();
        this.isChecked = false;
    }
}

