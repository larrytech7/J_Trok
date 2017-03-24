package comn.example.user.j_trok;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RegistrationForm extends AppCompatActivity  implements View.OnClickListener{

    private EditText password, confirmPassword;
    private TextView passwordHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_form);

        /*dropdown menu for countries*/
        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{"Cameroon","South Africa", "France"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        /*initialising views for the textWatcher*/
        password = (EditText) findViewById(R.id.password);
        confirmPassword =(EditText) findViewById(R.id.confirmPassword);
        passwordHint = (TextView) findViewById(R.id.passwordHint);

        /* Set Text Watcher listener */
        password.addTextChangedListener(passwordWatcher);
        confirmPassword.addTextChangedListener(passwordWatcher);

    }

    public void signup(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    /*textWatcher - needs its 3 methods to be implemented in order to work*/
    private final TextWatcher passwordWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            passwordHint.setVisibility(View.VISIBLE);
        }

        public void afterTextChanged(Editable s) {
            String Password = password.getText().toString();
            String ConfirmPassword = confirmPassword.getText().toString();

            /*check if both passwords are equal*/

            if (!Password.equals(ConfirmPassword)){
                passwordHint.setText("Passwords don't match!!!");
            }
            else if(Password.isEmpty()||ConfirmPassword.isEmpty()){
                passwordHint.setVisibility(View.GONE);
            }
            else{
                passwordHint.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onClick(View view) {

    }
}