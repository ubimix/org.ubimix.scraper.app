/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;

/**
 * @author kotelnikov
 */
public class ContentAdapter extends WrfResourceAdapter
    implements
    IContentAdapter {

    public interface IFileLock {
        File getFile();

        void unlock();
    }

    private WrfResource fResource;

    public ContentAdapter(IWrfResource instance) {
        fResource = (WrfResource) instance;
    }

    public void delete() throws IOException {
        IFileLock lock = lock(true);
        try {
            File file = lock.getFile();
            file.delete();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#exists()
     */
    public synchronized boolean exists() {
        File file = getResourceFile();
        return file.exists();
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#getContentInput()
     */
    public synchronized InputStream getContentInput() throws IOException {
        // FIXME: set the lock/unlock in the close method
        final IFileLock lock = lock(false);
        File file = lock.getFile();
        return new FileInputStream(file) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#getContentOutput()
     */
    public synchronized OutputStream getContentOutput() throws IOException {
        boolean ok = false;
        final IFileLock lock = lock(true);
        try {
            final File file = lock.getFile();
            file.getParentFile().mkdirs();
            FileOutputStream result = new FileOutputStream(file) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        lock.unlock();
                    }
                }
            };
            ok = true;
            return result;
        } finally {
            if (!ok) {
                lock.unlock();
            }
        }
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#getLastModified()
     */
    public long getLastModified() {
        File file = getResourceFile();
        return file.exists() ? file.lastModified() : -1;
    }

    private File getResourceFile() {
        File file = fResource.getResourceFile("data.bin");
        return file;
    }

    private IFileLock lock(final boolean writeLock) {
        return new IFileLock() {
            public File getFile() {
                final File file = getResourceFile();
                return file;
            }

            public void unlock() {
                if (writeLock) {
                    fResource.notifyAdapters(new ContentChangeEvent());
                }
            }
        };
    }

    public void remove() {
        IFileLock lock = lock(true);
        try {
            IOUtil.delete(lock.getFile());
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#writeContent(java.io.InputStream)
     */
    public synchronized void writeContent(final InputStream input)
        throws IOException {
        try {
            fResource.notifyAdapters(new ContentChangeEvent());
            OutputStream out = getContentOutput();
            try {
                byte[] buf = new byte[1024 * 20];
                int len;
                while ((len = input.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            input.close();
        }
    }
}
