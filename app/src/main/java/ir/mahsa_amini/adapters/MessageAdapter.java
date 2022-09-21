package ir.mahsa_amini.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.List;

import ir.mahsa_amini.R;
import ir.mahsa_amini.activities.MainActivity;
import ir.mahsa_amini.models.Message;
import ir.mahsa_amini.storage.Database;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private Database db;
    private Activity activity;

    public MessageAdapter(Context context, Activity activity, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.activity = activity;
        db = new Database(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.isUnlocked()) {
            holder.message.setText(message.getMessage());
        }
        else {
            StringBuffer stringBuffer = new StringBuffer();
            char chars[] = message.getMessage().toCharArray();
            int len = chars.length > 20 ? 20 : chars.length;
            for(int i = 0; i < len; i++) {
                String hexString = Integer.toHexString(chars[i]) + " ";
                stringBuffer.append(hexString);
            }
            holder.message.setText(stringBuffer);
        }
        holder.phone.setText((message.isSent() ? "به": "از") + " " + message.getPhone());
        holder.message_container.setOnClickListener(v -> {
            if (message.isUnlocked()) {
                new AlertDialog.Builder(activity)
                        .setMessage(message.getMessage())
                        .show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("رمزگشایی");
                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setHint("کلید پیام را وارد کنید.");
                builder.setView(input);

                builder.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        try {
                            String r_message = AESCrypt.decrypt(password, message.getMessage());
                            message.setMessage(r_message);
                            db.messageUnlock(String.valueOf(message.getId()), message.getMessage(),true);
                            holder.message.setText(message.getMessage());
                            new AlertDialog.Builder(activity)
                                    .setMessage(message.getMessage())
                                    .show();
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "کلید پیام نادرست است.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView phone;
        private TextView message;
        private LinearLayout message_container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            phone = itemView.findViewById(R.id.phone);
            message = itemView.findViewById(R.id.hex_message);
            message_container = itemView.findViewById(R.id.message_container);
        }
    }
}
