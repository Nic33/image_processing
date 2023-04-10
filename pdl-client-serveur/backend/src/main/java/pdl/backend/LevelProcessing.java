package pdl.backend;

import boofcv.struct.image.GrayU8;
import boofcv.alg.filter.convolve.GConvolveImageOps;
import boofcv.struct.convolve.Kernel2D_S32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.Planar;
import boofcv.alg.color.ColorHsv;
import java.lang.Exception;
import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;


import boofcv.io.image.ConvertBufferedImage;
import java.io.IOException;
import java.util.List;

public class LevelProcessing {

    private static final int white = 255, transparent = -256, tmp = 254;

    /********************************************
     * Initilising Functions
     ********************************************/

    /**
     * Coefficient array for Gaussian blur
     * 
     * @param size
     * @param kernel
     */
    private static void buildKernel(int size, int[][] kernel) {
        int rho = size / 3, half = size / 2;
        for (int i = -half, ii = 0; i < half + 1; i++, ii++) {
            for (int y = -half, yy = 0; y < half + 1; y++, yy++) {
                kernel[ii][yy] = (int) ((Math.exp(-(((Math.pow(i, 2) + Math.pow(y, 2)) / (2 * Math.pow(rho, 2))))))
                        * 100);
            }
        }
    }

    /**
     * Convertion of GrayU8 to Planar GrayU8
     * 
     * @param input original image
     * @param img   converted image result
     */
    public static void GrayU8ToPlanarGrayU8(GrayU8 input, Planar<GrayU8> img) {
        int gray;
        for (int y = 0; y < img.height; y++) {
            for (int x = 0; x < img.width; x++) {
                gray = input.get(x, y);
                for (int i = 0; i < img.getNumBands(); ++i) {
                    img.getBand(i).set(x, y, gray);
                }
            }
        }
    }

    /**
     * Copy classical planar to PNG planar format
     * 
     * @param input
     * @param output
     */
    private static void copyPlanarGrayU8PNGformat(Planar<GrayU8> input, Planar<GrayU8> output) {
        output.setNumberOfBands(4);
        for (int y = 0; y < input.height; y++)
            for (int i = 0; i < input.width; i++) {
                output.getBand(0).set(i, y, input.getBand(0).get(i, y));
                output.getBand(1).set(i, y, input.getBand(1).get(i, y));
                output.getBand(2).set(i, y, input.getBand(2).get(i, y));
                output.getBand(3).set(i, y, 255);
            }
    }

    /**
     * Conversion from stored image to processable image
     * 
     * @param image
     * @return
     * @throws IOException
     */
    private static Planar<GrayU8> getPlanarFromImage(My_Image image) throws IOException {
        Planar<GrayU8> input;
        if (image.isColor()) {
            input = ConvertBufferedImage.convertFromPlanar(
                    ImageIO.read(new ByteArrayInputStream(image.getData())),
                    null, true, GrayU8.class);
        } else {
            input = new Planar<>(GrayU8.class, ImageIO.read(new ByteArrayInputStream(image.getData())).getWidth(),
                    ImageIO.read(new ByteArrayInputStream(image.getData())).getHeight(), 3);
            GrayU8 tmp = ConvertBufferedImage.convertFromSingle(
                    ImageIO.read(new ByteArrayInputStream(image.getData())),
                    null, GrayU8.class);
            GrayU8ToPlanarGrayU8(tmp, input);
        }
        return input;
    }

    /********************************************
     * Sub Functions
     ********************************************/

    /**
     * Predicate for if the x position is in the planar
     * 
     * @param input
     * @param pos
     * @return
     */
    private static boolean isInPlanarX(Planar<GrayU8> input, int pos) {
        return pos > 0 && pos < input.width;
    }

    /**
     * Predicate for if the y position is in the planar
     * 
     * @param input
     * @param pos
     * @return
     */
    private static boolean isInPlanarY(Planar<GrayU8> input, int pos) {
        return pos > 0 && pos < input.height;
    }

    /**
     * Create a mask with the tmp value in an area delimited by outline at position
     * in input planar
     * 
     * @param input planar will be mask
     * @param tmp   value of pixel's mask
     * @param posx
     * @param posy
     * @throws Exception
     */
    private static void maskAtPos(Planar<GrayU8> input, int tmp, int posx, int posy) throws Exception {
        propagationMaskRec(input, tmp, posx, posy);
    }

