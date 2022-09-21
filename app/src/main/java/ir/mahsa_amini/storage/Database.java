package ir.mahsa_amini.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ir.mahsa_amini.models.Message;


public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME = "mahsadb";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "messages";
    private static final String ID_COL = "id";
    private static final String PHONE_COL = "phone";
    private static final String MESSAGE_COL = "message";
    private static final String SENT_COL = "sent";
    private static final String UNLOCK_COL = "unlock";

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public ArrayList<Message> readMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorMessages = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        ArrayList<Message> messages = new ArrayList<>();
        if (cursorMessages.moveToFirst()) {
            do {
                messages.add(new Message(
                        cursorMessages.getInt(0),
                        cursorMessages.getString(1),
                        cursorMessages.getString(2),
                        cursorMessages.getInt(3) == 1,
                        cursorMessages.getInt(4) == 1
                ));
            } while (cursorMessages.moveToNext());
        }

        cursorMessages.close();
        return messages;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PHONE_COL + " TEXT,"
                + MESSAGE_COL + " TEXT,"
                + SENT_COL + " INTEGER,"
                + UNLOCK_COL + " INTEGER)";

        db.execSQL(query);
    }

    public void messageUnlock(String id, String message, boolean unlocked) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();

        values.put(MESSAGE_COL, message);
        values.put(UNLOCK_COL, unlocked ? 1 : 0);

        db.update(TABLE_NAME, values, ID_COL + "=?", new String[]{id});
        db.close();
    }

    public void deleteMessage(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_NAME, ID_COL + "=?", new String[]{id});
        db.close();
    }

    public Message readMessage(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorMessages = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COL + " = " + id, null);
        ArrayList<Message> message = new ArrayList<>();
        if (cursorMessages.moveToFirst()) {
            do {
                message.add(new Message(
                        cursorMessages.getInt(0),
                        cursorMessages.getString(1),
                        cursorMessages.getString(2),
                        cursorMessages.getInt(3) == 1,
                        cursorMessages.getInt(4) == 1
                ));
            } while (cursorMessages.moveToNext());
        }

        cursorMessages.close();
        return message.get(0);
    }

    public void newMessage(String phone, String message, boolean sent, boolean unlocked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PHONE_COL, phone);
        values.put(MESSAGE_COL, message);
        values.put(SENT_COL, sent ? 1 : 0);
        values.put(UNLOCK_COL, unlocked ? 1 : 0);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}