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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Values of controllers can be stored inside properties files which can be saved to file or memory.
 *
 * @example controllers/ControlP5properties
 */
public class ControllerProperties {

  public static final int OPEN = 0;
  public static final int CLOSE = 1;
  public static String defaultName = "controlP5";

  PropertiesStorageFormat format;

  /**
   * all ControllerProperties will be stored inside Map allProperties. ControllerProperties need to
   * be unique or will otherwise be overwritten.
   *
   * <p>A hashSet containing names of PropertiesSets is assigned to each ControllerProperty.
   * HashSets are used instead of ArrayList to only allow unique elements.
   */
  private Map<ControllerProperty, HashSet<String>> allProperties;

  /** A set of unique property-set names. */
  private Set<String> allSets;

  private final ControlP5 controlP5;
  private String _myDefaultSetName = "default";
  protected static final Logger L = Logger.getLogger(ControllerProperties.class.getName());
  private Map<String, Set<ControllerProperty>> _mySnapshots;

  public ControllerProperties(ControlP5 theControlP5) {
    controlP5 = theControlP5;
    // setFormat( new SerializedFormat(controlP5) );
    setFormat(new JSONFormat(controlP5));
    allProperties = new HashMap<ControllerProperty, HashSet<String>>();
    allSets = new HashSet<String>();
    addSet(_myDefaultSetName);
    _mySnapshots = new LinkedHashMap<String, Set<ControllerProperty>>();
  }

  public Map<ControllerProperty, HashSet<String>> get() {
    return allProperties;
  }

  /**
   * adds a property based on names of setter and getter methods of a controller.
   *
   * @param thePropertySetter
   * @param thePropertyGetter
   */
  public ControllerProperty register(
      ControllerInterface<?> theController, String thePropertySetter, String thePropertyGetter) {
    ControllerProperty p =
        new ControllerProperty(theController, thePropertySetter, thePropertyGetter);
    if (!allProperties.containsKey(p)) {
      // register a new property with the main properties container
      allProperties.put(p, new HashSet<String>());
      // register the property wit the default properties set
      allProperties.get(p).add(_myDefaultSetName);
    }
    return p;
  }

  /**
   * registering a property with only one parameter assumes that there is a setter and getter
   * function present for the Controller. register("value") for example would create a property
   * reference to setValue and getValue. Notice that the first letter of value is being capitalized.
   *
   * @param theProperty
   * @return
   */
  public ControllerProperties register(ControllerInterface<?> theController, String theProperty) {
    theProperty = Character.toUpperCase(theProperty.charAt(0)) + theProperty.substring(1);
    register(theController, "set" + theProperty, "get" + theProperty);
    return this;
  }

  public ControllerProperties remove(
      ControllerInterface<?> theController, String theSetter, String theGetter) {
    ControllerProperty cp = new ControllerProperty(theController, theSetter, theGetter);
    allProperties.remove(cp);
    return this;
  }

  public ControllerProperties remove(ControllerInterface<?> theController) {
    ArrayList<ControllerProperty> list = new ArrayList<ControllerProperty>(allProperties.keySet());
    for (ControllerProperty cp : list) {
      if (cp.getController().equals(theController)) {
        allProperties.remove(cp);
      }
    }
    return this;
  }

  public ControllerProperties remove(ControllerInterface<?> theController, String theProperty) {
    return remove(theController, "set" + theProperty, "get" + theProperty);
  }

  public List<ControllerProperty> get(ControllerInterface<?> theController) {
    List<ControllerProperty> props = new ArrayList<ControllerProperty>();
    List<ControllerProperty> list = new ArrayList<ControllerProperty>(allProperties.keySet());
    for (ControllerProperty cp : list) {
      if (cp.getController().equals(theController)) {
        props.add(cp);
      }
    }
    return props;
  }

  public ControllerProperty getProperty(
      ControllerInterface<?> theController, String theSetter, String theGetter) {
    ControllerProperty cp = new ControllerProperty(theController, theSetter, theGetter);
    Iterator<ControllerProperty> iter = allProperties.keySet().iterator();
    while (iter.hasNext()) {
      ControllerProperty p = iter.next();
      if (p.equals(cp)) {
        return p;
      }
    }
    // in case the property has not been registered before, it will be
    // registered here automatically - you don't need to call
    // Controller.registerProperty() but can use Controller.getProperty()
    // instead.
    return register(theController, theSetter, theGetter);
  }

  public ControllerProperty getProperty(ControllerInterface<?> theController, String theProperty) {
    theProperty = Character.toUpperCase(theProperty.charAt(0)) + theProperty.substring(1);
    return getProperty(theController, "set" + theProperty, "get" + theProperty);
  }

