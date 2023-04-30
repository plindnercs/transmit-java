package edu.plus.cs.util;

import java.io.FileInputStream;
import java.io.IOException;

public class ChunkedIterator implements java.util.Iterator<byte[]> {
    private final FileInputStream input;
    private byte[] buffer;
    private boolean closed = false;

    public ChunkedIterator(FileInputStream input, int chunkSize) {
        this.input = input;
        this.buffer = new byte[chunkSize];
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        }
        try {
            buffer = new byte[buffer.length];
            int read = input.read(buffer);
            if (read == -1) {
                input.close();
                closed = true;
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public byte[] next() {
        return buffer;
    }
}