package net.gcdc.plugtestcms4.ping;


import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;

public class PingStatusThread
  extends Thread
{

  private final PingSettings pingSettings;
  
  public PingStatusThread (PingSettings pingSettings)
  {
    if (pingSettings == null)
      throw new IllegalArgumentException ();
    this.pingSettings = pingSettings;
  }

  @Override
  public void run ()
  {
    Log.LOGGER.log (Level.INFO, "PingStatusThread started: {0}.", this);
    try
    {
      boolean interrupted = false;
      while (! interrupted)
      {
        final String[] hostAddresses;
        hostAddresses = this.pingSettings.getHostAddressesToMonitor ();
        Log.LOGGER.log (Level.FINER, "PingStatusThread running: {0}, checking addresses: {1}.", new Object[] {this, hostAddresses});
        if (hostAddresses != null)
          for (String hostAddress : hostAddresses)
            if (hostAddress != null)
            {
              try
              {
                final boolean status;
                final int timeOut_ms = this.pingSettings.getTimeout_ms ();
                if (this.pingSettings.getUseExternalPing ())
                {
                  final int timeOut_s = Math.max (1, Math.round (timeOut_ms / 1000.0f));
                  final String pingCommandLine = "ping -c 1 -w " + timeOut_s + " -W " + timeOut_s + " " + hostAddress;
                  Log.LOGGER.log (Level.FINER, "Spawning ping: {0}.", pingCommandLine);
                  final Process pingProcess = Runtime.getRuntime ().exec (pingCommandLine);
                  final int pingReturnValue = pingProcess.waitFor ();
                  status = (pingReturnValue == 0);
                  Log.LOGGER.log (Level.FINER, "Ping return: {0}.", pingReturnValue);
                }
                else
                {
                  final InetAddress inetAddress = InetAddress.getByName (hostAddress);
                  status = inetAddress.isReachable (timeOut_ms);
                }
                Log.LOGGER.log (Level.FINER, "Status {0}: {1}.", new Object[] {hostAddress, status});
                this.pingSettings.setHostAddressStatus (hostAddress, status);
              }
              catch (IOException ioe)
              {
                Log.LOGGER.log (Level.FINE, "IOException for host {0}.", hostAddress);
                this.pingSettings.setHostAddressStatus (hostAddress, false);
              }
            }
        interrupted = isInterrupted ();
        if (! interrupted)
        {
          final long threadSleepTime_ms;
          if (this.pingSettings != null)
            threadSleepTime_ms = this.pingSettings.getPollInterval_ms ();
          else
            threadSleepTime_ms = 60000;
          Thread.sleep (threadSleepTime_ms);
        }
        else
          Log.LOGGER.log (Level.INFO, "PingStatusThread interrupted status set: {0}.", this); 
      }
    }
    catch (InterruptedException ie)
    {
      Log.LOGGER.log (Level.INFO, "PingStatusThread interrupted: {0}.", this);
    }
    Log.LOGGER.log (Level.INFO, "PingStatusThread stopped: {0}.", this);
  }

}
