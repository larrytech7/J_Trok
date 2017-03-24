package comn.example.user.j_trok;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private StaggeredGridLayoutManager _sGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        _sGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(_sGridLayoutManager);

        List<ItemObject> sList = getListItemData();


        ColorAdapter rcAdapter = new ColorAdapter(
                MainActivity.this, sList);
        recyclerView.setAdapter(rcAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        //hqndle click event here for now are going to toqst dqtq
                        String text = "Item has position";
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(getBaseContext(), text, duration);
                        toast.show();
                        Intent intent = new Intent(getBaseContext(),ItemDetail.class);
                        startActivity(intent);
                    }
                })
        );


    }
    public void start_new_deal(View view){
        Intent intent = new Intent(getBaseContext(),NewDeal.class);
        startActivity(intent);
    }


    private List<ItemObject> getListItemData()
    {
        List<ItemObject> listViewItems = new ArrayList<ItemObject>();
        listViewItems.add(new ItemObject("1984", "George Orwell"));
        listViewItems.add(new ItemObject("Pride and Prejudice", "Jane Austen"));
        listViewItems.add(new ItemObject("One Hundred Years of Solitude", "Gabriel Garcia Marquez"));
        listViewItems.add(new ItemObject("The Book Thief", "Markus Zusak"));
        listViewItems.add(new ItemObject("The Hunger Games", "Suzanne Collins"));
        listViewItems.add(new ItemObject("The Hitchhiker's Guide to the Galaxy", "Douglas Adams"));
        listViewItems.add(new ItemObject("The Theory Of Everything", "Dr Stephen Hawking"));


        return listViewItems;
    }
}
