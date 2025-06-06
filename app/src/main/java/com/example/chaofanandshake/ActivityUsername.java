package com.example.chaofanandshake;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityUsername extends AppCompatActivity {

    private ImageView backBtn;
    private TextInputEditText etNewUsername;
    private TextInputLayout layoutUsername;
    private DatabaseHelper dbHelper;
    private Button btnSaveName;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        dbHelper = new DatabaseHelper(this);

        backBtn = findViewById(R.id.backBtn);
        etNewUsername = findViewById(R.id.etNewUsername);
        layoutUsername = findViewById(R.id.layoutUsername);
        btnSaveName = findViewById(R.id.btnSaveName);

        etNewUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 15 ) {
                    etNewUsername.setText(charSequence.subSequence(0, 15));
                    etNewUsername.setSelection(15);
                    Toast.makeText(ActivityUsername .this, "Username can be up to 15 characters only.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // REMOVE ICON ERROR
        layoutUsername.setErrorIconDrawable(null);

        // Get current value and username
        String currentVal = getIntent().getStringExtra("currentValue");
        currentUsername = getIntent().getStringExtra("currentUsername");

        if (currentVal != null) {
            etNewUsername.setText(currentVal);
        }

        backBtn.setOnClickListener(v -> finish());

        // Live validation habang nagta-type
        etNewUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateAndShowErrors(s.toString());
            }
        });



        btnSaveName.setOnClickListener(v -> {
            String input = etNewUsername.getText().toString().trim();
            if (validateAndShowErrors(input)) {
                showSaveConfirmationDialog(input);
            }
        });
    }

    private void showSaveConfirmationDialog(String newUsername) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.username_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialogTitle.setText("Save Username");
        dialogMessage.setText("Are you sure you want to change your username?");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        SharedPreferences userPrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor edit = userPrefs.edit();
        btnSave.setOnClickListener(v -> {
            updateUsernameInDatabase(newUsername);
            String getUsername = etNewUsername.getText().toString().trim();
            edit.putString("username", getUsername);
            edit.apply();
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean validateAndShowErrors(String username) {
        String errors = validateUsername(username);
        if (!errors.isEmpty()) {
            layoutUsername.setError(errors);
            return false;
        } else {
            layoutUsername.setError(null);
            return true;
        }
    }

    private String validateUsername(String username) {
        StringBuilder errors = new StringBuilder();

        if (username.contains(" ")) {
            errors.append("• Should not contain spaces\n");
        }

        // Kung walang format error, saka lang iche-check kung taken na
        if (errors.length() == 0 && dbHelper.checkUsernameExists(username)) {
            errors.append("• Username already exists\n");
        }

        return errors.toString();
    }

    private void updateUsernameInDatabase(String updatedUsername) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", updatedUsername); // dapat lowercase kung 'username' sa DB

        int result = database.update("users", values, "username = ?", new String[]{currentUsername});

        if (result > 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedUsername", updatedUsername);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
