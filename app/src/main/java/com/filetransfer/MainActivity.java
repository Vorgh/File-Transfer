package com.filetransfer;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

    private Button selectFile;
    private Button sendFiles;
    private GridView gw;
    private MainGridAdapter adapter;

    protected FragmentManager fm = getSupportFragmentManager();
    protected FileServer fileServer;
    protected FileClient fileClient = null;

    public static ArrayList<File> selected = new ArrayList<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permission
        HelperUtils.requestPermissions(MainActivity.this);

        //Self IP Address
        TextView selfip = (TextView)findViewById(R.id.txt_selfipaddress);
        String ip = HelperUtils.getWifiIpAddress(this);
        if (ip == null) ip = "No wifi connection";
        selfip.setText("Your IP is: " + ip);

        //Grid View
        gw = (GridView)findViewById(R.id.gridView);
        adapter = new MainGridAdapter(MainActivity.this, selected);
        gw.setAdapter(adapter);

        //Select file button
        selectFile = (Button)findViewById(R.id.btn_selectfile);
        selectFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FileDialogFragment fdf = new FileDialogFragment();
                fdf.show(fm, "filedialog");
            }
        });

        //File server
        fileServer = new FileServer(this);

        //Send button
        final EditText destIP = (EditText)findViewById(R.id.etxt_ip);
        sendFiles = (Button)findViewById(R.id.btn_send);
        sendFiles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String ip = destIP.getText().toString();
                if (HelperUtils.isValidIP(ip))
                {
                    if (MainActivity.selected.size() > 0)
                    {
                        fileServer.onDestroy();
                        sendFiles.setEnabled(false);
                        //fileClient = new FileClient(MainActivity.this, ip, 5000);
                        fileClient = new FileClient(MainActivity.this, "10.0.2.2", 5000);
                        fileClient.execute();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Select some files first.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Not valid IP address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //ActionBar menu
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        View menuview = inflater.inflate(R.layout.appbar_layout, null);
        ImageButton menubutton = (ImageButton)menuview.findViewById(R.id.btn_menu);
        menubutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Menu Clicked",
                        Toast.LENGTH_LONG).show();
            }
        });
        actionBar.setCustomView(menuview);
        actionBar.setDisplayShowCustomEnabled(true);

        //showAcceptDialog("MUKODJ!");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case HelperUtils.REQUEST_EXTERNAL_STORAGE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "The permission is granted", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, "The permission is not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        MainActivity.selected.clear();
        adapter.notifyDataSetChanged();
        fileServer.onDestroy();
        if (fileClient != null)
            fileClient.cancel(true);
    }

    public void showAcceptDialog(String dialogText)
    {
        AcceptDialogFragment adf = new AcceptDialogFragment();
        Bundle stuff = new Bundle();
        stuff.putString("dialogtext", dialogText);
        adf.server = fileServer;
        adf.setArguments(stuff);
        adf.show(fm, "acceptdialog");
    }
}
