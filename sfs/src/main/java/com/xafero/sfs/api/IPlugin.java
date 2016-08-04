package com.xafero.sfs.api;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public interface IPlugin {

    Response serve(IHTTPSession session);

    boolean canHandle(String uri, String query);
}
