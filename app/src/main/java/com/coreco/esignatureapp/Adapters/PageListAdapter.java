package com.coreco.esignatureapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.coreco.esignatureapp.Model.Coordinates;
import com.coreco.esignatureapp.Model.PageDetails;
import com.coreco.esignatureapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageListAdapter  extends RecyclerView.Adapter<PageListAdapter.ViewHolder>{
    private ArrayList<HashMap<String,PageDetails>> listdata=new ArrayList<>();

    // RecyclerView recyclerView;
    public PageListAdapter(ArrayList<HashMap<String,PageDetails>> listdata) {
        this.listdata = listdata;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        HashMap<String, PageDetails> hashDetails=listdata.get(position);

        for (Map.Entry<String, PageDetails> entry : hashDetails.entrySet()) {
            String k = entry.getKey();
            PageDetails pageDetails = entry.getValue();

                holder.editPage.setText(pageDetails.getPage());
                for (int i = 0; i < pageDetails.getCoordinates().size(); i++) {
                    holder.editX.setText(pageDetails.getCoordinates().get(i).getX());
                    holder.editY.setText(pageDetails.getCoordinates().get(i).getY());
                    holder.editW.setText(pageDetails.getCoordinates().get(i).getW());
                    holder.editH.setText(pageDetails.getCoordinates().get(i).getH());
                }

            System.out.println("Key: " + k + ", Value: " + pageDetails);
        }


    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView editPage,editX,editY,editW,editH;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.editPage = itemView.findViewById(R.id.editPage);
            this.editX =itemView.findViewById(R.id.editX);
            this.editY =itemView.findViewById(R.id.editY);
            this.editW =itemView.findViewById(R.id.editW);
            this.editH =itemView.findViewById(R.id.editH);

            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativeLayout);
        }
    }
}
