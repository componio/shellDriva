/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resourcetypes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class is used to render an icon to a smaller resolution
 *
 * @author Tom
 */
public class IconRenderer {

    /**
     * Renders an image to a jpg
     *
     * @param iconPath
     * @param width
     * @param height
     * @param img_type
     * @param ext
     * @return
     * @throws IOException
     */
    public static String renderIcon(String iconPath, int width, int height, String img_type) throws IOException {
        String resized_image_file_path = null;
        File icon = new File(iconPath);
        if (icon.exists()) {
            BufferedImage originalImage = ImageIO.read(icon);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
            BufferedImage resizeImageJpg = resizeImage(originalImage, type, width, height);

            String parent = icon.getAbsoluteFile().getParent().replace("\\", "/");
            resized_image_file_path = parent + "/" + "tmp_" + width + "_" + height + "_" + icon.getName().substring(0, icon.getName().lastIndexOf(".")) + "." + img_type;
            ImageIO.write(resizeImageJpg, img_type, new File(resized_image_file_path));
        }
        return resized_image_file_path;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }
}