    /**
     * Set pixel at tmp
     * While pixel raound is colored, call himself recursivly
     * 
     * @param input image
     * @param tmp   tmp colorMask
     * @param oneX
     * @param oneY
     */
    private static void propagationMaskRec(Planar<GrayU8> input, int tmp, int oneX, int oneY) throws Exception {
        setValueAtPos(input, tmp, oneX, oneY);
        // Check if x is width and y is lenght
        if (oneX - 1 >= 0 && !isWhiteAtPos(input, oneX - 1, oneY) && !isValueAtPos(input, tmp, oneX - 1, oneY))
            propagationMaskRec(input, tmp, oneX - 1, oneY);
        if (oneX + 1 < input.width && !isWhiteAtPos(input, oneX + 1, oneY) && !isValueAtPos(input, tmp, oneX + 1, oneY))
            propagationMaskRec(input, tmp, oneX + 1, oneY);
        if (oneY - 1 >= 0 && !isWhiteAtPos(input, oneX, oneY - 1) && !isValueAtPos(input, tmp, oneX, oneY - 1))
            propagationMaskRec(input, tmp, oneX, oneY - 1);
        if (oneY + 1 < input.height && !isWhiteAtPos(input, oneX, oneY + 1)
                && !isValueAtPos(input, tmp, oneX, oneY + 1))
            propagationMaskRec(input, tmp, oneX, oneY + 1);
    }

