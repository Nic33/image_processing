package pdl.backend;

import boofcv.struct.image.GrayU8;
import boofcv.alg.filter.convolve.GConvolveImageOps;
import boofcv.struct.convolve.Kernel2D_S32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.Planar;
import boofcv.alg.color.ColorHsv;
import java.lang.Exception;

public class LevelProcessing {

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

            System.out.println("une erreur est survenu pour convolution");
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

}
