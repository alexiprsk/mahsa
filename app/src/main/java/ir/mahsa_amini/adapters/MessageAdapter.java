package ir.mahsa_amini.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.scottyab.aescrypt.AESCrypt;

import org.greenrobot.eventbus.EventBus;

import java.security.GeneralSecurityException;
import java.util.List;

import ir.mahsa_amini.R;
import ir.mahsa_amini.activities.MainActivity;
import ir.mahsa_amini.activities.SendMessage;
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

    private String hexMessage(String message) {
        StringBuffer stringBuffer = new StringBuffer();
        char chars[] = message.toCharArray();
        int len = chars.length > 20 ? 20 : chars.length;
        for(int i = 0; i < len; i++) {
            String hexString = Integer.toHexString(chars[i]) + " ";
            stringBuffer.append(hexString);
        }
        return stringBuffer.toString();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (message.isUnlocked()) {
            holder.message.setText(message.getMessage());
        }
        else {
            holder.message.setText(hexMessage(message.getMessage()));
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
                            message.setUnlocked(true);
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

        holder.menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.menu);
            popup.getMenuInflater()
                    .inflate(R.menu.message_menu, popup.getMenu());

            if (message.isUnlocked()) {
                popup.getMenu().findItem(R.id.decrypt_message).setEnabled(false);
            }
            else {
                popup.getMenu().findItem(R.id.encrypt_message).setEnabled(false);
            }

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.delete_message:
                            new AlertDialog.Builder(activity)
                                    .setMessage("آیا از حذف اطمینان دارید؟")
                                            .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    db.deleteMessage(String.valueOf(message.getId()));
                                                    messages.remove(message);
                                                    EventBus.getDefault().post(new Message(-1, "", "", false, false));
                                                }
                                            })
                                            .setNegativeButton("خیر", null)
                                            .show();
                            break;
                        case R.id.share_message:
                            if (message.isUnlocked()) {
                                Intent sendAgain = new Intent(activity, SendMessage.class);
                                sendAgain.putExtra("message", message.getMessage());
                                sendAgain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(sendAgain);
                            }
                            else {
                                Toast.makeText(context, "نمی توان پیام رمز شده را بازارسال کرد.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.encrypt_message:
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("رمزنگاری");
                            final EditText input = new EditText(activity);
                            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            input.setHint("کلید پیام را وارد کنید.");
                            builder.setView(input);

                            builder.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = input.getText().toString();
                                    try {
                                        String c_message = AESCrypt.encrypt(password, message.getMessage());
                                        message.setMessage(c_message);
                                        db.messageUnlock(String.valueOf(message.getId()), message.getMessage(),false);
                                        holder.message.setText(hexMessage(message.getMessage()));
                                        message.setUnlocked(false);
                                        EventBus.getDefault().post(new Message(-1, "", "", false, false));
                                    } catch (GeneralSecurityException e) {
                                        e.printStackTrace();
                                        Toast.makeText(context, "خطایی رخ داد.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            builder.show();
                            break;
                        case R.id.decrypt_message:
                            AlertDialog.Builder builderx = new AlertDialog.Builder(activity);
                            builderx.setTitle("رمزگشایی");
                            final EditText inputx = new EditText(activity);
                            inputx.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            inputx.setHint("کلید پیام را وارد کنید.");
                            builderx.setView(inputx);

                            builderx.setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = inputx.getText().toString();
                                    try {
                                        String r_message = AESCrypt.decrypt(password, message.getMessage());
                                        message.setMessage(r_message);
                                        db.messageUnlock(String.valueOf(message.getId()), message.getMessage(),true);
                                        holder.message.setText(message.getMessage());
                                        message.setUnlocked(true);
                                        EventBus.getDefault().post(new Message(-1, "", "", false, false));
                                    } catch (GeneralSecurityException e) {
                                        e.printStackTrace();
                                        Toast.makeText(context, "رمز پیام نادرست است.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            builderx.show();
                            break;
                    }
                    return true;
                }
            });

            popup.show();
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView phone;
        private TextView message;
        private ConstraintLayout message_container;
        private ImageView menu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            phone = itemView.findViewById(R.id.phone);
            message = itemView.findViewById(R.id.hex_message);
            message_container = itemView.findViewById(R.id.message_container);
            menu = itemView.findViewById(R.id.message_menu);
        }
    }
}
