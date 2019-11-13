package com.nilepoint.monitorevaluatemobile.dtn.commands;

import io.protostuff.Tag;

/**
 * Created by ashaw on 11/21/17.
 *
 * This command will send the current statistics from one peer to another.
 */

public class HelloCommand extends Command {
    @Tag(1)
    private String deviceId;

    @Tag(2)
    private Long numberOfParticipants;

    @Tag(3)
    private Long numberOfAreas;

    @Tag(4)
    private Long numberOfProjects;

    @Tag(5)
    private Long numberOfPlannedActivities;

    @Tag(6)
    private String userPhoto;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(Long numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public Long getNumberOfAreas() {
        return numberOfAreas;
    }

    public void setNumberOfAreas(Long numberOfAreas) {
        this.numberOfAreas = numberOfAreas;
    }

    public Long getNumberOfProjects() {
        return numberOfProjects;
    }

    public void setNumberOfProjects(Long numberOfProjects) {
        this.numberOfProjects = numberOfProjects;
    }

    public Long getNumberOfPlannedActivities() {
        return numberOfPlannedActivities;
    }

    public void setNumberOfPlannedActivities(Long numberOfPlannedActivities) {
        this.numberOfPlannedActivities = numberOfPlannedActivities;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
}
