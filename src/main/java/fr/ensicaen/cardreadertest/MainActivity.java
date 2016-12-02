package fr.ensicaen.cardreadertest;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import static example.utils.StringUtils.convertByteArrayToHexString;
import static example.utils.StringUtils.convertHexStringToByteArray;
import static example.utils.StringUtils.removeSpaces;

/**
 * @link old version : http://www.java2s.com/Open-Source/Android_Free_Code/NFC/reader/org_docrj_smartcard_readerReaderActivity_java.htm
 * @link old version : http://www.java2s.com/Open-Source/Android_Free_Code/NFC/reader/org_docrj_smartcard_readerReaderXcvr_java.htm
 * @link last version : https://github.com/doc-rj/smartcard-reader
 */
public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, NfcThread.UiCallback {

    public static final String TAG = "CardReaderTest";

    private EditText editId;
    private EditText editSelect;
    private EditText editIncrement;

    private NfcAdapter adapter;
    //    private PendingIntent nfcintent;
    private IsoDep iso;

    private String[][] nfctechfilter = new String[][] { new String[] { NfcA.class.getName() } };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VIEWS
        editId = (EditText)findViewById(R.id.editId);
        editSelect = (EditText)findViewById(R.id.editSelect);
        editIncrement = (EditText)findViewById(R.id.editIncrement);

        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            Toast.makeText(this, getString(R.string.nfc_unavailable), Toast.LENGTH_LONG).show();
            finish();
        }
//        nfcintent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!adapter.isEnabled()) {
            showMessage(getString(R.string.nfc_not_activated));
            return;
        }
        // register broadcast receiver
        IntentFilter filter = new IntentFilter(
                NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

        // listen for type A tags/smartcards, skipping ndef check
        adapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A
                | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);

//        adapter.enableForegroundDispatch(this, nfcintent, null, nfctechfilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        adapter.disableForegroundDispatch(this);

        unregisterReceiver(mBroadcastReceiver);
        adapter.disableReaderMode(this);
    }

//    protected void onNewIntent(Intent intent) {
////        super.onNewIntent(intent);
//        setIntent(intent);
//
//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        onTagDiscovered(tag);
//
//    }

    @Override
    public void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setEditText(final int id, final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText edit = (EditText) findViewById(id);
                edit.setText(txt);
            }
        });
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        //        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_ON);
                if (state == NfcAdapter.STATE_ON
                        || state == NfcAdapter.STATE_TURNING_ON) {
                    Log.d(TAG, "state: " + state);
                    if (state == NfcAdapter.STATE_ON) {
                        //Bundle extras = new Bundle();
                        //extras.putBoolean("bit_transparent_mode", true);
                        adapter
                                .enableReaderMode(
                                        MainActivity.this,
                                        MainActivity.this,
                                        NfcAdapter.FLAG_READER_NFC_A
                                                //NfcAdapter.FLAG_READER_NFC_B
                                                //| NfcAdapter.FLAG_READER_NFC_F
                                                //| NfcAdapter.FLAG_READER_NFC_V
                                                //| NfcAdapter.FLAG_READER_NFC_BARCODE
                                                //| NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
                                                | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                                        null);
                        //extras);
                    }
                } else {
                    showMessage(getString(R.string.nfc_not_activated));
                }
            }
        }
    };

    @Override
    public void onTagDiscovered(Tag tag) {
        Runnable nfcr = new NfcThread(this, tag, this);
        new Thread(nfcr).start();
    }
}
