package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Priority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoNote;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class EditActivity extends AppCompatActivity {

    private EditText editText;
    private Button deleteBtn;
    private Button saveBtn;
    private RadioGroup radioGroup;
    private AppCompatRadioButton lowRadio;
    private AppCompatRadioButton mediumRadio;
    private AppCompatRadioButton highRadio;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle(R.string.edit_a_note);

        dbHelper = new TodoDbHelper(this);
        database = dbHelper.getWritableDatabase();

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }
        radioGroup = findViewById(R.id.radio_group);
        lowRadio = findViewById(R.id.btn_low);
        mediumRadio = findViewById(R.id.btn_medium);
        highRadio = findViewById(R.id.btn_high);

        deleteBtn = findViewById(R.id.btn_delete);
        saveBtn = findViewById(R.id.btn_save);

        loadContent();

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.delete(TodoNote.TABLE_NAME, TodoNote._ID + "=?",
                        new String[]{getIntent().getStringExtra("ID")});
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(EditActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = updateNote2Database(content.toString().trim(),
                        getSelectedPriority());
                if (succeed) {
                    Toast.makeText(EditActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(EditActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        database = null;
        dbHelper.close();
        dbHelper = null;
    }

    private boolean updateNote2Database(String content, Priority priority) {
        if (database == null || TextUtils.isEmpty(content)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TodoNote.COLUMN_CONTENT, content);
        values.put(TodoNote.COLUMN_STATE, State.TODO.intValue);
        values.put(TodoNote.COLUMN_DATE, System.currentTimeMillis());
        values.put(TodoNote.COLUMN_PRIORITY, priority.intValue);
        long rowId = database.update(TodoNote.TABLE_NAME, values, TodoNote._ID + "=" + getIntent().getStringExtra("ID"), null);
        return rowId != -1;
    }

    private Priority getSelectedPriority() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.btn_high:
                return Priority.High;
            case R.id.btn_medium:
                return Priority.Medium;
            default:
                return Priority.Low;
        }
    }

    private void loadContent(){
        Cursor cursor = null;
        cursor = database.query(TodoNote.TABLE_NAME, null, TodoNote._ID + "=" + getIntent().getStringExtra("ID"), null,
               null, null, null);
        cursor.moveToFirst();
        String content = cursor.getString(cursor.getColumnIndex(TodoNote.COLUMN_CONTENT));
        int intPriority = cursor.getInt(cursor.getColumnIndex(TodoNote.COLUMN_PRIORITY));
        editText.setText(content);
        //Log.e("Btn", "Btn: " + intPriority);
        switch (intPriority) {
            case 2:
                highRadio.setChecked(true);
                return;
            case 1:
                mediumRadio.setChecked(true);
                return;
            case 0:
                lowRadio.setChecked(true);
                return;
        }
    }
}
