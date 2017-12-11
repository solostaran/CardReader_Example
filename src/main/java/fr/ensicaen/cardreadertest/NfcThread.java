package fr.ensicaen.cardreadertest;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

import static com.example.utils.StringUtils.convertByteArrayToHexString;
import static com.example.utils.StringUtils.convertHexStringToByteArray;
import static com.example.utils.StringUtils.removeSpaces;

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
    private String amount;

    public NfcThread(Context context, Tag tag, UiCallback cb, String amount) {
        this.context = context;
        this.tag = tag;
        this.cb = cb;
        this.amount = amount;
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

            //********************* ECHANGES AVEC LA CARTE ************************************
            Date begin = new Date();

            String ret = send_apdu("00 A4 04 00 09 F1 01 02 03 04 48 43 45 02 00");
            cb.setEditText(R.id.editSelectResponse, ret);

            ret = send_apdu("00 10 00 00");
            cb.setEditText(R.id.editIncrementResponse, ret);

            String apdu_mnt = "00 20 00 00 04" + (amount != null ? amount : " 00 00 00 64"); // amount = 1.00 by default
            Log.i(TAG, "SEND AMOUNT : "+amount);
            ret = send_apdu(apdu_mnt);
            cb.setEditText(R.id.editAmountResponse, ret);

            Date end = new Date();
            //*********************************************************************************

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
