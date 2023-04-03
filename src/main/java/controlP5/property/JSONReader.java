package controlP5.property;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

/** */
public class JSONReader {

  private final PApplet papplet;

  public JSONReader(Object o) {
    if (o instanceof PApplet) {
      papplet = (PApplet) o;
    } else {
      papplet = null;
      System.out.println("Sorry, argument is not of instance PApplet");
    }
  }

  public Object parse(String s) {
    if (s.indexOf("{") >= 0) {
      return get(JSONObject.parse(s), new LinkedHashMap());
    } else {
      return get(papplet.loadJSONObject(s), new LinkedHashMap());
    }
  }

  Object get(Object o, Object m) {
    if (o instanceof JSONObject) {
      if (m instanceof Map) {
        Set set = ((JSONObject) o).keys();
        for (Object o1 : set) {
          Object o2 = ControlP5.invoke(o, "opt", o1.toString());
          if (o2 instanceof JSONObject) {
            Map m1 = new LinkedHashMap();
            ((Map) m).put(o1.toString(), m1);
            get(o2, m1);
          } else if (o2 instanceof JSONArray) {
            List l1 = new ArrayList();
            ((Map) m).put(o1.toString(), l1);
            get(o2, l1);
          } else {
            ((Map) m).put(o1.toString(), o2);
          }
        }
      }
    } else if (o instanceof JSONArray) {
      if (m instanceof List) {
        List l = ((List) m);
        int n = 0;
        Object o3 = ControlP5.invoke(o, "opt", n);
        while (o3 != null) {
          if (o3 instanceof JSONArray) {
            List l1 = new ArrayList();
            l.add(l1);
            get(o3, l1);
          } else if (o3 instanceof JSONObject) {
            Map l1 = new LinkedHashMap();
            l.add(l1);
            get(o3, l1);
          } else {
            l.add(o3);
          }
          o3 = ControlP5.invoke(o, "opt", ++n);
        }
      } else {
        System.err.println("JSONReader type mismatch.");
      }
    }
    return m;
  }
}
