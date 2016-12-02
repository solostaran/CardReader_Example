package fr.ensicaen.cardreadertest;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;

import static example.utils.StringUtils.convertByteArrayToHexString;
import static example.utils.StringUtils.convertHexStringToByteArray;
import static example.utils.StringUtils.removeSpaces;

/**
 * Created by Joan on 09/12/2015.
 */
public class NfcThread implements Runnable {

    public static final String TAG = "CardReaderTestThread";

    public interface UiCallback {
        void showMessage(String msg);
        void setEditText(int id, String txt);
    }

    private Context context;
    private Tag tag;
    private UiCallback cb;
    public NfcThread(Context context, Tag tag, UiCallback cb) {
        this.context = context;
        this.tag = tag;
        this.cb = cb;
    }

    private IsoDep iso;

    @Override
    public void run() {
        String id = convertByteArrayToHexString(tag.getId());
        Log.d(TAG, "Tag detect " + id);
        cb.showMessage("Tag detect "+id);
        cb.setEditText(R.id.editId, id);

        iso = IsoDep.get(tag);
        if (iso == null) {
            cb.showMessage(context.getString(R.string.non_iso));
            return;
        }
        try {
            iso.connect();
        } catch (IOException e) {
            cb.showMessage(context.getString(R.string.iso_connect_error));
            Log.e(TAG, context.getString(R.string.iso_connect_error) + " : " + e.getMessage());
            return;
        }

        try {

            Date begin = new Date();

            String ret = send_apdu("00 A4 04 00 09 F1 01 02 03 04 48 43 45 01 00");
            cb.setEditText(R.id.editSelect, ret);

            ret = send_apdu("00 10 00 00");
            cb.setEditText(R.id.editIncrement, ret);

            Date end = new Date();

        } catch (IOException e) {
            cb.showMessage(context.getString(R.string.iso_read_error));
            Log.e(TAG, context.getString(R.string.iso_read_error) + " : " + e.getMessage());
        } finally {
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String send_apdu(String sapdu) throws IOException {
        Log.i(TAG, "SEND -> " + sapdu);
        final byte [] apdu = convertHexStringToByteArray(removeSpaces(sapdu));
        byte [] recv = iso.transceive(apdu);
        String ret = convertByteArrayToHexString(recv);
        Log.i(TAG, "RECV <- " + ret);
        return ret;
    }
}
