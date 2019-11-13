package com.nilepoint.monitorevaluatemobile.participant;

import com.nilepoint.model.StoredParticipant;

import java.util.List;

/**
 * Created by ashaw on 9/15/17.
 */

public interface ParticipantDataSource {
    public List<StoredParticipant> getParticipants();
    public List<StoredParticipant> getParticipants(String search);
    public List<StoredParticipant> getParticipants(String search, String sort);
}
