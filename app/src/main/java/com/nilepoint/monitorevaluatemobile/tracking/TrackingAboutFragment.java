package com.nilepoint.monitorevaluatemobile.tracking;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.group.InfoAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TrackingAboutFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class TrackingAboutFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditText dateView;
    private ImageButton datePickerButton;
    private RecyclerView activityInfoRecycler;
    private Button doneButton;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SimpleDateFormat sdf;
    private String trackedActivityId;

    public TrackingAboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_tracking_about, container, false);

        dateView = (EditText) rootView.findViewById(R.id.tracking_about_date);

        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // status spinner
        Spinner spinner = (Spinner) rootView.findViewById(R.id.status_spinner);

        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.status_array, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);

        Realm realm = Realm.getDefaultInstance();

        try {
            final TrackedActivity trackedActivity = realm.where(TrackedActivity.class).equalTo("id",
                    getHostActivity().getTrackedActivityId())
                    .findFirst();

            System.out.println(trackedActivity.getStatus());

            if (trackedActivity.getActivityDate() == null){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        trackedActivity.setActivityDate(new Date());
                    }
                });
            }

            selectedDate = trackedActivity.getActivityDate();

            final String date = sdf.format(trackedActivity.getActivityDate());

            dateView.setText(date);

            for (int i =0; i<spinnerAdapter.getCount(); i++) {
                if (spinnerAdapter.getItem(i).equals(trackedActivity.getStatus())) {
                    spinner.setSelection(i, false);
                }
            }
        } finally {
            realm.close();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final CharSequence status = spinnerAdapter.getItem(position);

                Realm realm = Realm.getDefaultInstance();

                try {
                    final TrackedActivity trackedActivity = realm.where(TrackedActivity.class).equalTo("id",
                            getHostActivity().getTrackedActivityId())
                            .findFirst();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            trackedActivity.setStatus(String.valueOf(status));
                        }
                    });

                } finally {
                    realm.close();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        datePickerButton = (ImageButton) rootView.findViewById(R.id.calendar_button);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        activityInfoRecycler = (RecyclerView) rootView.findViewById(R.id.tracking_about_info_list);

        layoutManager = new LinearLayoutManager(getActivity());

        adapter = new InfoAdapter(getActivity(),getHostActivity().getInfoList());

        activityInfoRecycler.setLayoutManager(layoutManager);
        activityInfoRecycler.setAdapter(adapter);

        doneButton = (Button)  rootView.findViewById(R.id.about_done_button);
        doneButton.setVisibility(View.GONE);

        return rootView;
    }

    public static Fragment newInstance(){
        TrackingAboutFragment fragment = new TrackingAboutFragment();

        Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    /**
     *
     * @return true if edits enabled, false if not enabled
     */
    public void toggleEdits(boolean editable){
        if(editable){
            datePickerButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);
        }
        else {
            datePickerButton.setVisibility(View.INVISIBLE);
            doneButton.setVisibility(View.GONE);
        }

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
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    public ActivityDetailsHost getHostActivity(){
        return (ActivityDetailsHost) getActivity();
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

    Date selectedDate;

    DatePickerDialog datePickerDialog;

    public void showDatePicker(){

        if (selectedDate == null) {
            selectedDate = new Date();
        }

        dateView.setText(sdf.format(selectedDate));

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(selectedDate);

        datePickerDialog = new DatePickerDialog(this.getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance(Locale.getDefault());

                calendar.set(year, monthOfYear, dayOfMonth);

                selectedDate = calendar.getTime();

                final String date = sdf.format(calendar.getTime());

                dateView.setText(date);

                Realm realm = Realm.getDefaultInstance();
                try {
                    final TrackedActivity trackedActivity = realm.where(TrackedActivity.class).equalTo("id",
                            getHostActivity().getTrackedActivityId())
                            .findFirst();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            trackedActivity.setActivityDate(selectedDate);
                        }
                    });


                    System.out.println("Got " + trackedActivity);
                } finally {
                    realm.close();
                }

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                datePickerDialog = null;
            }
        });

        datePickerDialog.show();
    }
}
