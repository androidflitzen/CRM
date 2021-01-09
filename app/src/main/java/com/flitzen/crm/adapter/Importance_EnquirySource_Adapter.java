package com.flitzen.crm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.items.CurrencyItem;
import com.flitzen.crm.items.Importance_EnquirySourceItem;

import java.util.ArrayList;

public class Importance_EnquirySource_Adapter extends RecyclerView.Adapter<Importance_EnquirySource_Adapter.ViewHolder>{

    private Context context;
    private ArrayList<Importance_EnquirySourceItem> sourceItemArrayList;
    private ItemClickListener clickListener;

    public Importance_EnquirySource_Adapter(Context context, ArrayList<Importance_EnquirySourceItem> sourceItemArrayList) {
        this.context=context;
        this.sourceItemArrayList=sourceItemArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.custom_currency, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtCurrencyName.setText(sourceItemArrayList.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return sourceItemArrayList.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView txtCurrencyName;
        public ViewHolder(View itemView) {
            super(itemView);
            this.txtCurrencyName =  itemView.findViewById(R.id.txtCurrencyName);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    public void updateList(ArrayList<Importance_EnquirySourceItem> sourceItemArrayList){
        this.sourceItemArrayList = sourceItemArrayList;
        notifyDataSetChanged();
    }
}