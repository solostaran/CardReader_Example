# CardReader_Example
## A simple example of an ISO14443 card reader with Android NFC API

Ensure the ISO7816 reading process of a SmartCard or an HCE application.

This is linked to the __HCE_Example__ project : https://github.com/solostaran/HCE_Example

## MainActivity

See the [_onResume_](https://github.com/solostaran/CardReader_Example/blob/eb4fbde4343cfcf099c2b6327b6b08b88ebf6ba5/cardreader_example/src/main/java/fr/ensicaen/cardreadertest/MainActivity.java#L67) event, the _enableReaderMode_ call ensures to catch the contactless communication.

The callback is into the activity as the _onTagDiscovered_ method. It launches the reading into a thread.

## NfcThread

The [_run_](https://github.com/solostaran/CardReader_Example/blob/eb4fbde4343cfcf099c2b6327b6b08b88ebf6ba5/cardreader_example/src/main/java/fr/ensicaen/cardreadertest/NfcThread.java#L42) method contains the traditionnal ISO7816 dialog.