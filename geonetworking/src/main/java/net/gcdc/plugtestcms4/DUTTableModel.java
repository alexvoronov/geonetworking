package net.gcdc.plugtestcms4;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.gcdc.plugtestcms4.ping.PingStatus;

public class DUTTableModel
extends AbstractTableModel
implements TableModel
{

  private final List<DUT> duts;

  public DUTTableModel (List<DUT> duts)
  {
    super ();
    this.duts = duts;
  }

  @Override
  public int getColumnCount ()
  {
    return 10;
  }

  @Override
  public Class<?> getColumnClass (int columnIndex)
  {
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    if (columnIndex == 0)
      return Boolean.class;
    if (columnIndex == 1)
      return String.class;
    if (columnIndex == 2)
      return String.class;
    if (columnIndex == 3)
      return String.class;
    if (columnIndex == 4)
      return String.class;
    if (columnIndex == 5)
      return Boolean.class;
    if (columnIndex == 6)
      return Integer.class;
    if (columnIndex == 7)
      return Boolean.class;
    if (columnIndex == 8)
      return Integer.class;
    if (columnIndex == 9)
      return PingStatus.class;
    throw new RuntimeException ();
  }

  @Override
  public String getColumnName (int columnIndex)
  {
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    if (columnIndex == 0)
      return "Act";
    if (columnIndex == 1)
      return "Name";
    if (columnIndex == 2)
      return "Pwd";
    if (columnIndex == 3)
      return "Eth";
    if (columnIndex == 4)
      return "WLAN";
    if (columnIndex == 5)
      return "Rx";
    if (columnIndex == 6)
      return "RxPort";
    if (columnIndex == 7)
      return "Tx";
    if (columnIndex == 8)
      return "TxPort";
    if (columnIndex == 9)
      return "Reach";
    throw new RuntimeException ();
  }

  @Override
  public int getRowCount ()
  {
    return (this.duts == null) ? 0 : this.duts.size ();
  }

  @Override
  public Object	getValueAt (int rowIndex, int columnIndex)
  {
    if (rowIndex < 0 || rowIndex >= getRowCount ())
      return null;
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    final DUT dut = this.duts.get (rowIndex);
    if (dut == null)
      return null;
    if (columnIndex == 0)
      return dut.getActive ();
    if (columnIndex == 1)
      return dut.getName ();
    if (columnIndex == 2)
      return dut.getRootPassword ();
    if (columnIndex == 3)
      return dut.getIpv4AddressEth ();
    if (columnIndex == 4)
      return dut.getIpv4AddressWLAN ();
    if (columnIndex == 5)
      return dut.getReceives ();
    if (columnIndex == 6)
      return dut.getRxPort ();
    if (columnIndex == 7)
      return dut.getTransmits ();
    if (columnIndex == 8)
      return dut.getTxPort ();
    if (columnIndex == 9)
      return dut.getPingStatus ();
    throw new RuntimeException ();
  }

  @Override
  public boolean isCellEditable (int rowIndex, int columnIndex)
  {
    if (rowIndex < 0 || rowIndex >= getRowCount ())
      return false;
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return false;
    final DUT dut = this.duts.get (rowIndex);
    if (dut == null)
      return false;
    if (columnIndex == 0)
      return true;
    if (columnIndex == 1)
      return false;
    if (columnIndex == 2)
      return false;
    if (columnIndex == 3)
      return false;
    if (columnIndex == 4)
      return false;
    if (columnIndex == 5)
      return true;
    if (columnIndex == 6)
      return false;
    if (columnIndex == 7)
      return true;
    if (columnIndex == 8)
      return false;
    if (columnIndex == 9)
      return false;
    throw new RuntimeException ();
  }

  @Override
  public void setValueAt (Object aValue, int rowIndex, int columnIndex)
  {
    if (! isCellEditable (rowIndex, columnIndex))
      return;
    final DUT dut = this.duts.get (rowIndex); // Non-null.
    if (columnIndex == 0)
    {
      if (aValue != null && (aValue instanceof Boolean))
      {
        final boolean newValue = ((Boolean) aValue);
        if (newValue != dut.getActive ())
        {
          dut.setActive (newValue);
          // XXX Notify...
        }
      }
      return;
    }
    if (columnIndex == 1)
      return;
    if (columnIndex == 2)
      return;
    if (columnIndex == 3)
      return;
    if (columnIndex == 4)
      return;
    if (columnIndex == 5)
    {
      if (aValue != null && (aValue instanceof Boolean))
      {
        final boolean newValue = ((Boolean) aValue);
        if (newValue != dut.getReceives ())
        {
          dut.setReceives (newValue);
          // XXX Notify...
        }
      }
      return;
    }
    if (columnIndex == 6)
      return;
    if (columnIndex == 7)
    {
      if (aValue != null && (aValue instanceof Boolean))
      {
        final boolean newValue = ((Boolean) aValue);
        if (newValue != dut.getTransmits ())
        {
          dut.setTransmits (newValue);
          // XXX Notify...
        }
      }
      return;
    }
    if (columnIndex == 8)
      return;
    if (columnIndex == 9)
      return;
    throw new RuntimeException ();
  }

}
