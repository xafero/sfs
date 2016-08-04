package com.xafero.sfs;

import com.xafero.sfs.impl.PopupPlugin;
import java.awt.Desktop;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class ManualTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        IOFileFilter noFiles = FileFilterUtils.falseFileFilter();
        File home = new File(System.getProperty("user.home"));
        File root = null;
        for (File item : FileUtils.listFilesAndDirs(home, noFiles, FileFilterUtils.trueFileFilter())) {
            if (item.getName().equals("Project1")) {
                root = item;
                break;
            }
        }
        System.out.println(root);
        String title = "Popup Helper";
        String href = "index.html";
        String popup = "Test popup";
        int width = 850;
        int height = 650;
        Hoster host = new Hoster(root, new PopupPlugin(title, href, popup, width, height));
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(host);
        Thread.sleep(2 * 1000);
        Desktop.getDesktop().browse(host.getEndpoint());
        System.out.println("Try it!");
    }
}
