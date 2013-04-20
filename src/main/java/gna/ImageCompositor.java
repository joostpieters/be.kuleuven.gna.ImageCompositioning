package gna;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

public class ImageCompositor {
  public static int pixelSqDistance(int x, int y) {
    int r = (x & 0xFF0000) - (y & 0xFF0000) >> 16;
    int g = (x & 0xFF00) - (y & 0xFF00) >> 8;
    int b = (x & 0xFF) - (y & 0xFF);

    return r * r + g * g + b * b;
  }

  public static int[][] getImageData(Image img) {
    int w = img.getWidth(null);
    int h = img.getHeight(null);
    int[] data = new int[w * h];
    PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, data, 0, w);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      System.err.println("interrupted waiting for pixels!");
      return new int[0][0];
    }

    int[][] out = new int[h][w];
    int row = 0;
    for (int i = 0; row < h; row++) {
      for (int col = 0; col < w; i++) {
        out[row][col] = data[i];

        col++;
      }
    }
    return out;
  }

  private static void flipHorizontal(int[][] img) {
    int w = img[0].length;
    for (int row = 0; row < img.length; row++)
      for (int col = 0; col < w / 2; col++) {
        int t = img[row][col];
        img[row][col] = img[row][(w - col - 1)];
        img[row][(w - col - 1)] = t;
      }
  }

  private static void flipVertical(int[][] img) {
    int h = img.length;
    for (int row = 0; row < h / 2; row++)
      for (int col = 0; col < img[0].length; col++) {
        int t = img[row][col];
        img[row][col] = img[(h - row - 1)][col];
        img[(h - row - 1)][col] = t;
      }
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("ImageCompositor requires (at least) 2 arguments.");
      return;
    }
    Image image1 = null;
    Image image2 = null;
    try {
      image1 = ImageIO.read(new File(args[0]));
    } catch (IOException e) {
      System.out.println("unable to read image: " + args[0]);
      return;
    }
    try {
      image2 = ImageIO.read(new File(args[1]));
    } catch (IOException e) {
      System.out.println("unable to read image: " + args[1]);
      return;
    }
    if (image1 == null) {
      System.out.println("unable to read image: " + args[0]);
      return;
    }
    if (image2 == null) {
      System.out.println("unable to read image: " + args[1]);
      return;
    }
    int[][] img1 = getImageData(image1);

    int w1 = img1[0].length;
    int h1 = img1.length;

    int[][] img2 = getImageData(image2);
    int w2 = img2[0].length;
    int h2 = img2.length;

    int offsetx = 0;
    int offsety = 0;
    if (args.length > 3) {
      offsetx = Integer.parseInt(args[2]);
      offsety = Integer.parseInt(args[3]);
      if (offsetx < 0) {
        System.out.println("x offset must be less than or equal to zero.");
        System.exit(0);
        return;
      }
    }

    int x1 = 0;
    int x2 = 0;
    int y1 = 0;
    int y2 = 0;
    int w = 0;
    int h = 0;
    if (offsetx >= 0) {
      x1 = offsetx;
      w = Math.min(w1 - offsetx, w2);
    } else {
      x2 = -offsetx;
      w = Math.min(w1, w2 + offsetx);
    }

    if (offsety >= 0) {
      y1 = offsety;
      h = Math.min(h1 - offsety, h2);
    } else {
      y2 = -offsety;
      h = Math.min(h1, h2 + offsety);
    }

    int[][] a = new int[h][w];
    int[][] b = new int[h][w];

    System.out.println(x1 + " " + x2 + " " + y1 + " " + y2 + " " + w + " " + h
        + " " + w1 + " " + h1);

    for (int row = 0; row < h; row++) {
      for (int col = 0; col < w; col++) {
        a[row][col] = img1[(row + y1)][(col + x1)];
        b[row][col] = img2[(row + y2)][(col + x2)];
      }

    }

    if ((offsetx > 0) && (offsety != 0)) {
      flipHorizontal(a);
      flipHorizontal(b);
    }
    if (offsety < 0) {
      flipVertical(a);
      flipVertical(b);
    }
    int[][] mask = new Stitcher().stitch(a, b);
    if ((offsetx > 0) && (offsety != 0)) {
      flipHorizontal(a);
      flipHorizontal(b);
      flipHorizontal(mask);
    }
    if (offsety < 0) {
      flipVertical(a);
      flipVertical(b);
      flipVertical(mask);
    }

    int out_w = w1 + w2 - w;
    int out_h = h1 + h2 - h;
    int[] data = new int[out_w * out_h];
    for (int row = 0; row < h1; row++) {
      for (int col = 0; col < w1; col++) {
        data[((row + y2) * out_w + col + x2)] = img1[row][col];
      }
    }
    for (int row = 0; row < h2; row++) {
      for (int col = 0; col < w2; col++) {
        data[((row + y1) * out_w + col + x1)] = img2[row][col];
      }
    }
    for (int row = 0; row < h; row++) {
      for (int col = 0; col < w; col++) {
        data[((row + y1 + y2) * out_w + col + x1 + x2)] = (((mask[row][col] > 0 ? 1
            : 0) ^ ((offsetx > 0) && (offsety != 0) ? 1 : 0)) != 0 ? b[row][col]
            : a[row][col]);
      }
    }

    try {
      if (args.length > 4) {
        Image ground_truth_img = StdDraw.getImage(args[4]);
        int[][] ground_truth = getImageData(ground_truth_img);
        if ((ground_truth.length != out_h) || (ground_truth[0].length != out_w)) {
          System.out.println("The stitched image's dimensions (" + out_w
              + " x " + out_h + ") differ from the ground truth image "
              + args[4] + "'s dimensions (" + ground_truth[0].length + " x "
              + ground_truth.length + ").");
          for (int row = 0; row < out_h; row++)
            for (int col = 0; col < out_w; col++)
              data[(row * out_w + col)] = -65536;
        } else {
          int row = 0;
          for (int i = 0; row < out_h; row++) {
            for (int col = 0; col < out_w; i++) {
              data[i] = ((data[i] == 0) || (data[i] == ground_truth[row][col]) ? lighter(data[i])
                  : -65536);

              col++;
            }
          }
        }
      }
    } catch (Exception e) {
    }
    Image out = StdDraw.getImage(data, out_w, out_h);
    StdDraw.setCanvasSize(out_w, out_h);
    StdDraw.setXscale(-0.5D, 0.5D);
    StdDraw.setYscale(-0.5D, 0.5D);
    StdDraw.picture(0.0D, 0.0D, out, "");
  }

  public static int lighter(int i) {
    return i & 0xFF000000 | 0xC0C0C0 | (i & 0xFCFCFC) >> 2;
  }
}
