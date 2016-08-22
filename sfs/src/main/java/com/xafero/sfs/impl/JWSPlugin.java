package com.xafero.sfs.impl;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FilenameUtils;

import com.xafero.sfs.api.IMetaPlugin;
import com.xafero.sfs.api.IPlugin;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * The Java Web Start plug-in contains JNLP descriptor generator (XML document)
 */
public class JWSPlugin implements IPlugin, IMetaPlugin {

    private final File jarFile;
    private final File iconFile;
    private final String jwsPrefix;
    private final String homepage;
    private final String longDesc;
    private final String shortDesc;

    private URI endpoint;

    private String name;
    private int size;
    private String comment;
    private Manifest manifest;
    private String jnlpName;

    private String splash;
    private String app4MimeType;
    private String app4Exts;
    private boolean addShortCut;
    private boolean addUpdate;
    private String[] appArgs;
    private String vmArgs;
    private String vmUrl;

    public JWSPlugin(File jarFile, File iconFile) throws IOException {
        this(jarFile, iconFile, "webstart", "http://www.localhost.com",
                "A long story.", "Just short.");
    }

    public JWSPlugin(File jarFile, File iconFile, String jwsPrefix,
            String homepage, String longDesc, String shortDesc)
            throws IOException {
        this.jarFile = jarFile;
        this.iconFile = iconFile;
        this.jwsPrefix = jwsPrefix;
        this.homepage = homepage;
        this.longDesc = longDesc;
        this.shortDesc = shortDesc;
        getManifest(jarFile);
    }

    private void getManifest(File file) throws IOException {
        try (JarFile jar = new JarFile(file, false, JarFile.OPEN_READ)) {
            name = jar.getName();
            size = jar.size();
            comment = jar.getComment();
            manifest = jar.getManifest();
        }
        this.jnlpName = FilenameUtils.getBaseName(jarFile + "") + ".jnlp";
    }

