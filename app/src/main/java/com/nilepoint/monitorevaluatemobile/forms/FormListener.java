package com.nilepoint.monitorevaluatemobile.forms;

import com.nilepoint.amqp.messages.MapMessage;

/**
 * Created by ashaw on 5/21/17.
 */

public interface FormListener {
    void onNew(MapMessage participant);
}
