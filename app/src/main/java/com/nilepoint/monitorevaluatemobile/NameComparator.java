package com.nilepoint.monitorevaluatemobile;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.StoredParticipant;

import org.bouncycastle.util.Store;

import java.util.Comparator;

/**
 * Created by claudiatrafton on 4/15/17.
 * Sorts the list of participants according to the specifiction of first or last name, depending on map key passed in
 */

    /*Comparator classes for sorting by either first or last name*/
public class NameComparator implements Comparator<StoredParticipant> {

    private String sortKey;

    public NameComparator(String sortKey){
        this.sortKey = sortKey;

    }

    public String getSortKey(){
        return this.sortKey;
    }

    @Override
    public int compare(StoredParticipant sp1, StoredParticipant sp2) {
        //Log.d("COMPARE", "comparing "  + sp1.getMap().get(sortKey) + " with " + sp2.getMap().get(sortKey));
        String cmp1 = sp1.toMessage().getMap().get(getSortKey());
        String cmp2 = sp2.toMessage().getMap().get(getSortKey());

        if (cmp1 == null){
            return -1;
        }

        if (cmp2 == null){
            return 1;
        }

        return cmp1.compareTo(cmp2);

    }
}