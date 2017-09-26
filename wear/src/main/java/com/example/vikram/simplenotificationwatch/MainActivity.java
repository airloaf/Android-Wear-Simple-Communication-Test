package com.example.vikram.simplenotificationwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    //Google Api Client and Node
    private GoogleApiClient m_googleApiClient;
    private Node m_node;

    //Send button
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the layout via stub
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                //Get the send button
                sendBtn = (Button) stub.findViewById(R.id.bt_send);
                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage("Message from Watch!");
                    }
                });

            }
        });

        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        m_googleApiClient.connect();

    }

    private void sendMessage(final String s){

        new Thread(new Runnable(){
            public void run(){

                if(m_node != null){
                    byte[] bytes = s.getBytes();
                    Wearable.MessageApi.sendMessage(m_googleApiClient, m_node.getId(), "WatchToPhone", bytes).await();
                }

            }
        }).start();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

        new Thread(new Runnable(){
            public void run(){
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(m_googleApiClient).await();
                List<Node> nodes = result.getNodes();
                if(nodes.size() > 0)
                    m_node = nodes.get(0);
            }

        }).start();
        Wearable.MessageApi.addListener(m_googleApiClient, this);

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Couldn't Connect", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("/PhoneToWatch")) {

            //Send a success message to the screen
            Intent success = new Intent(this, MyConfirmation.class);
            success.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
            success.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Phone Message to Watch");
            startActivity(success);

        }
    }
}
