/*
 * Copyright 2010, 2011 sshj contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.schmizz.sshj.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopier
        extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public interface ErrorCallback {

        void onError(IOException ioe);

    }

    public interface Listener {

        void reportProgress(long transferred);

    }

    public static ErrorCallback closeOnErrorCallback(final Closeable... toClose) {
        return new ErrorCallback() {
            @Override
            public void onError(IOException ioe) {
                IOUtils.closeQuietly(toClose);
            }
        };
    }

    public static String copyStreamToString(InputStream stream)
            throws IOException {
        final StringBuilder sb = new StringBuilder();
        int read;
        while ((read = stream.read()) != -1)
            sb.append((char) read);
        return sb.toString();
    }

    private static final ErrorCallback NULL_CALLBACK = new ErrorCallback() {
        @Override
        public void onError(IOException ioe) {
        }
    };

    private static final Listener NULL_LISTENER = new Listener() {
        @Override
        public void reportProgress(long transferred) {
        }
    };

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InputStream in;
    private final OutputStream out;
    private int bufSize = 1;
    private boolean keepFlushing = false;
    private long length = -1;

    private Listener listener = NULL_LISTENER;
    private ErrorCallback errCB = NULL_CALLBACK;

    public StreamCopier(String name, InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        setName(name);
    }

    public StreamCopier bufSize(int bufSize) {
        this.bufSize = bufSize;
        return this;
    }

    public StreamCopier keepFlushing(boolean keepFlushing) {
        this.keepFlushing = keepFlushing;
        return this;
    }

    public StreamCopier listener(Listener listener) {
        if (listener == null) listener = NULL_LISTENER;
        this.listener = listener;
        return this;
    }

    public StreamCopier errorCallback(ErrorCallback errCB) {
        if (errCB == null) errCB = NULL_CALLBACK;
        this.errCB = errCB;
        return this;
    }

    public StreamCopier length(long length) {
        this.length = length;
        return this;
    }

    public StreamCopier daemon(boolean choice) {
        setDaemon(choice);
        return this;
    }

    public long copy()
            throws IOException {
        final byte[] buf = new byte[bufSize];
        long count = 0;
        int read = 0;

        final long startTime = System.currentTimeMillis();

        if (length == -1) {
            while ((read = in.read(buf)) != -1)
                count = write(buf, count, read);
        } else {
            while (count < length && (read = in.read(buf, 0, (int) Math.min(bufSize, length - count))) != -1)
                count = write(buf, count, read);
        }

        if (!keepFlushing)
            out.flush();

        final double timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
        final double sizeKiB = count / 1024.0;
        logger.info(sizeKiB + " KiB transferred  in {} seconds ({} KiB/s)", timeSeconds, (sizeKiB / timeSeconds));

        if (length != -1 && read == -1)
            throw new IOException("Encountered EOF, could not transfer " + length + " bytes");

        return count;
    }

    private long write(byte[] buf, long count, int read)
            throws IOException {
        out.write(buf, 0, read);
        count += read;
        if (keepFlushing)
            out.flush();
        listener.reportProgress(count);
        return count;
    }

    @Override
    public void run() {
        try {
            log.debug("Wil pipe from {} to {}", in, out);
            copy();
            log.debug("EOF on {}", in);
        } catch (IOException ioe) {
            log.error("In pipe from {} to {}: " + ioe.toString(), in, out);
            errCB.onError(ioe);
        }
    }

}