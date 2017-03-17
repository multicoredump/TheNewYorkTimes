package com.coremantra.tutorial.thenewyorktimes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.coremantra.tutorial.thenewyorktimes.R;
import com.coremantra.tutorial.thenewyorktimes.models.SearchFilters;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link android.support.v4.app.DialogFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFilterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFilterFragment extends DialogFragment {

    private static final String ARG_FILTERS = "filters";

    private SearchFilters filters;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.etBeginDate)
    EditText etBeginDate;

    @BindView(R.id.btSave)
    Button btSave;

    @BindView(R.id.cbFood)
    CheckBox cbFood;

    @BindView(R.id.cbFashion)
    CheckBox cbFashion;

    @BindView(R.id.cbDining)
    CheckBox cbDining;

    @BindView(R.id.cbTravel)
    CheckBox cbTravel;

    @BindView(R.id.cbTech)
    CheckBox cbTech;

    @BindView(R.id.radioOldest)
    RadioButton radioOldest;

    @BindView(R.id.radioNewest)
    RadioButton radioNewest;

    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // update search filters
           if (mListener != null) {

               filters.update(etBeginDate.getText().toString(),
                       radioOldest.isChecked(),
                       cbFood.isChecked(), cbFashion.isChecked(), cbDining.isChecked(), cbTravel.isChecked(), cbTech.isChecked());

               mListener.onFinishDialog(filters);
           }
           dismiss();
        }
    };

    public SearchFilterFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener {
        void onFinishDialog(SearchFilters filters);
    }

    public static SearchFilterFragment newInstance(SearchFilters filters) {
        SearchFilterFragment fragment = new SearchFilterFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTERS, Parcels.wrap(filters));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           filters = Parcels.unwrap(getArguments().getParcelable(ARG_FILTERS));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_filter, container, false);
        ButterKnife.bind(this, view);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (filters.getBeginDateString() != null) {
            etBeginDate.setText(filters.getBeginDateString());
        }
        cbFood.setChecked(filters.isFood());
        cbFashion.setChecked(filters.isFashion());
        cbDining.setChecked(filters.isDining());
        cbTravel.setChecked(filters.isTravel());
        cbTech.setChecked(filters.isTech());

        radioOldest.setChecked(filters.isSortOldest());
        radioNewest.setChecked(!filters.isSortOldest());

        btSave.setOnClickListener(saveClickListener);
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



    public void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelection(index);
    }
}
