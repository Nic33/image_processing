package pdl.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.StandardOpenOption;

public final class FilesMethodes {
    // Check recursively all repertory and load all image file in /ressources/images
    public static void loadArboFiles(Map<Long, My_Image> images, File here) {
        byte[] fileContent;
        try {
            for (File file : here.listFiles()) { // Repertory case
                if (file.isDirectory()) {
                    loadArboFiles(images, file);
                } else if (!extensionCheck(file.getName())) {// wrong file type case
                    continue;
                } else { // Good file type case
                    fileContent = Files.readAllBytes(file.toPath());
                    My_Image img = createImage(file, fileContent);
                    images.put(img.getId(), img);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static My_Image createImage(File file, byte[] fileContent) {
        try {
            BufferedImage bimg = ImageIO.read(file);
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            boolean isColor = bimg.getColorModel().getColorSpace().isCS_sRGB();
            My_Image img;

            if (isColor) {
                img = new My_Image(file.getName(), fileContent,
                        getExtensionType(file.getName()),
                        width + " × " + height + " × 3", true);
            } else {
                img = new My_Image(file.getName(), fileContent,
                        getExtensionType(file.getName()),
                        width + " × " + height, false);
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static My_Image createImageFromBuffurerImage(String name, String type, BufferedImage bimg, byte[] fileContent) {
        try {
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            boolean isColor = bimg.getColorModel().getColorSpace().isCS_sRGB();
            My_Image img;

            if (isColor) {
                img = new My_Image(name, fileContent, type,
                        width + " × " + height + " × 3", true);
            } else {
                img = new My_Image(name, fileContent, type,
                        width + " × " + height, false);
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean extensionCheck(String name) { // Return if the extension type was expected
        String extension = getExtensionType(name);
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    public static void saveImage(My_Image image) throws IOException {
        Path path = FileSystems.getDefault().getPath("save", "/images/save");
        Files.write(path, image.getData(), StandardOpenOption.CREATE);
    }

    public static String getExtensionType(String name) {
        String extension = "";
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot == -1)
            lastIndexOfDot = name.lastIndexOf('/');
        if (lastIndexOfDot > 0 && lastIndexOfDot < name.length() - 1) {
            extension = name.substring(lastIndexOfDot + 1);
        }
        return extension;
    }

    public static String getNameWithoutExtension(String name) {
        String fileName = new File(name).getName();
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0 && lastIndexOfDot < fileName.length() - 1) {
            return fileName.substring(0, lastIndexOfDot);
        }
        return fileName;
    }
}
