package com.ccreanga.history.util;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class IOUtil {

    /**
     * RFC 7320 - 6.6. Tear-down
     *
     * @param socket
     */
    public static void closeSocketPreventingReset(Socket socket) {
        try {
            socket.shutdownOutput();
            InputStream in = socket.getInputStream();
            int counter = 16000;//todo - try to find the best value
            while ((in.read() != -1) && (counter-- > 0)) ;
            socket.close();
        } catch (IOException e) {/**ignore**/}
    }

    public static void closeSilent(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {/**ignore**/}
    }


    public static String getIp(Socket socket) {
        return socket.getRemoteSocketAddress().toString();
    }

    public static long copy(InputStream from, OutputStream to, int bufferSize) throws IOException {
        return copy(from, to, -1, -1, bufferSize, null);
    }


    public static long copy(InputStream from, OutputStream to) throws IOException {
        return copy(from, to, -1, -1);
    }

    public static long copy(InputStream from, OutputStream to, long inputOffset, long length) throws IOException {
        return copy(from, to, inputOffset, length, 8 * 1024, null);
    }

    public static long copy(InputStream from, OutputStream to, long inputOffset, long length, int bufferSize, MessageDigest md) throws IOException {

        byte[] buffer = new byte[bufferSize];

        if (inputOffset > 0) {
            long skipped = from.skip(inputOffset);
            if (skipped != inputOffset) {
                throw new EOFException("Bytes to skip: " + inputOffset + " actual: " + skipped);
            }

        }
        if (length == 0) {
            return 0;
        }
        final int bufferLength = buffer.length;
        int bytesToRead = bufferLength;
        if (length > 0 && length < bufferLength) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && -1 != (read = from.read(buffer, 0, bytesToRead))) {
            to.write(buffer, 0, read);
            if (md != null)
                md.update(buffer, 0, read);
            totalRead += read;
            if (length > 0) {
                bytesToRead = (int) Math.min(length - totalRead, bufferLength);
            }
        }
        return totalRead;
    }

    public static LocalDateTime modifiedDateAsUTC(File file) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();
    }

    public static String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

}
