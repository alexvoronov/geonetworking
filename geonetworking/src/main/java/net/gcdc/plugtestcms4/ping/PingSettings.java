package net.gcdc.plugtestcms4.ping;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PingSettings
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY CHANGE SUPPORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  protected final PropertyChangeSupport pcs = new PropertyChangeSupport (this);

  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    this.pcs.addPropertyChangeListener (listener);
  }

  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    this.pcs.removePropertyChangeListener (listener);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY active
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private boolean active = false;
  
  public final synchronized boolean getActive ()
  {
    return this.active;
  }
  
  public final /* synchronized */ void setActive (boolean active)
  {
    boolean fireEvent = false;
    synchronized (this)
    {
      if (active != this.active)
      {
        fireEvent = true;
        if (this.pingStatusThread != null)
        {
          this.pingStatusThread.interrupt ();
          this.pingStatusThread = null;
        }
        this.active = active;
        if (this.active)
        {
          this.pingStatusThread = new PingStatusThread (this);
          this.pingStatusThread.start ();
        }
      }
    }
    if (fireEvent)
      this.pcs.firePropertyChange ("active", ! this.active, this.active);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY pingStatusThread
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private Thread pingStatusThread = null;
  
  public final synchronized Thread getPingStatusThread ()
  {
    return this.pingStatusThread;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY pollInterval_ms
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private int pollInterval_ms = 10000;
  
  public final synchronized int getPollInterval_ms ()
  {
    return this.pollInterval_ms;
  }
  
  public final synchronized void setPollInterval_ms (int pollInterval_ms)
  {
    if (pollInterval_ms < 0)
      throw new IllegalArgumentException ();
    if (pollInterval_ms != this.pollInterval_ms)
    {
      final int oldPollInterval_ms = this.pollInterval_ms;
      this.pollInterval_ms = pollInterval_ms;
      this.pcs.firePropertyChange ("pollInterval_ms", oldPollInterval_ms, this.pollInterval_ms);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY timeout_ms
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private int timeout_ms = 50;
  
  public final synchronized int getTimeout_ms ()
  {
    return this.timeout_ms;
  }
  
  public final synchronized void setTimeout_ms (int timeout_ms)
  {
    if (timeout_ms < 0)
      throw new IllegalArgumentException ();
    if (timeout_ms != this.timeout_ms)
    {
      final int oldTimeout_ms = this.timeout_ms;
      this.timeout_ms = timeout_ms;
      this.pcs.firePropertyChange ("timeout_ms", oldTimeout_ms, this.timeout_ms);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY useExternalPing
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private boolean useExternalPing = false;
  
  public final boolean getUseExternalPing ()
  {
    return this.useExternalPing;
  }
  
  public final void setUseExternalPing (boolean useExternalPing)
  {
    if (useExternalPing != this.useExternalPing)
    {
      this.useExternalPing = useExternalPing;
      this.pcs.firePropertyChange ("useExternalPing", ! this.useExternalPing, this.useExternalPing);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY hostAddressesToMonitor
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private String[] hostAddressesToMonitor = new String[] { };

  public final synchronized String[] getHostAddressesToMonitor ()
  {
    return this.hostAddressesToMonitor;
  }

  public final synchronized void setHostAddressesToMonitor (String[] hostAddressesToMonitor)
  {
    if (hostAddressesToMonitor == null)
      throw new IllegalArgumentException ();
    for (String hostAddressToMonitor : hostAddressesToMonitor)
      if (hostAddressToMonitor == null)
        throw new IllegalArgumentException ();
    if (hostAddressesToMonitor != this.hostAddressesToMonitor)
    {
      final String[] oldHostAddressesToMonitor = this.hostAddressesToMonitor;
      this.hostAddressesToMonitor = hostAddressesToMonitor;
      this.pcs.firePropertyChange ("hostAddressesToMonitor", oldHostAddressesToMonitor, this.hostAddressesToMonitor);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTY hostStatus
  //
  // METHOD setHostAddressStatus
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<String, Boolean> hostAddressStatus = new HashMap<> ();
  
  public final synchronized PingStatus[] getHostStatus ()
  {
    final PingStatus[] hostStatus = new PingStatus[this.hostAddressesToMonitor.length];
    for (int i = 0; i < this.hostAddressesToMonitor.length; i++)
      if (! this.hostAddressStatus.containsKey (this.hostAddressesToMonitor[i]))
        hostStatus[i] = PingStatus.UNKNOWN;
      else if (this.hostAddressStatus.get (this.hostAddressesToMonitor[i]))
        hostStatus[i] = PingStatus.READY;
      else
        hostStatus[i] = PingStatus.NOT_READY;
    return hostStatus;
  }
  
  protected final /* synchronized */ void setHostAddressStatus (String hostAddress, boolean status)
  {
    if (hostAddress == null)
      return;
    boolean fireEvent;
    synchronized (this)
    {
      if (this.hostAddressStatus.containsKey (hostAddress) && this.hostAddressStatus.get (hostAddress) == status)
        return;
      fireEvent = true;
      this.hostAddressStatus.put (hostAddress, status);
    }
    if (fireEvent)
      this.pcs.firePropertyChange ("hostStatus", null, null);
  }
  
}
