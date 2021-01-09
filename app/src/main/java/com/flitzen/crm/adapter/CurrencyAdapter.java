package com.flitzen.crm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.flitzen.crm.R;
import com.flitzen.crm.activity.AddCompanyActivity;
import com.flitzen.crm.items.CurrencyItem;

import java.util.ArrayList;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder>{

    private Context context;
    private  ArrayList<CurrencyItem> currencyArray;
    private ItemClickListener clickListener;

    public CurrencyAdapter(Context context, ArrayList<CurrencyItem> currencyArray) {
        this.context=context;
        this.currencyArray=currencyArray;
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

        holder.txtCurrencyName.setText(currencyArray.get(position).getCurrencyName());

    }


    @Override
    public int getItemCount() {
        System.out.println("========size  "+currencyArray.size());
        return currencyArray.size();
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

    public void updateList(ArrayList<CurrencyItem> currencyArray){
        this.currencyArray = currencyArray;
        notifyDataSetChanged();
    }
}