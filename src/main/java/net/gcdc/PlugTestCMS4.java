package net.gcdc;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.gcdc.plugtestcms4.*;
import net.gcdc.plugtestcms4.ping.*;

public class PlugTestCMS4
  extends JFrame
{

  final JTabbedPane tabbedPane;
  final DUTsPane dutsPane;

  public static void main (String[] args)
  {
    SwingUtilities.invokeLater (new Runnable ()
      {
        public void run ()
        {
          new PlugTestCMS4 ().setVisible (true);
        }
      });
  }

  public PlugTestCMS4 ()
  {
    this.dutList.add (new DUT ("i-Game-1", "192.168.159.101", "192.168.73.201", 1236));
    this.dutList.add (new DUT ("i-Game-2", "192.168.159.102", "192.168.73.202", 1237));
    this.dutList.add (new DUT ("i-Game-3", "192.168.159.103", "192.168.73.203", 1238));
    setTitle ("PlugTestCMS4 Manager - i-Game");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    this.tabbedPane = new JTabbedPane ();
    this.dutsPane = new DUTsPane ();
    this.tabbedPane.add ("DUTs", this.dutsPane);
    setContentPane (this.tabbedPane);
    pack ();
  }

  private class DUTsPane
  extends JComponent
  {

    final DUTTableModel model;
    final JScrollPane scrollPane;
    final JTable table;

    public DUTsPane ()
    {
      this.model = new DUTTableModel (PlugTestCMS4.this.dutList);
      this.table = new JTable (this.model);
      this.table.setDefaultRenderer (PingStatus.class, new PingStatusRenderer (true));
      this.scrollPane = new JScrollPane (this.table);
      setLayout (new BorderLayout ());
      add (this.scrollPane, BorderLayout.CENTER);
      new UpdateThread ().start ();
    }

    private class UpdateThread extends Thread
    {
      @Override
      public void run ()
      {
        try
        {
          while (true)
          {
            Thread.sleep (3000L);
            if (DUTsPane.this.model != null)
              SwingUtilities.invokeLater (new Runnable ()
              {
                @Override
                public void run ()
                {
                  DUTsPane.this.model.fireTableDataChanged ();
                }
              });
          }
        }
        catch (InterruptedException ie)
        {
          // Nothing to do; fall through and terminate Thread.
        }
      }
    }

  }

  private final List<DUT> dutList = new ArrayList<> ();

}
