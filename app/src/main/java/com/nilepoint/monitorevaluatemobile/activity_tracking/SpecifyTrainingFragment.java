package com.nilepoint.monitorevaluatemobile.activity_tracking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormFragment;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.SelectionController;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpecifyTrainingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpecifyTrainingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpecifyTrainingFragment extends FormFragment implements ValidateableFormFragment{

    private static final String TITLE = "Specify Training Profile";
    private static final String TRAINING = "Training";
    private static final String MODULE = "Module";
    private static final String LESSON = "Lesson";

    private OnFragmentInteractionListener mListener;

    public SpecifyTrainingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SpecifyTrainingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpecifyTrainingFragment newInstance(String param1, String param2) {
        SpecifyTrainingFragment fragment = new SpecifyTrainingFragment();
        Bundle args = new Bundle();
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

        Context context = getActivity();
        //FormController.generateViewId();

        //controller = new FormController(context);

        FormSectionController section = new FormSectionController(context, TITLE);

        section.addElement(new SelectionController(context, TRAINING, TRAINING, true, TRAINING,
                Arrays.asList("Training 1", "Training 2"), true));

        section.addElement(new SelectionController(context, MODULE, MODULE, true, MODULE,
                Arrays.asList("Module 1", "Module 2"), true));

        section.addElement(new SelectionController(context, LESSON, LESSON, true, LESSON,
                Arrays.asList("Lesson 1", "Lesson 2"), true));


        //controller.addSection(sectionTraining);
        controller.addSection(section);
    }

    public boolean validate() {
        return false;
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
