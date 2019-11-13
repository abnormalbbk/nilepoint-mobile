package com.nilepoint.monitorevaluatemobile.participant;

import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.Map;

/**
 * Created by claudiatrafton on 7/2/17.
 * Parses out map information as Strings
 */

public class HhInfoParser {

    /**
     * @return a string representation of the participant residence
     */
    public String parseResidence(Map<String, String> map) {
        String residence = map.get(FormKeyIDs.CLUSTER_ID) + "\n" +
                map.get(FormKeyIDs.COMMUNITY_ID);

        if (map.get(FormKeyIDs.VILLAGE_ID) != null) {
            residence = residence + "\n" +
                    map.get(FormKeyIDs.VILLAGE_ID);
        }
        return residence;
    }

    /**
     *
     * @return a string representation of the household
     */
    public String parseHhNumbers(Map<String, String> map) {
        String household = "";
        for (int i = 0; i < FormKeyIDs.HOUSEHOLD_MEMBER_KEYS.length; i++) {
            if (map.containsKey(FormKeyIDs.HOUSEHOLD_MEMBER_KEYS[i])) {
                household = household + map.get(FormKeyIDs.HOUSEHOLD_MEMBER_KEYS[i]) + " " + FormKeyIDs.HOUSEHOLD_MEMBER_LABELS[i] + "\n";
            }
        }

        return household;
    }
}
