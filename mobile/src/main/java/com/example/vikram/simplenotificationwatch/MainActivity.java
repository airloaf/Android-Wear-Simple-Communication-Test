package com.example.vikram.simplenotificationwatch;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private GoogleApiClient m_googleApiClient;
    private Node m_node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create Google Api Client
        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        m_googleApiClient.connect();

        //Set the button listener
        Button send = findViewById(R.id.bt_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable(){
                    public void run(){
                        if(m_node != null){
                            byte[] bytes = ("wew lad".getBytes());
                            Wearable.MessageApi.sendMessage(m_googleApiClient, m_node.getId(), "/PhoneToWatch", bytes).await();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Toast to indicate connected
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();

        //Get the node of the watch
        new Thread(new Runnable(){

            public void run(){

                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi
                        .getConnectedNodes(m_googleApiClient).await();
                List<Node> nodes = result.getNodes();
                if(nodes.size() > 0)
                    m_node = nodes.get(0);

            }

        }).start();

        //Set the Message Api Listeners to this class
        Wearable.MessageApi.addListener(m_googleApiClient, this);

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Connection Failed toast
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Check the message and toast if its the correct message
        if(messageEvent.getPath().equals("WatchToPhone"))
            Toast.makeText(getApplication(), "Message from Watch", Toast.LENGTH_LONG).show();
    }
}
