package com.filetransfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ádám on 2016.10.27..
 */

public class MainGridAdapter extends BaseAdapter
{
    Context context;
    ArrayList<File> files;
    //ArrayList<Integer> imageIds;
    MainGridAdapter adapter = this;

    public MainGridAdapter(Context context, ArrayList<File> files)
    {
        this.context = context;
        this.files = files;
        //this.imageIds = imageIds;
    }

    @Override
    public int getCount()
    {
        return files.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup)
    {
        View grid = view;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        GridItemHolder holder;

        if (grid == null)
        {
            grid = inflater.inflate(R.layout.cell_layout, null);
            holder = new GridItemHolder();
            holder.fileImage = (ImageView)grid.findViewById(R.id.fileGridImage);
            holder.fileRemoveButton = (ImageButton)grid.findViewById(R.id.btn_removefile);
            holder.fileName = (TextView)grid.findViewById(R.id.fileGridText);

            grid.setTag(holder);
        }
        else
        {
            holder = (GridItemHolder)grid.getTag();
        }

        holder.fileName.setText(files.get(position).getName());
        holder.fileImage.setImageResource(R.drawable.file_icon);
        holder.fileRemoveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                files.remove(position);
                MainActivity.selected = files;
                adapter.notifyDataSetChanged();
            }
        });

        return grid;
    }

    //:O
    /*private Activity getActivity()
    {
        while (context instanceof ContextWrapper)
        {
            if (context instanceof Activity)
            {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }*/

    public static class GridItemHolder
    {
        TextView fileName;
        ImageView fileImage;
        ImageButton fileRemoveButton;
    }
}
