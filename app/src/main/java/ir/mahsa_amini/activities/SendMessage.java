package ir.mahsa_amini.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import ir.mahsa_amini.R;
import ir.mahsa_amini.dialogs.RandomKeyDialog;
import ir.mahsa_amini.storage.Database;

public class SendMessage extends AppCompatActivity {

    private ImageView back;
    private TextInputLayout receiver_text;
    private TextInputEditText receiver;
    private TextInputEditText message;
    private AppCompatButton send_message;
    private  TextInputEditText password;
    private TextInputLayout password_text;
    private Database db;
    private static final int PICK_CONTACT_REQUEST_CODE = 0x1401;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        detectUI();
        defineUI();
    }

    private void detectUI() {
        back = findViewById(R.id.back);
        receiver_text = findViewById(R.id.receiver_text);
        receiver = findViewById(R.id.receiver);
        message = findViewById(R.id.message);
        send_message = findViewById(R.id.send_message);
        password = findViewById(R.id.password);
        password_text = findViewById(R.id.password_text);
        db = new Database(getApplicationContext());
    }

    private String fixPhoneNumber(String phone) {
        return "0" + phone.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
    }

    private void defineUI() {
        back.setOnClickListener(v -> {
            finish();
        });

        receiver_text.setStartIconOnClickListener(v -> {
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST_CODE);
        });

        password_text.setStartIconOnClickListener(v -> {
            new RandomKeyDialog(SendMessage.this).getPass(password);
        });

        send_message.setOnClickListener(v -> {
            if (receiver.getText().toString().length() > 0 && message.getText().toString().length() > 0) {
                String phoneText = receiver.getText().toString();
                String phone = phoneText.contains("-") ? fixPhoneNumber(phoneText) : phoneText;
                if (PhoneNumberUtils.isGlobalPhoneNumber(phone)) {
                    try {
                        String encrypted = AESCrypt.encrypt(password.getText().toString(), message.getText().toString());
                        SmsManager sms = SmsManager.getDefault();
                        if (encrypted.length() > 160) {
                            ArrayList<String> parts = sms.divideMessage(encrypted);
                            sms.sendMultipartTextMessage(phoneText, null, parts,
                                    null, null);
                        }
                        else {
                            sms.sendTextMessage(phone, null, encrypted, null,null);
                        }
                        db.newMessage(phone, message.getText().toString(), true, true);
                        Toast.makeText(this, "پیام ارسال شد.", Toast.LENGTH_SHORT).show();
                        Intent redirect = new Intent(SendMessage.this, MainActivity.class);
                        redirect.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(redirect);
                    } catch (GeneralSecurityException e){
                        Toast.makeText(this, "خطایی رخ داد.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "شماره تلفن گیرنده نادرست است.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "شماره تلفن گیرنده و پیام را وارد کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        String extraMessage = getIntent().getStringExtra("message");
        if (extraMessage != null && !extraMessage.equals("")) {
            message.setText(extraMessage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                receiver.setText(number);
            }
        }
    }
}