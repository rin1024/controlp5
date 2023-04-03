// MyCanvas, your Canvas render class
class MyCanvas extends Canvas {

  int y;
  int mx = 0;
  int my = 0;
  public void setup(PGraphics pg) {
    y = 200;
  }  

  public void update(PApplet p) {
    mx = p.mouseX;
    my = p.mouseY;
  }

  public void draw(PGraphics pg) {
    // renders a square with randomly changing colors
    // make changes here.
    pg.fill(100);
    pg.rect(mx-20, my-20, 240, 30);
    pg.fill(255);
    pg.text("This text is drawn by MyCanvas", mx,my);
  }
}
