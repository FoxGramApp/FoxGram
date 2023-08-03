package it.foxgram.android.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.json.JSONException;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class DateHelper extends DCHelper {
    private static final JSONObject account_date;
    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    private static final String[] ids;
    private static final String fileName = "account_registration_date_id.json";
    private static final int[] time;
    private static final int minId;
    private static final int maxId;

    static {
        try {
            account_date = loadTimeFromJson(ApplicationLoader.applicationContext);
            Iterator<String> keysIterator = account_date.keys();
            List<String> idList = new ArrayList<>();
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                idList.add(key);
            }
            ids = idList.toArray(new String[0]);

            time = new int[ids.length];
            int i = 0;
            while (i < ids.length) {
                time[i] = Integer.parseInt(ids[i]);
                i++;
            }

            minId = time[0];
            maxId = time[time.length - 1];
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static String getDate(long id) {
        Date dateResult = getAccountRegistration(id);
        if (dateResult == null) return LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
        return String.format(dateFormat.format(dateResult));
    }

    @NonNull
    public static JSONObject loadTimeFromJson(Context context) throws IOException, JSONException {
        InputStream inputStream = context.getAssets().open(fileName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new JSONObject(new String(buffer, StandardCharsets.UTF_8));
    }

    @NonNull
    private static Date getAccountRegistration(long id) {
        try {
            if (id <= minId) return new Date((Long) account_date.get(ids[0]));
            else if (id >= maxId) return new Date((Long) account_date.get(ids[ids.length - 1]));
            else {
                int INDEX = Arrays.binarySearch(time, (int) id);
                if (INDEX >= 0) {
                    long dateInMillis = (Long) account_date.get(ids[INDEX]);
                    String formattedDate = dateFormat.format(new Date(dateInMillis));
                    Date parseDate = dateFormat.parse(formattedDate);
                    if (parseDate == null) throw new ParseException("Failed to parse date", 0);
                    return parseDate;
                } else {
                    int insertionPoint = - (INDEX + 1), lowerId = insertionPoint - 1;
                    long lowTime = (Long) account_date.get(ids[lowerId]), MAX_AGE = (Long) account_date.get(ids[insertionPoint]);
                    double ratio = (double) (id - time[lowerId]) / (time[insertionPoint] - time[lowerId]);
                    long midTime = (long) (ratio * (MAX_AGE - lowTime) + lowTime);
                    String formattedDate = dateFormat.format(new Date(midTime));
                    Date parsedDate = dateFormat.parse(formattedDate);
                    if (parsedDate == null) throw new ParseException("Date parse failed", 0);
                    return parsedDate;
                }
            }
        } catch (Exception e) {throw new RuntimeException(e);}
    }
}
