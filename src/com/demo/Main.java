package com.demo;

import com.demo.Utils.ManifestParser;
import com.demo.Utils.Utils;

public class Main {

    private void run() {
        ManifestParser parser = new ManifestParser("src/com/demo/apk/AndroidManifest.xml");
        parser.parseManifestHeader();
        parser.parseStringChuck();
    }

    public static void main(String[] args) {
	    new Main().run();
    }
}