    @Override
    public void setMeta(URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response resp = null;
        try {
            String uri = session.getUri();
            String file = FilenameUtils.getName(uri);
            System.out.println(" REQ " + uri + " = " + file);
            if (file.equalsIgnoreCase(jnlpName)) {
                String content = generateJnlp(jnlpName);
                resp = newFixedLengthResponse(content);
                resp.setMimeType("application/x-java-jnlp-file");
            } else if (file.equalsIgnoreCase(jnlpName + ".xml")) {
                String content = generateJnlp(jnlpName);
                resp = newFixedLengthResponse(content);
                resp.setMimeType("text/xml");
            } else if (file.equalsIgnoreCase(jarFile.getName())) {
                String mimeType = "application/java-archive";
                InputStream data = new FileInputStream(jarFile);
                long bytes = jarFile.length();
                resp = newFixedLengthResponse(Status.OK, mimeType, data, bytes);
                resp.setKeepAlive(true);
            } else if (file.equalsIgnoreCase(iconFile.getName())) {
                String ext = FilenameUtils.getExtension(iconFile + "");
                String mimeType = "image/" + ext;
                InputStream data = new FileInputStream(iconFile);
                long bytes = iconFile.length();
                resp = newFixedLengthResponse(Status.OK, mimeType, data, bytes);
            }
            return resp;
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public boolean canHandle(String uri, String query) {
        return uri.toLowerCase().contains("/" + jwsPrefix);
    }

    private String generateJnlp(String name) throws XMLStreamException,
            IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try (StringWriter out = new StringWriter()) {
            XMLStreamWriter xml = factory.createXMLStreamWriter(out);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("jnlp");
            xml.writeAttribute("spec", /* "1.7+" */ "1.0+");
            xml.writeAttribute("codebase", endpoint + "/" + jwsPrefix + "/");
            xml.writeAttribute("href", name);
            xml.writeStartElement("information");
            writeText(xml, "title", read(Name.IMPLEMENTATION_TITLE));
            writeText(xml, "vendor", read(Name.IMPLEMENTATION_VENDOR));
            writeEmpty(xml, "homepage", "href", homepage);
            writeText(xml, "description", longDesc);
            writeText(xml, "description", shortDesc, "kind", "short");
            writeEmpty(xml, "icon", "href", iconFile.getName());
            if (splash != null) {
                writeEmpty(xml, "icon", "kind", "splash", "href", splash);
            }
            xml.writeEmptyElement("offline-allowed");
            if (app4MimeType != null && app4Exts != null) {
                writeElem(xml, "association", "mime-type", app4MimeType,
                        "extensions", app4Exts);
            }
            if (addShortCut) {
                xml.writeStartElement("shortcut");
                xml.writeAttribute("online", "false");
                xml.writeAttribute("install", "true");
                xml.writeEmptyElement("desktop");
                writeElem(xml, "menu", "sub-menu",
                        read(Name.IMPLEMENTATION_VENDOR));
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeStartElement("security");
            xml.writeEmptyElement("all-permissions");
            xml.writeEndElement();
            addMacOSSettings(xml);
            xml.writeStartElement("resources");
            xml.writeStartElement("j2se");
            xml.writeAttribute("version", "1.7+");
            if (vmArgs != null) {
                xml.writeAttribute("java-vm-args", vmArgs);
            }
            if (vmUrl != null) {
                xml.writeAttribute("href", vmUrl);
            }
            xml.writeEndElement();
            writeEmpty(xml, "jar", "href", jarFile.getName() /* , "main", "true" */);
            xml.writeEndElement();
            if (appArgs == null || appArgs.length < 1) {
                writeEmpty(xml, "application-desc", "main-class",
                        read(Name.MAIN_CLASS));
            } else {
                xml.writeStartElement("application-desc");
                xml.writeAttribute("main-class", read(Name.MAIN_CLASS));
                for (String mainArg : appArgs) {
                    writeText(xml, "argument", mainArg);
                }
                xml.writeEndElement();
            }
            if (addUpdate) {
                xml.writeStartElement("update");
                xml.writeAttribute("check", "background");
                xml.writeAttribute("policy", "prompt-update");
                xml.writeEndElement();
            }
            xml.writeEndDocument();
            xml.flush();
            xml.close();
            return out.toString();
        }
    }

    private String read(Name name) {
        return manifest.getMainAttributes().getValue(name);
    }

    private void addMacOSSettings(XMLStreamWriter xml)
            throws XMLStreamException {
        xml.writeStartElement("resources");
        xml.writeAttribute(" os", "Mac OS X");
        writeEmpty(xml, "property", "name", "com.apple.smallTabs", "value",
                "true");
        writeEmpty(xml, "property", "name", "com.apple.hwaccel", "value",
                "true");
        writeEmpty(xml, "property", "name", "apple.laf.useScreenMenuBar",
                "value", "true");
        writeEmpty(xml, "property", "name", "file.encoding", "value", "UTF-8");
        xml.writeEndElement();
    }

    private void writeEmpty(XMLStreamWriter xml, String element,
            String... attrs) throws XMLStreamException {
        xml.writeEmptyElement(element);
        for (int i = 0; i < attrs.length; i = i + 2) {
            String key = attrs[i];
            String val = attrs[i + 1];
            xml.writeAttribute(key, val);
        }
    }

    private void writeElem(XMLStreamWriter xml, String element, String... attrs)
            throws XMLStreamException {
        xml.writeStartElement(element);
        for (int i = 0; i < attrs.length; i = i + 2) {
            String key = attrs[i];
            String val = attrs[i + 1];
            xml.writeAttribute(key, val);
        }
        xml.writeEndElement();
    }

    private void writeText(XMLStreamWriter xml, String element, String content,
            String... attrs) throws XMLStreamException {
        xml.writeStartElement(element);
        for (int i = 0; i < attrs.length; i = i + 2) {
            String key = attrs[i];
            String val = attrs[i + 1];
            xml.writeAttribute(key, val);
        }
        xml.writeCharacters(content);
        xml.writeEndElement();
    }
}
