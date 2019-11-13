package com.nilepoint.monitorevaluatemobile.dtn;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.services.DTNService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashaw on 6/11/17.
 */

public class BluetoothInformationActivity extends AppCompatActivity {
    DTNService dtn = WLTrackApp.dtnService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dtn_information);

        ListView mainListView = (ListView) findViewById(R.id.dtn_list_view);

        BluetoothConvergenceLayer layer = (BluetoothConvergenceLayer) dtn.getDTN().getConvergenceLayer(BluetoothConvergenceLayer.class);

        ArrayList<Node> neighbors = new ArrayList<>();

        for (NeighborRegistration reg : layer.getNeighborRegistry().getRegistrations()){
            neighbors.add(reg.getNode());
        }

        mainListView.setAdapter(new DTNInformationListAdapter(this, android.R.layout.simple_list_item_1, neighbors));
    }

    class DTNInformationListAdapter extends ArrayAdapter<Node> {

        List<Node> neighbors;

        public DTNInformationListAdapter(Context context, int resource, List<Node> objects) {
            super(context, resource, objects);

            this.neighbors = neighbors;
        }
    }
}
