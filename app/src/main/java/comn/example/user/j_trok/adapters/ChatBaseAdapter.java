package comn.example.user.j_trok.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import comn.example.user.j_trok.R;

/**
 * Created by Larry Akah on 5/22/17.
 */

public class ChatBaseAdapter extends RecyclerView.Adapter<ChatBaseAdapter.ViewHolder> {

    private Context context;
    private List<String> chats;

    public ChatBaseAdapter(Context _ctx, List<String> items) {
        this.context = _ctx;
        chats = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_incoming, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.userNameTextView.setText(chats.get(position));
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.chatUsernameTextView)
        TextView userNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
