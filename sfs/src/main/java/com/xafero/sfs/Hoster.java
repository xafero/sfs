package com.xafero.sfs;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.xafero.sfs.api.IMetaPlugin;
import com.xafero.sfs.api.IPlugin;
import com.xafero.sfs.net.WebServer;

public class Hoster implements Runnable, Closeable {

    private final WebServer web;
    private final IPlugin[] plugins;

    public Hoster(File source, IPlugin... plugins) {
        this("localhost", 9999, source, plugins);
    }

    public Hoster(String host, int port, File source, IPlugin... plugins) {
        this.web = new WebServer(host, port, source, plugins);
        this.plugins = plugins;
    }

    public URI getEndpoint() {
        return web.getEndpoint();
    }

    @Override
    public void run() {
        try {
            for (IPlugin plugin : plugins) {
                if (plugin instanceof IMetaPlugin) {
                    ((IMetaPlugin) plugin).setMeta(getEndpoint());
                }
            }
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
