package com.flitzen.crm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flitzen.crm.R;

import com.flitzen.crm.adapter.QuotationAdapter;


public class QuotationFragment extends Fragment {

    private View viewQuotation;
    RecyclerView recyclerQuotation;
    QuotationAdapter quotationAdapter;
    RelativeLayout noDataLayout;
    SwipeRefreshLayout swipeRefresh;
    CardView cardAddEnquiry;
    TextView txtAddBtnName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewQuotation=inflater.inflate(R.layout.fragment_quotations, container, false);

        recyclerQuotation =  viewQuotation.findViewById(R.id.recyclerQuotation);
        noDataLayout =  viewQuotation.findViewById(R.id.noDataLayout);
        swipeRefresh =  viewQuotation.findViewById(R.id.swipeRefresh);

        quotationAdapter  = new QuotationAdapter(getActivity());
        recyclerQuotation.setHasFixedSize(true);
        recyclerQuotation.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerQuotation.setAdapter(quotationAdapter);

        return viewQuotation;
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView txtTile=((AppCompatActivity) getActivity()).findViewById(R.id.txtTile);
        txtTile.setText(getContext().getResources().getString(R.string. quotations));
        cardAddEnquiry=((AppCompatActivity) getActivity()).findViewById(R.id.cardAddEnquiry);
        txtAddBtnName=((AppCompatActivity) getActivity()).findViewById(R.id.txtAddBtnName);
        txtAddBtnName.setText(getActivity().getResources().getString(R.string.new_quotation));
        cardAddEnquiry.setVisibility(View.VISIBLE);
    }
}
