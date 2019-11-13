package com.nilepoint.monitorevaluatemobile.persistence;

import android.content.Context;

import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.BundleBatch;
import com.nilepoint.dtn.BundleRecord;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.dtn.storage.StorageAdapter;
import com.nilepoint.model.RealmBundleRecord;

import java.util.Collection;

import io.realm.Realm;


/* Bundle storage using the realm database system */

public class PaperStorage implements StorageAdapter {

    PersistentStorageList<Bundle> bundleStorage;
    Context context;

    public PaperStorage(Context context) {
        this.context = context;
        bundleStorage = new PersistentStorageList<Bundle>("bundle-storage", context);
    }

    @Override
    public Bundle getById(String s) {
        for (Bundle bundle : bundleStorage){
            if (bundle.getId().equals(s)){
                return bundle;
            }
        }

        return null;
    }

    @Override
    public void store(Bundle bundle) {
        bundleStorage.add(bundle);
    }

    @Override
    public void remove(Bundle bundle) {
        bundleStorage.remove(bundle);
    }

    @Override
    public Collection<Bundle> list() {
        return bundleStorage;
    }

    @Override
    public void store(BundleBatch bundleBatch) {
        // none
    }

    @Override
    public void remove(BundleBatch bundleBatch) {
        // none
    }

    @Override
    public Collection<BundleBatch> listBatches() {
        // none
        return null;
    }

    @Override
    public BundleRecord findBundleRecord(String bundleId, String destinationId) {
        Realm realm = Realm.getDefaultInstance();

        RealmBundleRecord record = realm.where(RealmBundleRecord.class)
                .equalTo("bundleId", bundleId)
                .equalTo("destinationId", destinationId).findFirst();

        if (record == null){
            return null;
        }

        BundleRecord bundleRecord = new BundleRecord(record.getBundleId(), record.getDestinationId());
        bundleRecord.setDateSent(record.getDateSent());

        return bundleRecord;
    }

    @Override
    public BundleRecord storeBundleRecord(Bundle bundle, Node neighbor) {
        /*Realm realm = Realm.getDefaultInstance();

        BundleRecord record = findBundleRecord(bundle.getUuid(), neighbor.getId());

        if (record == null){
            try {
                realm.beginTransaction();

                RealmBundleRecord realmRecord = new RealmBundleRecord(bundle.getUuid(), neighbor.getId());

                realm.copyToRealm(realmRecord);

                record = new BundleRecord(realmRecord.getBundleId(), realmRecord.getDestinationId());
            } finally {
                realm.commitTransaction();
            }
        }*/


        return null;
    }
}