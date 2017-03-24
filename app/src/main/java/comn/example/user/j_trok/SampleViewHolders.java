package comn.example.user.j_trok;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static comn.example.user.j_trok.R.styleable.RecyclerView;

/**
 * Created by USER on 07/12/2016.
 */
public class SampleViewHolders extends RecyclerView.ViewHolder
{
    public TextView ItemName;
    public TextView authorName;

    public SampleViewHolders(View itemView)
    {
        super(itemView);
        ItemName = (TextView) itemView.findViewById(R.id.itemName);
        authorName = (TextView) itemView.findViewById(R.id.authorName);
    }


}