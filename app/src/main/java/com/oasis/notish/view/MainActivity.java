package com.oasis.notish.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oasis.notish.DbModel.Note;
import com.oasis.notish.R;
import com.oasis.notish.database.DbHelper;
import com.oasis.notish.utils.MyDividerItemDecoration;
import com.oasis.notish.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesView;

    private DbHelper  db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);

        //Initialize database

        db = new DbHelper(this);

        //Get all notes from the database and add them into our notes array list

        notesList.addAll(db.getAllNotes());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });


        //Set up adapter with recycler view

        mAdapter = new NotesAdapter(this,notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this,LinearLayoutManager.VERTICAL,16));
        recyclerView.setAdapter(mAdapter);
        
        toggleEmptyNotes();

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }



    //End on create

    /**
     * Inserting new note in db
     * and refreshing the list
     */

    private void createNote(String note) {

        // inserting note in db and getting
        // newly inserted note id

        long id = db.insertNote(note);

        //get the newly inserted note from db

        Note n = db.getNote(id);


        if (n != null) {
            // Adding new note to array list at 0 position

            notesList.add(0,n);

            // Refreshing the list

            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
        }

    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */

    private void updateNote (String note, int position){
        Note n = notesList.get(position);

        // updating note text

        n.setNote(note);

        //Updating note in database

        db.updateNote(n);

        // refreshing the list
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */

    private void deleteNote (int position) {
        // Deleting note from db

        db.deleteNote(notesList.get(position));

        // Removing the note from the list

        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }


    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {

        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    showNoteDialog(true,notesList.get(position),position);
                } else {
                   deleteNote(position);
                }
            }
        });

        builder.show();
    }


    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title)
                : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogbox, int id) {

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

            final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
            alertDialog.show();


            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Show text message when no text is entered

                    if (TextUtils.isEmpty(inputNote.getText().toString())) {
                        Toast.makeText(MainActivity.this,"Enter Note!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        alertDialog.dismiss();
                    }

                    // check if user is updating the note

                    if (shouldUpdate && note != null) {
                        // update note by its id
                        updateNote(inputNote.getText().toString(),position);
                    } else {
                        //Create new note

                        createNote(inputNote.getText().toString());
                    }
                }
            });

    }


    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (db.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
