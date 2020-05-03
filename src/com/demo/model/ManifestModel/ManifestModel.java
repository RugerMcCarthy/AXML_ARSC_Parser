package com.demo.model.ManifestModel;

public class ManifestModel {
    public byte[] magicNumber = new byte[4];
    public int fileSize;
    public StringChuckModel stringChuckModel = new StringChuckModel();
    public ResourceChuckModel resourceChuckModel = new ResourceChuckModel();
}
