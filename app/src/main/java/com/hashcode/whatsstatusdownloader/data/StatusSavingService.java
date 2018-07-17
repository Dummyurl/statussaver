package com.hashcode.whatsstatusdownloader.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.hashcode.whatsstatusdownloader.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by oluwalekefakorede on 03/09/2017.
 */

public class StatusSavingService extends IntentService {
    public final static String FETCHED_STATUSES = "fetched-statuses";
    public static final String SELECTED_STATUSES = "selected-statuses";
    private final static String ACTION_FETCH_STATUS = "fetch-status";
    private final static String ACTION_SAVE_STATUS = "save-status";
    private static final String TAG = StatusSavingService.class.getSimpleName();
    public static String FOLDER_PATH = "folder-path";

    public StatusSavingService() {
        super(TAG);
    }

    public static void performSave(Context context, ArrayList<String> paths) {
        Intent intent = new Intent(context, StatusSavingService.class);
        intent.setAction(ACTION_SAVE_STATUS);
        intent.putExtra(SELECTED_STATUSES, paths);
        context.startService(intent);
    }

    public static void performFetch(Context context) {
        Intent intent = new Intent(context, StatusSavingService.class);
        intent.setAction(ACTION_FETCH_STATUS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        if (ACTION_FETCH_STATUS.equals(action)) {
            fetchAllStatus();
        } else if (ACTION_SAVE_STATUS.equals(action)) {
            ArrayList<String> selectedStatuses = intent.getStringArrayListExtra(SELECTED_STATUSES);
            saveAllSelectedStatus(selectedStatuses);
        }
    }

    private void fetchAllStatus() {
        String foldPath = new StringBuffer().append(Environment.getExternalStorageDirectory()
                .getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString();

        String businessfoldPath = new StringBuffer().append(Environment.getExternalStorageDirectory()
                .getAbsolutePath()).append("/WhatsApp Business/Media/.Statuses/").toString();

        File whatsAppFile = new File(foldPath);
        File whatsAppBusinessFile = new File(businessfoldPath);
        if (whatsAppFile.exists()) {

        } else if (!whatsAppFile.exists()) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.FetchStatusReceiver.PROCESS_FETCH);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("the-user-has-whatsapp", false);
            sendBroadcast(broadcastIntent);

            if (!whatsAppBusinessFile.exists()) {
                Intent broadcastBusinessIntent = new Intent();
                broadcastBusinessIntent.setAction(MainActivity.FetchStatusReceiver.PROCESS_FETCH);
                broadcastBusinessIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastBusinessIntent.putExtra("the-user-has-business-whatsapp", false);
                sendBroadcast(broadcastBusinessIntent);
                return;
            }
            return;
        }

        Date currentDate = new Date();
        long cTime = currentDate.getTime();
        File whatsappFiles[] = whatsAppFile.listFiles();
//        File businessFiles[] = whatsAppBusinessFile.listFiles();
        Arrays.sort(whatsappFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

//        Arrays.sort(businessFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        ArrayList<String> whatsAppStatuses = new ArrayList<>();

        for (int i = 0; i < whatsappFiles.length; i++) {
            long diff = cTime - whatsappFiles[i].lastModified();
            if (diff <= (24 * 60 * 60 * 1000)) {
                whatsAppStatuses.add(foldPath + whatsappFiles[i].getName());

            }            //here populate your listview
        }

//        for (int i = 0; i < businessFiles.length; i++) {
//            long diff = cTime - businessFiles[i].lastModified();
//            if (diff <= (24 * 60 * 60 * 1000)) {
//                whatsAppStatuses.add(businessfoldPath + businessFiles[i].getName());
//
//            }
//        }
        sendFetchBroadCast(whatsAppStatuses, foldPath);
    }

    private void saveAllSelectedStatus(ArrayList<String> statuses) {
        for (String status : statuses) {
            String[] splitStatus = status.split("/");
            String destinationFilename = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/WhatsAppSaver" + File.separatorChar + splitStatus[splitStatus.length - 1];
            try {
                copyFile(new File(status), new File(destinationFilename));
                Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                intent.setData(Uri.fromFile(new File(destinationFilename)));
                sendBroadcast(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFetchBroadCast(ArrayList<String> statuses, String folderPath) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.FetchStatusReceiver.PROCESS_FETCH);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(FOLDER_PATH, folderPath);
        broadcastIntent.putExtra(FETCHED_STATUSES, statuses);
        sendBroadcast(broadcastIntent);
    }

    public void copyFile(File file, File file2) throws IOException {
        Throwable th;
        Throwable th2;
        if (!file2.getParentFile().exists()) {
            file2.getParentFile().mkdirs();
        }
        if (!file2.exists()) {
            file2.createNewFile();
        }
        FileChannel fileChannel = (FileChannel) null;
        FileChannel fileChannel2 = (FileChannel) null;
        FileChannel channel;
        try {
            channel = new FileInputStream(file).getChannel();
            try {
                fileChannel = new FileOutputStream(file2).getChannel();
            } catch (Throwable th3) {
                th = th3;
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel2 != null) {
                    fileChannel2.close();
                }
                throw th;
            }
            try {
                fileChannel.transferFrom(channel, (long) 0, channel.size());
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (Throwable th4) {
                th2 = th4;
                fileChannel2 = fileChannel;
                th = th2;
                if (channel != null) {
                    channel.close();
                }
                if (fileChannel2 != null) {
                    fileChannel2.close();
                }
                throw th;
            }
        } catch (Throwable th5) {
            th2 = th5;
            channel = fileChannel;
            th = th2;
            if (channel != null) {
                channel.close();
            }
            if (fileChannel2 != null) {
                fileChannel2.close();
            }
        }
    }

}
