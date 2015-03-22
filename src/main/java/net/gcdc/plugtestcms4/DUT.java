package net.gcdc.plugtestcms4;

import net.gcdc.plugtestcms4.ping.PingStatus;
import net.gcdc.plugtestcms4.ping.PingSettings;

public class DUT
{

  public DUT (String name, String ipv4AddressEth, String ipv4AddressWLAN, int rxPort)
  {
    if (name == null || ipv4AddressEth == null || ipv4AddressWLAN == null)
      throw new IllegalArgumentException ();
    this.name = name;
    this.ipv4AddressEth = ipv4AddressEth;
    this.ipv4AddressWLAN = ipv4AddressWLAN;
    this.rxPort = rxPort;
  }

  private boolean active = false;

  public synchronized boolean getActive ()
  {
    return this.active;
  }

  public synchronized void setActive (boolean active)
  {
    if (active != this.active)
    {
      this.active = active;
      if (this.active)
      {
        synchronized (this.pingSettingsLock)
        {
          if (this.pingSettings == null)
          {
            this.pingSettings = new PingSettings ();
          }
          this.pingSettings.setActive (false);
          this.pingSettings.setHostAddressesToMonitor (new String[] { this.ipv4AddressEth });
          this.pingSettings.setActive (true);
        }
      }
      else
      {
        synchronized (this.pingSettingsLock)
        {
          if (this.pingSettings != null)
          {
            this.pingSettings.setActive (false);
          }
        }
      }
    }
  }

  private String name = "Unset";

  public String getName ()
  {
    return this.name;
  }

  public void setName (String name)
  {
    if (name == null)
      throw new IllegalArgumentException ();
    this.name = name;
  }

  private String rootPassword = "voyage";

  public final String getRootPassword ()
  {
    return this.rootPassword;
  }

  private String ipv4AddressEth = "Unset";

  public String getIpv4AddressEth ()
  {
    return this.ipv4AddressEth;
  }

  public void setIpv4AddressEth (String ipv4AddressEth)
  {
    if (ipv4AddressEth == null)
      throw new IllegalArgumentException ();
    if (! ipv4AddressEth.equals (this.ipv4AddressEth))
    {
      this.ipv4AddressEth = ipv4AddressEth;
    }
  }

  private String ipv4AddressWLAN = "Unset";

  public String getIpv4AddressWLAN ()
  {
    return this.ipv4AddressWLAN ;
  }

  public void setIpv4AddressWLAN (String ipv4AddressWLAN)
  {
    if (ipv4AddressWLAN == null)
      throw new IllegalArgumentException ();
    if (! ipv4AddressWLAN.equals (this.ipv4AddressWLAN))
    {
      this.ipv4AddressWLAN = ipv4AddressWLAN;
    }
  }

  private boolean receives = false;

  public boolean getReceives ()
  {
    return this.receives;
  }

  public void setReceives (boolean receives)
  {
    if (receives != this.receives)
    {
      this.receives = receives;
    }
  }

  private int rxPort = 1236;

  public int getRxPort ()
  {
    return this.rxPort;
  }

  public void setRxPort (int rxPort)
  {
    if (rxPort != this.rxPort)
    {
      this.rxPort = rxPort;
    }
  }

  private boolean transmits = false;

  public boolean getTransmits ()
  {
    return this.transmits;
  }

  public void setTransmits (boolean transmits)
  {
    if (transmits != this.transmits)
    {
      this.transmits = transmits;
    }
  }

  private int txPort = 1235;

  public int getTxPort ()
  {
    return this.txPort;
  }

  public void setTxPort (int txPort)
  {
    if (txPort != this.txPort)
    {
      this.txPort = txPort;
    }
  }

  private PingSettings pingSettings = null;

  private Object pingSettingsLock = new Object ();

  public PingStatus getPingStatus ()
  {
    if (! getActive ())
      return PingStatus.UNKNOWN;
    synchronized (this.pingSettingsLock)
    {
      if (this.pingSettings == null)
        return PingStatus.UNKNOWN;
      final PingStatus[] hostStatus = this.pingSettings.getHostStatus ();
      if (hostStatus == null || hostStatus.length == 0)
        return PingStatus.UNKNOWN;
      return hostStatus[0];
    }
  }

}
