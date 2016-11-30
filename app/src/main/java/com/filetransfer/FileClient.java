package com.filetransfer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Ádám on 2016.10.28..
 */

public class FileClient extends AsyncTask<Void, Void, Void>
{
    static final int BUFFERSIZE = 4096;
    static final String TRANSFER_ACCEPT = "FILETRANSFER_ACCEPTED";
    static final String TRANSFER_DECLINE = "FILETRANSFER_DECLINED";

    String destAddress;
    int destPort;
    Socket socket = null;
    MainActivity activity;

    BufferedInputStream is = null;
    BufferedOutputStream os = null;


    FileClient(MainActivity activity, String destAddress, int destPort)
    {
        this.activity = activity;
        this.destAddress = destAddress;
        this.destPort = destPort;
    }

    @Override
    public Void doInBackground(Void... arg0)
    {
        try
        {
            socket = new Socket(destAddress, destPort);
            is = new BufferedInputStream(socket.getInputStream(), BUFFERSIZE);
            os = new BufferedOutputStream(socket.getOutputStream(), BUFFERSIZE);

            byte[] buffer = new byte[BUFFERSIZE];
            int r;
            String filedata = "";
            String acceptString = "";
            byte newline = (byte)'\n';

            for (File f : MainActivity.selected)
            {
                filedata += f.getName() + "," + f.length() + ";";
            }
            filedata += '\n';

            os.write(filedata.getBytes());
            os.flush();

            long startTime = System.nanoTime();
            long elapsedTime = 0;
            while (elapsedTime < 10000000000L)
            {
                if ((r = is.read()) != -1)
                {
                    if (r == newline)
                        break;
                    else
                        acceptString += (char)r;
                }
                elapsedTime = System.nanoTime() - startTime;
            }

            if (acceptString.equals(TRANSFER_ACCEPT))
            {
                for (int i=0; i<MainActivity.selected.size(); i++)
                {
                    FileInputStream fis = new FileInputStream(MainActivity.selected.get(i));
                    while ((r = fis.read(buffer)) != -1 && !socket.isClosed() && socket.isConnected())
                    {
                        os.write(buffer, 0 ,r);
                    }
                    os.flush();
                    fis.close();
                }
            }

            Log.d("ACCEPT", acceptString);
        }
        catch (UnknownHostException e) { Log.e("CLIENTUNKNOWNHOST", e.getMessage()); }
        catch (IOException e) { Log.e("CLIENTIO", e.getMessage()); }

        return null;
    }

    @Override
    protected void onPostExecute(Void v)
    {
        activity.fileServer = new FileServer(activity);
    }


    @Override
    public void onCancelled()
    {
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (IOException e) { Log.e("CLOSEIOCANCELLED", e.getMessage()); }
        }
        activity.fileServer = new FileServer(activity);
    }
}
