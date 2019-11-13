package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.amqp.messages.MapMessage;

import java.util.ArrayList;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created by ashaw on 11/21/17.
 */

public class ParticipantsResponse extends Command {
    @Tag(1)
    List<MapMessage> participants   = new ArrayList<>();

    @Tag(2)
    List<MapMessage> households     = new ArrayList<>();

    public ParticipantsResponse() {
    }

    public ParticipantsResponse(List<MapMessage> participants) {
        this.participants = participants;
    }
    public ParticipantsResponse(List<MapMessage> participants, List<MapMessage> households) {
        this.participants = participants;
        this.households = households;
    }

    public List<MapMessage> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MapMessage> participants) {
        this.participants = participants;
    }

    public List<MapMessage> getHouseholds() {
        return households;
    }

    public void setHouseholds(List<MapMessage> households) {
        this.households = households;
    }
}
