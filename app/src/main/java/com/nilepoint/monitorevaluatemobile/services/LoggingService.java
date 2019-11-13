package com.nilepoint.monitorevaluatemobile.services;

import com.nilepoint.concurrency.Task;
import com.nilepoint.model.Device;
import com.nilepoint.model.StoredLog;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

/**
 * Created by ashaw on 6/18/17.
 */

public class LoggingService {

    static LoggingService singleton;

    public final static String ERROR = "ERROR";
    public final static String INFO = "INFO";
    public final static String DEBUG = "DEBUG";

    static {
        singleton = new LoggingService();
    }

    private Task storeLogTask;

    private LoggingService(){
        storeLogTask = new StoreLogTask().scheduleEvery(1, TimeUnit.SECONDS);
    }

    public ThreadLocal<Realm> tlr = new ThreadLocal<Realm>(){
        @Override
        protected Realm initialValue() {
            return Realm.getDefaultInstance();
        }
    };

    private LinkedBlockingQueue<StoredLog>  logQueue = new LinkedBlockingQueue<>();

    class StoreLogTask extends Task {
        @Override
        public void run() {
            final StoredLog log = logQueue.poll();

            if (log != null) {
                Realm realm = Realm.getDefaultInstance();

                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Device device = realm.where(Device.class).findFirst();

                            log.setDevice(device);

                            realm.copyToRealm(log);
                        }
                    });
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }
        }
    }

    public void addLog(StoredLog log){
        logQueue.add(log);
    }

    public void debug(String text){
        logQueue.add(new StoredLog(null, DEBUG,  text));
    }

    public void error(String text){
        logQueue.add(new StoredLog(null, ERROR, text));
    }

    public void info(String text){
        logQueue.add(new StoredLog(null, INFO, text));
    }

    public void debug(String category, String text){
        logQueue.add(new StoredLog(null, DEBUG,  text));
    }

    /*public void error(String category, String text){
        logQueue.add(new StoredLog(null, ERROR, text,category));
    }

    public void info(String category, String text){
        logQueue.add(new StoredLog(null, INFO, text,category));
    } */

    public static LoggingService getInstance(){
        return singleton;
    }
}
