package net.gcdc.plugtestcms4.ping;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class PingStatusRenderer extends JLabel
implements TableCellRenderer
{

  Border unselectedBorder = null;
  Border selectedBorder = null;
  boolean isBordered = true;

  public PingStatusRenderer (boolean isBordered)
  {
    this.isBordered = isBordered;
    setOpaque (true);
  }

  public Component getTableCellRendererComponent (
    JTable table, Object pingStatus,
    boolean isSelected, boolean hasFocus,
    int row, int column)
  {
    final Color newColor;
    if (pingStatus == null || ! (pingStatus instanceof PingStatus))
      newColor = Color.ORANGE;
    else
      switch ((PingStatus) pingStatus)
      {
        case UNKNOWN:
          newColor = Color.ORANGE;
          break;
        case READY:
          newColor = Color.GREEN;
          break;
        case NOT_READY:
          newColor = Color.RED;
          break;
        default:
          newColor = Color.ORANGE;
          break;
      }
    setBackground (newColor);
    if (this.isBordered)
    {
      if (isSelected)
      {
        if (this.selectedBorder == null)
        {
          this.selectedBorder = BorderFactory.createMatteBorder (2, 5, 2, 5, table.getSelectionBackground ());
        }
        setBorder (this.selectedBorder);
      }
      else
      {
        if (this.unselectedBorder == null)
        {
          this.unselectedBorder = BorderFactory.createMatteBorder (2, 5, 2, 5, table.getBackground ());
        }
        setBorder (this.unselectedBorder);
      }
    }
    return this;
  }

}
