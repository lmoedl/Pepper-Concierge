/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import java.awt.image.BufferedImage;

/**
 *
 * @author lothar
 */
public class Picture {
    private BufferedImage image;
    private String filename;
    public Picture(BufferedImage image, String filename) {
        this.image = image;
        this.filename = filename;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public String getFilename() {
        return this.filename;
}
}
