package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Household;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ParticipantsResponse;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by ashaw on 11/21/17.
 */

public class ParticipantsResponseHandler extends CommandHandler<ParticipantsResponse> {
    public ParticipantsResponseHandler() {
        super(ParticipantsResponse.class);
    }

    @Override
    public void handleCommand(final ParticipantsResponse cmd) {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    if (cmd.getHouseholds() != null) {
                        System.out.println("Got households from another peer. size: " + cmd.getHouseholds()
                                .size());
                        for (final MapMessage householdMessage : cmd.getHouseholds()) {
                            //System.out.println("Adding household " + householdMessage.getId());
                            try {
                                final Household household = realm
                                        .where(Household.class)
                                        .equalTo("id", householdMessage.getId()).findFirst();


                                Household hh = Household.fromMessage(householdMessage);
                                if (household == null) {
                                    //System.out.println("Household not found " + hh.getId() + ". Adding to Realm");
                                    realm.copyToRealm(hh);
                                } else {
                                    //System.out.println("Household found " + hh.getId() + ". Updating in Realm");
                                    if (household.getVersion() < householdMessage.getVersion()) {
                                        household.setHeadOfHousehold(hh.getHeadOfHousehold());
                                        household.setMembers(hh.getMembers());
                                    }
                                }
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                                e.printStackTrace();
                            }
                        }
                    }

                    if (cmd.getParticipants() != null) {
                        System.out.println("Got participants from another peer. size: " + cmd.getParticipants()
                                .size());
                        for (final MapMessage participant : cmd.getParticipants()) {
                            //System.out.println("Adding participant " + participant.getId());

                            try {
                                final StoredParticipant sParticipant = realm
                                        .where(StoredParticipant.class)
                                        .equalTo("id", participant.getId()).findFirst();

                                if (sParticipant != null) {
                                    sParticipant.setMessage(participant);
                                    sParticipant.setVersion(participant.getVersion());
                                } else {

                                    realm.copyToRealm(new StoredParticipant(participant));
                                }

                            } catch (Exception e) {
                                Crashlytics.logException(e);
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        });
    }
}
