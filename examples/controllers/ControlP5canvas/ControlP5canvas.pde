/**
 * ControlP5 Canvas
 * The ControlWindowCanvas allow you to add custom graphics to 
 * the default controlP5 renderer or a controlWindow rednerer.
 *
 * find a list of public methods available for the Canvas Controller 
 * at the bottom of this sketch's source code
 *
 * by Andreas Schlegel, 2011
 * www.sojamo.de/libraries/controlp5
 * 
 */

import controlP5.app.ControlP5;
import controlP5.ControlEvent;
import controlP5.ControlWindow;
import controlP5.Canvas;
import org.apache.log4j.Logger;

ControlP5 cp5;

ControlWindow controlWindow;

Canvas cc;

void setup() {
  size(400, 400);
  frameRate(30);
  cp5 = new ControlP5(this);

  // create a control window canvas and add it to
  // the previously created control window.  
  cc = new MyCanvas();
  cc.pre(); // use cc.post(); to draw on top of existing controllers.
  cp5.addCanvas(cc); // add the canvas to cp5
}

void draw() {
  background(0);
  fill(60);
  rect(100, 100, 200, 200);
}


/*
a list of all methods available for the Canvas Controller
use ControlP5.printPublicMethodsFor(Canvas.class);
to print the following list into the console.

You can find further details about class Canvas in the javadoc.

Format:
ClassName : returnType methodName(parameter type)


controlP5.Canvas : void moveTo(ControlWindow) 
controlP5.Canvas : void setup(PGraphics) 
controlP5.Canvas : void update(PApplet) 
java.lang.Object : String toString() 
java.lang.Object : boolean equals(Object) 

created: 2015/03/24 12:20:53

*/
