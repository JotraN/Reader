package io.github.jotran.reader.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Submission;

import java.util.List;

import io.github.jotran.reader.R;

public class SubmissionsRecyclerAdapter extends
        RecyclerView.Adapter<SubmissionsRecyclerAdapter.ViewHolder> {
    private List<Submission> mSubmissions;
    private SubmissionsAdapterListener mListener;

    public interface SubmissionsAdapterListener {
        void onSubmissionClicked(Submission submission);
    }

    public SubmissionsRecyclerAdapter(List<Submission> submissions) {
        mSubmissions = submissions;
    }

    /**
     * {@code ViewHolder} to hold the views used.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitleTv, mDateTv, mScoreTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitleTv = (TextView) itemView.findViewById(R.id.text_title);
            mDateTv = (TextView) itemView.findViewById(R.id.text_date);
            mScoreTv = (TextView) itemView.findViewById(R.id.text_score);
            itemView.setOnClickListener(v -> {
                if (mListener != null)
                    mListener.onSubmissionClicked(mSubmissions
                            .get(getAdapterPosition()));
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Submission submission = mSubmissions.get(position);
        holder.mTitleTv.setText(submission.getTitle());
        holder.mDateTv.setText(submission.getCreated().toString());
        holder.mScoreTv.setText(submission.getScore().toString());
    }

    @Override
    public int getItemCount() {
        return mSubmissions.size();
    }

    /**
     * Sets the listener to use.
     *
     * @param listener the listener to use
     */
    public void setSubmissionsListener(SubmissionsAdapterListener listener) {
        mListener = listener;
    }

    /**
     * Add the given {@code Submission} to the current list of {@code Submission}s.
     *
     * @param submission the submission to add
     */
    public void addSubmissions(Submission submission) {
        mSubmissions.add(submission);
        notifyDataSetChanged();
    }

    /**
     * Clears the current list of {@code Submission}s.
     */
    public void clear(){
        mSubmissions.clear();
        notifyDataSetChanged();
    }
}
