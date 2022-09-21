package ir.mahsa_amini.dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import ir.mahsa_amini.R;
import ir.mahsa_amini.security.PasswordGenerator;
import ir.mahsa_amini.storage.SafeCache;

public class RandomKeyDialog {
    private Dialog dialog;
    private Context context;

    public RandomKeyDialog(Context context) {
        this.context = context;
    }

    private String generateRandomPassword() {
        PasswordGenerator pg = new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .build();
        return pg.generate(32);
    }

    public void getPass(TextInputEditText password) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.random_key_dialog);
        TextView random_password = dialog.findViewById(R.id.random_password);
        ImageView copy_pass = dialog.findViewById(R.id.copy_pass);
        random_password.setText(generateRandomPassword());
        copy_pass.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", random_password.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "کلمه عبور کپی شد.", Toast.LENGTH_SHORT).show();
        });

        random_password.setOnClickListener(v -> {
            password.setText(random_password.getText().toString());
            dialog.dismiss();
        });
        dialog.show();
    }
}
