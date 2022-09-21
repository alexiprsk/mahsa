package ir.mahsa_amini.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ir.mahsa_amini.R;
import ir.mahsa_amini.adapters.MessageAdapter;
import ir.mahsa_amini.models.Message;
import ir.mahsa_amini.security.Permitor;
import ir.mahsa_amini.storage.Database;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton send_message;
    private RecyclerView messagesRecycler;
    private List<Message> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private Database db;
    private LinearLayout noMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();
        detectUI();
        defineUI();
    }

    private void permissionCheck() {
        if (!Permitor.hasPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
        || !Permitor.hasPermission(getApplicationContext(), Manifest.permission.READ_SMS)
        || !Permitor.hasPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS)) {
            Permitor.requestPermission(MainActivity.this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
            }, 0x1401);
        }
    }

    private void detectUI() {
        send_message = findViewById(R.id.send_message);
        messagesRecycler = findViewById(R.id.messages);
        noMessage = findViewById(R.id.no_message);
        messageAdapter = new MessageAdapter(getApplicationContext(), MainActivity.this, messages);
        db = new Database(getApplicationContext());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message message) {
        if (message.getId() == -1) {
            messageAdapter.notifyDataSetChanged();
            if (messages.size() == 0){
                noMessage.setVisibility(View.VISIBLE);
            }
            else {
                noMessage.setVisibility(View.INVISIBLE);
            }
        }
        else {
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    private void defineUI() {
        messagesRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messagesRecycler.setAdapter(messageAdapter);
        getMessages();

        send_message.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SendMessage.class));
        });
    }

    private void getMessages() {
        ArrayList<Message> _messages = db.readMessages();
        if (_messages.size() > 0) {
            noMessage.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < _messages.size(); i++) {
            messages.add(_messages.get(i));
            messageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}