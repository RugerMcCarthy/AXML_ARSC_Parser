package com.demo;

import com.demo.Utils.ManifestParser;
import com.demo.Utils.Utils;

public class Main {

    private void run() {
        ManifestParser parser = new ManifestParser("src/com/demo/apk/xml/activity_banner_demo.xml");
        parser.parseTo("src/com/demo/apk/result/AndroidManifest.xml");
    }

    public static void main(String[] args) {
	    new Main().run();
    }
}
