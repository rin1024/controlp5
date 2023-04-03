package controlP5.controller.scrollablelist;

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
import static controlP5.app.ControlP5.b;

import controlP5.*;
import controlP5.app.ControlP5;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;

/**
 * A ScrollableList is a list of vertically aligned items which can be scrolled if required.
 *
 * @example controllers/ControlP5scrollableList
 */
public class ScrollableList extends Controller<ScrollableList> implements ControlListener {
  protected static final Logger L = Logger.getLogger(ScrollableList.class.getName());

  private int _myType = DROPDOWN;
  protected int _myBackgroundColor = 0x00ffffff;
  protected int itemHeight = 13;
  protected int barHeight = 10;
  private float scrollSensitivity = 1;
  private boolean isOpen = true;
  protected List<Map<String, Object>> items;
  protected int itemRange = 5;
  protected int itemHover = -1;
  private int itemIndexOffset = 0;
  private int itemSpacing = 1;
  private int _myDirection = PApplet.DOWN;
  private boolean isBarVisible = true;
  public static final int LIST = ControlP5.LIST;
  public static final int DROPDOWN = ControlP5.DROPDOWN;
  public static final int CHECKBOX = ControlP5.CHECKBOX; /* TODO */
  public static final int TREE = ControlP5.TREE; /* TODO */

  public ScrollableList(ControlP5 theControlP5, String theName) {
    this(theControlP5, theControlP5.getDefaultTab(), theName, 0, 0, 99, 199);
    theControlP5.register(theControlP5.getApp(), theName, this);
  }

  /** TODO: should to be change to protected */
  public ScrollableList(
      ControlP5 theControlP5,
      ControllerGroup<?> theGroup,
      String theName,
      int theX,
      int theY,
      int theW,
      int theH) {
    super(theControlP5, theGroup, theName, theX, theY, theW, theH);
    items = new ArrayList<Map<String, Object>>();
    updateHeight();
    getValueLabel().align(PApplet.LEFT, PApplet.CENTER);
  }

  public boolean isOpen() {
    return isOpen;
  }

  public ScrollableList open() {
    return setOpen(true);
  }

  public ScrollableList close() {
    return setOpen(false);
  }

  public ScrollableList setOpen(boolean b) {
    isOpen = b;
    return this;
  }

  @Override
  public int getHeight() {
    return isOpen ? super.getHeight() : barHeight;
  }

  public ScrollableList setType(int theType) {
    _myType = theType;
    return this;
  }

  public void setDirection(int theDirection) {
    _myDirection = (theDirection == PApplet.UP) ? PApplet.UP : PApplet.DOWN;
  }

  @Override
  protected boolean inside() {
    /* constrain the bounds of the controller to the
     * dimensions of the cp5 area, required since
     * PGraphics as render area has been introduced. */
    float x0 = PApplet.max(0, x(position) + x(_myParent.getAbsolutePosition()));
    float x1 =
        PApplet.min(
            cp5.getGraphics().width, x(position) + x(_myParent.getAbsolutePosition()) + getWidth());
    float y0 = PApplet.max(0, y(position) + y(_myParent.getAbsolutePosition()));
    float y1 =
        PApplet.min(
            cp5.getGraphics().height,
            y(position) + y(_myParent.getAbsolutePosition()) + getHeight());
    if (y1 < y0) {
      float ty = y0;
      y0 = y1;
      y1 = ty;
    }
    return (_myControlWindow.getPointer().getX() > x0
        && _myControlWindow.getPointer().getX() < x1
        && _myControlWindow.getPointer().getY() > (y1 < y0 ? y1 : y0)
        && _myControlWindow.getPointer().getY() < (y0 < y1 ? y1 : y0));
  }

  @Override
  protected void onRelease() {
    if (!isDragged) {
      if (getPointer().y() >= 0 && getPointer().y() <= barHeight) {
        setOpen(!isOpen());
      } else if (isOpen) {

        double n = Math.floor((getPointer().y() - barHeight) / itemHeight);

        // n += itemRange; /* UP */
        int index = (int) n + itemIndexOffset;
        updateIndex(index);
      }
    }
  }

  private void updateIndex(int theIndex) {
    if (theIndex >= items.size()) {
      return;
    }

    Map m = items.get(theIndex);

    switch (_myType) {
      case (LIST):
        super.setValue(theIndex);
        for (Object o : items) {
          ((Map) o).put("state", false);
        }
        m.put("state", !ControlP5.b(m.get("state")));
        break;
      case (DROPDOWN):
        super.setValue(theIndex);
        setOpen(false);
        getCaptionLabel().setText((m.get("text").toString()));
        break;
      case (CHECKBOX):
        m.put("state", !ControlP5.b(m.get("state")));
        break;
    }
  }

  public ScrollableList setValue(float theValue) {
    updateIndex((int) (theValue));
    return this;
  }

  @Override
  protected void onDrag() {
    scroll(getPointer().dy());
  }

  @Override
  protected void onScroll(int theValue) {
    scroll(theValue);
  }

  private void scroll(int theValue) {
    if (isOpen) {
      itemIndexOffset += theValue;
      itemIndexOffset =
          (int) (Math.floor(Math.max(0, Math.min(itemIndexOffset, items.size() - itemRange))));
      itemHover = -2;
    }
  }

  @Override
  protected void onLeave() {
    itemHover = -1;
  }

  private void updateHover() {
    if (getPointer().y() > barHeight) {
      double n = Math.floor((getPointer().y() - barHeight) / itemHeight);
      itemHover = (int) (itemIndexOffset + n);
    } else {
      itemHover = -1;
    }
  }

