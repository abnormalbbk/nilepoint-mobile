package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHGroup;
import com.nilepoint.fh.bridge.model.FHProject;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ParticipantsResponse;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SendSystemDataCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SystemDataResponse;
import com.nilepoint.monitorevaluatemobile.persistence.SystemStorage;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by ashaw on 11/21/17.
 */

public class SendSystemDataHandler extends CommandHandler<SendSystemDataCommand> {
    public SendSystemDataHandler() {
        super(SendSystemDataCommand.class);
    }

    @Override
    public void handleCommand(SendSystemDataCommand cmd) {
        Node node = cmd.getSourceNode();

        SystemStorage storage = new SystemStorage();

        SystemDataResponse response = new SystemDataResponse();

        List<FHProject> projects = Paper.book().read("projects.fh");

        //List<FHGroup> groups = Paper.book().read("groups.fh");

        FHArea area = Paper.book().read("area.fh");

        response.setArea(area);
        response.setProjects(projects);
        //response.setUsers(storage.getUsersAsMapMessages());
        //response.setGroups(groups);

        WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Peer system data sync starting. ")
                , node);

        WLTrackApp.dtnService.btlayer.sendRaw(response, node);

        WLTrackApp.dtnService.btlayer.sendRaw(new ResponseMessageCommand("Peer system data sync done. ")
                , node);

        System.out.println(String.format("Sending %s projects, and %s to %s",
                response.getProjects().size(),
                response.getArea(),
                node.getName()
        ));
    }
}
