package com.flitzen.crm.adapter;

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

public class EnquiryAdapter extends RecyclerView.Adapter<EnquiryAdapter.ViewHolder>{

    Context context;
    ArrayList<EnquiryItem> enquiryItemArrayList;
    private ItemClickListener clickListener;

    public EnquiryAdapter(Context context,ArrayList<EnquiryItem> enquiryItemArrayList) {
        this.context=context;
        this.enquiryItemArrayList=enquiryItemArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.custom_enquiry_iten, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }


    @Override
    public int getItemCount() {
        return enquiryItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public CardView cardAddFollowUp;
        public TextView txtDate,txtTile,txtServiceName,txtEnquirySource,txtImportance;
        public ViewHolder(View itemView) {
            super(itemView);
            cardAddFollowUp =  itemView.findViewById(R.id.cardAddFollowUp);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtTile = itemView.findViewById(R.id.txtTile);
            txtServiceName = itemView.findViewById(R.id.txtServiceName);
            txtEnquirySource = itemView.findViewById(R.id.txtEnquirySource);
            txtImportance = itemView.findViewById(R.id.txtImportance);
            itemView.setOnClickListener(this);
            cardAddFollowUp.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }
}