  @Override
  protected void onEnter() {
    updateHover();
  }

  @Override
  protected void onMove() {
    updateHover();
  }

  @Override
  protected void onEndDrag() {
    updateHover();
  }

  /** TODO: should to be private */
  public int updateHeight() {
    itemRange = (PApplet.abs(getHeight()) - (isBarVisible() ? barHeight : 0)) / itemHeight;
    return itemHeight * (items.size() < itemRange ? items.size() : itemRange);
  }

  public ScrollableList setItemHeight(int theHeight) {
    itemHeight = theHeight;
    updateHeight();
    return this;
  }

  public ScrollableList setBarHeight(int theHeight) {
    barHeight = theHeight;
    updateHeight();
    return this;
  }

  public int getBarHeight() {
    return barHeight;
  }

  public ScrollableList setScrollSensitivity(float theSensitivity) {
    scrollSensitivity = theSensitivity;
    return this;
  }

  public ScrollableList setBarVisible(boolean b) {
    isBarVisible = b;
    updateHeight();
    return this;
  }

  public boolean isBarVisible() {
    return isBarVisible;
  }

  private Map<String, Object> getDefaultItemMap(String theName, Object theValue) {
    Map<String, Object> item = new HashMap<String, Object>();
    item.put("name", theName);
    item.put("text", theName);
    item.put("value", theValue);
    item.put("color", getColor());
    item.put(
        "view",
        new CDrawable() {
          @Override
          public void draw(PGraphics theGraphics) {}
        });
    item.put("state", false);
    return item;
  }

  public ScrollableList addItem(String theName, Object theValue) {
    Map<String, Object> item = getDefaultItemMap(theName, theValue);
    items.add(item);
    return this;
  }

  public ScrollableList addItems(String[] theItems) {
    addItems(Arrays.asList(theItems));
    return this;
  }

  public ScrollableList addItems(List<String> theItems) {
    for (int i = 0; i < theItems.size(); i++) {
      addItem(theItems.get(i).toString(), i);
    }
    return this;
  }

  public ScrollableList addItems(Map<String, Object> theItems) {
    for (Map.Entry<String, Object> item : theItems.entrySet()) {
      addItem(item.getKey(), item.getValue());
    }
    return this;
  }

  public ScrollableList setItems(String[] theItems) {
    setItems(Arrays.asList(theItems));
    return this;
  }

  public ScrollableList setItems(List<String> theItems) {
    items.clear();
    return addItems(theItems);
  }

  public ScrollableList setItems(Map<String, Object> theItems) {
    items.clear();
    return addItems(theItems);
  }

  public ScrollableList removeItems(List<String> theItems) {
    for (String s : theItems) {
      removeItem(s);
    }
    return this;
  }

  public ScrollableList removeItem(String theName) {
    if (theName != null) {

      List l = new ArrayList();
      for (Map m : items) {
        if (theName.equals(m.get("name"))) {
          l.add(m);
        }
      }
      items.removeAll(l);
    }
    return this;
  }

  public void updateItemIndexOffset() {
    int m1 = items.size() > itemRange ? (itemIndexOffset + itemRange) : items.size();
    int n = (m1 - items.size());
    if (n >= 0) {
      itemIndexOffset -= n;
    }
  }

  public Map<String, Object> getItem(int theIndex) {
    return items.get(theIndex);
  }

  public Map<String, Object> getItem(String theName) {
    if (theName != null) {
      for (Map<String, Object> o : items) {
        if (theName.equals(o.get("name"))) {
          return o;
        }
      }
    }
    return Collections.EMPTY_MAP;
  }

  public List getItems() {
    return Collections.unmodifiableList(items);
  }

  public ScrollableList clear() {
    for (int i = items.size() - 1; i >= 0; i--) {
      items.remove(i);
    }
    items.clear();
    itemIndexOffset = 0;
    return this;
  }

  @Override
  public void controlEvent(ControlEvent theEvent) {
    // TODO Auto-generated method stub
  }

  public ScrollableList setBackgroundColor(int theColor) {
    _myBackgroundColor = theColor;
    return this;
  }

  public int getBackgroundColor() {
    return _myBackgroundColor;
  }

  @Override
  public ScrollableList updateDisplayMode(int theMode) {
    _myDisplayMode = theMode;
    switch (theMode) {
      case (DEFAULT):
        _myControllerView = new ScrollableListView();
        break;
      case (IMAGE):
      case (SPRITE):
      case (CUSTOM):
      default:
        break;
    }
    return this;
  }

  public void keyEvent(KeyEvent theKeyEvent) {
    if (isInside && theKeyEvent.getAction() == KeyEvent.PRESS) {
      switch (theKeyEvent.getKeyCode()) {
        case (ControlP5.UP):
          scroll(theKeyEvent.isAltDown() ? -itemIndexOffset : theKeyEvent.isShiftDown() ? -10 : -1);
          updateHover();
          break;
        case (ControlP5.DOWN):
          scroll(
              theKeyEvent.isAltDown()
                  ? items.size() - itemRange
                  : theKeyEvent.isShiftDown() ? 10 : 1);
          updateHover();
          break;
        case (ControlP5.LEFT):
          break;
        case (ControlP5.RIGHT):
          break;
        case (ControlP5.ENTER):
          onRelease();
          break;
      }
    }
  }
  /* TODO keycontrol: arrows, return dragging moving items
   * sorting custom view custom event types */

  public int getItemSpacing() {
    return itemSpacing;
  }

  public int getItemIndexOffset() {
    return itemIndexOffset;
  }
}
