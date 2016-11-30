package com.filetransfer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Ádám on 2016.11.11..
 */

public class AcceptDialogFragment extends android.support.v4.app.DialogFragment
{

    private TextView title;
    private TextView text;
    protected Button accept;
    protected Button cancel;

    protected FileServer server;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.acceptdialog_layout, container);

        initComponents(view);

        return view;
    }

    private void initComponents(View view)
    {
        title = (TextView)view.findViewById(R.id.acceptdialog_title);
        accept = (Button)view.findViewById(R.id.acceptdialog_ok);
        accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
                try
                {
                    if (server.socket != null)
                    {
                        OutputStream os = server.socket.getOutputStream();
                        String answer = FileServer.TRANSFER_ACCEPT + '\n';
                        os.write(answer.getBytes());
                        os.flush();
                    }
                }
                catch (IOException e) { Log.e("FILE", e.getMessage()); }

                server.startDownload();
            }
        });
        cancel = (Button)view.findViewById(R.id.acceptdialog_cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
                try
                {
                    if (server.socket != null)
                    {
                        OutputStream os = server.socket.getOutputStream();
                        String answer = FileServer.TRANSFER_DECLINE + '\n';
                        os.write(answer.getBytes());
                        os.flush();
                    }
                }
                catch (IOException e) { Log.e("FILE", e.getMessage()); }
            }
        });
        text = (TextView)view.findViewById(R.id.acceptdialog_text);
        text.setText(getArguments().getString("dialogtext"));
    }
}
