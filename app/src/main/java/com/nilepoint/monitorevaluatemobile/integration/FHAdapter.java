package com.nilepoint.monitorevaluatemobile.integration;

import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.model.Area;

/**
 * Created by ashaw on 11/21/17.
 */

public class FHAdapter {
    public FHArea translateArea(Area area){
        FHArea fhArea = new FHArea();

        return fhArea;
    }
    public Area translateArea(FHArea area){
        return null;
    }
}
