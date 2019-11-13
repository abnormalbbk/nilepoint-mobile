package com.nilepoint.monitorevaluatemobile;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.AMQPConfiguration;
import com.nilepoint.amqp.AMQPConnection;
import com.nilepoint.amqp.Endpoint;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.api.ExchangeAPIManager;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.exchange.model.Participant;
import com.nilepoint.fh.bridge.NPExchangeAPIManager;
import com.nilepoint.formbuilder.FormService;
import com.nilepoint.model.Area;
import com.nilepoint.model.Form;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.FormSection;
import com.nilepoint.model.Household;
import com.nilepoint.model.StoredForm;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothRegistration;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandCenter;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.HelloCommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.ParticipantsResponseHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.ResponseMessageHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SendParticipantsCommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SendSystemDataHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SyncDataCommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.handlers.SystemDataResponseHandler;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantFormFactory;
import com.nilepoint.monitorevaluatemobile.persistence.Environment;
import com.nilepoint.monitorevaluatemobile.services.HouseholdService;
import com.nilepoint.monitorevaluatemobile.services.ParticipantService;
import com.nilepoint.monitorevaluatemobile.services.DTNService;
import com.nilepoint.monitorevaluatemobile.services.LoggingService;
import com.nilepoint.monitorevaluatemobile.services.UpdateRoutingService;
import com.nilepoint.monitorevaluatemobile.settings.DTNSettingsActivity;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;
import com.nilepoint.monitorevaluatemobile.user.UserSession;
import com.nilepoint.serialization.ProtostuffManager;

import io.fabric.sdk.android.Fabric;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.paperdb.Book;
import io.paperdb.Paper;
import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * Created by ashaw on 3/1/17.
 */

public class WLTrackApp extends Application {
    public static String TAG = "MonitorEvaluateApp";
    public static FormService formService;
    public static DTNService dtnService;
    public static ParticipantService participantService;
    public static HouseholdService householdService;
    public static UpdateRoutingService participantUpdateService;
    public static LoggingService log;

    static AMQPConnection<MapMessage> messageConnection;

    public static MobileDevice device;

    public RemoteLogger logger;

    public static Boolean sendingParticipants = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
         //Bugsee.launch(this, "6c3fe64c-1a7f-409b-b986-f474e0025198");
        BasicConfigurator.configure();

        Paper.init(this);
        Realm.init(this);

        Book book = Paper.book();

        // create device if it doesn't exist

        if (!book.contains("device")){
            Log.i(TAG, "Device doesn't exist, creating it");
            createDevice();
            Log.i(TAG, "Created: " + Paper.book().read("device"));


            book.write(DTNSettingsActivity.AMQP_LAYER_ACTIVE_LABEL, true);
            book.write(DTNSettingsActivity.TCP_LAYER_ACTIVE_LABEL, false);
            book.write(DTNSettingsActivity.BT_LAYER_ACTIVE_LABEL, true);

            createEnvironments();

            // boolean to tell us if data is initialized on this phone yet.

            Paper.book().write("data.init", false);

        }

        device = book.read("device");

        if (!book.contains("environments")) {
            createEnvironments();
        }

        if (book.contains("environmentName")){
            setEnvironment();
            afterEnvironmentSelection();
        }

        logger = new RemoteLogger();

        createForms();

