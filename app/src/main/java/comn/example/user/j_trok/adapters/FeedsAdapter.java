package comn.example.user.j_trok.adapters;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.ui.PostDetailActivity;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.MyViewHolder> {
    private String[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        public CardView mCardView;
        @BindView(R.id.authorName)
        public TextView mTextView;

        public MyViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedsAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTextView.setText(mDataset[position]);

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}