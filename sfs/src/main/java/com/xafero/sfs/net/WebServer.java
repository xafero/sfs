package com.xafero.sfs.net;

import com.xafero.sfs.api.IPlugin;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;
import java.io.File;

public class WebServer extends SimpleWebServer implements Closeable {

    private final URI endpoint;
    private final IPlugin[] plugins;

    public WebServer(String hostname, int port, File wwwroot, IPlugin... plugins) {
        super(hostname, port, wwwroot, false);
        endpoint = URI.create(String.format("http://%s:%s", hostname, port));
        this.plugins = plugins;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    @Override
    public void start() throws IOException {
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String query = session.getQueryParameterString();
        for (IPlugin plugin : plugins) {
            if (plugin.canHandle(uri, query)) {
                return plugin.serve(session);
            }
        }
        return super.serve(session);
    }

    @Override
    public void close() throws IOException {
        closeAllConnections();
        stop();
    }
}