  public HashSet<ControllerProperty> getPropertySet(ControllerInterface<?> theController) {
    HashSet<ControllerProperty> set = new HashSet<ControllerProperty>();
    Iterator<ControllerProperty> iter = allProperties.keySet().iterator();
    while (iter.hasNext()) {
      ControllerProperty p = iter.next();
      if (p.getController().equals(theController)) {
        set.add(p);
      }
    }
    return set;
  }

  public ControllerProperties addSet(String theSet) {
    allSets.add(theSet);
    return this;
  }

  /** Moves a ControllerProperty from one set to another. */
  public ControllerProperties move(ControllerProperty theProperty, String fromSet, String toSet) {
    if (!exists(theProperty)) {
      return this;
    }
    if (allProperties.containsKey(theProperty)) {
      if (allProperties.get(theProperty).contains(fromSet)) {
        allProperties.get(theProperty).remove(fromSet);
      }
      addSet(toSet);
      allProperties.get(theProperty).add(toSet);
    }
    return this;
  }

  public ControllerProperties move(
      ControllerInterface<?> theController, String fromSet, String toSet) {
    HashSet<ControllerProperty> set = getPropertySet(theController);
    for (ControllerProperty cp : set) {
      move(cp, fromSet, toSet);
    }
    return this;
  }

  /** copies a ControllerProperty from one set to other(s); */
  public ControllerProperties copy(ControllerProperty theProperty, String... theSet) {
    if (!exists(theProperty)) {
      return this;
    }
    for (String s : theSet) {
      allProperties.get(theProperty).add(s);
      if (!allSets.contains(s)) {
        addSet(s);
      }
    }
    return this;
  }

  public ControllerProperties copy(ControllerInterface<?> theController, String... theSet) {
    HashSet<ControllerProperty> set = getPropertySet(theController);
    for (ControllerProperty cp : set) {
      copy(cp, theSet);
    }
    return this;
  }

  /** removes a ControllerProperty from one or multiple sets. */
  public ControllerProperties remove(ControllerProperty theProperty, String... theSet) {
    if (!exists(theProperty)) {
      return this;
    }
    for (String s : theSet) {
      allProperties.get(theProperty).remove(s);
    }
    return this;
  }

  public ControllerProperties remove(ControllerInterface<?> theController, String... theSet) {
    HashSet<ControllerProperty> set = getPropertySet(theController);
    for (ControllerProperty cp : set) {
      remove(cp, theSet);
    }
    return this;
  }

  /** stores a ControllerProperty in one particular set only. */
  public ControllerProperties only(ControllerProperty theProperty, String theSet) {
    // clear all the set-references for a particular property
    allProperties.get(theProperty).clear();
    // add theSet to the empty collection of sets for this particular
    // property
    allProperties.get(theProperty).add(theSet);
    return this;
  }

  ControllerProperties only(ControllerInterface<?> theController, String... theSet) {
    return this;
  }

  private boolean exists(ControllerProperty theProperty) {
    return allProperties.containsKey(theProperty);
  }

  public ControllerProperties print() {
    for (Entry<ControllerProperty, HashSet<String>> entry : allProperties.entrySet()) {
      System.out.println(entry.getKey() + "\t" + entry.getValue());
    }
    return this;
  }

  /** deletes a ControllerProperty from all Sets including the default set. */
  public ControllerProperties delete(ControllerProperty theProperty) {
    if (!exists(theProperty)) {
      return this;
    }
    allProperties.remove(theProperty);
    return this;
  }

  /**
   * logs all registered properties in memory. Here, clones of properties are stored inside a map
   * and can be accessed by key using the getLog method.
   *
   * @see controlP5.ControllerProperties#getSnapshot(String)
   * @param theKey
   * @return ControllerProperties
   */
  public ControllerProperties setSnapshot(String theKey) {
    Set<ControllerProperty> l = new HashSet<ControllerProperty>();
    for (ControllerProperty cp : allProperties.keySet()) {
      format.updatePropertyValue(cp);
      try {
        l.add((ControllerProperty) cp.clone());
      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
      }
    }
    _mySnapshots.put(theKey, l);
    return this;
  }

  /**
   * convenience method, setSnapshot(String) also works here since it will override existing log
   * with the same key.
   */
  public ControllerProperties updateSnapshot(String theKey) {
    return setSnapshot(theKey);
  }

  /** removes a snapshot by key. */
  public ControllerProperties removeSnapshot(String theKey) {
    _mySnapshots.remove(theKey);
    return this;
  }

  ControllerProperties setSnapshot(String theKey, String... theSets) {
    return this;
  }

  /** saves a snapshot into your sketch's sketch folder. */
  public ControllerProperties saveSnapshot(String theKey) {
    saveSnapshotAs(controlP5.getApp().sketchPath(theKey), theKey);
    return this;
  }

