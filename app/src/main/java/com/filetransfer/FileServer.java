package com.filetransfer;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Ádám on 2016.10.28..
 */

public class FileServer
{
    static final int PORT = 5001;
    //static final int PORT = 5000;
    static final int BUFFERSIZE = 4096;
    public static final String TRANSFER_ACCEPT = "FILETRANSFER_ACCEPTED";
    public static final String TRANSFER_DECLINE = "FILETRANSFER_DECLINED";

    MainActivity activity;
    ServerSocket serverSocket;
    Socket socket = null;
    ServerAcceptThread serverAcceptThread;
    Thread serverDownloadThread;

    ArrayList<FileDataContainer> fileDatas = new ArrayList<FileDataContainer>();

    public FileServer(MainActivity activity)
    {
        this.activity = activity;
        serverAcceptThread = new ServerAcceptThread();
        serverAcceptThread.start();
    }

    public void onDestroy()
    {
        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e) { Log.e("onDestroy", "Couldn't close socket"); }
        }
        serverAcceptThread.interrupt();
        if (serverDownloadThread != null)
            serverDownloadThread.interrupt();
    }

    private class ServerAcceptThread extends Thread
    {
        private Object pauseLock;
        private boolean paused;

        ServerAcceptThread()
        {
            pauseLock = new Object();
            paused = false;
        }

        public void onPause()
        {
            synchronized (pauseLock)
            {
                paused = true;
            }
        }

        public void onResume()
        {
            synchronized (pauseLock)
            {
                paused = false;
                pauseLock.notifyAll();
            }
        }

        @Override
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket(PORT);
            }
            catch (IOException e) { Log.e("SOCKETCREATE", "Couldn't create socket"); }

            while (!isInterrupted())
            {
                synchronized (pauseLock)
                {
                    while (paused)
                    {
                        try
                        {
                            pauseLock.wait();
                        }
                        catch (InterruptedException e) { Log.e("ACCEPTPAUSE", e.getMessage()); }
                    }
                }
                try
                {
                    socket = serverSocket.accept();
                    Log.d("SOCKET", "connection successful");
                }
                catch (IOException e)
                {
                    Log.e("SOCKETACCEPT", e.getMessage());
                    continue;
                }

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(activity, "Connected", Toast.LENGTH_LONG).show();
                    }
                });

                acceptFile(socket);
            }
            Log.d("THREAD", "Thread exited successfully");
        }

        private void acceptFile(Socket socket)
        {
            InputStream is;
            OutputStream os;

            try
            {
                int r;
                is = socket.getInputStream();
                os = socket.getOutputStream();
                String filedata = "";
                byte newline = (byte)'\n';

                while ((r=is.read()) != -1)
                {
                    if (r == newline)
                        break;
                    filedata += (char)r;
                }
                String[] filenames = filedata.split(";");
                long totalSize = 0;
                for (String s : filenames)
                {
                    String[] tmp = s.split(",");
                    FileDataContainer fdc = new FileDataContainer(tmp[0], Long.parseLong(tmp[1]));
                    fileDatas.add(fdc);
                    totalSize += fdc.length;
                }

                os.write((TRANSFER_ACCEPT + '\n').getBytes());
                os.flush();

                final String dialogText = socket.getRemoteSocketAddress().toString() + " is trying to send you "
                        + fileDatas.size() + " file(s) (Total: " + HelperUtils.convertFileSize(totalSize) + ").";

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        activity.showAcceptDialog(dialogText);
                    }
                });

            }
            catch (IOException e) { Log.e("FILE", e.getMessage()); }
        }
    }

    public void startDownload()
    {
        serverAcceptThread.onPause();
        serverDownloadThread = new Thread(new ServerDownloadThread());
        serverDownloadThread.start();
    }

    private class ServerDownloadThread implements Runnable
    {
        @Override
        public void run()
        {
            InputStream is = null;
            OutputStream os = null;
            DownloadManager dm = (DownloadManager)activity.getSystemService(Context.DOWNLOAD_SERVICE);
            int index = 0;
            int r;
            byte[] buffer = new byte[BUFFERSIZE];
            long currentLength = 0;
            long lengthThreshold = fileDatas.get(index).length;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileDatas.get(index).name);
            Log.d("FILENAME", file.getAbsolutePath());

            try
            {
                is = socket.getInputStream();
                os = new FileOutputStream(file);

                while (socket.isConnected() && !socket.isClosed() && (r = is.read(buffer)) != -1)
                {
                    if (currentLength + r >= lengthThreshold)
                    {
                        int writeSize = (int) (lengthThreshold - currentLength);
                        os.write(buffer, 0, writeSize);
                        os.flush();
                        os.close();
                        dm.addCompletedDownload(file.getName(), file.getName(), true,
                                HelperUtils.getMimeType(file.getAbsolutePath()), file.getAbsolutePath(), file.length(), true);

                        if (index < fileDatas.size() - 1)
                        {
                            index++;
                            lengthThreshold = fileDatas.get(index).length;
                            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileDatas.get(index).name);
                            os = new FileOutputStream(file);
                            os.write(buffer, writeSize, buffer.length - writeSize);
                            currentLength = buffer.length - writeSize;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        currentLength += r;
                        os.write(buffer, 0, r);
                    }
                }
            }
            catch (IOException e) { Log.e("FILE", e.getMessage()); }
            finally
            {
                fileDatas.clear();
                serverAcceptThread.onResume();

                if (os != null)
                {
                    try
                    {
                        os.close();
                    }
                    catch (IOException e) {Log.e("SERVEROSCLOSE", e.getMessage());}
                }
                if (socket != null)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (IOException e) { Log.e("SERVERSOCKETCLOSE", e.getMessage()); }
                }
            }
        }
    }

    class FileDataContainer
    {
        String name;
        long length;

        FileDataContainer(String name, long length)
        {
            this.name = name;
            this.length = length;
        }
    }
}
