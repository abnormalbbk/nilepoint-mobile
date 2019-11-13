package com.nilepoint.monitorevaluatemobile.forms;

/**
 * Keys for the different form Elements.
 * Made for ease of access thrughout the application
 * Created by claudiatrafton on 4/23/17.
 */

public class FormKeyIDs {

    public final static String GIVEN_NAME_ID = "givenName";
    public final static String FATHER_NAME_ID = "fatherName";
    public final static String PREFERRED_NAME_ID = "preferredName";
    public final static String PHONE_NUMBER_ID = "phoneNumber";
    public final static String ESTIMATED_AGE = "estimatedAge";
    public final static String PSNP_NUMBER = "psnpNumber";
    public final static String BIRTHDAY_ID = "birthday";
    public final static String GENDER_ID = "gender";

    //address
    public final static String CLUSTER_ID = "cluster";
    public final static String COMMUNITY_ID = "community";
    public final static String VILLAGE_ID = "village";
    public final static String PARTICIPANT_CODE = "participantCode";

    //household members


    public final static String[] HOUSEHOLD_MEMBER_KEYS= {"adultsF","childrenF1","childrenF2","childrenF3","childrenF4",
                                                         "adultsM","childrenM1","childrenM2","childrenM3","childrenM4"};


    public final static String [] HOUSEHOLD_MEMBER_LABELS = {"Female Adults 18+", "Female Children 5-17", "Female Children 24-59 Months",
                                                             "Female Children 6-23 Months", "Female Children 0-5 Months", "Male Adults 18+",
                                                              "Male Children 5-17" ,"Male Children 24-59 Months","Male Children 6-23 Months","Male Children 0-5 Months"};
}
