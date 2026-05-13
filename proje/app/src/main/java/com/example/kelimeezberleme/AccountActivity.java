package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AccountActivity extends BottomNavActivity {
    private static final int REQUEST_PICK_PROFILE_IMAGE = 42;

    private DatabaseHelper db;
    private ImageView imgAccountProfile;
    private TextView tvCurrentUser;
    private TextView tvFullName;
    private String currentUser;
    private String selectedProfileImagePath = "";
    private ImageView dialogProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        db = new DatabaseHelper(this);
        imgAccountProfile = findViewById(R.id.imgAccountProfile);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        tvFullName = findViewById(R.id.tvFullName);
        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);

        MaterialButton btnSettings = findViewById(R.id.btnAccountSettings);
        MaterialButton btnResetPassword = findViewById(R.id.btnAccountResetPassword);
        MaterialButton btnLogout = findViewById(R.id.btnAccountLogout);

        loadProfile();
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));

        btnResetPassword.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, ForgotPasswordActivity.class)));

        btnLogout.setOnClickListener(v -> {
            AppSettings.clearCurrentUser(this);
            AppSettings.clearRememberedLogin(this);
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile() {
        currentUser = AppSettings.getCurrentUser(this);
        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String username = profile == null ? currentUser : profile.username;
        String fullName = profile == null ? "" : profile.fullName;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;

        tvCurrentUser.setText(username == null || username.trim().isEmpty() ? "Bilinmiyor" : username);
        if (fullName == null || fullName.trim().isEmpty()) {
            tvFullName.setText("Gerçek ad eklenmedi");
        } else {
            tvFullName.setText(fullName.trim());
        }
        applyProfileImage(imgAccountProfile, selectedProfileImagePath);
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        dialogProfileImage = dialogView.findViewById(R.id.imgEditProfile);
        MaterialButton btnChooseProfileImage = dialogView.findViewById(R.id.btnChooseProfileImage);
        MaterialButton btnShowPasswordReset = dialogView.findViewById(R.id.btnShowPasswordReset);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.tilEditUsername);
        TextInputLayout tilFullName = dialogView.findViewById(R.id.tilEditFullName);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.tilEditPassword);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etEditUsername);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etEditFullName);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etEditPassword);
        TextView tvPasswordResetHint = dialogView.findViewById(R.id.tvPasswordResetHint);

        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String profileUsername = profile == null ? currentUser : profile.username;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;
        etUsername.setText(profileUsername);
        etFullName.setText(profile == null ? "" : profile.fullName);
        applyProfileImage(dialogProfileImage, selectedProfileImagePath);

        btnChooseProfileImage.setOnClickListener(v -> openProfileImagePicker());
        btnShowPasswordReset.setOnClickListener(v -> {
            tilPassword.setVisibility(View.VISIBLE);
            tvPasswordResetHint.setVisibility(View.VISIBLE);
            btnShowPasswordReset.setVisibility(View.GONE);
            etPassword.requestFocus();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Profili Düzenle")
                .setView(dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilUsername.setError(null);
            tilFullName.setError(null);
            tilPassword.setError(null);

            String newUsername = getText(etUsername);
            String fullName = getText(etFullName);
            String newPassword = getText(etPassword);

            String usernameError = AccountSecurity.validateUsername(newUsername);
            if (usernameError != null) {
                tilUsername.setError(usernameError);
                return;
            }
            if (db.isUsernameTakenByOtherUser(newUsername, currentUser)) {
                tilUsername.setError("Bu kullanıcı adı zaten kullanılıyor.");
                return;
            }
            if (!newPassword.isEmpty()) {
                String passwordError = AccountSecurity.validatePassword(newUsername, newPassword);
                if (passwordError != null) {
                    tilPassword.setError(passwordError);
                    return;
                }
            }

            if (db.updateUserProfile(currentUser, newUsername, fullName, newPassword, selectedProfileImagePath)) {
                updateStoredUsername(currentUser, newUsername);
                currentUser = newUsername;
                loadProfile();
                Toast.makeText(this, "Profil güncellendi.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Profil güncellenemedi.", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.setOnDismissListener(d -> dialogProfileImage = null);
        dialog.show();
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_PROFILE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some image providers grant only temporary access even when opened through ACTION_OPEN_DOCUMENT.
            }
            selectedProfileImagePath = uri.toString();
            if (dialogProfileImage != null) {
                applyProfileImage(dialogProfileImage, selectedProfileImagePath);
            }
        }
    }

    private void updateStoredUsername(String oldUsername, String newUsername) {
        AppSettings.setCurrentUser(this, newUsername);
        if (AppSettings.isRememberedLoginEnabled(this)
                && oldUsername != null
                && oldUsername.equalsIgnoreCase(AppSettings.getRememberedUser(this))) {
            AppSettings.setRememberedLogin(this, newUsername, true);
        }
    }

    private void applyProfileImage(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setPadding(dp(12), dp(12), dp(12), dp(12));
            imageView.setImageResource(R.drawable.ic_person_circle_24);
            return;
        }
        imageView.setPadding(0, 0, 0, 0);
        imageView.setImageURI(Uri.parse(imagePath));
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