        setupShutdownHook();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mCurrentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mCurrentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        logger.info(TAG, "Wordlink Track Started - v 1.1.0.0");
    }

    private void createForms(){
        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            Realm.compactRealm(realm.getConfiguration());

            StoredForm participantForm = realm
                    .where(StoredForm.class)
                    .equalTo("name", "Participant")
                    .greaterThanOrEqualTo("version",
                            ParticipantFormFactory.DEFAULT_FORM_VERSION).findFirst();

            ParticipantFormFactory formFactory = new ParticipantFormFactory(formService);

            // no form, create it.
            if (participantForm == null) {
                formFactory.createParticipantForm();
            }

            //bootstrap the household form.
            StoredForm householdForm = realm
                    .where(StoredForm.class)
                    .equalTo("name", "Household")
                    .greaterThanOrEqualTo("version",
                            ParticipantFormFactory.DEFAULT_FORM_VERSION).findFirst();

            if (householdForm == null) {
                formFactory.createHouseholdForm();
            }

            // fix external Id issue
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (final StoredParticipant participant : realm.where(StoredParticipant.class).findAll()){


                        if (participant.getExternalId() == null){
                            MapMessage msg = participant.toMessage();

                            String pCode = msg.getMap().get(FormKeyIDs.PARTICIPANT_CODE);

                            if (pCode != null){
                                System.out.println("Fixing participant " + participant.getId()
                                        + " pcode: " + pCode);
                                participant.setExternalId(pCode);


                            }
                        }
                    }

                }
            });

        } catch ( Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void afterEnvironmentSelection(){
        Book book = Paper.book();

        Environment environment = book.read("environment");
        if (book.contains("environment")) {

            ExchangeAPIManager.getInstance().setApiKey(environment.getExchangeKey());
            ExchangeAPIManager.getInstance().setEndpointUrl(environment.getExchangeHostname());

            ProtostuffManager<Form> psManager = new ProtostuffManager<>(Form.class);

            AMQPConfiguration<Form> formAMQPConfiguration = new AMQPConfiguration<>()
                    .endpoint(new Endpoint(environment.getAmqpHostname()))
                    .username(environment.getAmqpUsername())
                    .password(environment.getAmqpPassword())
                    .exchangeType("direct")
                    .deserializer(psManager)
                    .serializer(psManager)
                    .secure(true)
                    .ack(true)
                    .exchange("formExchange")
                    .queue("form-" + device.getId());

            formService = new FormService(this, formAMQPConfiguration);
        }

        createForms();

        StatisticsManager.startTask();

        createDTN();

        queueConnect();

        participantService = new ParticipantService();
        householdService = new HouseholdService();

        initCommandCenter();
    }

    public static void afterLogin(Context context, String userId){
        UserSession.userId = userId;

        MobileDevice apiDevice = Paper.book().read("device");

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id", userId).findFirst();

            apiDevice.setLastUser(user.getFirstName() + " " + user.getLastName());

            Environment environment = Paper.book().read("environment");

            // update connection
            ProtostuffManager<MapMessage> meessagePSManager = new ProtostuffManager<>(MapMessage.class);

            // this is the topic exchange for participant data - polling jobs and such.
            AMQPConfiguration<MapMessage> participantAMQPConfiguration = new AMQPConfiguration<>()
                    .endpoint(new Endpoint(environment.getAmqpHostname()))
                    .secure(true)
                    .username(environment.getAmqpUsername())
                    .password(environment.getAmqpPassword())
                    .exchangeType("topic")
                    .deserializer(meessagePSManager)
                    .serializer(meessagePSManager)
                    .ack(true)
                    .exchange("participantExchange")
                    .topic("country.all") // get all updates for the country
                    .topic("cluster.all") // get all updates for the cluster
                    .queue("participant-" + apiDevice.getId()); // participant queue


            for (Area area : user.getAreas()){
                Log.i(TAG, "Subscribing to topic " + "area." + area.getId());

                participantAMQPConfiguration.topic("area." + area.getId());
            }

            participantUpdateService = new UpdateRoutingService(context, participantAMQPConfiguration);

            // for testing - uncomment to delete all households and participants
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //realm.where(Household.class).findAll().deleteAllFromRealm();
                    //realm.where(StoredParticipant.class).findAll().deleteAllFromRealm();
                }
            });

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        Paper.book().write("device", apiDevice);



    }
    private void createDTN(){
        dtnService = new DTNService(this);
    }

    /**
     * Sets up a new choice element with passed in choices
     * @param fe a choice element
     * @param choices array of choices
     * @param fs its parent section
     */
    public void setElementChoices(FormElement fe, String[] choices, FormSection fs){
        fe.setChoices(Arrays.asList(choices));
        fs.addElement(fe);
    }

    public static void send(MapMessage message){
        if (messageConnection != null) {
            messageConnection.sendAsync(message);
        } else {
            Log.e(TAG, "Cannot send message " + message + " connection is null");
        }
    }

    public FormService getFormService() {
        return formService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    public void queueConnect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isNetworkAvailable()){
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex){

                    }

                    queueConnect();
                    return;
                }
                try {
                    messageConnection.connect();
                } catch (Throwable e){
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex){
                        Crashlytics.logException(ex);
                    }
                    queueConnect();
                }
            }
        }).start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void createDevice(){

        final String deviceId = UUID.randomUUID().toString();

        Paper.book().write("device.id", deviceId);
        Paper.book().write("device.name", Build.MODEL);

        MobileDevice apiDevice = new MobileDevice(deviceId, Build.MODEL);

        Paper.book().write("device", apiDevice);
    }

    /**
     * Setup environments here. If you want to add a new development instance just create a new environment
     * and add it to this method.
     */
    public void createEnvironments() {

        List<Environment> environments = new ArrayList<>();

        Environment dev = new Environment("Development",
                "exchange.nilepoint.com",
                "fhexchange",
                "mqdkkqwe3232139j2i3",
                "https://exchange.nilepoint.com/",
                "abc123",
                "https://beta-auth.fh.org/api/v1/auth",
                "beta-api2.fh.org"
        );

        Environment alpha = new Environment("Alpha",
                "alpha-mq.fh.org",
                "fhexchange",
                "Micah68",
                "https://alpha-exchange.fh.org/",
                "abc123",
                "https://alpha-auth.fh.org/api/v1/auth",
                "alpha-api2.fh.org"
        );

        Environment beta = new Environment("Beta",
                "beta-mq.fh.org",
                "fhexchange",
                "mqdkkqwe3232139j2i3",
                "https://beta-exchange.fh.org/",
                "abc123",
                "https://beta-auth.fh.org/api/v1/auth",
                "beta-api2.fh.org"
        );

        Environment npbeta = new Environment("Beta-NP",
                "mq1.nilepoint.com",
                "fhexchange",
                "mqdkkqwe3232139j2i3",
                "https://exchange.nilepoint.com/",
                "abc123",
                "https://beta-auth.fh.org/api/v1/auth",
                "beta-api2.fh.org"
        );

        Environment pgdev = new Environment("PG-Dev",
                "exchange-dev.nilepoint.com",
                "fhexchange",
                "mqdkkqwe3232139j2i3",
                "https://exchange-dev.nilepoint.com",
                "abc123",

                "https://exchange-dev.nilepoint.com/login/authenticate",
                "exchange-dev.nilepoint.com"
        );


        Environment npStaging = new Environment("NP-Staging",
                "exchange-staging.nilepoint.com",
                "fhexchange",
                "mqdkkqwe3232139j2i3",
                "https://exchange-staging.nilepoint.com",
                "abc123",

                "https://exchange-staging.nilepoint.com/login/authenticate",
                "exchange-staging.nilepoint.com"
        );
        environments.add(dev);
        environments.add(alpha);
        environments.add(beta);
        environments.add(npbeta);
        environments.add(pgdev);
        environments.add(npStaging);


        Paper.book().write("environments", environments);
        Paper.book().write("environment", pgdev);
    }

    public static void setEnvironment(){
        Book book = Paper.book();

        List<Environment> environments = book.read("environments");

        if (book.contains("environmentName")){
            for (Environment env : environments){
                if (env.getName().equals(book.read("environmentName"))){
                    Log.d(TAG,"Setting environment to " + env.getName());
                    book.write("environment", env);
                }
            }
        }
    }

    public static void customToast(final String message){
        if (mCurrentActivity != null) {
            customToast(mCurrentActivity, message);
        }
    }
    public static void customToast(final Activity context, final String message){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, R.string.env_restart, Toast.LENGTH_SHORT);
                LayoutInflater inflater = context.getLayoutInflater();

                View layouttoast = inflater.inflate(R.layout.toast, (ViewGroup) context.findViewById(R.id.toastcustom));
                ((TextView) layouttoast.findViewById(R.id.texttoast)).setText(message);

                toast.setView(layouttoast);
                toast.show();
            }
        });
    }

    /**
     * Command center provides a central place to handle incoming messages on the bluetooth
     * DTN layer. TODO: Expand to bundle layer
     */
    private void initCommandCenter(){
        CommandCenter cmd = CommandCenter.getInstance();

        /**
         * Handle basic hello messages. These messages are heartbeats.
         */
        cmd.addHandler(new HelloCommandHandler());
        /**
         * Handle messages that ask us to send participants to the remote host.
         */
        cmd.addHandler(new SendParticipantsCommandHandler());
        /**
         * Handle the participant response.
         */
        cmd.addHandler(new ParticipantsResponseHandler());

        /**
         * Handle messages that ask us to send system data
         */
        cmd.addHandler(new SendSystemDataHandler());
        /**
         * Handle messages that contain system data.
         */
        cmd.addHandler(new SystemDataResponseHandler());
        /**
         * Handle messages from other phones.
         */
        cmd.addHandler(new ResponseMessageHandler(this));

        cmd.addHandler(new SyncDataCommandHandler(this));
    }

    private void setupShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
               if (dtnService.btlayer != null){
                   for (BluetoothRegistration reg : dtnService.btlayer.getBluetoothRegistrations().values()){
                       System.out.println("Stopping bluetooth server on " + reg.getDevice());
                       reg.getSender().disconnect();
                   }
               }
            }
        });
    }

    private static Activity mCurrentActivity = null;

    public static Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public static void setCurrentActivity(Activity mCurrentActivity){
        WLTrackApp.mCurrentActivity = mCurrentActivity;
    }


}
