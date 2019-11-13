package com.nilepoint.monitorevaluatemobile.forms;

import com.nilepoint.amqp.messages.MapMessage;

import java.util.Map;

/**
 * Created by claudiatrafton on 5/21/17.
 * FUnctions to assist in converting form data to useable formats
 */

public class FormDataConverter {

    /**
     * turns Map from previous activity into a MapMessage
     * @param map
     * @return information from the form as a mapmessage
     */
    public MapMessage toMapMessage(Map<String,String> map) {
        MapMessage msg = new MapMessage();
        for(Map.Entry<String,String> entry: map.entrySet()){
            msg.put(entry.getKey(), entry.getValue());
        }
        return msg;
    }
}
