package com.filetransfer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FileDialogAdapter extends ArrayAdapter<FileItem>
{
    Context context;
    int layoutResId;
    ArrayList<FileItem> items = null;

    public FileDialogAdapter(Context context, int layoutResId, ArrayList<FileItem> items)
    {
        super(context, layoutResId, items);
        this.layoutResId = layoutResId;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        FileItemHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResId, parent, false);
            holder = new FileItemHolder();
            holder.icon = (ImageView)row.findViewById(R.id.fileicon);
            holder.filename = (TextView)row.findViewById(R.id.filename);
            holder.checkBox = (CheckBox)row.findViewById(R.id.filecheckBox);
            row.setTag(holder);
        }
        else
        {
            holder = (FileItemHolder)row.getTag();
        }

        FileItem item = items.get(position);
        holder.icon.setImageResource(item.icon);
        holder.filename.setText(item.filename);
        holder.checkBox.setTag(item);
        if (item.file.isDirectory())
            holder.checkBox.setEnabled(false);
        else
            holder.checkBox.setEnabled(true);
        holder.checkBox.setChecked(item.isChecked);

        return row;
    }

    static class FileItemHolder
    {
        CheckBox checkBox;
        ImageView icon;
        TextView filename;
    }
}
