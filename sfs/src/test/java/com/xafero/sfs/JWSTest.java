package com.xafero.sfs;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import com.xafero.sfs.impl.JWSPlugin;

public class JWSTest {

    public static void main(String[] args) throws IOException,
            InterruptedException {
        IOFileFilter noFiles = FileFilterUtils.falseFileFilter();
        File home = new File(System.getProperty("user.home"));
        File root = null;
        for (File item : FileUtils.listFilesAndDirs(home, noFiles,
                FileFilterUtils.trueFileFilter())) {
            if (item.getName().equals("mctest")) {
                root = item;
                break;
            }
        }
        File jar = new File(root, "mucommander.jar");
        File icon = new File(root, "icon.gif");
        System.out.println(jar + " & " + icon);
        Hoster host = new Hoster(root, new JWSPlugin(jar, icon));
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(host);
        Thread.sleep(2 * 1000);
        Desktop.getDesktop().browse(
                URI.create(host.getEndpoint() + "/webstart"));
        System.out.println("Try it!");
    }
}
