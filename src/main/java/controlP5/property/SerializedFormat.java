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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/** */
public class SerializedFormat implements PropertiesStorageFormat {
  protected static final Logger L = Logger.getLogger(SerializedFormat.class.getName());

  private final ControlP5 controlP5;

  public SerializedFormat(ControlP5 _controlP5) {
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

  public boolean load(String thePropertiesPath) {
    try {
      FileInputStream fis = new FileInputStream(thePropertiesPath);
      ObjectInputStream ois = new ObjectInputStream(fis);
      int size = ois.readInt();
      L.info("loading " + size + " property-items. ");

      for (int i = 0; i < size; i++) {
        try {
          ControllerProperty cp = (ControllerProperty) ois.readObject();
          ControllerInterface<?> ci = controlP5.getController(cp.getAddress());
          ci = (ci == null) ? controlP5.getGroup(cp.getAddress()) : ci;
          ci.setId(cp.getId());
          Method method;
          try {
            method = ci.getClass().getMethod(cp.getSetter(), new Class[] {cp.getType()});
            method.setAccessible(true);
            method.invoke(ci, new Object[] {cp.getValue()});
          } catch (Exception e) {
            L.error(e.toString());
          }

        } catch (Exception e) {
          L.warn("skipping a property, " + e);
        }
      }
      ois.close();
    } catch (Exception e) {
      L.warn("Exception during deserialization: " + e);
      return false;
    }
    return true;
  }

  public String getExtension() {
    return "ser";
  }

  public void compile(Set<ControllerProperty> theProperties, String thePropertiesPath) {
    int active = 0;
    int total = 0;
    HashSet<ControllerProperty> propertiesToBeSaved = new HashSet<ControllerProperty>();
    for (ControllerProperty cp : theProperties) {
      if (cp.isActive()) {
        if (updatePropertyValue(cp)) {
          active++;
          cp.setId(cp.getController().getId());
          propertiesToBeSaved.add(cp);
        }
      }
      total++;
    }

    int ignored = total - active;

    try {
      FileOutputStream fos = new FileOutputStream(thePropertiesPath);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      L.info("Saving property-items to " + thePropertiesPath);
      oos.writeInt(active);

      for (ControllerProperty cp : propertiesToBeSaved) {
        if (cp.isActive()) {
          oos.writeObject(cp);
        }
      }
      L.info(active + " items saved, " + (ignored) + " items ignored. Done saving properties.");
      oos.flush();
      oos.close();
      fos.close();
    } catch (Exception e) {
      L.warn("Exception during serialization: " + e);
    }
  }
}
