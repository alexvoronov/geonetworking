package net.gcdc.plugtestcms4;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.gcdc.geonetworking.LocationTable;
import net.gcdc.plugtestcms4.ping.PingStatus;

public class LocationTableTableModel
extends AbstractTableModel
implements TableModel
{

  private final LocationTable locationTable;

  public LocationTableTableModel (LocationTable locationTable)
  {
    super ();
    this.locationTable = locationTable;
  }

  @Override
  public int getColumnCount ()
  {
    return 1;
  }

  @Override
  public Class<?> getColumnClass (int columnIndex)
  {
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    if (columnIndex == 0)
      return Boolean.class;
    throw new RuntimeException ();
  }

  @Override
  public String getColumnName (int columnIndex)
  {
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    if (columnIndex == 0)
      return "Act";
    throw new RuntimeException ();
  }

  @Override
  public int getRowCount ()
  {
    throw new UnsupportedOperationException ();
    // return (this.locationTable == null) ? 0 : this.locationTable.size ();
  }

  @Override
  public Object	getValueAt (int rowIndex, int columnIndex)
  {
    if (rowIndex < 0 || rowIndex >= getRowCount ())
      return null;
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return null;
    //final DUT dut = this.duts.get (rowIndex);
    //if (dut == null)
    //  return null;
    //if (columnIndex == 0)
    //  return dut.getActive ();
    if (columnIndex == 0)
      return Boolean.TRUE;
    //if (columnIndex == 1)
    //  return dut.getName ();
    //if (columnIndex == 2)
    //  return dut.getRootPassword ();
    //if (columnIndex == 3)
    //  return dut.getIpv4AddressEth ();
    //if (columnIndex == 4)
    //  return dut.getIpv4AddressWLAN ();
    //if (columnIndex == 5)
    //  return dut.getReceives ();
    //if (columnIndex == 6)
    //  return dut.getRxPort ();
    //if (columnIndex == 7)
    //  return dut.getTransmits ();
    //if (columnIndex == 8)
    //  return dut.getTxPort ();
    //if (columnIndex == 9)
    //  return dut.getPingStatus ();
    throw new RuntimeException ();
  }

  @Override
  public boolean isCellEditable (int rowIndex, int columnIndex)
  {
    if (rowIndex < 0 || rowIndex >= getRowCount ())
      return false;
    if (columnIndex < 0 || columnIndex >= getColumnCount ())
      return false;
    //final DUT dut = this.duts.get (rowIndex);
    //if (dut == null)
    //  return false;
    if (columnIndex == 0)
      return false;
    throw new RuntimeException ();
  }

  @Override
  public void setValueAt (Object aValue, int rowIndex, int columnIndex)
  {
    if (! isCellEditable (rowIndex, columnIndex))
      return;
    // final DUT dut = this.duts.get (rowIndex); // Non-null.
    // if (columnIndex == 0)
    // {
    //   if (aValue != null && (aValue instanceof Boolean))
    //   {
    //     final boolean newValue = ((Boolean) aValue);
    //     if (newValue != dut.getActive ())
    //     {
    //       dut.setActive (newValue);
    //       // XXX Notify...
    //     }
    //   }
    //   return;
    // }
    if (columnIndex == 0)
      return;
    throw new RuntimeException ();
  }

}
