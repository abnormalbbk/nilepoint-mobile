package com.nilepoint.monitorevaluatemobile.dtn.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.protostuff.Tag;

/**
 * Created by ashaw on 11/21/17.
 */

public class SendParticipantsCommand extends Command {
    @Tag(1)
    Map<String,Integer> exclusions = new HashMap<>();

    public SendParticipantsCommand() {
    }


    public SendParticipantsCommand(Map<String,Integer> exclusions) {
        this.exclusions = exclusions;
    }

    public Map<String, Integer> getExclusions() {
        return exclusions;
    }

    public void setExclusions(Map<String, Integer> exclusions) {
        this.exclusions = exclusions;
    }
}
