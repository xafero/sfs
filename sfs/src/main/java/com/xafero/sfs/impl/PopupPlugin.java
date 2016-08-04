package com.xafero.sfs.impl;

import com.xafero.sfs.api.IPlugin;
import com.xafero.sfs.net.Defaults;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class PopupPlugin implements IPlugin {

    private final String href;
    private final String title;
    private final String refTitle;
    private final int width;
    private final int height;

    public PopupPlugin(String title, String href, String refTitle, int width, int height) {
        this.href = href;
        this.title = title;
        this.refTitle = refTitle;
        this.width = width;
        this.height = height;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String content = String.format("<html>\n"
                + "    <head>\n"
                + "        <title>%s</title>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <script type='text/javascript'>\n"
                + "            window.opener = self;\n"
                + "            window.open('%s', '%s', 'width=%s,height=%s,' \n"
                + "            + 'scrollbars=no,toolbar=no,menubar=no,resizable=no,'\n"
                + "            + 'location=no,directories=no,status=no');\n"
                + "            self.close();\n"
                + "        </script>\n"
                + "    </body>\n"
                + "</html>", title, href, refTitle, width, height);
        Response resp = newFixedLengthResponse(content);
        return resp;
    }

    @Override
    public boolean canHandle(String uri, String query) {
        return uri.equals(Defaults.ROOT_PAGE);
    }
}
