/**
 * Created by daipei on 2017/11/10.
 */

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.awt.Color;
import java.util.ArrayList;

public class SeamCarver {

    private static double BORDER_ENERGY = 1000;
    private ArrayList<ArrayList<Integer>> colorInfo;
    private int width;
    private int height;

    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        width = picture.width();
        height = picture.height();
        colorInfo = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            colorInfo.add(new ArrayList<Integer>());
            for (int j = 0; j < height; j++) {
                Color color = picture.get(i, j);
                colorInfo.get(i).add(color.hashCode());
            }
        }
    }

    public Picture picture() {
        Picture carvedPic = new Picture(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = new Color(colorInfo.get(i).get(j));
                carvedPic.set(i, j, color);
            }
        }
        return carvedPic;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double energy(int x, int y) {
        validateCol(x);
        validateRow(y);
        if (isBorder(x, y)) {
            return BORDER_ENERGY;
        }
        Color left = new Color(colorInfo.get(x - 1).get(y));
        Color top = new Color(colorInfo.get(x).get(y - 1));
        Color bottom = new Color(colorInfo.get(x).get(y + 1));
        Color right = new Color(colorInfo.get(x + 1).get(y));
        double d1 = Math.pow(right.getRed() - left.getRed(), 2);
        double d2 = Math.pow(right.getGreen() - left.getGreen(), 2);
        double d3 = Math.pow(right.getBlue() - left.getBlue(), 2);
        double d4 = Math.pow(bottom.getRed() - top.getRed(), 2);
        double d5 = Math.pow(bottom.getGreen() - top.getGreen(), 2);
        double d6 = Math.pow(bottom.getBlue() - top.getBlue(), 2);
        return Math.sqrt(d1 + d2 + d3 + d4 + d5 + d6);
    }

    public int[] findHorizontalSeam() {
        int[] seams;
        int[][] edgeTo = new int[width][height];
        double[] weight1 = weight(false);
        double[] weight2 = new double[height];
        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                relax(i, j, edgeTo, weight1, weight2, false);
            }
            double[] tmp = weight1;
            weight1 = weight2;
            weight2 = tmp;
        }
        seams = seam(edgeTo, weight1, false);
        return seams;
    }

    public int[] findVerticalSeam() {
        int[] seams;
        int[][] edgeTo = new int[width][height];
        double[] weight1 = weight(true);
        double[] weight2 = new double[width];
        for (int j = 1; j < height; j++) {
            for (int i = 0; i < width; i++) {
                relax(i, j, edgeTo, weight1, weight2, true);
            }
            double[] tmp = weight1;
            weight1 = weight2;
            weight2 = tmp;
        }
        seams = seam(edgeTo, weight1, true);
        return seams;
    }

    private double[] weight(boolean vertical) {
        int length = vertical ? width : height;
        double[] matirx = new double[length];
        if (vertical) {
            for (int i = 0; i < length; i++) {
                matirx[i] = energy(i, 0);
            }
        } else {
            for (int j = 0; j < length; j++) {
                matirx[j] = energy(0, j);
            }
        }
        return matirx;
    }

    private void relax(int x, int y, int[][] edgeTo, double[] weight1, double[] weight2, boolean vertical) {
        double minEnergy = Double.MAX_VALUE;
        int neighborID = 0;
        int[] neighbors = neighbor(vertical ? x : y, vertical);
        if (vertical) {
            for (int id : neighbors) {
                double en = weight1[id];
                if (en < minEnergy) {
                    minEnergy = en;
                    neighborID = id;
                }
            }
            weight2[x] = energy(x, y) + minEnergy;
        } else {
            for (int id : neighbors) {
                double en = weight1[id];
                if (en < minEnergy) {
                    minEnergy = en;
                    neighborID = id;
                }
            }
            weight2[y] = energy(x, y) + minEnergy;
        }
        edgeTo[x][y] = neighborID;
    }

    private int[] seam(int[][] edgeTo, double[] energyMatrix, boolean vertical) {
        int[] seam;
        int index = 0;
        if (vertical) {
            seam = new int[height];
            for (int i = 1; i < width; i++) {
                if (energyMatrix[i] < energyMatrix[index]) {
                    index = i;
                }
            }
            seam[height - 1] = index;
            for (int j = height - 2; j >= 0; j--) {
                seam[j] = edgeTo[index][j + 1];
                index = seam[j];
            }
        } else  {
            seam = new int[width];
            for (int j = 1; j < height; j++) {
                if (energyMatrix[j] < energyMatrix[index]) {
                    index = j;
                }
            }
            seam[width - 1] = index;
            for (int i = width - 2; i >= 0; i--) {
                seam[i] = edgeTo[i + 1][index];
                index = seam[i];
            }
        }
        return seam;
    }

    private int[] neighbor(int key, boolean vertical) {
        int[] neighbor;
        if ((vertical && width == 1) || (!vertical && height == 1)) {
            neighbor = new int[1];
            neighbor[0] = key;
        } else if (key == 0) {
            neighbor = new int[2];
            neighbor[0] = key;
            neighbor[1] = key + 1;
        } else if ((key == width - 1 && vertical) || (key == height - 1 && !vertical)) {
            neighbor = new int[2];
            neighbor[0] = key - 1;
            neighbor[1] = key ;
        } else {
            neighbor = new int[3];
            neighbor[0] = key - 1;
            neighbor[1] = key;
            neighbor[2] = key + 1;
        }
        return neighbor;
    }

    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        if (seam.length != width) {
            throw new IllegalArgumentException();
        }
        for (int row : seam) {
            validateRow(row);
        }
        validateSeam(seam);
        for (int i = 0; i < width; i++) {
            int target = seam[i];
            colorInfo.get(i).remove(target);
        }
        height--;
    }

    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }
        if (seam.length != height) {
            throw new IllegalArgumentException();
        }
        for (int col : seam) {
            validateCol(col);
        }
        validateSeam(seam);
        for (int i = 0; i < width - 1; i++) {
            for (int j = 0; j < height; j++) {
                int target = seam[j];
                if (i < target) {
                    continue;
                }
                colorInfo.get(i).set(j, colorInfo.get(i + 1).get(j));
            }
        }
        width--;
        colorInfo.remove(width);
    }

    private void validateSeam(int[] seam) {
        for (int i = 1; i < seam.length; i++) {
           if (Math.abs(seam[i] - seam[i - 1]) > 1) {
               throw new IllegalArgumentException();
           }
        }
    }

    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IllegalArgumentException();
        }
    }

    private void validateCol(int col) {
        if (col < 0 || col >= width) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isBorder(int x, int y) {
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return true;
        }
        return false;
    }

    private static final boolean HORIZONTAL   = true;
    private static final boolean VERTICAL     = false;

    private static void printSeam(SeamCarver carver, int[] seam, boolean direction) {
        double totalSeamEnergy = 0.0;

        for (int row = 0; row < carver.height(); row++) {
            for (int col = 0; col < carver.width(); col++) {
                double energy = carver.energy(col, row);
                String marker = " ";
                if ((direction == HORIZONTAL && row == seam[col]) ||
                        (direction == VERTICAL   && col == seam[row])) {
                    marker = "*";
                    totalSeamEnergy += energy;
                }
                StdOut.printf("%7.2f%s ", energy, marker);
            }
            StdOut.println();
        }
        // StdOut.println();
        StdOut.printf("Total energy = %f\n", totalSeamEnergy);
        StdOut.println();
        StdOut.println();
    }

    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        StdOut.printf("%s (%d-by-%d image)\n", args[0], picture.width(), picture.height());
        StdOut.println();
        StdOut.println("The table gives the dual-gradient energies of each pixel.");
        StdOut.println("The asterisks denote a minimum energy vertical or horizontal seam.");
        StdOut.println();

        SeamCarver carver = new SeamCarver(picture);

        StdOut.printf("Vertical seam: { ");
        int[] verticalSeam = carver.findVerticalSeam();
        for (int x : verticalSeam)
            StdOut.print(x + " ");
        StdOut.println("}");
        printSeam(carver, verticalSeam, VERTICAL);

        StdOut.printf("Horizontal seam: { ");
        int[] horizontalSeam = carver.findHorizontalSeam();
        for (int y : horizontalSeam)
            StdOut.print(y + " ");
        StdOut.println("}");

        printSeam(carver, horizontalSeam, HORIZONTAL);

    }

}
