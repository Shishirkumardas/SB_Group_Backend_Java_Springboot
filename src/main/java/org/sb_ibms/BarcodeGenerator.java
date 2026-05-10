package org.sb_ibms;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class BarcodeGenerator {

    public byte[] generateBarcode(
            String text,
            int width,
            int height
    ) throws Exception {

        Map<EncodeHintType, Object> hints =
                new HashMap<>();

        // IMPORTANT FOR SCANNING QUALITY
        hints.put(
                EncodeHintType.MARGIN,
                20
        );

        hints.put(
                EncodeHintType.CHARACTER_SET,
                "UTF-8"
        );

        BitMatrix matrix =
                new MultiFormatWriter().encode(
                        text,
                        BarcodeFormat.CODE_128,
                        width,
                        height,
                        hints
                );

        BufferedImage barcodeImage =
                MatrixToImageWriter.toBufferedImage(matrix);

        // EXTRA SPACE FOR TEXT
        int textAreaHeight = 45;

        BufferedImage finalImage =
                new BufferedImage(
                        width,
                        height + textAreaHeight,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                finalImage.createGraphics();

        // HIGH QUALITY RENDERING
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // WHITE BACKGROUND
        graphics.setColor(Color.WHITE);

        graphics.fillRect(
                0,
                0,
                width,
                height + textAreaHeight
        );

        // DRAW BARCODE
        graphics.drawImage(
                barcodeImage,
                0,
                0,
                null
        );

        // DRAW TEXT UNDER BARCODE
        graphics.setColor(Color.BLACK);

        graphics.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        FontMetrics metrics =
                graphics.getFontMetrics();

        int textWidth =
                metrics.stringWidth(text);

        graphics.drawString(
                text,
                (width - textWidth) / 2,
                height + 30
        );

        graphics.dispose();

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        ImageIO.write(
                finalImage,
                "png",
                baos
        );

        return baos.toByteArray();
    }

    // OPTIONAL QR CODE SUPPORT
    public byte[] generateQRCode(
            String text,
            int width,
            int height
    ) throws Exception {

        Map<EncodeHintType, Object> hints =
                new HashMap<>();

        hints.put(
                EncodeHintType.MARGIN,
                2
        );

        hints.put(
                EncodeHintType.CHARACTER_SET,
                "UTF-8"
        );

        BitMatrix matrix =
                new MultiFormatWriter().encode(
                        text,
                        BarcodeFormat.QR_CODE,
                        width,
                        height,
                        hints
                );

        BufferedImage image =
                MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }
}