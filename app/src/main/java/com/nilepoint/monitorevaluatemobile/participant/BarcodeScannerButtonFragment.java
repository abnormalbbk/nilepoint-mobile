package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.nilepoint.monitorevaluatemobile.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BarcodeScannerButtonFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarcodeScannerButtonFragment#newInstance} factory method to
 * create an instance of this fragment.
 * FRAGMENT THAT WILL CONTAIN THE BUTTON FOR THE BARCODE SCANNER
 */
public class BarcodeScannerButtonFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ImageButton scanBarcodeButton;
    private String callerActivity;

    public BarcodeScannerButtonFragment(){

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BarcodeScannerButtonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarcodeScannerButtonFragment newInstance() {
        BarcodeScannerButtonFragment fragment = new BarcodeScannerButtonFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_barcode_scanner_button, container, false);
        final String flag = getArguments().getString("activityStartFlag");
        scanBarcodeButton = (ImageButton) rootView.findViewById(R.id.find_participant_barcode_button);

        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),BarcodeScannerHostActivity.class);
                if(flag == null)
                    intent.putExtra("activityStartFlag","viewProfile");
                else
                    intent.putExtra("activityStartFlag", flag);
                startActivity(intent);
            }
        });

        return rootView;
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
