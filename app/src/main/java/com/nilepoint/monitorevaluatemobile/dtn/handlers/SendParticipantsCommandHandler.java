package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ParticipantsResponse;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SendParticipantsCommand;
import com.nilepoint.monitorevaluatemobile.persistence.ParticipantStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ashaw on 11/21/17.
 */

public class SendParticipantsCommandHandler extends CommandHandler<SendParticipantsCommand> {
    public SendParticipantsCommandHandler() {
        super(SendParticipantsCommand.class);
    }

    @Override
    public synchronized void handleCommand(SendParticipantsCommand cmd) {
        Node node = cmd.getSourceNode();

        System.out.println("Got Send Participants Command from " + node);

        Map<String, Integer> exclusions = cmd.getExclusions();

        ParticipantStorage storage = new ParticipantStorage();

        WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Building participant list...")
                , node);

        List<MapMessage> participants = storage.getAllParticipantsAsMapMessage(cmd.getExclusions());

        final ArrayList<String> participantIds = new ArrayList<>();

        for (MapMessage participant : participants ){
            participantIds.add(participant.getId());
        }

        if (!participants.isEmpty()) {
            List<MapMessage> households = storage.getHouseholdsForParticipantIds(participantIds);

            try {
                if (!households.isEmpty()) {
                    WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Receiving "
                                    + households.size()
                                    + " households")
                            , node);

                    System.out.println("Receiving " + households.size() + " households");

                    for (int i = 0; i < households.size(); i += 100) {

                        List<MapMessage> msgs = households.subList(i, Math.min(i + 100, households.size() - 1));

                        WLTrackApp.dtnService.btlayer.sendRaw(
                                new ParticipantsResponse(null, msgs)
                                , node);
                    }

                    WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Done receiving households.")
                            , node);

                    WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Receiving " + participants.size() + " participants.")
                            , node);

                    for (int i = 0; i < participants.size(); i += 100) {

                        List<MapMessage> msgs = participants.subList(i, Math.min(i + 100, participants.size() - 1));

                        WLTrackApp.dtnService.btlayer.sendRaw(
                                new ParticipantsResponse(msgs, null)
                                , node);
                    }

                    WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Done receiving participants.")
                            , node);
                }
            } catch (Exception e){
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
    }
}
