package com.nilepoint.monitorevaluatemobile.activity_tracking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormFragment;
import com.github.dkharrat.nexusdialog.controllers.DatePickerController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.SelectionController;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpecifyTrackingActivityFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpecifyTrackingActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpecifyTrackingActivityFragment extends FormFragment implements ValidateableFormFragment {

    private static final String TITLE = "Specify Activity Profile";
    private static final String PROJECT = "Select Project";
    private static final String CATEGORY = "Select Activity Category";
    private static final String COMMUNITY = "Community";
    private static final String CLUSTER = "Cluster";
    private OnFragmentInteractionListener mListener;

    public SpecifyTrackingActivityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpecifyTrackingActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpecifyTrackingActivityFragment newInstance(String param1, String param2) {
        SpecifyTrackingActivityFragment fragment = new SpecifyTrackingActivityFragment();
        Bundle args = new Bundle();
        /*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2); */
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(TITLE);
    }


    @Override
    public void initForm(FormController controller) {
        Context context = getContext();
        FormSectionController section = new FormSectionController(context, TITLE);

        section.addElement(new DatePickerController(context, "date", "Select Activity Date"));

        section.addElement(new SelectionController(context, PROJECT, "Select Project", true, PROJECT,
                Arrays.asList("Project 1", "Project 2"), true));

        section.addElement(new SelectionController(context, CATEGORY, "Select Activity Category", true, CATEGORY,
                Arrays.asList("Category 1", "Category 2"), true));

        section.addElement(new SelectionController(context, CLUSTER, CLUSTER, true, CLUSTER,
                Arrays.asList("Cluster 1", "Cluster 2"), true));

        section.addElement(new SelectionController(context, COMMUNITY, COMMUNITY, true, COMMUNITY,
                Arrays.asList("COMMUNITY 1", "COMMUNITY 2"), true));

        controller.addSection(section);
    }

    public boolean validate() {
        getFormController().resetValidationErrors();
        if(!getFormController().isValidInput()){
            getFormController().showValidationErrors();
            return false;
        }
        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
