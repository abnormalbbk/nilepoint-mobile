package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHGroup;
import com.nilepoint.fh.bridge.model.FHPlannedActivity;
import com.nilepoint.fh.bridge.model.FHProject;

import java.util.ArrayList;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created by ashaw on 11/21/17.
 */

public class SystemDataResponse extends Command {
    /*@Tag(1)
    List<MapMessage> users = new ArrayList<>();*/
    @Tag(2)
    FHArea area;
    @Tag(3)
    List<FHProject> projects = new ArrayList<>();
    /*@Tag(4)
    List<FHGroup> groups = new ArrayList<>();*/

    public SystemDataResponse() {
    }

    public FHArea getArea() {
        return area;
    }

    public void setArea(FHArea area) {
        this.area = area;
    }

    public List<FHProject> getProjects() {
        return projects;
    }

    public void setProjects(List<FHProject> projects) {
        this.projects = projects;
    }
}
