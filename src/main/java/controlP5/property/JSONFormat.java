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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import processing.data.JSONArray;
import processing.data.JSONObject;

/** */
public class JSONFormat implements PropertiesStorageFormat {
  protected static final Logger L = Logger.getLogger(JSONFormat.class.getName());

  private final ControlP5 controlP5;

  public JSONFormat(ControlP5 _controlP5) {
    controlP5 = _controlP5;
  }

  private boolean checkSerializable(Object theProperty) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(out);
      stream.writeObject(theProperty);
      stream.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean updatePropertyValue(ControllerProperty theProperty) {
    Method method;
    try {
      method = theProperty.getController().getClass().getMethod(theProperty.getGetter());
      Object value = method.invoke(theProperty.getController());
      theProperty.setType(method.getReturnType());
      theProperty.setValue(value);
      if (checkSerializable(value)) {
        return true;
      }
    } catch (Exception e) {
      L.error("" + e);
    }
    return false;
  }

  public void compile(Set<ControllerProperty> theProperties, String thePropertiesPath) {
    long t = System.currentTimeMillis();
    JSONObject json = new JSONObject();
    for (ControllerProperty cp : theProperties) {

      if (cp.isActive()) {
        if (updatePropertyValue(cp)) {
          cp.setId(cp.getController().getId());

          if (!json.keys().contains(cp.getAddress())) {
            json.setJSONObject(cp.getAddress(), new JSONObject());
          }
          JSONObject item = json.getJSONObject(cp.getAddress());
          String key = cp.getSetter();
          key = Character.toLowerCase(key.charAt(3)) + key.substring(4);
          if (cp.getValue() instanceof Number) {
            if (cp.getValue() instanceof Integer) {
              item.setInt(key, ControlP5.i(cp.getValue()));
            } else if (cp.getValue() instanceof Float) {
              item.setFloat(key, ControlP5.f(cp.getValue()));
            } else if (cp.getValue() instanceof Double) {
              item.setDouble(key, ControlP5.d(cp.getValue()));
            }
          } else if (cp.getValue() instanceof Boolean) {
            item.setBoolean(key, ControlP5.b(cp.getValue()));
          } else {

            if (cp.getValue().getClass().isArray()) {
              JSONArray arr = new JSONArray();
              if (cp.getValue() instanceof int[]) {
                for (Object o : (int[]) cp.getValue()) {
                  arr.append(ControlP5.i(o));
                }
              } else if (cp.getValue() instanceof float[]) {
                for (Object o : (float[]) cp.getValue()) {
                  arr.append(ControlP5.f(o));
                }
              }
              item.setJSONArray(key, arr);
            } else {
              item.setString(key, cp.getValue().toString());
            }
          }
        }
      }
    }
    json.save(new File(getPathWithExtension(this, thePropertiesPath)), "");
  }

  private String getPathWithExtension(PropertiesStorageFormat theFormat, String thePropertiesPath) {
    return (thePropertiesPath.endsWith("." + theFormat.getExtension()))
        ? thePropertiesPath
        : thePropertiesPath + "." + theFormat.getExtension();
  }

  public String getExtension() {
    return "json";
  }

  public boolean load(String thePropertiesPath) {
    JSONReader reader = new JSONReader(controlP5.getApp());
    Map<?, ?> entries = ControlP5.toMap(reader.parse(thePropertiesPath));
    for (Map.Entry entry : entries.entrySet()) {
      String name = entry.getKey().toString();
      Controller c = controlP5.getController(name);
      Map<?, ?> values = ControlP5.toMap(entry.getValue());
      for (Map.Entry value : values.entrySet()) {
        String i0 = value.getKey().toString();
        String member = "set" + Character.toUpperCase(i0.charAt(0)) + i0.substring(1);
        Object i1 = value.getValue();
        if (i1 instanceof Number) {
          ControlP5.invoke(c, member, ControlP5.f(value.getValue()));
        } else if (i1 instanceof String) {
          ControlP5.invoke(c, member, ControlP5.s(value.getValue()));
        } else if (i1 instanceof float[]) {
          ControlP5.invoke(c, member, (float[]) i1);
        } else {
          if (i1 instanceof List) {
            List l = (List) i1;
            float[] arr = new float[l.size()];
            for (int i = 0; i < l.size(); i++) {
              arr[i] = ControlP5.f(l.get(i));
            }
            ControlP5.invoke(c, member, arr);
          } else {
            ControlP5.invoke(c, member, value.getValue());
          }
        }
      }
    }
    return false;
  }
}
