package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.squareup.picasso.Picasso;

import java.io.File;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // == Accesses Extra Data From MainActivity Buttons ==

        // get the data added to the intent & use it to display this activity
        String task = getIntent().getStringExtra("task");
        // grab the label from the page
        TextView titleView = findViewById(R.id.detailTaskTitle);
        // set its text to be the item name
        titleView.setText(task);

        // Gets the body of the task to render it
        String taskBody = getIntent().getStringExtra("taskBody");
        TextView bodyView = findViewById(R.id.task_detail_desc);
        bodyView.setText(taskBody);

        // == Get the String of the image to ultimately render it

        // get the string from the intent
        String taskImageStr = getIntent().getStringExtra("taskImage");

//        TextView imageView = findViewById(R.id.testImageString);
//        imageView.setText(taskImageStr);

        // Use Picasso to display the image
        Picasso.get().load(
                "https://taskmaster288ee44745fb44eaa8db972bda84b42f-local.s3.us-west-2.amazonaws.com/"
                        + taskImageStr).into((ImageView)findViewById(R.id.taskImageShow));


//        // plug the string into transferUtility to download image from S3
//        TransferUtility transferUtility =
//                TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
//                        .build();
//
//        TransferObserver downloadObserver =
//                transferUtility.download(
//                        taskImageStr,
//                        new File(getApplicationContext().getFilesDir(), "downloadFromAws.jpg"));
//
//        downloadObserver.setTransferListener(new TransferListener() {
//
//            @Override
//            public void onStateChanged(int id, TransferState state) {
//                // grab data from downloaded file, download.txt to set image view
//                // set the taskImageShow
//                // getExternalFilesDir(Enironment.Directory_PICTURES)
//                // Set image as a bitmap, then decode the file with .decodeFile(takes in the absolute path to the file
//
//
//            }
//
//            @Override
//            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
//                int percentDone = (int) percentDonef;
//
//                Log.i("Your Activity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent
//                        + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
//            }
//
//            @Override
//            public void onError(int id, Exception ex) {
//
//            }
//        });

    }
}
