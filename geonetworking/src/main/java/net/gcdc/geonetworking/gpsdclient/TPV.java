package net.gcdc.geonetworking.gpsdclient;

import com.google.gson.annotations.SerializedName;

/** Time-Position-Velocity reply from GPSd server. */
public final class TPV{
    /** Message class, always String "TPV". */
	@SerializedName("class")
	private String   clazz;
    /** NMEA mode: 0=no mode value yet seen, 1=no fix, 2=2D, 3=3D. */
    private int      mode;
	/** Name of originating device. */
	private String   device;
	/** Time/date stamp in ISO8601 format, UTC. May have a fractional part of up to .001 sec precision. May be absent if mode is not 2 or 3. */
	private String   time;
	/** Estimated timestamp error (in seconds, 95% confidence). Present if time is present. */
	private double   ept;
	/** Latitude in degrees: +/- signifies North/South. Present when mode is 2 or 3. */
	private double   lat;
	/** Longitude in degrees: +/- signifies East/West. Present when mode is 2 or 3. */
	private double   lon;
	/** Altitude in meters. Present if mode is 3. */
	private double   alt;
	/** Course over ground, degrees from true north. */
	private double   track;
	/** Speed over ground, meters per second. */
	private double   speed;
	/** Climb (positive) or sink (negative) rate, meters per second. */
	private double   climb;

	public String   clazz()  { return clazz;  }
	public String   device() { return device; }
	public String   time()   { return time;   }
	public double   ept()    { return ept;    }
	public double   lat()    { return lat;    }
	public double   lon()    { return lon;    }
	public double   alt()    { return alt;    }
	public double   track()  { return track;  }
	public double   speed()  { return speed;  }
	public double   climb()  { return climb;  }
	public int      mode()   { return mode;   }

	@Override
	public String toString() {
		return "class="+clazz
			 + " device="+device
			 + " time="+time
			 + " ept="+ept
			 + " lat="+lat
			 + " lon="+lon
			 + " alt="+alt
			 + " track="+track
			 + " speed="+speed
			 + " climb="+climb
			 + " mode="+mode;
	}
}
