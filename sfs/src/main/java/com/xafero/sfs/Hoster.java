package com.xafero.sfs;

import com.xafero.sfs.api.IPlugin;
import com.xafero.sfs.net.WebServer;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Hoster implements Runnable, Closeable {

    private final WebServer web;

    public Hoster(File source, IPlugin... plugins) {
        this.web = new WebServer("localhost", 9999, source, plugins);
    }

    public URI getEndpoint() {
        return web.getEndpoint();
    }

    @Override
    public void run() {
        try {
            web.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start webserver!", e);
        }
    }

    @Override
    public void close() throws IOException {
        web.close();
    }
}
