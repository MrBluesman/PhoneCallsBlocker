package com.example.ukasz.phonecallsblocker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

import java.util.List;
import java.util.Objects;

public class AddPhoneBlock extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private Toolbar mActionBar;
    private EditText nrBlocked;
    private Button nrImport;
    private Spinner category;
    private EditText description;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_block);

        //set toolbar
        mActionBar = findViewById(R.id.add_phone_block_toolbar);
        setSupportActionBar(mActionBar);
        mActionBar.setTitle(R.string.add_phone_block_title);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //nr info ---------------------------------------------------------------------------
        nrBlocked = findViewById(R.id.add_phone_block_nr_blocked_editText);
        description = findViewById(R.id.add_phone_block_descriptionEditText);

        //spinner
        category = findViewById(R.id.add_phone_block_spinner);
        loadCategoriesToSpinner(category);
        category.setOnItemSelectedListener(this);
        //nr info ---------------------------------------------------------------------------

        addButton = findViewById(R.id.add_phone_block_addButton);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(nrBlocked.getText().toString().length() == 0)
                {
                    nrBlocked.setError("Podaj numer telefonu");
                    Toast.makeText(v.getContext(), "Podaj numer telefonu", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //TODO: get phone number ((TelephonyManager)v.getContext().getSystemService(Context.TELEPHONY_SERVICE) is not working anymore)
//                    Toast.makeText(v.getContext(), String.valueOf(category.getSelectedItemPosition()), Toast.LENGTH_SHORT).show();
                    DatabaseHandler db = new DatabaseHandler(v.getContext());
//                    //Insertings block
//                    Log.d("Insert: ", "Inserting..");
//                    TelephonyManager tMgr = (TelephonyManager)v.getContext().getSystemService(Context.TELEPHONY_SERVICE);
//                    String mPhoneNumber = tMgr.getLine1Number();
                    db.addBlocking(new Block("721315333", nrBlocked.getText().toString(),
                            category.getSelectedItemPosition(), description.getText().toString(), true));
                    Toast.makeText(v.getContext(), "Numer dodany", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    /**
     * Loads categories from database to spinner.
     * @param spinner spinner which will have a set adapter with loaded categories.
     */
    public void loadCategoriesToSpinner(Spinner spinner)
    {
        DatabaseHandler db = new DatabaseHandler(this);
        List<String> categories = db.getAllCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
        {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
//        CharSequence elem = (CharSequence) parent.getItemAtPosition(position);
//        Toast t = Toast.makeText(this, elem, Toast.LENGTH_SHORT);
//        t.show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    private class SHORT {
    }
}
