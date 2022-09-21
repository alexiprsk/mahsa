package ir.mahsa_amini.sms;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ir.mahsa_amini.R;
import ir.mahsa_amini.activities.MainActivity;
import ir.mahsa_amini.models.Message;
import ir.mahsa_amini.storage.Database;

public class SmsReceiver extends BroadcastReceiver {

    public static boolean isBase64Encoded(String s) {
        String pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        return m.find();
    }

    private void sendNotification(Context context, String message, String title, int id) {
        String CHANNEL_ID = "";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = "mahsa_sms_channel";
            CharSequence name = "mahsa_sms_channel";
            String Description = "The SMS channel for mahsa chat";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            manager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.message_24)
                .setContentTitle(title)
                .setContentText(message);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        manager.notify(id, builder.build());
    }

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Database db;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = new Database(context);
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }

                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();
                if (isBase64Encoded(message)) {
                    try {
                        Base64.decode(message, Base64.DEFAULT);
                        db.newMessage(sender, message, false, false);
                        EventBus.getDefault().post(new Message(-1, sender, message, false, false));
                        sendNotification(context, "یک پیام رمزشده جدید برای شما ارسال شده است.", "پیام جدید", new Random().nextInt(1024));
                    } catch (RuntimeException ex) {

                    }
                }
            }
        }
    }
}