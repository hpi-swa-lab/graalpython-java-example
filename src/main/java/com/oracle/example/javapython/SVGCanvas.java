/*
 * Copyright (c) 2021 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package com.oracle.example.javapython;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 * This class is not very interesting for the purpose of this example repository, It merely
 * provides rendering of SVG data into an AWT canvas using the Batik library.
 */
public final class SVGCanvas extends Canvas {
    private static final long serialVersionUID = 1L;
    private BufferedImage img;

    @Override
    public void paint(Graphics g) {
        if (img != null) {
            g.drawImage(img, 0, 0, getWidth(), getHeight(),
                            0, 0, img.getWidth(), img.getHeight(),
                            Color.WHITE, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void rasterizeSVG(InputStream svg) {
        ImageTranscoder t = new ImageTranscoder() {
                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    img = image;
                    repaint();
                }
            };
        t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float)getWidth());
        t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float)getHeight());
        try {
            t.transcode(new TranscoderInput(svg), null);
        } catch (TranscoderException e) {
            e.printStackTrace();
            return;
        }
    }
}
