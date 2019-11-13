package com.nilepoint.monitorevaluatemobile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.persistence.Datastore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class TestParticipantStorage extends InstrumentationTestCase {

    private Context instrumentationCtx;

    @Before
    public void setup() {
        instrumentationCtx = InstrumentationRegistry.getContext();
    }
    @Test
    public void testParticipantStorage() {



        MapMessage msg = new MapMessage();

        msg.put("firstName", "Adam");
        msg.put("lastName", "Shaw");
        msg.put("someAttribute", "15");

        Datastore ds = Datastore.init(instrumentationCtx);

        StoredParticipant participant = ds.storeParticipant(msg);

        assertTrue(participant.getId() != null);

        MapMessage map = ds.findParticipantById(participant.getId()).toMessage();

        assertNotNull(map);

        /**
         * Make sure we got a fully persisted map
         */
        assertEquals(map.getMap().get("firstName"), "Adam");
        assertEquals(map.getMap().get("lastName"), "Shaw");
        assertEquals(map.getMap().get("someAttribute"), "15");
    }

    @Test
    public void testParticipantUpdate() {
        MapMessage msg = new MapMessage();

        msg.put("firstName", "Adam");
        msg.put("lastName", "Shaw");
        msg.put("someAttribute", "15");

        Datastore ds = Datastore.init(instrumentationCtx);

        StoredParticipant participant = ds.storeParticipant(msg);

        MapMessage participantToUpdate = participant.toMessage();

        participantToUpdate.put("firstName","Bob");
        participantToUpdate.put("someOtherAttribute", "This is a Test");

        assertNotNull(participantToUpdate.getId());

        StoredParticipant p = ds.updateParticipant(participantToUpdate);

        assertNotNull(p);
        assertEquals(p.getFirstName(), "Bob");
        assertEquals(p.toMessage().getMap().get("someOtherAttribute"), "This is a Test");
    }

    @Test
    public void testParticipantSearch() {
        MapMessage msg = new MapMessage();

        msg.put("firstName", "Adam");
        msg.put("lastName", "Shaw");

        MapMessage msg2 = new MapMessage();

        msg2.put("firstName", "Adam");
        msg2.put("lastName", "Bob");

        MapMessage msg3 = new MapMessage();

        // his name is Bobby Bobby???

        msg3.put("firstName", "Robert");
        msg3.put("lastName", "Bobby");

        Datastore ds = Datastore.init(instrumentationCtx);

        // get rid of all participants that we created in previous tests

        ds.deleteAllParticipants();

        // store the three participants we created above
        ds.storeParticipant(msg);
        ds.storeParticipant(msg2);
        ds.storeParticipant(msg3);

        // find all participants with the last name shaw
       /* List<MapMessage> msgs = ds.findParticipants("Shaw");

        // should only be one
        assertTrue(msgs.size() == 1);

        MapMessage p = msgs.get(0);

        // make sure it's all good
        assertNotNull(p.getId());

        // find all participants with the first name Adam
        assertEquals(p.getMap().get("firstName"), "Adam");

        // find peeps named Adam
        msgs = ds.findParticipants("Adam");

        // there should be two

        assertTrue(msgs.size() == 2); */

    }
}
