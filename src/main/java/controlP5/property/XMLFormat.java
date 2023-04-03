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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import processing.core.PApplet;

/** */
public class XMLFormat implements PropertiesStorageFormat {
  protected static final Logger L = Logger.getLogger(XMLFormat.class.getName());

  private final ControlP5 controlP5;

  public XMLFormat(ControlP5 _controlP5) {
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
    System.out.println(
        "Dont use the XMLFormat yet, it is not fully implemented with 0.5.9, use SERIALIZED instead.");
    System.out.println("Compiling with XMLFormat");
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<properties name=\"" + thePropertiesPath + "\">\n");
    for (ControllerProperty cp : theProperties) {
      if (cp.isActive()) {
        updatePropertyValue(cp);
        xml.append(getXML(cp));
      }
    }
    xml.append("</properties>");
    controlP5.getApp().saveStrings(thePropertiesPath, PApplet.split(xml.toString(), "\n"));
    System.out.println("saving xml, " + thePropertiesPath);
  }

  public String getExtension() {
    return "xml";
  }

  public boolean load(String thePropertiesPath) {
    String s;
    try {
      s = PApplet.join(controlP5.getApp().loadStrings(thePropertiesPath), "\n");
    } catch (Exception e) {
      L.warn(thePropertiesPath + ", file not found.");
      return false;
    }
    System.out.println("loading xml \n" + s);
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(s));
      Document doc = db.parse(is);
      doc.getDocumentElement().normalize();
      NodeList nodeLst = doc.getElementsByTagName("property");
      for (int i = 0; i < nodeLst.getLength(); i++) {
        Node node = nodeLst.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element fstElmnt = (Element) node;
          String myAddress = getElement(fstElmnt, "address");
          String mySetter = getElement(fstElmnt, "setter");
          String myType = getElement(fstElmnt, "type");
          String myValue = getElement(fstElmnt, "value");
          // String myClass = getElement(fstElmnt, "class");
          // String myGetter = getElement(fstElmnt, "getter");
          try {
            System.out.print("setting controller " + myAddress + "   ");
            ControllerInterface<?> ci = controlP5.getController(myAddress);
            ci = (ci == null) ? controlP5.getGroup(myAddress) : ci;
            System.out.println(ci);
            Method method;
            try {
              Class<?> c = getClass(myType);
              System.out.println(myType + " / " + c);
              method = ci.getClass().getMethod(mySetter, new Class[] {c});
              method.setAccessible(true);
              method.invoke(ci, new Object[] {getValue(myValue, myType, c)});
            } catch (Exception e) {
              L.error(e.toString());
            }
          } catch (Exception e) {
            L.warn("skipping a property, " + e);
          }
        }
      }
    } catch (SAXException e) {
      L.warn("SAXException, " + e);
      return false;
    } catch (IOException e) {
      L.warn("IOException, " + e);
      return false;
    } catch (ParserConfigurationException e) {
      L.warn("ParserConfigurationException, " + e);
      return false;
    }
    return true;
  }

  private Object getValue(String theValue, String theType, Class<?> theClass) {
    if (theClass == int.class) {
      return Integer.parseInt(theValue);
    } else if (theClass == float.class) {
      return Float.parseFloat(theValue);
    } else if (theClass == boolean.class) {
      return Boolean.parseBoolean(theValue);
    } else if (theClass.isArray()) {
      System.out.println("this is an array: " + theType + ", " + theValue + ", " + theClass);
      int dim = 0;
      while (true) {
        if (theType.charAt(dim) != '[' || dim >= theType.length()) {
          break;
        }
        dim++;
      }
    } else {
      System.out.println("is array? " + theClass.isArray());
    }
    return theValue;
  }

  private Class<?> getClass(String theType) {
    if (theType.equals("int")) {
      return int.class;
    } else if (theType.equals("float")) {
      return float.class;
    } else if (theType.equals("String")) {
      return String.class;
    }
    try {
      return Class.forName(theType);
    } catch (ClassNotFoundException e) {
      L.warn("ClassNotFoundException, " + e);
    }
    return null;
  }

  private String getElement(Element theElement, String theName) {
    NodeList fstNmElmntLst = theElement.getElementsByTagName(theName);
    Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
    NodeList fstNm = fstNmElmnt.getChildNodes();
    return ((Node) fstNm.item(0)).getNodeValue();
  }

  public String getXML(ControllerProperty theProperty) {
    // Mapping Between JSON and Java Entities
    // http://code.google.com/p/json-simple/wiki/MappingBetweenJSONAndJavaEntities
    String s = "\t<property>\n";
    s += "\t\t<address>" + theProperty.getAddress() + "</address>\n";
    s += "\t\t<class>" + CP.formatGetClass(theProperty.getController().getClass()) + "</class>\n";
    s += "\t\t<setter>" + theProperty.getSetter() + "</setter>\n";
    s += "\t\t<getter>" + theProperty.getGetter() + "</getter>\n";
    s += "\t\t<type>" + CP.formatGetClass(theProperty.getType()) + "</type>\n";
    s +=
        "\t\t<value>"
            + cdata(ControllerProperties.OPEN, theProperty.getValue().getClass())
            + (theProperty.getValue().getClass().isArray()
                ? CP.arrayToString(theProperty.getValue())
                : theProperty.getValue())
            + cdata(ControllerProperties.CLOSE, theProperty.getValue().getClass())
            + "</value>\n";
    s += "\t</property>\n";
    return s;
  }

  private String cdata(int a, Class<?> c) {
    if (c == String.class || c.isArray()) {
      return (a == ControllerProperties.OPEN ? "<![CDATA[" : "]]>");
    }
    return "";
  }
}
