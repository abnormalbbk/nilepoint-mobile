package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashaw on 11/21/17.
 */

public class CommandCenter {
    
    public List<CommandHandler<Command>> handlers = new ArrayList<>();


    static CommandCenter instance;

    static {
        instance = new CommandCenter();
    }

    private CommandCenter() {
    }

    public void commandReceived(final Command command){
        System.out.println("Command received " + command + " from " + command.getSourceNode());

        Node source = command.getSourceNode();

        WLTrackApp.dtnService.btlayer.getNeighborRegistry().addOrUpdateRegistration(source);

        MobileDevice mobileDevice = MobileDeviceRegistry.getInstance().findByNode(source);

        List<CommandHandler<Command>> handlers = findHandlers(command);

        for (final CommandHandler<Command> handler : handlers){
            if (handler.getDeviceFilter() == null || handler.getDeviceFilter().equals(mobileDevice)) {
                new Thread() {
                    @Override
                    public void run() {
                        handler.handleCommand(command);
                    }
                }.start();
            }
        }

    }

    private List<CommandHandler<Command>> findHandlers(Command command){
        List<CommandHandler<Command>> handlerList = new ArrayList<>();

        for (CommandHandler<Command> handler : handlers){
            if (handler.commandClass.equals(command.getClass())){
                handlerList.add(handler);
            }
        }

        return handlerList;
    }

    public void addHandler(CommandHandler handler){
        handlers.add(handler);
    }

    public void removeHandler(CommandHandler<?> handler){
        handlers.remove(handler);
    }

    public static CommandCenter getInstance() {
        return instance;
    }
}
