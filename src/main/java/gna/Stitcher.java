package gna;

/**
 * Implement the methods stitch, seam and floodfill.
 * 
 * @author Kasper Vervaecke
 */
public class Stitcher {

  public static final int IMAGE1 = 0;
  public static final int IMAGE2 = 1;
  public static final int SEAM = 2;

  /**
   * Return the sequence of positions on the seam. The first position in the
   * sequence is (0, 0) and the last is (width - 1, height - 1). Each position
   * on the seam must be adjacent to its predecessor and successor (if any).
   * Positions that are diagonally adjacent are considered adjacent.
   * 
   * <code>image1</code> and <code>image2</code> are both non-null and have
   * equal dimensions.
   */
  public Iterable<Position> seam(int[][] image1, int[][] image2) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Apply the floodfill algorithm described in the assignment to mask. You can assume
   * the mask contains a seam from the upper left corner to the bottom right corner.
   */
  public void floodfill(int[][] mask) {
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Return the mask to stitch two images together. The seam runs from the upper
   * left to the lower right corner, with the rightmost part coming from the
   * first image. A pixel in the mask is 0 on the places where <code>img1</code>
   * should be used, and 1 where <code>img2</code> should be used. On the seam
   * record a value of 2.
   * 
   * ImageCompositor will only call this method (not seam and floodfill) to
   * stitch two images.
   * 
   * <code>image1</code> and <code>image2</code> are both non-null and have
   * equal dimensions.
   */
  public int[][] stitch(int[][] image1, int[][] image2) {
    // use seam and floodfill to implement this method
    throw new RuntimeException("not implemented yet");
  }
}
