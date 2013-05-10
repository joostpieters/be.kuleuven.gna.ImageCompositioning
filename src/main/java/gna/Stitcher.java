package gna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Implement the methods stitch, seam and floodfill.
 *
 * @author Kasper Vervaecke
 */
public class Stitcher {

    public static final int IMAGE1 = 0;
    public static final int IMAGE2 = 1;
    public static final int SEAM = 2;

    private PriorityQueue<Position> priorityQueue;
    private int[][] image1;
    private int[][] image2;
    Position[][] positions;

    /**
     * Return the sequence of positions on the seam. The first position in the
     * sequence is (0, 0) and the last is (width - 1, height - 1). Each position
     * on the seam must be adjacent to its predecessor and successor (if any).
     * Positions that are diagonally adjacent are considered adjacent.
     * <p/>
     * <code>image1</code> and <code>image2</code> are both non-null and have
     * equal dimensions.
     */
    public Iterable<Position> seam(int[][] image1, int[][] image2) {
        this.image1 = image1;
        this.image2 = image2;
        positions = new Position[image1.length][image1[0].length];
        ArrayList<Position> seam = new ArrayList<Position>();
        priorityQueue = new PriorityQueue<Position>();
        for (int i = 0; i < image1.length; i++) {
            for (int j = 0; j < image1[0].length; j++) {
                positions[i][j] = new Position(i, j);
                if (i == 0 && j == 0) {
                    positions[i][j].setDistanceToSource(0);
                } else {
                    positions[i][j].setDistanceToSource(Integer.MAX_VALUE);
                }
            }
        }
        priorityQueue.add(positions[0][0]);
        positions[0][0].setInQueue(true);
        while (!priorityQueue.isEmpty()) {
            relax(priorityQueue.poll());
        }
        Position last = positions[positions.length - 1][positions[0].length - 1];
        seam.add(last);
        while (last.getDistanceToSource() != 0) {
            last = last.getPrevious();
            seam.add(last);
        }
        Collections.reverse(seam);
        return seam;
    }

    /**
     * Apply the floodfill algorithm described in the assignment to mask. You can assume
     * the mask contains a seam from the upper left corner to the bottom right corner.
     */
    public void floodfill(int[][] mask) {
        PriorityQueue<Position> positionPriorityQueue = new PriorityQueue<Position>();
        Position position = positions[0][positions[0].length - 1];
        positionPriorityQueue.add(position);
        while (!positionPriorityQueue.isEmpty()) {
            position = positionPriorityQueue.poll();
            if (mask[position.getX()][position.getY()] == IMAGE1) {
                mask[position.getX()][position.getY()] = IMAGE2;
                if (position.getX() > 0) {
                    positionPriorityQueue.add(positions[position.getX() - 1][position.getY()]);
                }
                if (position.getY() > 0) {
                    positionPriorityQueue.add(positions[position.getX()][position.getY() - 1]);
                }
                if (position.getX() < positions.length - 1) {
                    positionPriorityQueue.add(positions[position.getX() + 1][position.getY()]);
                }
                if (position.getY() < positions[0].length - 1 ) {
                    positionPriorityQueue.add(positions[position.getX()][position.getY() + 1]);
                }
            }
        }
    }

    /**
     * Return the mask to stitch two images together. The seam runs from the upper
     * left to the lower right corner, with the rightmost part coming from the
     * first image. A pixel in the mask is 0 on the places where <code>img1</code>
     * should be used, and 1 where <code>img2</code> should be used. On the seam
     * record a value of 2.
     * <p/>
     * ImageCompositor will only call this method (not seam and floodfill) to
     * stitch two images.
     * <p/>
     * <code>image1</code> and <code>image2</code> are both non-null and have
     * equal dimensions.
     */
    public int[][] stitch(int[][] image1, int[][] image2) {
        Iterable<Position> seam = seam(image1, image2);
        int[][] image = new int[image1.length][image1[0].length];
        for (Position position : seam) {
            image[position.getX()][position.getY()] = SEAM;
        }
        floodfill(image);
        return image;
    }

    private void relax(Position position) {
        boolean hasLeft = position.getX() > 0;
        boolean hasTop = position.getY() > 0;
        boolean hasRight = position.getX() < positions.length - 1;
        boolean hasBottom = position.getY() < positions[0].length - 1;
        if (hasTop) {
            checkWeights(position, positions[position.getX()][position.getY() - 1]);
        }
        if (hasRight) {
            checkWeights(position, positions[position.getX() + 1][position.getY()]);
        }
        if (hasBottom) {
            checkWeights(position, positions[position.getX()][position.getY() + 1]);
        }
        if (hasLeft) {
            checkWeights(position, positions[position.getX() - 1][position.getY()]);
        }
        if (hasTop && hasLeft) {
            checkWeights(position, positions[position.getX() - 1][position.getY() - 1]);
        }
        if (hasTop && hasRight) {
            checkWeights(position, positions[position.getX() + 1][position.getY() - 1]);
        }
        if (hasBottom && hasRight) {
            checkWeights(position, positions[position.getX() + 1][position.getY() + 1]);
        }
        if (hasBottom && hasLeft) {
            checkWeights(position, positions[position.getX() - 1][position.getY() + 1]);
        }
    }

    private void checkWeights(Position position, Position otherPosition) {
        if (otherPosition.getDistanceToSource() > position.getDistanceToSource() +
                getSQDistance(otherPosition)) {
            otherPosition.setDistanceToSource(position.getDistanceToSource() +
                    getSQDistance(otherPosition));
            otherPosition.setPrevious(position);
            if (!otherPosition.isInQueue()) {
                otherPosition.setInQueue(true);
                priorityQueue.add(otherPosition);
            }
        }
    }

    private int getSQDistance(Position position) {
        return ImageCompositor.pixelSqDistance(image1[position.getX()][position.getY()],
                image2[position.getX()][position.getY()]);
    }
}
