/**
 * @version $Id: UriRecordHelper.java 168 2012-08-09 09:14:39Z mroland $
 * 
 * @author Michael Roland <mi.roland@gmail.com>
 *
 * Copyright (c) 2010-2012 Michael Roland
 *
 * ALL RIGHTS RESERVED.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name Michael Roland nor the names of any contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL ROLAND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.ndef;


import android.annotation.SuppressLint;

import example.utils.StringUtils;

import java.util.Arrays;


/**
 * Utilities for URI record encoding.
 */
public class UriRecordHelper {

    /**
     * Type Name Format field for NDEF URI record.
     */
    public static final int URI_TNF = 1;
    /**
     * Full type name of NDEF URI record.
     */
    public static final String URI_TYPE_FULL = "urn:nfc:wkt:U";
    /**
     * Abbreviated type name for Type field of NDEF URI record.
     */
    public static final String URI_TYPE_FIELD = "U";
    /**
     * URI prefix table.
     */
    private static final String[] URI_PREFIX = new String[]{
        /* 0x00 */"",
        /* 0x01 */ "http://www.",
        /* 0x02 */ "https://www.",
        /* 0x03 */ "http://",
        /* 0x04 */ "https://",
        /* 0x05 */ "tel:",
        /* 0x06 */ "mailto:",
        /* 0x07 */ "ftp://anonymous:anonymous@",
        /* 0x08 */ "ftp://ftp.",
        /* 0x09 */ "ftps://",
        /* 0x0A */ "sftp://",
        /* 0x0B */ "smb://",
        /* 0x0C */ "nfs://",
        /* 0x0D */ "ftp://",
        /* 0x0E */ "dav://",
        /* 0x0F */ "news:",
        /* 0x10 */ "telnet://",
        /* 0x11 */ "imap:",
        /* 0x12 */ "rtsp://",
        /* 0x13 */ "urn:",
        /* 0x14 */ "pop:",
        /* 0x15 */ "sip:",
        /* 0x16 */ "sips:",
        /* 0x17 */ "tftp:",
        /* 0x18 */ "btspp://",
        /* 0x19 */ "btl2cap://",
        /* 0x1A */ "btgoep://",
        /* 0x1B */ "tcpobex://",
        /* 0x1C */ "irdaobex://",
        /* 0x1D */ "file://",
        /* 0x1E */ "urn:epc:id:",
        /* 0x1F */ "urn:epc:tag:",
        /* 0x20 */ "urn:epc:pat:",
        /* 0x21 */ "urn:epc:raw:",
        /* 0x22 */ "urn:epc:",
        /* 0x23 */ "urn:nfc:"
    };

    private UriRecordHelper() {
    }

    /**
     * Encode a URI string as payload for an NDEF URI record.
     * @param uri  URI string to be encoded.
     * @return     Payload of NDEF URI record.
     */
    public static byte[] encodeUriRecordPayload(String uri) {
        if (uri == null) {
            return new byte[0];
        }

        // Default: no prefix
        int prefixCode = 0;
        String reducedUri = uri;

        // Find best-matching prefix code:
        for (int i = 1; i < URI_PREFIX.length; ++i) {
            if (uri.startsWith(URI_PREFIX[i])) {
                // First match is best match by design!
                prefixCode = i;

                // Remove prefix from URI string:
                reducedUri = uri.substring(URI_PREFIX[i].length());
                break;
            }
        }

        // URI is UTF8 encoded:
        byte[] reducedUriBytes = StringUtils.convertUTF8StringToByteArray(reducedUri);

        // Assemble record payload:
        byte[] payload = new byte[reducedUriBytes.length + 1];
        payload[0] = (byte) (prefixCode & 0x0FF);
        System.arraycopy(reducedUriBytes, 0, payload, 1, reducedUriBytes.length);

        return payload;
    }

    /**
     * Decode an NDEF URI record's payload into a URI string.
     * @param payload NDEF URI record payload to be decoded.
     * @return        Decoded URI string.
     */
    @SuppressLint("NewApi")
	public static String decodeUriRecordPayload(byte[] payload) {
        if ((payload != null) && (payload.length > 0)) {
            StringBuilder uri = new StringBuilder();

            // Get prefix code:
            int prefixCode = payload[0] & 0x0FF;

            // Decode prefix code:
            if (prefixCode < URI_PREFIX.length) {
                uri.append(URI_PREFIX[prefixCode]);
            }

            // Get remaining URI:
            byte[] reducedUriBytes = Arrays.copyOfRange(payload, 1, payload.length);

            // Decode remaining URI:
            uri.append(StringUtils.convertByteArrayToUTF8String(reducedUriBytes));

            return uri.toString();
        } else {
            return "";
        }
    }
}
