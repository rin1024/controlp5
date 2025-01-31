package controlP5.controller.color;

/**
 * controlP5 is a processing gui library.
 *
 * <p>2006-2015 by Andreas Schlegel
 *
 * <p>This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * @author Andreas Schlegel (http://www.sojamo.de)
 * @modified ##date##
 * @version ##version##
 */
import controlP5.*;
import controlP5.app.ControlP5;

class ColorPalette extends ControlGroup<ColorPalette> {

  protected ColorPalette(
      ControlP5 theControlP5,
      ControllerGroup<?> theParent,
      String theName,
      int theX,
      int theY,
      int theWidth,
      int theHeight) {
    super(theControlP5, theParent, theName, theX, theY, theWidth, theHeight);
  }
}
