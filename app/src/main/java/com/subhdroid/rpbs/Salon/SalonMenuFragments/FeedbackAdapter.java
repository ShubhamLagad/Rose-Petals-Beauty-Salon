package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subhdroid.rpbs.R;

import java.util.ArrayList;


public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    Context context;
    ArrayList<FeedbackModel> feedbackList;

    FeedbackAdapter(Context context, ArrayList<FeedbackModel> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.feedback_row, parent, false);
        FeedbackAdapter.ViewHolder viewHolder = new FeedbackAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.ViewHolder holder, int position) {

        holder.feedbackComment.setText(feedbackList.get(position).getComment());
        holder.feedbackCustName.setText("--By " + feedbackList.get(position).getFeedbackName());
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView feedbackComment, feedbackCustName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            feedbackComment = itemView.findViewById(R.id.feedbackComment);
            feedbackCustName = itemView.findViewById(R.id.feedbackCustName);
        }
    }
}
