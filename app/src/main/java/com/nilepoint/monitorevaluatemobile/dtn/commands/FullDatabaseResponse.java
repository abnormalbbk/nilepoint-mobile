package com.nilepoint.monitorevaluatemobile.dtn.commands;


import com.nilepoint.dtn.Bundle;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by ashaw on 7/16/17.
 */

public class FullDatabaseResponse extends Command {
    @Tag(1)
    List<Bundle> bundles;


    public FullDatabaseResponse() {
    }

    public FullDatabaseResponse(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    public List<Bundle> getBundles() {
        return bundles;
    }

    public void setBundles(List<Bundle> bundles) {
        this.bundles = bundles;
    }
}
