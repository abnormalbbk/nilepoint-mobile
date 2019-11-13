package com.nilepoint.monitorevaluatemobile.init;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothConvergenceLayer;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandCenter;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseCallback;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataResponse;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SyncDataCommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SyncDataResponseHandler;

import io.paperdb.Paper;

/**
 * Created by ashaw on 2/1/18.
 */

public class ConnectedPeerActivity extends AppCompatActivity {

    MobileDevice mobileDevice;
    Node node;
    EditText errorView;
    Button doneButton;


    Handler mainHandler;

    BluetoothConvergenceLayer net = WLTrackApp.dtnService.btlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connected_peer);

        errorView = (EditText) findViewById(R.id.errorText);
        doneButton = (Button) findViewById(R.id.peer_done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupCommandCenter();

        // get the mobile device
        String deviceId  = getIntent().getStringExtra("device.id");
        Boolean isClient = getIntent().getBooleanExtra("isClient", false);
        Boolean isInit   = Paper.book().read("data.init");

        MobileDeviceRegistry tracker = MobileDeviceRegistry.getInstance();

        mobileDevice = tracker.findById(deviceId);

        node = tracker.findNodeByDevice(mobileDevice);


        MobileDevice myDevice = Paper.book().read("device");

        SyncDataCommand syncCmd = new SyncDataCommand(node, isClient);

        syncCmd.setSourceDevice(myDevice);
        // if init is false (the device has no data) then we need to init it from the peer. This will
        // send the system data over.
        syncCmd.setInit(isInit == false);

        System.out.println("Sending sync dta command, init: " + isInit);

        mainHandler = new Handler(getMainLooper());

        net.sendRaw(syncCmd, node);
    }

    CommandHandler<ResponseMessageCommand> addMessageHandler;
    CommandHandler<SyncDataCommand> syncDataCommandHandler;
    CommandHandler<SyncDataResponse> syncDataResponseHandler;

    private void setupCommandCenter(){
        CommandCenter cmd = CommandCenter.getInstance();

        addMessageHandler = new AddMessageToError(mobileDevice);
        syncDataResponseHandler = new SyncDataResponseHandler();

        syncDataResponseHandler.setCallback(new ResponseCallback() {
            @Override
            public void infoMessage(final String message) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorView.append(message + "\n");
                    }
                });
            }

            @Override
            public void errorMessage(final String error) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorView.append(error + "\n");
                    }
                });
            }

            @Override
            public void isDone() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorView.append("Sync has completed.\n");
                        doneButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        cmd.addHandler(addMessageHandler);
        cmd.addHandler(syncDataResponseHandler);
        //cmd.addHandler(syncDataCommandHandler);
    }

    private void removeListeners(){
        CommandCenter cmd = CommandCenter.getInstance();

        cmd.removeHandler(addMessageHandler);
       // cmd.removeHandler(syncDataCommandHandler);
        cmd.removeHandler(syncDataResponseHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        removeListeners();
    }

    class AddMessageToError extends CommandHandler<ResponseMessageCommand> {
        public AddMessageToError(MobileDevice device) {
            super(ResponseMessageCommand.class);

            setDeviceFilter(device);
        }

        @Override
        public void handleCommand(final ResponseMessageCommand cmd) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    errorView.append(cmd.getMessage() + "\n");
                }
            });
        }
    }
}
