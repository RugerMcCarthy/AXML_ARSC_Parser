package com.demo;

import com.demo.Parser.ResourceParser;
import com.demo.Utils.ManifestParser;
import com.demo.Utils.Utils;

public class Main {

    private void run() {
        ResourceParser parser = new ResourceParser("src/com/demo/apk/arsc/resources.arsc");
        parser.parseTo("src/com/demo/apk/result/resouces_parse_result");
    }

    public static void main(String[] args) {
	    new Main().run();
    }
}
