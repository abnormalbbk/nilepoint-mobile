package com.nilepoint.monitorevaluatemobile.persistence;

import android.content.Context;
import android.util.Log;

import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.persistence.ProtostuffObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import io.paperdb.Paper;

/**
 * Created by ashaw on 7/12/17.
 */

/**
 * This array list is thread safe and uses Paper, a key value store, to persist on disk
 * after each write. By default this does not allow duplicates to be added (since it's primarily
 * used for bundle storage. This can be changed by setting allow Duplicates to be true).
 *
 * This class was created because Realm was having a hard time using multiple threads - different
 * results were returned.
 *
 * @param <E> The type of object this storage array list will store.
 */
public class PersistentStorageList<E> extends CopyOnWriteArrayList<E> {


    public static String TAG = "PersistentStorageList";

    String name;
    Context context;

    private boolean allowDuplicates = false;

    public PersistentStorageList(String name, Context context) {
        this.context = context;
        this.name = name;

        restore();

       // Log.d(TAG,"PersistentStorageList: Storage after restore: " + this);
    }

    @Override
    public boolean add(E object){
       // Log.d(TAG,"PersistentStorageList: Adding " + object);

        if (allowDuplicates == false && this.contains(object)){
            //Log.d(TAG,"PersistentStorageList: Duplicate object, not adding " + object);

            return false;
        }

        boolean didAdd = super.add(object);

        if (didAdd){
            save();
        }

        return didAdd;
    }

    @Override
    public boolean remove(Object object){
       // Log.d(TAG,"PersistentStorageList: Removing " + object);

        boolean didRemove = super.remove(object);

        //Log.d(TAG,"PersistentStorageList: Did Remove? " + didRemove);

        if (didRemove){
            save();
        }

        return didRemove;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> collection) {
       // Log.d(TAG,"PersistentStorageList: Removing " + collection);

        boolean didRemove = super.removeAll(collection);

        if (didRemove){
            save();
        }

        return didRemove;
    }

    public void save(){

       // Log.d(TAG,"PersistentStorageList: Saving " + this + " to storage");

        ArrayList<ProtostuffPayload> arrayList = new ArrayList<>();

        for (E obj : this){
            arrayList.add(new ProtostuffPayload(obj));
        }

        try {
            Paper.book("persistent-storage").write(name + "-storage", arrayList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void restore(){
        //Log.d(TAG,"PersistentStorageList: Restoring " +name+  " from storage");
        try {
            ArrayList<ProtostuffPayload> arr = Paper.book("persistent-storage").read(name + "-storage");

            if (arr ==  null){
                Log.d(TAG,"PersistentStorageList: Could not restore " + name + " from storage");
                return;
            }

            for (ProtostuffPayload obj : arr){

                super.add((E) obj.unpack());
            }

            Log.d(TAG,"PersistentStorageList: Successfully loaded " + this.size() + " objects");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean allowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    public void clear(){
        super.clear();

        save();
    }
}
