/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lmoedl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

/**
 *
 * @author lothar
 */
public class Util {
    private static final int HEIGHT = 640;
    private static final int WIDTH = 480;

    public static Picture toPicture(byte[] data) {
        int[] intArray;
        intArray = new int[HEIGHT * WIDTH];
        for (int i = 0; i < HEIGHT * WIDTH; i++) {
            intArray[i] = ((255 & 0xFF) << 24) | // alpha
                    ((data[i * 3 + 0] & 0xFF) << 16) | // red
                    ((data[i * 3 + 1] & 0xFF) << 8) | // green
                    ((data[i * 3 + 2] & 0xFF) << 0); // blue
        }
        BufferedImage img = new BufferedImage(HEIGHT, WIDTH,
                BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, HEIGHT, WIDTH, intArray, 0, HEIGHT);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String dateStr = dateFormat.format(date);
        String fileName = String.format("Photo-%s.png", dateStr);
        File out = new File(fileName);
        try {
            ImageIO.write(img, "png", out);
        } catch (IOException e) {
            System.out.println("Error creating picture file");
        }
        return new Picture(img, fileName);
}
}