    /**
     * Main sub function of Bottom function called by FrontEnd
     * Supress in input the location delimited by supprMask will be replaced by a
     * Gaussian blur only from the location in input by the floatMask
     * 
     * @param input     used planar for copy
     * @param output    resulted planar from process
     * @param kernel    coefficien gaussien blur array
     * @param supprMask mask define the blured area in output
     * @param floatMask mask define the area will be used to blur on supprMask area
     *                  in output
     * @param tmpsuppr  target location to launch the mask suppr process
     * @param tmpfloat  target location to launch the mask float process
     * @throws Exception
     */
    private static void convolutionButton(Planar<GrayU8> input, Planar<GrayU8> output, int[][] kernel,
            Planar<GrayU8> supprMask, Planar<GrayU8> floatMask, int tmpsuppr, int tmpfloat) throws Exception {
        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {
                if (isValueAtPos(supprMask, tmpsuppr, x, y)) {
                    for (int i = 0; i < 3; ++i) {
                        int size = 0, cpt = 0, yyy = 0, xxx = 0, tabii = 0, tabyy = 0;
                        size = kernel.length;

                        if (!(x <= size / 2) && !(x >= (input.width - size / 2)) && !(y <= size / 2)
                                && !(y >= (input.height - size / 2))) { // Main case in center of the image
                            int count = 0;
                            for (int xx = x - size / 2, tabi = 0; xx < x + size / 2; xx++, tabi++) {
                                for (int yy = y - size / 2, taby = 0; yy < y + size / 2; yy++, taby++) {
                                    if (isValueAtPos(floatMask, tmpfloat, xx, yy)) {
                                        count += input.getBand(i).get(xx, yy) * kernel[tabi][taby];
                                        cpt += kernel[tabi][taby];
                                    }
                                }
                            }
                            output.getBand(i).set(x, y, count / cpt);
                        } else { // Special case if near of the edge and the kernel have many position outside
                                 // the image
                            int count = 0;

                            for (int xx = x - size / 2, tabi = 0; xx < x + size / 2; xx++, tabi++) {
                                for (int yy = y - size / 2, taby = 0; yy < y + size / 2; yy++, taby++) {
                                    if (isValueAtPos(floatMask, tmpfloat, xx, yy)) {
                                        xxx = xx;
                                        yyy = yy;
                                        if (xx < 0) {
                                            xxx = 0 - xx;
                                            tabii = size - tabi - 1;

                                        } else if (xx >= (input.width)) {
                                            xxx = input.width - (xx - input.width + 1);
                                            tabii = size - tabi - 1;

                                        }
                                        if (yy < 0) {
                                            yyy = 0 - yy;
                                            tabyy = size - taby - 1;

                                        } else if (yy >= (input.height)) {
                                            yyy = input.height - (yy - input.height + 1);
                                            tabyy = size - taby - 1;
                                        }
                                        count += input.getBand(i).get(xxx, yyy) * kernel[tabii][tabyy];
                                        cpt += kernel[tabii][tabyy];
                                    }
                                }
                            }
                            output.getBand(i).set(x, y, count / cpt);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draw a line of transparency (in png) or white (in others extension)
     * 
     * @param input image
     * @param x1    X position of first point
     * @param y1    Y position of first point
     * @param x2    X position of second point
     * @param y2    Y position of second point
     * @throws Exception
     */
    private static void transformColorToTransparencyLine(Planar<GrayU8> input, int x1, int y1, int x2, int y2)
            throws Exception {

        // System.out.println(x1 + " " + y1 + " : " + x2 + " " + y2);
        if (x1 == 0 && y1 == 0 || x2 == 0 && y2 == 0)
            return;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            setWhiteAtPos(input, x1, y1);

            if (x1 == x2 && y1 == y2) { // Exit loop when end point is reached
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * Set pixel at tmp up to one white pixel
     * 
     * @param input image
     */
    private static void setMasque(Planar<GrayU8> input) throws Exception {

        // TMP part
        // left to right
        for (int y = 0; y < input.height; y++)
            for (int i = 0; i < input.width; i++) {
                if (isWhiteAtPos(input, i, y) || isTmpAtPos(input, i, y))
                    break;
                setTmpAtPos(input, i, y);
            }

        // right to left
        for (int y = 0; y < input.height; y++)
            for (int i = input.width - 1; i >= 0; i--) {
                if (isWhiteAtPos(input, i, y) || isTmpAtPos(input, i, y))
                    break;
                setTmpAtPos(input, i, y);
            }

        // top to bottom
        for (int i = 0; i < input.width; i++)
            for (int y = 0; y < input.height; y++) {
                if (isWhiteAtPos(input, i, y) || isTmpAtPos(input, i, y))
                    break;
                setTmpAtPos(input, i, y);
            }

        // bottom to top
        for (int i = 0; i < input.width; i++)
            for (int y = input.height - 1; y >= 0; y--) {
                if (isWhiteAtPos(input, i, y) || isTmpAtPos(input, i, y))
                    break;
                setTmpAtPos(input, i, y);
            }
    }

    /**
     * Set pixel at white
     * While pixel raound is colored, whitening
     * 
     * @param input image
     */
    private static void propagationTransparency(Planar<GrayU8> input) throws Exception {
        // Whiting part
        // left to right
        for (int y = 0; y < input.height; y++)
            for (int i = 0; i < input.width; i++) {
                if (isWhiteAtPos(input, i, y))
                    break;
                setWhiteAtPos(input, i, y);
            }

        // right to left
        for (int y = 0; y < input.height; y++)
            for (int i = input.width - 1; i >= 0; i--) {
                if (isWhiteAtPos(input, i, y))
                    break;
                setWhiteAtPos(input, i, y);
            }

        // top to bottom
        for (int i = 0; i < input.width; i++)
            for (int y = 0; y < input.height; y++) {
                if (isWhiteAtPos(input, i, y))
                    break;
                setWhiteAtPos(input, i, y);
            }

        // bottom to top
        for (int i = 0; i < input.width; i++)
            for (int y = input.height - 1; y >= 0; y--) {
                if (isWhiteAtPos(input, i, y))
                    break;
                setWhiteAtPos(input, i, y);
            }
    }

    /**
     * course the image if the color is tmp check aroud the pixel is color and
     * propagate white on pos
     * 
     * @param input
     * @throws Exception
     */
    private static void parcourTmp(Planar<GrayU8> input) throws Exception {
        for (int y = 0; y < input.height; y++)
            for (int i = 0; i < input.width; i++) {
                if (isTmpAtPos(input, i, y)) {
                    if (i - 1 >= 0 && !isWhiteAtPos(input, i - 1, y) && !isTmpAtPos(input, i - 1, y))
                        propagationTransparencyRec(input, i - 1, y);
                    if (i + 1 < input.width && !isWhiteAtPos(input, i + 1, y) && !isTmpAtPos(input, i + 1, y))
                        propagationTransparencyRec(input, i + 1, y);
                    if (y - 1 >= 0 && !isWhiteAtPos(input, i, y - 1) && !isTmpAtPos(input, i, y - 1))
                        propagationTransparencyRec(input, i, y - 1);
                    if (y + 1 < input.height && !isWhiteAtPos(input, i, y + 1) && !isTmpAtPos(input, i, y + 1))
                        propagationTransparencyRec(input, i, y + 1);
                }
            }
    }

    /**
     * Set pixel at white
     * While pixel raound is colored, call himself recursivly
     * 
     * @param input image
     * @param oneX
     * @param oneY
     */
    private static void propagationTransparencyRec(Planar<GrayU8> input, int oneX, int oneY) throws Exception {
        setWhiteAtPos(input, oneX, oneY);
        // Check if x is width and y is lenght
        if (oneX - 1 >= 0 && !isWhiteAtPos(input, oneX - 1, oneY) && !isTmpAtPos(input, oneX - 1, oneY))
            propagationTransparencyRec(input, oneX - 1, oneY);
        if (oneX + 1 < input.width && !isWhiteAtPos(input, oneX + 1, oneY) && !isTmpAtPos(input, oneX + 1, oneY))
            propagationTransparencyRec(input, oneX + 1, oneY);
        if (oneY - 1 >= 0 && !isWhiteAtPos(input, oneX, oneY - 1) && !isTmpAtPos(input, oneX, oneY - 1))
            propagationTransparencyRec(input, oneX, oneY - 1);
        if (oneY + 1 < input.height && !isWhiteAtPos(input, oneX, oneY + 1) && !isTmpAtPos(input, oneX, oneY + 1))
            propagationTransparencyRec(input, oneX, oneY + 1);
    }

    /**
     * Function for set the transparency on planar
     * 
     * @param input
     * @throws Exception
     */
    private static void transformWhiteIntoAlphaTransparency(Planar<GrayU8> input) throws Exception {
        for (int y = 0; y < input.height; y++)
            for (int i = 0; i < input.width; i++) {
                if (isWhiteAtPos(input, i, y))
                    input.getBand(3).set(i, y, transparent);
            }
    }

    /**
     * convert colored background to transparent
     * 
     * @param input image
     */
    private static void transparencyBackground(Planar<GrayU8> input) throws Exception {
        setMasque(input);
        parcourTmp(input);
        propagationTransparency(input);
        transformWhiteIntoAlphaTransparency(input);
    }

    /**
     * Set white color at position
     * 
     * @param input image
     * @param oneX
     * @param oneY
     * @throws Exception
     */
    private static void setWhiteAtPos(Planar<GrayU8> input, int oneX, int oneY) throws Exception {
        input.getBand(0).set(oneX, oneY, white);
        input.getBand(1).set(oneX, oneY, white);
        input.getBand(2).set(oneX, oneY, white);
    }

    /**
     * Set tmp color at position
     * 
     * @param input image
     * @param oneX
     * @param oneY
     * @throws Exception
     */
    private static void setTmpAtPos(Planar<GrayU8> input, int oneX, int oneY) throws Exception {
        input.getBand(0).set(oneX, oneY, tmp);
        input.getBand(1).set(oneX, oneY, tmp);
        input.getBand(2).set(oneX, oneY, tmp);
    }

    /**
     * Set value color at position
     * 
     * @param input image
     * @param tmp   value
     * @param oneX
     * @param oneY
     * @throws Exception
     */
    private static void setValueAtPos(Planar<GrayU8> input, int value, int oneX, int oneY) throws Exception {
        input.getBand(0).set(oneX, oneY, value);
        input.getBand(1).set(oneX, oneY, value);
        input.getBand(2).set(oneX, oneY, value);
    }

    /**
     * return if the pixel at pos is white
     * 
     * @param input image
     * @param oneX  x position
     * @param oneY  y position
     * @return
     * @throws Exception
     */
    private static boolean isWhiteAtPos(Planar<GrayU8> input, int oneX, int oneY) throws Exception {
        return input.getBand(0).get(oneX, oneY) == white && input.getBand(1).get(oneX, oneY) == white
                && input.getBand(2).get(oneX, oneY) == white;
    }

    /**
     * return if the pixel at pos is tmp
     * 
     * @param input image
     * @param oneX  x position
     * @param oneY  y position
     * @return boolean
     * @throws Exception
     */
    private static boolean isTmpAtPos(Planar<GrayU8> input, int oneX, int oneY) throws Exception {
        return input.getBand(0).get(oneX, oneY) == tmp && input.getBand(1).get(oneX, oneY) == tmp
                && input.getBand(2).get(oneX, oneY) == tmp;
    }

    /**
     * return if the pixel at pos is value param
     * 
     * @param input
     * @param value pixel value
     * @param oneX  x position
     * @param oneY  y position
     * @return boolean
     * @throws Exception
     */
    private static boolean isValueAtPos(Planar<GrayU8> input, int value, int oneX, int oneY) throws Exception {
        return input.getBand(0).get(oneX, oneY) == value && input.getBand(1).get(oneX, oneY) == value
                && input.getBand(2).get(oneX, oneY) == value;
    }

    /**
     * Convert int list to int [][2] array at the position marked by -1
     * 
     * @param listPoint
     * @param pos       focus the location array
     * @return int[][]
     */
    private static int[][] listIntergerTOIntArray(List<Integer> listPoint, int pos) {

        int sumNegOne = 0, negOneCurrently = 0;
        for (int i = 0; i < listPoint.size(); i++)
            if (listPoint.get(i) == -1)
                sumNegOne++;
        int[][] tab = new int[((listPoint.size() - sumNegOne) / 2)][2];

        for (int listPos = 0, tabPos = 0; listPos < listPoint.size(); listPos++) {
            if (listPoint.get(listPos) == -1) {
                negOneCurrently++;
                continue;
            }
            if (negOneCurrently == pos) {
                if ((listPos - negOneCurrently) % 2 == 0) {
                    tab[tabPos][0] = listPoint.get(listPos);
                } else {
                    tab[tabPos][1] = listPoint.get(listPos);
                    tabPos++;
                }
            }
        }
        return tab;
    }

    /**
     * Set the outline of the mask with form by point in tab
     * 
     * @param input image to process
     * @param tab   array of point
     * @throws Exception
     */
    private static void outlineMask(Planar<GrayU8> input, int[][] tab) throws Exception {
        for (int i = 0; i < tab.length - 1; i++) {
            transformColorToTransparencyLine(input, tab[i][0], tab[i][1], tab[i + 1][0],
                    tab[i + 1][1]);
        }
    }

    /**
     * Return int[2] with the lower in x position in tab
     * 
     * @param tab
     * @return int[]
     */
    private static int[] extractTheLeftestPoint(int[][] tab) {
        int[] extracted = new int[2];
        extracted[0] = -1;
        for (int i = 0; i < tab.length; i++) {
            if (tab[i][0] != 0 && extracted[0] > tab[i][0] || extracted[0] == -1) {
                extracted[0] = tab[i][0];
                extracted[1] = tab[i][1];
            }
        }
        return extracted;
    }

    /********************************************
     * Called Functions by FrontEnd
     ********************************************/

    /**
     * Set transparency around the form construct by <b>tab</b> in input and return
     * it.
     * 
     * @param input     image to process
     * @param listPoint List of point with tuple contain x and y position
     * @return New planar GrayU8 without background
     * @throws Exception
     */
    public static Planar<GrayU8> Cut(Planar<GrayU8> input, List<Integer> listPoint) throws Exception {

        int[][] tab = listIntergerTOIntArray(listPoint, 0);
        Planar<GrayU8> output = input.clone();
        copyPlanarGrayU8PNGformat(input, output);

        outlineMask(output, tab);
        transparencyBackground(output);

        return output;
    }

    /**
     * Create from an list of point representing two nested contours, the center one
     * of which is replaced by a blur on the second zone
     * 
     * Tips: zone selected must be not too large surface to avoid stack overflow
     * 
     * 
     * @param input
     * @param listPoint
     * @return resulted planar
     * @throws Exception
     */
    public static Planar<GrayU8> Bottom(Planar<GrayU8> input, List<Integer> listPoint)
            throws Exception {

        int size = 121;
        int[][] kernel = new int[size][size];
        buildKernel(size, kernel);

        Planar<GrayU8> finalarray = input.clone();

        int[][] tab = listIntergerTOIntArray(listPoint, 0);
        int[][] tabTwo = listIntergerTOIntArray(listPoint, 1);

        int[] leftestPointFloating = new int[2], leftestPointSupp = new int[2], pointFloating = new int[2],
                pointSupp = new int[2];
        leftestPointFloating = extractTheLeftestPoint(tab);
        leftestPointSupp = extractTheLeftestPoint(tabTwo);

        pointFloating[0] = (leftestPointFloating[0] + leftestPointSupp[0]) / 2;
        pointFloating[1] = (leftestPointFloating[1] + leftestPointSupp[1]) / 2;

        pointSupp[0] = leftestPointFloating[0] + 4;
        pointSupp[1] = leftestPointFloating[1];

        Planar<GrayU8> output = input.clone();

        copyPlanarGrayU8PNGformat(input, output);
        outlineMask(output, tab);
        outlineMask(output, tabTwo);

        Planar<GrayU8> floatingSourceMask = output.clone();
        int tmpFloat = 253;
        maskAtPos(floatingSourceMask, tmpFloat, pointFloating[0], pointFloating[1]);

        Planar<GrayU8> suppressMask = output.clone();
        int tmpSupp = 254;
        maskAtPos(suppressMask, tmpSupp, pointSupp[0], pointSupp[1]);

        convolutionButton(input, finalarray, kernel, suppressMask, floatingSourceMask, tmpSupp, tmpFloat);

        return finalarray;
    }

    public static Planar<GrayU8> FiltreColor(Planar<GrayU8> input, Long delta) throws Exception {
        if (delta < 0 || delta > 360)
            throw new Exception("Bad value argument");
        float rgb[] = new float[3];

        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                int r = input.getBand(0).get(x, y);
                int g = input.getBand(1).get(x, y);
                int b = input.getBand(2).get(x, y);

                float t, s, v;

                // -------------------------------------------------//
                float max = Math.max(Math.max(r, g), b);
                v = max;

                // -------------------------------------------------//
                float min = Math.min(Math.min(r, g), b);
                if (max == 0) {
                    s = 0;
                } else {
                    s = 1 - min / max;
                }

                t = (float) (delta * Math.PI / 180);

                ColorHsv.hsvToRgb(t, s, v, rgb);

                input.getBand(0).set(x, y, (int) (rgb[0]));
                input.getBand(1).set(x, y, (int) (rgb[1]));
                input.getBand(2).set(x, y, (int) (rgb[2]));

            }
        }
        return input;
    }

    public static Planar<GrayU8> FiltreFlou(Planar<GrayU8> input, Long delta) throws Exception { // 0 -> moyenneur
                                                                                                 // 1 -> gaussien
        if (delta != 0 && delta != 1)
            throw new Exception("Bad value argument");
        int size = 5;

        double[][] kernel = new double[size][size];
        int[][] kernel2 = new int[size][size];

        double sigma = 4.0 / 3.0;
        int mediane = size / 2;
        double sum = 0;

        int div = 0;

        for (int y = -mediane; y <= mediane; ++y) {
            for (int x = -mediane; x <= mediane; ++x) {
                double value = (1.0 / (2.0 * Math.PI * Math.pow(sigma, 2)))
                        * Math.exp(-((Math.pow(x, 2) + Math.pow(y, 2)) / (2.0 * Math.pow(sigma, 2))));
                kernel[mediane + x][mediane + y] = value;
                sum += value;
            }
        }

        if (delta == 0) {

            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {

                    kernel2[x][y] = 1;
                    div += kernel2[x][y];

                }
            }

        } else if (delta == 1) {

            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    kernel2[x][y] = (int) Math.round((kernel[x][y] * 100) / sum);
                    div += kernel2[x][y];
                }
            }

        } else {

            // System.out.println("une erreur est survenu pour convolution");
            return input;
        }

        Planar<GrayU8> output = input;

        for (int y = mediane; y < input.height - mediane; y++) {
            for (int x = mediane; x < input.width - mediane; x++) {

                int sum1 = 0;
                int sum2 = 0;
                int sum3 = 0;

                for (int i = -mediane; i <= mediane; i++) {
                    for (int j = -mediane; j <= mediane; j++) {

                        int xx = x + j;
                        int yy = y + i;

                        int R = input.getBand(0).get(xx, yy) * kernel2[mediane + i][mediane + j];
                        int G = input.getBand(1).get(xx, yy) * kernel2[mediane + i][mediane + j];
                        int B = input.getBand(2).get(xx, yy) * kernel2[mediane + i][mediane + j];

                        sum1 += R;
                        sum2 += G;
                        sum3 += B;

                    }
                }

                long mean1 = (Math.round(sum1 / div));
                long mean2 = (Math.round(sum2 / div));
                long mean3 = (Math.round(sum3 / div));

                output.getBand(0).set(x, y, (int) mean1);
                output.getBand(1).set(x, y, (int) mean2);
                output.getBand(2).set(x, y, (int) mean3);

            }
        }

        return output;
    }

    public static Planar<GrayU8> FiltreContour(Planar<GrayU8> input) {

        int[] h1 = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        int[] h2 = {
                -1, -2, -1,
                0, 0, 0,
                1, 2, 1
        };

        Kernel2D_S32 kernelH1 = new Kernel2D_S32(3, h1);
        Kernel2D_S32 kernelH2 = new Kernel2D_S32(3, h2);

        GrayS32 gradientX = new GrayS32(input.width, input.height);
        GrayS32 gradientY = new GrayS32(input.width, input.height);

        GConvolveImageOps.convolve(kernelH1, input.getBand(0), gradientX);
        GConvolveImageOps.convolve(kernelH2, input.getBand(0), gradientY);

        Planar<GrayU8> output = new Planar<GrayU8>(GrayU8.class, input.width, input.height, 3);

        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {

                int gx = gradientX.get(x, y);
                int gy = gradientY.get(x, y);

                int value = (int) Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));

                if (value > 255) {
                    value = 255;
                }
                if (value < 0) {
                    value = 0;
                }

                output.getBand(0).set(x, y, value);
                output.getBand(1).set(x, y, value);
                output.getBand(2).set(x, y, value);
            }
        }

