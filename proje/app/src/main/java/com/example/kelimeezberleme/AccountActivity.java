package com.example.kelimeezberleme;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.InputStream;

public class AccountActivity extends BottomNavActivity {
    private static final int REQUEST_PICK_PROFILE_IMAGE = 42;
    private static final long MAX_PROFILE_IMAGE_BYTES = 2L * 1024L * 1024L;
    private static final int MAX_PROFILE_IMAGE_SIDE = 2048;

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
        MaterialButton btnLogout = findViewById(R.id.btnAccountLogout);

        loadProfile();
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));

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
        MaterialButton btnShowPasswordReset = dialogView.findViewById(R.id.btnShowPasswordReset);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.tilEditUsername);
        TextInputLayout tilFullName = dialogView.findViewById(R.id.tilEditFullName);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etEditUsername);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etEditFullName);

        DatabaseHelper.UserProfile profile = db.getUserProfile(currentUser);
        String profileUsername = profile == null ? currentUser : profile.username;
        selectedProfileImagePath = profile == null ? "" : profile.profileImagePath;
        etUsername.setText(profileUsername);
        etFullName.setText(profile == null ? "" : profile.fullName);
        applyProfileImage(dialogProfileImage, selectedProfileImagePath);

        dialogProfileImage.setOnClickListener(v -> showProfileImageOptions());
        btnShowPasswordReset.setOnClickListener(v -> showResetPasswordDialog(getText(etUsername)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Profili Düzenle")
                .setView(dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilUsername.setError(null);
            tilFullName.setError(null);

            String newUsername = getText(etUsername);
            String fullName = getText(etFullName);

            String usernameError = AccountSecurity.validateUsername(newUsername);
            if (usernameError != null) {
                tilUsername.setError(usernameError);
                return;
            }
            if (db.isUsernameTakenByOtherUser(newUsername, currentUser)) {
                tilUsername.setError("Bu kullanıcı adı zaten kullanılıyor.");
                return;
            }

            if (db.updateUserProfile(currentUser, newUsername, fullName, "", selectedProfileImagePath)) {
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
        applyRoundedDialogCorners(dialog);
    }

    private void showResetPasswordDialog(String usernameForValidation) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        TextInputLayout tilCurrentPassword = dialogView.findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNewPassword = dialogView.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword = dialogView.findViewById(R.id.tilConfirmPassword);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Şifremi Sıfırla")
                .setView(dialogView)
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String currentPassword = getText(etCurrentPassword);
            String newPassword = getText(etNewPassword);
            String confirmPassword = getText(etConfirmPassword);
            String username = usernameForValidation == null || usernameForValidation.trim().isEmpty()
                    ? currentUser
                    : usernameForValidation.trim();

            if (!db.checkUser(currentUser, currentPassword)) {
                tilCurrentPassword.setError("Eski şifre hatalı.");
                return;
            }
            String passwordError = AccountSecurity.validatePassword(username, newPassword);
            if (passwordError != null) {
                tilNewPassword.setError(passwordError);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Yeni şifreler eşleşmiyor.");
                return;
            }

            if (db.updatePassword(currentUser, newPassword)) {
                Toast.makeText(this, "Şifre güncellendi.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Şifre güncellenemedi.", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    private void applyRoundedDialogCorners(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.getDecorView().setBackgroundResource(R.drawable.dialog_rounded_bg);
            window.getDecorView().setPadding(dp(4), dp(4), dp(4), dp(4));
        }
    }

    private void openProfileImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_PROFILE_IMAGE);
    }

    private void showProfileImageOptions() {
        String[] options = selectedProfileImagePath == null || selectedProfileImagePath.trim().isEmpty()
                ? new String[]{"Galeriden Seç"}
                : new String[]{"Galeriden Seç", "Mevcut Resmi Kaldır"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(options, (selectionDialog, which) -> {
                    if (which == 0) {
                        openProfileImagePicker();
                        return;
                    }
                    selectedProfileImagePath = "";
                    if (dialogProfileImage != null) {
                        applyProfileImage(dialogProfileImage, selectedProfileImagePath);
                    }
                })
                .create();
        dialog.show();
        applyRoundedDialogCorners(dialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (!isProfileImageAllowed(uri)) {
                return;
            }
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

    private boolean isProfileImageAllowed(Uri uri) {
        if (getUriSize(uri) > MAX_PROFILE_IMAGE_BYTES) {
            showProfileImageWarning("Seçilen resim dosyası çok büyük.");
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (IOException ignored) {
            showProfileImageWarning("Resim okunamadı.");
            return false;
        }

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            showProfileImageWarning("Geçerli bir resim seç.");
            return false;
        }
        if (options.outWidth > MAX_PROFILE_IMAGE_SIDE || options.outHeight > MAX_PROFILE_IMAGE_SIDE) {
            showProfileImageWarning("Seçilen resmin çözünürlüğü çok yüksek.");
            return false;
        }
        return true;
    }

    private long getUriSize(Uri uri) {
        try (AssetFileDescriptor descriptor = getContentResolver().openAssetFileDescriptor(uri, "r")) {
            if (descriptor != null) {
                return descriptor.getLength();
            }
        } catch (IOException ignored) {
            return -1L;
        }
        return -1L;
    }

    private void showProfileImageWarning(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
