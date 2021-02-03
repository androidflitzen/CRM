package com.flitzen.crm.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.items.EnquiryItem;

import java.util.ArrayList;

public class QuotationAdapter extends RecyclerView.Adapter<QuotationAdapter.ViewHolder>{

    Context context;

    public QuotationAdapter(Context context) {
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.custom_quotation_iten, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CardView cardAddFollowUp;
        public TextView txtDate,txtTime,txtServiceName,txtEnquirySource,txtImportance,txtCompanyName;
        public ViewHolder(View itemView) {
            super(itemView);
            cardAddFollowUp =  itemView.findViewById(R.id.cardAddFollowUp);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtServiceName = itemView.findViewById(R.id.txtServiceName);
            txtEnquirySource = itemView.findViewById(R.id.txtEnquirySource);
            txtImportance = itemView.findViewById(R.id.txtImportance);
            txtCompanyName = itemView.findViewById(R.id.txtCompanyName);
        }
    }
}