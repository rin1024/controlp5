package controlP5;

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
import controlP5.controller.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used for automated controller creation using annotations. Very much inspired by Karsten Schmidt's
 * (toxi) cp5magic
 *
 * @example use/ControlP5annotation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ControlElement {

  String[] properties() default {};

  String label() default "";

  int x() default -1;

  int y() default -1;
}
