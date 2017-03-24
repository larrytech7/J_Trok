package comn.example.user.j_trok;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by USER on 07/12/2016.
 */

public class ItemObject {

    private String _name;
    private String _author;

    public ItemObject(String name, String auth)
    {
        this._name = name;
        this._author = auth;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        this._name = name;
    }

    public String getAuthor()
    {
        return _author;
    }

    public void setAuthor(String auth)
    {
        this._author = auth;
    }
}