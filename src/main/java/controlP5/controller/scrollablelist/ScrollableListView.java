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
import java.util.Map;
import org.apache.log4j.Logger;
import processing.core.PApplet;
import processing.core.PGraphics;

/** */
public class ScrollableListView implements ControllerView<ScrollableList> {
  protected static final Logger L = Logger.getLogger(ControllerView.class.getName());

  /**
   * @param g PGraphics
   * @param c ScrollableList
   */
  public void display(PGraphics g, ScrollableList c) {

    // setHeight( -200 ); /* UP */

    g.noStroke();

    if (c.isBarVisible()) {
      boolean b = c.itemHover == -1 && c.isInside() && !c.isDragged();
      g.fill(b ? c.getColor().getForeground() : c.getColor().getBackground());
      g.rect(0, 0, c.getWidth(), c.barHeight);

      g.pushMatrix();
      g.translate(c.getWidth() - 8, c.barHeight / 2 - 2);

      // キャプション部分
      g.fill(c.getColor().getCaptionLabel());
      if (c.isOpen()) {
        g.triangle(-3, 0, 3, 0, 0, 3);
      } else {
        g.triangle(-3, 3, 3, 3, 0, 0);
      }
      g.popMatrix();

      c.getCaptionLabel().draw(g, 4, c.barHeight / 2);
    }

    // リスト表示
    if (c.isOpen()) {
      int bar = (c.isBarVisible() ? c.barHeight : 0);
      int h = ((c.updateHeight()));

      // 箱の色
      g.pushMatrix();
      // g.translate( 0 , - ( h + bar +
      // c.getItemSpacing() ) ); /* UP */
      g.fill(c.getBackgroundColor());
      g.rect(0, bar, c.getWidth(), h);

      // 中身
      g.pushMatrix();
      g.translate(0, (bar == 0 ? 0 : (c.barHeight + c.getItemSpacing())));
      /* draw visible items */
      c.updateItemIndexOffset();
      int m0 = c.getItemIndexOffset();
      int m1 =
          c.items.size() > c.itemRange ? (c.getItemIndexOffset() + c.itemRange) : c.items.size();

      for (int i = m0; i < m1; i++) {
        Map<String, Object> item = c.items.get(i);
        CColor color = (CColor) item.get("color");
        g.fill(
            (b(item.get("state")))
                ? color.getActive()
                : (i == c.itemHover)
                    ? (c.isMousePressed() ? color.getActive() : color.getForeground())
                    : color.getBackground());
        g.rect(0, 0, c.getWidth(), c.itemHeight - 1);
        c.getValueLabel().set(item.get("text").toString()).draw(g, 4, c.itemHeight / 2);
        g.translate(0, c.itemHeight);
      }

      g.popMatrix();

      if (c.isInside()) {
        int m = c.items.size() - c.itemRange;
        if (m > 0) {
          g.fill(c.getColor().getCaptionLabel());
          g.pushMatrix();
          int s = 4; /* spacing */
          int s2 = s / 2;
          g.translate(c.getWidth() - s, c.barHeight);
          int len = (int) PApplet.map((float) Math.log(m * 10), 0, 10, h, 0);
          int pos = (int) (PApplet.map(c.getItemIndexOffset(), 0, m, s2, h - len - s2));
          g.rect(0, pos, s2, len);
          g.popMatrix();
        }
      }
      g.popMatrix();
    }
  }
}