        return output;
    }

    public static Planar<GrayU8> Luminosite(Planar<GrayU8> input, Long delta) throws Exception {
        if (delta < 0 && delta > 255)
            throw new Exception("Bad value argument");
        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                int R = (int) (input.getBand(0).get(x, y) + delta);
                int G = (int) (input.getBand(1).get(x, y) + delta);
                int B = (int) (input.getBand(2).get(x, y) + delta);

                if (R < 0) {

                    R = 0;

                }
                if (G < 0) {

                    G = 0;

                }
                if (B < 0) {

                    B = 0;

                }
                if (R > 255) {

                    R = 255;

                }
                if (G > 255) {

                    G = 255;

                }
                if (B > 255) {

                    B = 255;

                }

                input.getBand(0).set(x, y, R);
                input.getBand(1).set(x, y, G);
                input.getBand(2).set(x, y, B);

            }
        }
        return input;
    }

    public static Planar<GrayU8> EgalHisto(Planar<GrayU8> input) {

        long i_min = 255;
        long i_max = 0;

        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                int R = input.getBand(0).get(x, y);
                int G = input.getBand(1).get(x, y);
                int B = input.getBand(2).get(x, y);

                long value = Math.round(0.3 * R + 0.59 * G + 0.11 * B);

                if (value > i_max) {
                    i_max = value;
                }

                if (value < i_min) {
                    i_min = value;
                }

            }
        }

        int tab[] = new int[256];

        for (int a = 0; a < input.height; ++a) {
            for (int b = 0; b < input.width; ++b) {

                int R = input.getBand(0).get(b, a);
                int G = input.getBand(1).get(b, a);
                int B = input.getBand(2).get(b, a);

                int value = (int) Math.round(0.3 * R + 0.59 * G + 0.11 * B);

                tab[value]++;

            }
        }

        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                int R = input.getBand(0).get(x, y);
                int G = input.getBand(1).get(x, y);
                int B = input.getBand(2).get(x, y);

                int value = (int) Math.round(0.3 * R + 0.59 * G + 0.11 * B);

                int sum = 0;

                for (int i = 0; i <= value; i++) {

                    sum += tab[i];
                }

                int cal = (int) Math.round(((i_max - i_min) * sum) / (input.height * input.width) + i_min);

                R = R + cal - value;
                G = G + cal - value;
                B = B + cal - value;

                if (R > 255) {
                    R = 255;
                }
                if (G > 255) {
                    G = 255;
                }
                if (B > 255) {
                    B = 255;
                }

                if (R < 0) {
                    R = 0;
                }
                if (G < 0) {
                    G = 0;
                }
                if (B < 0) {
                    B = 0;
                }

                input.getBand(0).set(x, y, R);
                input.getBand(1).set(x, y, G);
                input.getBand(2).set(x, y, B);
            }
        }

        return input;
    }

    public static Planar<GrayU8> KeepColor(Planar<GrayU8> input, Long delta, List<Integer>mylist){

        // Etape 1 : calculer la valeur trigo de la couleur rgb

        float[] hsv_color = new float[3];
        int r = mylist.get(0);
        int g = mylist.get(1);
        int b = mylist.get(2);

        ColorHsv.rgbToHsv(r, g, b, hsv_color);

        for (int i = 0; i < 3; i++){
            System.out.println(hsv_color[i]);
        } 

        float delta_2 = ((float)delta/100);

        System.out.println("delta " + delta_2);

        float color_min = hsv_color[0] - delta_2;
        float color_max = hsv_color[0] + delta_2;

        // verification :
        boolean pbl = false;

        if (color_max >= 2*Math.PI){
            color_max = (float) (color_max - (2*Math.PI));
            pbl = true;
        }
        if (color_min < 0){
            color_min = (float) ((2*Math.PI) + color_min);
            pbl = true;
        }

        System.out.println("["+ color_min + ", " + color_max + "]");

        // Etape 2 : on garde la couleur hsv

        BufferedImage image = ConvertBufferedImage.convertTo(input, null, true);
		Planar<GrayF32> rgb = ConvertBufferedImage.convertFromPlanar(image, null, true, GrayF32.class);
        // convertir l'image en espace de couleurs HSV
		
        Planar<GrayF32> hsv = rgb.createSameShape();
		ColorHsv.rgbToHsv(rgb, hsv);
        
        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                float H = hsv.getBand(0).get(x, y); // (0, 2*pi) -> 2*pi = 360°
                float S = hsv.getBand(1).get(x, y); // (0, 1)
                float V = hsv.getBand(2).get(x, y); // (0, 255)

                if (pbl == false){
                    if (H <= color_max && H >= color_min ) {

                        hsv.getBand(0).set(x, y, H);
                        hsv.getBand(1).set(x, y, S);
                        hsv.getBand(2).set(x, y, V);
    
                    } else {
                        // convertir tous les autres pixels en noir et blanc
                        hsv.getBand(0).set(x, y, 0);
                        hsv.getBand(1).set(x, y, 0);
                        hsv.getBand(2).set(x, y, V);
                    }
                    
                }else{ // on passe par 0 sur le cercle trigo

                    if (H <= color_min && H <= color_max ) {

                        hsv.getBand(0).set(x, y, H);
                        hsv.getBand(1).set(x, y, S);
                        hsv.getBand(2).set(x, y, V);
    
                    } else {
                        // convertir tous les autres pixels en noir et blanc
                        hsv.getBand(0).set(x, y, 0);
                        hsv.getBand(1).set(x, y, 0);
                        hsv.getBand(2).set(x, y, V);
                    }
                }

            }
        }
           
        
        ColorHsv.hsvToRgb(hsv, rgb);

        image = ConvertBufferedImage.convertTo(rgb, null, true);
        Planar<GrayU8> rgb2 = ConvertBufferedImage.convertFromPlanar(image, null, true, GrayU8.class);

        return rgb2;
    }
        

    public static Planar<GrayU8> Gray(Planar<GrayU8> input){

        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {

                int R = (int) (input.getBand(0).get(x, y));
                int G = (int) (input.getBand(1).get(x, y));
                int B = (int) (input.getBand(2).get(x, y));

                int value = (int) Math.round(0.3 * R + 0.59 * G + 0.11 * B);

                input.getBand(0).set(x, y, value);
                input.getBand(1).set(x, y, value);
                input.getBand(2).set(x, y, value);
            }
        }
        return input;
    }
    
    public static Planar<GrayU8> Edit(Planar<GrayU8> input1, Planar<GrayU8> input2, List<Integer> mylist) {

        System.out.println("size = " + input1.getWidth() + ", " + input1.getHeight() + " / " + input2.getWidth() + ", " + input2.getHeight());

        int position_X = (int) mylist.get(0);
        int position_Y = (int) mylist.get(1);
        int front_width = (int) mylist.get(2);
        int front_height = (int) mylist.get(3);
    
        // Convertir les images en objets BufferedImage
        BufferedImage image1 = ConvertBufferedImage.convertTo(input1, null, true);
        BufferedImage image2 = ConvertBufferedImage.convertTo(input2, null, true);
    
        // Redimensionner l'image 2 avec les nouvelles dimensions
        Image scaledImage = image2.getScaledInstance(front_width, front_height, Image.SCALE_SMOOTH);
    
        // Dessiner l'image 2 sur l'image 1 aux positions spécifiées
        Graphics2D g2d = image1.createGraphics();
        g2d.drawImage(scaledImage, position_X, position_Y, null);
        g2d.dispose();
    
        // Convertir l'image BufferedImage en objet Planar<GrayU8>
        Planar<GrayU8> output = ConvertBufferedImage.convertFromPlanar(image1, null, true, GrayU8.class);
    
        return output;
    }

}
