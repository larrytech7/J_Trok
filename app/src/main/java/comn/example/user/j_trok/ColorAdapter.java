package comn.example.user.j_trok;

/**
 * Created by USER on 23/03/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<SampleViewHolders> {

    private List<ItemObject> itemList;
    private Context context;

    public ColorAdapter(Context context, List<ItemObject> itemList)
    {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public SampleViewHolders onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_view, null);
        SampleViewHolders rcv = new SampleViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(SampleViewHolders holder, int position)
    {
        holder.ItemName.setText(itemList.get(position).getName());
        holder.authorName.setText(itemList.get(position).getAuthor());
    }

    @Override
    public int getItemCount()
    {
        return this.itemList.size();
    }
}
