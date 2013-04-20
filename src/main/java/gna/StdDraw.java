package gna;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

final class StdDraw implements ActionListener, MouseListener,
    MouseMotionListener, KeyListener {
  public static final Color BLACK = Color.BLACK;

  public static final Color BLUE = Color.BLUE;

  public static final Color CYAN = Color.CYAN;

  public static final Color DARK_GRAY = Color.DARK_GRAY;

  public static final Color GRAY = Color.GRAY;

  public static final Color GREEN = Color.GREEN;

  public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;

  public static final Color MAGENTA = Color.MAGENTA;

  public static final Color ORANGE = Color.ORANGE;

  public static final Color PINK = Color.PINK;

  public static final Color RED = Color.RED;

  public static final Color WHITE = Color.WHITE;

  public static final Color YELLOW = Color.YELLOW;

  public static final Color BOOK_BLUE = new Color(9, 90, 166);

  public static final Color BOOK_RED = new Color(173, 32, 24);

  private static final Color DEFAULT_PEN_COLOR = BLACK;

  private static final Color DEFAULT_CLEAR_COLOR = WHITE;
  private static Color penColor;
  private static int width = 512;

  private static int height = 512;
  private static double penRadius;
  private static boolean defer = false;
  private static double xmin;
  private static double ymin;
  private static double xmax;
  private static double ymax;
  private static Object mouseLock = new Object();

  private static Object keyLock = new Object();

  private static final Font DEFAULT_FONT = new Font("SansSerif", 0, 16);
  private static Font font;
  private static BufferedImage offscreenImage;
  private static BufferedImage onscreenImage;
  private static Graphics2D offscreen;
  private static Graphics2D onscreen;
  private static StdDraw std = new StdDraw();
  private static JFrame frame;
  private static boolean mousePressed = false;

  private static double mouseX = 0.0D;

  private static double mouseY = 0.0D;

  private static LinkedList<Character> keysTyped = new LinkedList<Character>();

  static {
    init();
  }

  public static void setCanvasSize(int w, int h) {
    if ((w < 1) || (h < 1))
      throw new RuntimeException("width and height must be positive");

    width = w;

    height = h;

    init();
  }

  private static void init() {
    if (frame != null)
      frame.setVisible(false);

    frame = new JFrame();

    offscreenImage = new BufferedImage(width, height, 2);

    onscreenImage = new BufferedImage(width, height, 2);

    offscreen = offscreenImage.createGraphics();

    onscreen = onscreenImage.createGraphics();

    setXscale();

    setYscale();

    offscreen.setColor(DEFAULT_CLEAR_COLOR);

    offscreen.fillRect(0, 0, width, height);

    setPenColor();

    setPenRadius();

    setFont();

    clear();

    RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    hints
        .put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    offscreen.addRenderingHints(hints);

    ImageIcon icon = new ImageIcon(onscreenImage);

    JLabel draw = new JLabel(icon);

    draw.addMouseListener(std);

    draw.addMouseMotionListener(std);

    frame.setContentPane(draw);

    frame.addKeyListener(std);

    frame.setResizable(false);

    frame.setDefaultCloseOperation(3);

    frame.setTitle("Standard Draw");

    frame.setJMenuBar(createMenuBar());

    frame.pack();

    frame.requestFocusInWindow();

    frame.setVisible(true);
  }

  private static JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu menu = new JMenu("File");

    menuBar.add(menu);

    JMenuItem menuItem1 = new JMenuItem(" Save...   ");

    menuItem1.addActionListener(std);

    menuItem1.setAccelerator(KeyStroke.getKeyStroke(83, Toolkit
        .getDefaultToolkit().getMenuShortcutKeyMask()));

    menu.add(menuItem1);

    return menuBar;
  }

  public static void setXscale() {
    setXscale(0.0D, 1.0D);
  }

  public static void setYscale() {
    setYscale(0.0D, 1.0D);
  }

  public static void setXscale(double min, double max) {
    double size = max - min;

    xmin = min - 0.05D * size;

    xmax = max + 0.05D * size;
  }

  public static void setYscale(double min, double max) {
    double size = max - min;

    ymin = min - 0.05D * size;

    ymax = max + 0.05D * size;
  }

  private static double scaleX(double x) {
    return width * (x - xmin) / (xmax - xmin);
  }

  private static double scaleY(double y) {
    return height * (ymax - y) / (ymax - ymin);
  }

  private static double factorX(double w) {
    return w * width / Math.abs(xmax - xmin);
  }

  private static double factorY(double h) {
    return h * height / Math.abs(ymax - ymin);
  }

  private static double userX(double x) {
    return xmin + x * (xmax - xmin) / width;
  }

  private static double userY(double y) {
    return ymax - y * (ymax - ymin) / height;
  }

  public static void clear() {
    clear(DEFAULT_CLEAR_COLOR);
  }

  public static void clear(Color color) {
    offscreen.setColor(color);

    offscreen.fillRect(0, 0, width, height);

    offscreen.setColor(penColor);

    draw();
  }

  public static void setPenRadius() {
    setPenRadius(0.002D);
  }

  public static void setPenRadius(double r) {
    if (r < 0.0D)
      throw new RuntimeException("pen radius must be positive");

    penRadius = r * 512.0D;

    BasicStroke stroke = new BasicStroke((float) penRadius);

    offscreen.setStroke(stroke);
  }

  public static void setPenColor() {
    setPenColor(DEFAULT_PEN_COLOR);
  }

  public static void setPenColor(Color color) {
    penColor = color;

    offscreen.setColor(penColor);
  }

  public static void setFont() {
    setFont(DEFAULT_FONT);
  }

  public static void setFont(Font f) {
    font = f;
  }

  public static void pixel(double x, double y) {
    offscreen.fillRect((int) Math.round(scaleX(x)),
        (int) Math.round(scaleY(y)), 1, 1);
  }

  public static void polygon(double[] x, double[] y) {
    int N = x.length;

    GeneralPath path = new GeneralPath();

    path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));

    for (int i = 0; i < N; i++) {
      path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
    }
    path.closePath();

    offscreen.draw(path);

    draw();
  }

  public static void filledPolygon(double[] x, double[] y) {
    int N = x.length;

    GeneralPath path = new GeneralPath();

    path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));

    for (int i = 0; i < N; i++) {
      path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
    }
    path.closePath();

    offscreen.fill(path);

    draw();
  }

  public static Image getImage(int[] data, int w, int h) {
    return frame.createImage(new MemoryImageSource(w, h, data, 0, w));
  }

  public static Image getImage(String filename) {
    ImageIcon icon = new ImageIcon(filename);

    if ((icon == null) || (icon.getImageLoadStatus() != 8)) {
      try {
        URL url = new URL(filename);

        icon = new ImageIcon(url);
      } catch (Exception localException) {
      }

    }

    if ((icon == null) || (icon.getImageLoadStatus() != 8)) {
      URL url = StdDraw.class.getResource(filename);

      if (url == null)
        throw new RuntimeException("image " + filename + " not found");

      icon = new ImageIcon(url);
    }

    return icon.getImage();
  }

  public static void picture(double x, double y, Image image, String s) {
    double xs = scaleX(x);

    double ys = scaleY(y);

    int ws = image.getWidth(null);

    int hs = image.getHeight(null);

    if ((ws < 0) || (hs < 0))
      throw new RuntimeException("image " + s + " is corrupt");

    offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0D),
        (int) Math.round(ys - hs / 2.0D), null);

    draw();
  }

  public static void picture(double x, double y, String s) {
    Image image = getImage(s);

    picture(x, y, image, s);
  }

  public static void picture(double x, double y, String s, double degrees) {
    Image image = getImage(s);

    double xs = scaleX(x);

    double ys = scaleY(y);

    int ws = image.getWidth(null);

    int hs = image.getHeight(null);

    if ((ws < 0) || (hs < 0))
      throw new RuntimeException("image " + s + " is corrupt");

    offscreen.rotate(Math.toRadians(-degrees), xs, ys);

    offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0D),
        (int) Math.round(ys - hs / 2.0D), null);

    offscreen.rotate(Math.toRadians(degrees), xs, ys);

    draw();
  }

  public static void picture(double x, double y, String s, double w, double h) {
    Image image = getImage(s);

    double xs = scaleX(x);

    double ys = scaleY(y);

    double ws = factorX(w);

    double hs = factorY(h);

    if ((ws < 0.0D) || (hs < 0.0D))
      throw new RuntimeException("image " + s + " is corrupt");

    if ((ws <= 1.0D) && (hs <= 1.0D))
      pixel(x, y);
    else {
      offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0D),
          (int) Math.round(ys - hs / 2.0D), (int) Math.round(ws),
          (int) Math.round(hs), null);
    }

    draw();
  }

  public static void picture(double x, double y, String s, double w, double h,
      double degrees) {
    Image image = getImage(s);

    double xs = scaleX(x);

    double ys = scaleY(y);

    double ws = factorX(w);

    double hs = factorY(h);

    if ((ws < 0.0D) || (hs < 0.0D))
      throw new RuntimeException("image " + s + " is corrupt");

    if ((ws <= 1.0D) && (hs <= 1.0D))
      pixel(x, y);

    offscreen.rotate(Math.toRadians(-degrees), xs, ys);

    offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0D),
        (int) Math.round(ys - hs / 2.0D), (int) Math.round(ws),
        (int) Math.round(hs), null);

    offscreen.rotate(Math.toRadians(degrees), xs, ys);

    draw();
  }

  public static void text(double x, double y, String s) {
    offscreen.setFont(font);

    FontMetrics metrics = offscreen.getFontMetrics();

    double xs = scaleX(x);

    double ys = scaleY(y);

    int ws = metrics.stringWidth(s);

    int hs = metrics.getDescent();

    offscreen.drawString(s, (float) (xs - ws / 2.0D), (float) (ys + hs));

    draw();
  }

  public static void textLeft(double x, double y, String s) {
    offscreen.setFont(font);

    FontMetrics metrics = offscreen.getFontMetrics();

    double xs = scaleX(x);

    double ys = scaleY(y);

    int hs = metrics.getDescent();

    offscreen.drawString(s, (float) xs, (float) (ys + hs));

    show();
  }

  public static void textRight(double x, double y, String s) {
    offscreen.setFont(font);

    FontMetrics metrics = offscreen.getFontMetrics();

    double xs = scaleX(x);

    double ys = scaleY(y);

    int ws = metrics.stringWidth(s);

    int hs = metrics.getDescent();

    offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));

    show();
  }

  public static void show(int t) {
    defer = false;

    draw();
    try {
      Thread.currentThread();
      Thread.sleep(t);
    } catch (InterruptedException e) {
      System.out.println("Error sleeping");
    }
    defer = true;
  }

  public static void show() {
    defer = false;

    draw();
  }

  private static void draw() {
    if (defer)
      return;

    onscreen.drawImage(offscreenImage, 0, 0, null);

    frame.repaint();
  }

  public static void save(String filename) {
    File file = new File(filename);

    String suffix = filename.substring(filename.lastIndexOf('.') + 1);

    if (suffix.toLowerCase().equals("png")) {
      try {
        ImageIO.write(offscreenImage, suffix, file);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      String msg = "Invalid image file type: " + suffix + "\n"
          + "Images may only be saved in .png format.";

      System.out.println(msg);

      JOptionPane.showMessageDialog(null, msg);
    }
  }

  public void actionPerformed(ActionEvent e) {
    FileDialog chooser = new FileDialog(frame, "Use a .png or .jpg extension",
        1);

    chooser.setVisible(true);

    String filename = chooser.getFile();

    if (filename != null) {
      save(chooser.getDirectory() + File.separator + chooser.getFile());
    }
  }

  public static boolean mousePressed() {
    synchronized (mouseLock) {
      return mousePressed;
    }
  }

  public static double mouseX() {
    synchronized (mouseLock) {
      return mouseX;
    }
  }

  public static double mouseY() {
    synchronized (mouseLock) {
      return mouseY;
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    synchronized (mouseLock) {
      mouseX = userX(e.getX());

      mouseY = userY(e.getY());

      mousePressed = true;
    }
  }

  public void mouseReleased(MouseEvent e) {
    synchronized (mouseLock) {
      mousePressed = false;
    }
  }

  public void mouseDragged(MouseEvent e) {
    synchronized (mouseLock) {
      mouseX = userX(e.getX());

      mouseY = userY(e.getY());
    }
  }

  public void mouseMoved(MouseEvent e) {
    synchronized (mouseLock) {
      mouseX = userX(e.getX());

      mouseY = userY(e.getY());
    }
  }

  public static boolean hasNextKeyTyped() {
    synchronized (keyLock) {
      return !keysTyped.isEmpty();
    }
  }

  public static char nextKeyTyped() {
    synchronized (keyLock) {
      return ((Character) keysTyped.removeLast()).charValue();
    }
  }

  public void keyTyped(KeyEvent e) {
    synchronized (keyLock) {
      keysTyped.addFirst(Character.valueOf(e.getKeyChar()));
    }
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }
}