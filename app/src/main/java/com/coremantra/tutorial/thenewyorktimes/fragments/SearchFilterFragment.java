package com.coremantra.tutorial.thenewyorktimes.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.coremantra.tutorial.thenewyorktimes.R;
import com.coremantra.tutorial.thenewyorktimes.databinding.FragmentSearchFilterBinding;
import com.coremantra.tutorial.thenewyorktimes.models.SearchFilters;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFilterFragment extends DialogFragment {

    private static final String ARG_FILTERS = "filters";
    private static final String TAG = "NY: " + SearchFilterFragment.class.getName();

    private SearchFilters filters;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.etBeginDate)
    EditText etBeginDate;

    @BindView(R.id.cbNoBeginDate)
    CheckBox cbIgnoreBeginDate;

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

    @BindView(R.id.spSort)
    Spinner spSort;

    private Calendar beginDate = Calendar.getInstance();
    private String displayDatePattern = "MM/dd/yy";
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat(displayDatePattern, Locale.US);


    FragmentSearchFilterBinding binding;

    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // update search filters
            if (mListener != null) {

                if (cbIgnoreBeginDate.isChecked()) {
                    Log.d(TAG, " -------- saving ignore state");
                    filters.update(spSort.getSelectedItem().toString().toLowerCase().equals(SearchFilters.SORT_OLDEST.toLowerCase()),
                            cbFood.isChecked(), cbFashion.isChecked(), cbDining.isChecked(), cbTravel.isChecked(), cbTech.isChecked());
                } else {
                    Log.d(TAG, " -------- OVERWRITING ignore state");
                    Date date = new Date();

                    try {
                        date = displayDateFormat.parse(etBeginDate.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    filters.update(date, spSort.getSelectedItem().toString().toLowerCase().equals(SearchFilters.SORT_OLDEST.toLowerCase()),
                            cbFood.isChecked(), cbFashion.isChecked(), cbDining.isChecked(), cbTravel.isChecked(), cbTech.isChecked());
                }

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_filter, container, false);
        binding.setFilters(filters);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String sortOrder[] = getActivity().getResources().getStringArray(R.array.sort_order);

        //Set begin date
        Date date = null;
        try {
            date = SearchFilters.getQueryDateFormat().parse(filters.getBeginDateString());
            beginDate.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        etBeginDate.setText(displayDateFormat.format(date));

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                beginDate.set(Calendar.YEAR, year);
                beginDate.set(Calendar.MONTH, monthOfYear);
                beginDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etBeginDate.setText(displayDateFormat.format(beginDate.getTime()));
            }

        };

        etBeginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide Keyboard
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                new DatePickerDialog(getContext(), onDateSetListener, beginDate
                        .get(Calendar.YEAR), beginDate.get(Calendar.MONTH),
                        beginDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        boolean ignoreBeginDate = filters.isIgnoreBeginDate();
        cbIgnoreBeginDate.setChecked(ignoreBeginDate);

        if (ignoreBeginDate)
            etBeginDate.setEnabled(false);

        cbIgnoreBeginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etBeginDate.setEnabled(!cbIgnoreBeginDate.isChecked());

            }
        });

        Log.d(TAG, "Sort order is " + filters.getSortOrder());
        for (int i = 0; i < sortOrder.length; i++) {
            if (sortOrder[i].toLowerCase().equals(filters.getSortOrder())) {
                spSort.setSelection(i);
                break;
            }
        }

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
}
