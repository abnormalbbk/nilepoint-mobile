package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Household;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SystemDataResponse;
import com.nilepoint.monitorevaluatemobile.integration.SystemDataProcessor;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 11/21/17.
 */

public class SystemDataResponseHandler extends CommandHandler<SystemDataResponse> {
    public SystemDataResponseHandler() {
        super(SystemDataResponse.class);
    }

    @Override
    public void handleCommand(final SystemDataResponse cmd) {
        Realm realm = Realm.getDefaultInstance();

        try {


            SystemDataProcessor processor = new SystemDataProcessor();

            System.out.println("SystemDataResponseHandler: Persisting " + cmd.getProjects().size() + " projects");
            processor.processProjects(cmd.getProjects());

            Paper.book().write("projects.fh", cmd.getProjects());

            System.out.println("SystemDataResponseHandler: Persisting Area: " + cmd.getArea());
            processor.processBaseArea(realm, cmd.getArea());
            Paper.book().write("area.fh", cmd.getArea());

            /*System.out.println("SystemDataResponseHandler: Persisting Groups: " + cmd.getGroups());
            processor.processGroups(realm, cmd.getGroups());
            Paper.book().write("groups.fh", cmd.getGroups());*/

        } catch (Exception ex) {
            Crashlytics.logException(ex);
            ex.printStackTrace();
        } finally {
            realm.close();
        }

    }
}