  /** saves a snapshot to the file with path given by the first parameter (thePropertiesPath). */
  public ControllerProperties saveSnapshotAs(String thePropertiesPath, String theKey) {
    Set<ControllerProperty> log = _mySnapshots.get(theKey);
    if (log == null) {
      return this;
    }
    thePropertiesPath =
        getPathWithExtension(format, controlP5.checkPropertiesPath(thePropertiesPath));

    format.compile(log, thePropertiesPath);

    return this;
  }

  private String getPathWithExtension(PropertiesStorageFormat theFormat, String thePropertiesPath) {
    return (thePropertiesPath.endsWith("." + theFormat.getExtension()))
        ? thePropertiesPath
        : thePropertiesPath + "." + theFormat.getExtension();
  }

  /**
   * restores properties previously stored as snapshot in memory.
   *
   * @see controlP5.ControllerProperties#setSnapshot(String)
   */
  public ControllerProperties getSnapshot(String theKey) {
    Set<ControllerProperty> l = _mySnapshots.get(theKey);
    if (l != null) {
      for (ControllerProperty cp : l) {
        ControllerInterface<?> ci = controlP5.getController(cp.getAddress());
        ci = (ci == null) ? controlP5.getGroup(cp.getAddress()) : ci;
        ControlP5.invoke((Controller) ci, cp.getSetter(), cp.getValue());
      }
    }
    return this;
  }

  /**
   * properties stored in memory can be accessed by index, getSnapshotIndices() returns the index of
   * the snapshot list.
   */
  public ArrayList<String> getSnapshotIndices() {
    return new ArrayList<String>(_mySnapshots.keySet());
  }

  /** load properties from the default properties file 'controlP5.properties' */
  public boolean load() {
    return load(controlP5.getApp().sketchPath(defaultName + "." + format.getExtension()));
  }

  public boolean load(String thePropertiesPath) {
    return format.load(
        getPathWithExtension(format, controlP5.checkPropertiesPath(thePropertiesPath)));
  }

  public PropertiesStorageFormat getFormat() {
    return format;
  }

  /**
   * use ControllerProperties.SERIALIZED, ControllerProperties.XML or ControllerProperties.JSON as
   * parameter.
   */
  public void setFormat(PropertiesStorageFormat theFormat) {
    format = theFormat;
  }

  public void setFormat(String theFormat) {
    if (theFormat.equals(ControlP5.JSON)) {
      setFormat(new JSONFormat(controlP5));
    } else if (theFormat.equals(ControlP5.SERIALIZED)) {
      setFormat(new SerializedFormat(controlP5));
    } else {
      System.out.println("sorry format " + theFormat + " does not exist.");
    }
  }

  /**
   * saves all registered properties into the default 'controlP5.properties' file into your sketch
   * folder.
   */
  public boolean save() {
    System.out.println(
        "save properties using format "
            + format
            + " ("
            + format.getExtension()
            + ") "
            + controlP5.getApp().sketchPath(defaultName));
    format.compile(
        allProperties.keySet(),
        getPathWithExtension(format, controlP5.getApp().sketchPath(defaultName)));
    return true;
  }

  /** saves all registered properties into a file specified by parameter thePropertiesPath. */
  public boolean saveAs(final String thePropertiesPath) {
    format.compile(
        allProperties.keySet(),
        getPathWithExtension(format, controlP5.checkPropertiesPath(thePropertiesPath)));
    return true;
  }

  /** saves a list of properties sets into a file specified by parameter thePropertiesPath. */
  public boolean saveAs(String thePropertiesPath, String... theSets) {
    thePropertiesPath = controlP5.checkPropertiesPath(thePropertiesPath);
    HashSet<ControllerProperty> sets = new HashSet<ControllerProperty>();
    Iterator<ControllerProperty> iter = allProperties.keySet().iterator();
    while (iter.hasNext()) {
      ControllerProperty p = iter.next();
      if (allProperties.containsKey(p)) {
        HashSet<String> set = allProperties.get(p);
        for (String str : set) {
          for (String s : theSets) {
            if (str.equals(s)) {
              sets.add(p);
            }
          }
        }
      }
    }
    format.compile(sets, getPathWithExtension(format, thePropertiesPath));
    return true;
  }

  /** @exclude {@inheritDoc} */
  public String toString() {
    String s = "";
    s += this.getClass().getName() + "\n";
    s += "total num of properties:\t" + allProperties.size() + "\n";
    for (ControllerProperty c : allProperties.keySet()) {
      s += "\t" + c + "\n";
    }
    s += "total num of sets:\t\t" + allSets.size() + "\n";
    for (String set : allSets) {
      s += "\t" + set + "\n";
    }
    return s;
  }
}
