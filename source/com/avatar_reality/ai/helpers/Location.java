package com.avatar_reality.ai.helpers;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * This is a simple data class describing a location in a 3-dimensional space.
 * This could also be seen as a vector class and it has several methods defined that
 * perform vector operations.
 */
public class Location
	implements Serializable
{
	public static final double UNIT = 0.5;
	
	private static final long serialVersionUID = -3448257610149269888L;
	
	public static final Location ZERO =	new Location( 0, 0, 0);
	
	/**
	 * The following are constant definitions of the 26 directions to immediate neighboring
	 * locations. The naming is based on a compass, where N=North S=South E=East and W=West.
	 * U=Up and D=Down. So UNW stands for up-north-west
	 */
	public static final Location N =	new Location( UNIT, 0, 0);
	public static final Location S = 	new Location(-UNIT, 0, 0);
	public static final Location W = 	new Location( 0,-UNIT, 0);
	public static final Location E = 	new Location( 0, UNIT, 0);
	public static final Location NW = 	new Location( UNIT,-UNIT, 0);
	public static final Location NE = 	new Location( UNIT, UNIT, 0);
	public static final Location SW = 	new Location(-UNIT,-UNIT, 0);
	public static final Location SE = 	new Location(-UNIT, UNIT, 0);
	
	public static final Location UN = 	new Location( UNIT, 0, UNIT);
	public static final Location US = 	new Location(-UNIT, 0, UNIT);
	public static final Location UW = 	new Location( 0,-UNIT, UNIT);
	public static final Location UE = 	new Location( 0, UNIT, UNIT);
	public static final Location UNW = 	new Location( UNIT,-UNIT, UNIT);
	public static final Location UNE = 	new Location( UNIT, UNIT, UNIT);
	public static final Location USW = 	new Location(-UNIT,-UNIT, UNIT);
	public static final Location USE = 	new Location(-UNIT, UNIT, UNIT);
	
	public static final Location DN = 	new Location( UNIT, 0,-UNIT);
	public static final Location DS = 	new Location(-UNIT, 0,-UNIT);
	public static final Location DW = 	new Location( 0,-UNIT,-UNIT);
	public static final Location DE = 	new Location( 0, UNIT,-UNIT);
	public static final Location DNW = 	new Location( UNIT,-UNIT,-UNIT);
	public static final Location DNE = 	new Location( UNIT, UNIT,-UNIT);
	public static final Location DSW = 	new Location(-UNIT,-UNIT,-UNIT);
	public static final Location DSE = 	new Location(-UNIT, UNIT,-UNIT);
	
	public static final Location U = 	new Location( 0, 0, UNIT);
	public static final Location D = 	new Location( 0, 0,-UNIT);

	public static final Location[] NEIGHBORING_LOCATIONS =
		{
			N,S,W,E,NW,NE,SW,SE,UN,US,UW,UE,UNW,UNE,USW,USE,DN,DS,DW,DE,DNW,DSW,DSE,U,D
		};

	public static final long X_MASK = 0x00000000ffffL;
	public static final long Y_MASK = 0x0000ffff0000L;
	public static final long Z_MASK = 0xffff00000000L;
	
	public static final long X_BIT_START = 0;
	public static final long Y_BIT_START = 16;
	public static final long Z_BIT_START = 32;
	
	public static final long X_BIT = 1L<<X_BIT_START;
	public static final long Y_BIT = 1L<<Y_BIT_START;
	public static final long Z_BIT = 1L<<Z_BIT_START;
	
	/**
	 * Round a coordinate to the closest UNIT.
	 * 
	 * @param x
	 * 
	 * @return round(x/UNIT)*UNIT
	 */
	public static final double round(double x)
	{
		return Math.round(x/UNIT)*UNIT;
	}
	
	private float _x;
	private float _y;
	private float _z;
	
	private long _key;
	
	public Location()
	{
	}
	
	public Location(double x, double y, double z)
	{
		_x = (float)x;
		_y = (float)y;
		_z = (float)z;
		createKey();
	}
	
	public Location(Double x, Double y, Double z)
	{
		this(x.doubleValue(),y.doubleValue(),z.doubleValue());
	}
	
	public Location createClone()
	{
		return new Location(_x,_y,_z);
	}

	/**
	 * 
	 */
	public void createKey()
	{
		long x = Math.round(_x/UNIT) << X_BIT_START;
		long y = Math.round(_y/UNIT) << Y_BIT_START;
		long z = Math.round(_z/UNIT) << Z_BIT_START;
		_key = x | y | z;
	}
	
	public double getX()
	{
		return _x;
	}
	
	public double getY()
	{
		return _y;
	}
	
	public double getZ()
	{
		return _z;
	}
	
	/**
	 * Add a location to the current location and return the new location as result.
	 * 
	 * @param delta
	 * @return the resulting location
	 */
	public Location add(Location delta)
	{
		if (delta==null)
			return this;
		
		return new Location(_x+delta.getX(), _y+delta.getY(), _z+delta.getZ());
	}
	
	public Location subtract(Location delta)
	{
		return new Location(_x-delta.getX(), _y-delta.getY(), _z-delta.getZ());
	}
	
	public Location multiply(double factor)
	{
		return new Location(_x*factor, _y*factor, _z*factor);
	}
	
	public Location divide(double factor)
	{
		if (factor==0.0)
			return ZERO;
		return new Location(_x/factor, _y/factor, _z/factor);
	}
	
	public Location round()
	{
		return new Location(Math.round(_x), Math.round(_y), Math.round(_z));
	}
	
	public double getDistance(Location compare)
	{
		double deltaX = compare.getX()-_x;
		double deltaY = compare.getY()-_y;
		double deltaZ = compare.getZ()-_z;
		
		double horizontalDistance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		return Math.sqrt(horizontalDistance*horizontalDistance + deltaZ*deltaZ);
	}
	
	public double getHorizontalDistance(Location compare)
	{
		double deltaX = compare.getX()-_x;
		double deltaY = compare.getY()-_y;
		
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}
	
	public double getVerticalDistance(Location compare)
	{
		return Math.abs(compare.getZ()-_z);
	}
	
	public double getLength()
	{
		double deltaX = getX();
		double deltaY = getY();
		double deltaZ = getZ();
		
		double horizontalDistance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		return Math.sqrt(horizontalDistance*horizontalDistance + deltaZ*deltaZ);
	}
	
	public double getManhattenDistance(Location compare)
	{
		double deltaX = Math.abs(compare.getX()-_x);
		double deltaY = Math.abs(compare.getY()-_y);
		double deltaZ = Math.abs(compare.getZ()-_z);
		
		return deltaX + deltaY + deltaZ;
	}
	
	public String toString()
	{
		return "("+_x+","+_y+","+_z+")";
	}
	
	public long toKey()
	{
		return _key;
	}
	
	public void setKey(long key)
	{
		_key = key;
		_x = (float)(((key & X_MASK) >> X_BIT_START) * UNIT);
		_y = (float)(((key & Y_MASK) >> Y_BIT_START) * UNIT);
		_z = (float)(((key & Z_MASK) >> Z_BIT_START) * UNIT);
	}
	
	public String toXYZ()
	{
		return ""+_x+" "+_y+" "+_z;
	}
	
	public String toXML(String name)
	{
		StringBuilder location = new StringBuilder("<Location name='"+name+"'>\n");
		location.append(toString());
		location.append("\n</Location>\n");
		return location.toString();
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof Location))
			return false;
		
		Location compare = (Location) o;
		return (_x==compare.getX() && _y==compare.getY() && _z==compare.getZ());
	}
	
	public int hashCode()
	{
		return (int)_key;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x)
	{
		_x = (float)x;
		createKey();
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y)
	{
		_y = (float)y;
		createKey();
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z)
	{
		_z = (float)z;
		createKey();
	}
	
	/**
	 * @param x the x to set
	 */
	void initX(double x)
	{
		_x = (float)x;
	}

	/**
	 * @param y the y to set
	 */
	void initY(double y)
	{
		_y = (float)y;
	}

	/**
	 * @param z the z to set
	 */
	void initZ(double z)
	{
		_z = (float)z;
	}
	
	public Location normalize()
	{
		return divide(getLength());
	}
		
	public static Location parseLocation(String line)
	{
		StringTokenizer tokenizer = new StringTokenizer(line.trim(),"(,) \n");
		double x = Double.parseDouble(tokenizer.nextToken());
		double y = Double.parseDouble(tokenizer.nextToken());
		double z = Double.parseDouble(tokenizer.nextToken());
		Location newLocation = new Location(x,y,z);
		return newLocation;
	}
	
	public static double getDistance(String l1, String l2)
	{
		double distance = parseLocation(l1).getDistance(parseLocation(l2));
		System.out.println("Distance: "+distance);
		return distance;
	}
}
