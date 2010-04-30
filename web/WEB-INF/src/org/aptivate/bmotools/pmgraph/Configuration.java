package org.aptivate.bmotools.pmgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Noe Andres Rodriguez Gonzalez.
 * 
 * This configuration class for the application is used to keep all
 * access to configuration data in a single place. 
 * 
 * Addition: RuchiR
 * 
 * Updated for accessing and modifying local subnet in configuration 
 * files "database.properties" and "pmacctd.conf". 
 * 
 */
public class Configuration
{
	
	static final String CONFIGURATION_FILE = "/database.properties";
	private static final long DAY = 86400000;
	private static Properties s_properties;


	/**
	 * Read the content of the properties file and return it in a Properties
	 * object
	 * 
	 * @return java.utils.Properties - object created after reading the properties
	 *         file.
	 * @throws IOException 
	 */		
	private static void readConfiguration() throws IOException
	{
		if(s_properties == null)
		{
			s_properties = new Properties();
			InputStream stream = DataAccess.class.getResourceAsStream(CONFIGURATION_FILE);		
			s_properties.loadFromXML(stream);
			stream.close();
		}
	}
	
	/**
	 * Force a reload of the configuration (testing purposes only).
	 * @throws IOException 
	 * 
	*/ 
	public static void forceConfigReload() throws IOException
	{
		s_properties = null;
		readConfiguration();
	}

	public static String [] getLocalSubnet() throws IOException
	{

		readConfiguration();
		int i = 2;
		String AllSubnets = s_properties.getProperty("LocalSubnet");
		String subnet = s_properties.getProperty("LocalSubnet" + i);
		while (subnet != null)
		{
             AllSubnets += " " + subnet; 
             i++;
             subnet = s_properties.getProperty("LocalSubnet" + i);
		}		
		return (AllSubnets.split(" "));
	}
	
	public static String getBandwidth() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("TotalBandwidth");
	}
	
	public static Properties getProperties() throws IOException
	{
		return s_properties;
	}
	public static String getDatabaseURL() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DatabaseURL");
	}

	public static String getDatabaseUser() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DatabaseUser");
	}

	public static String getResultDatabaseTable() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DatabaseTable");
	}
	
	public static String getDatabasePass() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DatabasePass");
	}
	

	public static String getDHCPAddress() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DHCPAddress");
	}

	public static String getDHCPName() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DHCPName");
	}

	public static String getDHCPPass() throws IOException
	{

		readConfiguration();
		return s_properties.getProperty("DHCPPass");
	}
	
	public static String getResultDatabaseLongTable() throws IOException
	{
		readConfiguration();
		return s_properties.getProperty("DatabaseLongTable");
	}
	
	public static String getResultDatabaseVeryLongTable() throws IOException
	{
		readConfiguration();
		return s_properties.getProperty("DatabaseVeryLongTable");
	}
	
	public static String getTimespansForLongGraph() throws IOException
	{
		readConfiguration();
		return s_properties.getProperty("TimespansForLongGraph");
	}

	public static String getJdbcDriver() throws IOException
	{
		readConfiguration();
		return s_properties.getProperty("JdbcDriver");
	}

	public static Integer getDHCPPort() throws IOException
	{
		readConfiguration();
		return Integer.valueOf(s_properties.getProperty("DHCPPort"));
	}
	
	public static Integer getResultLimit() throws IOException
	{
		readConfiguration();
		return Integer.valueOf(s_properties.getProperty("ResultLimit"));
	}
	
	public static boolean updateConf(String newSubnet, Hashtable<String,Integer> hashDelSubnets) throws IOException
	{	
		readConfiguration();
		boolean result = true;        
		Properties tempProps = (Properties)s_properties.clone();
		FileOutputStream out = new FileOutputStream((DataAccess.class.getResource(CONFIGURATION_FILE)).getPath());
		if (!hashDelSubnets.isEmpty())
			delSubnetConf(hashDelSubnets, tempProps);
		if ((newSubnet != null) && (newSubnet != "")) 
		{ 
			newSubnet = RequestParams.setAddSubnet(newSubnet);
			result = addSubnetConf(newSubnet, tempProps);
		}
		s_properties = tempProps;
		tempProps.storeToXML(out, "");
		out.close();
		return result;
	}
	
	public static boolean addSubnetConf(String newSubnet, Properties tempProps) throws IOException
	{	
		int i=2;
		boolean insert = false;
		if (tempProps.getProperty("LocalSubnet") != null)
			if (tempProps.getProperty("LocalSubnet").equals(newSubnet))
				insert = true;
		while ((tempProps.getProperty("LocalSubnet"+i) != null) && (!insert))
		{
			if (tempProps.getProperty("LocalSubnet"+i).equals(newSubnet))
				insert = true;
			else
				i++;
		}		
		if (!insert) 
			tempProps.put("LocalSubnet"+i, newSubnet);	
		
		return(!insert);
	}
	
	public static boolean addSubnetConf(String newSubnet) throws IOException
	{	
		readConfiguration();
		boolean result = false;
		// Copy to a temporary properties object to prevent error where the configuration file ends up
		// empty.
		Properties tempProps = (Properties)s_properties.clone();
		FileOutputStream out = new FileOutputStream((DataAccess.class.getResource(CONFIGURATION_FILE)).getPath());		
		int i=2;
		boolean insert = false;
		if (tempProps.getProperty("LocalSubnet") != null)
			if (tempProps.getProperty("LocalSubnet").equals(newSubnet))
				insert = true;
		while ((tempProps.getProperty("LocalSubnet"+i) != null) && (!insert))
		{
			if (tempProps.getProperty("LocalSubnet"+i).equals(newSubnet))
				insert = true;
			else
				i++;
		}		
		if (!insert) 
		{
			tempProps.put("LocalSubnet"+i, newSubnet);
			result = true;	
		}
		s_properties = tempProps;
		tempProps.storeToXML(out, "");
		out.close();
		return result;
	}
	
	public static boolean delSubnetConf(Hashtable<String,Integer> hashDelSubnets, Properties tempProps) throws IOException
	{	
		boolean result = false;				
		for (Enumeration e = hashDelSubnets.keys (); e.hasMoreElements ();) 
		{			
			String key = (String) e.nextElement ();
			int value = hashDelSubnets.get (key);
			tempProps.remove(key);
			int i = value + 1;		    
			while (tempProps.getProperty("LocalSubnet"+i) != null) {
				if ((i-1) == 1)
					tempProps.setProperty("LocalSubnet", tempProps.getProperty("LocalSubnet"+i));
				else
					tempProps.setProperty("LocalSubnet"+(i-1), tempProps.getProperty("LocalSubnet"+i));
				i++;
			}
			tempProps.remove("LocalSubnet"+(i-1));
		}
		result = true;		
		return result;
	}
	
	public static boolean delSubnetConf(Hashtable<String,Integer> hashDelSubnets) throws IOException
	{	
		readConfiguration();
		boolean result = false;
		Properties tempProps = (Properties)s_properties.clone();
		FileOutputStream out = new FileOutputStream((DataAccess.class.getResource(CONFIGURATION_FILE)).getPath());		
			
		for (Enumeration e = hashDelSubnets.keys (); e.hasMoreElements ();) 
		{
			String key = (String) e.nextElement ();
			int value = hashDelSubnets.get (key);
			tempProps.remove(key);
			int i = value + 1;
			while (tempProps.getProperty("LocalSubnet"+i) != null) {
				if ((i-1) == 1)
					tempProps.setProperty("LocalSubnet", tempProps.getProperty("LocalSubnet"+i));
				else
					tempProps.setProperty("LocalSubnet"+(i-1), tempProps.getProperty("LocalSubnet"+i));
				i++;
			}
			tempProps.remove("LocalSubnet"+(i-1));
		}
		s_properties = tempProps;
		tempProps.storeToXML(out, "");
		out.close();
		result = true;		
		return result;
	}
	
	
	public static void processLine(String aLine, String localSubnet, String oldSubnet, StringBuilder contents) throws IOException
	{
	    // use a second Scanner to parse the content of each line 
	    Scanner scanner = new Scanner(aLine);
	    scanner.useDelimiter("\n");
	    if ( scanner.hasNext() ){
	      String name = scanner.next();
	      if(name.contains("pcap_filter"))
	      {	   
	    	  String oldLine = "pcap_filter: not (src and dst net " + oldSubnet + "0/24)";	    	  
	    	  String newLine = "pcap_filter: not (src and dst net " + localSubnet + "0/24)";	    	  
	    	  name = name.replace(oldLine, newLine);
	      }	
	      contents.append(name);
	      contents.append("\n");
		  
	    }
	    else {	    	
	    	contents.append("\n");
	    }	    	    
	    //(no need for finally here, since String is source)
	    scanner.close();
	  }
	
	 public static void processLineByLine(String localSubnet, String oldSubnet) throws IOException {
		 
		    String fullPath = (DataAccess.class.getResource(CONFIGURATION_FILE)).getPath();
		    String pmacctPath = fullPath.replace("/web/WEB-INF/classes/database.properties", "/config/pmacctd.conf");		    		    		    
		    File fFile = new File(pmacctPath);
		    Scanner scanner = new Scanner(fFile);
		    StringBuilder contents = new StringBuilder();
		    try {
		      //first use a Scanner to get each line
		      while ( scanner.hasNextLine() ){
		        processLine( scanner.nextLine(), localSubnet, oldSubnet, contents );
		      }
		    }
		    finally {
		      //ensure the underlying stream is always closed
		      scanner.close();
		    }
		    
		    // use buffering
		    Writer output = new BufferedWriter(new FileWriter(pmacctPath));
		    try {
		      //FileWriter always assumes default encoding is OK!
		      output.write( contents.toString() );
		    }
		    finally {
		      output.close();
		    }		    
	}	 
	 
	 	/**
		 * Gets the time period for each step on th x-axis of the graph
		 * @param isLong Whether or not a long time period is covered
		 * @return The value of each step on the x-axis of the graph as a long value in milliseconds
		 */
		public static int getResolution(boolean isLong, long timeSpan)
		{
			int requiredTimePeriod = TimePeriod.MINUTE.getTimePeriod();
			if(isLong)
			{
				int[] timePeriods = TimePeriod.getTimePeriodLengths();
				boolean found = false;
				long[] times = getTimeSpanLengths();
				if(times.length != 0)
				{
					for(int period : timePeriods)
					{
						// Ensure that the time period divided by the timespan is less than the number of minutes in a day
						// If it isn't then there's no real point in using it.
						for(long time : times)
						{
							if(timeSpan >= time)
							{
								if(timeSpan/(long)period < (60 * 24))
								{
									requiredTimePeriod = period;
									found = true;
									break;
								}
							}
						}
						if(!found)
						{
							requiredTimePeriod = timePeriods[timePeriods.length - 1];
						}
						else
						{
							break;
						}
					}
				}
			}
			return requiredTimePeriod;
		}
		
		/**
		 * This gets the table in which the data is stored
		 * @param timeSpan The length of time covered by the graph
		 * @return The name of the table
		 * @throws IOException
		 */
		public static String findTable(long timeSpan) throws IOException
		{
			long[] timeSpans = getTimeSpanLengths();
			for(int i = timeSpans.length - 1; i >= 0; i--)
			{
				if(timeSpan >= timeSpans[i])
				{
					switch(i){
						case 0:
							return Configuration.getResultDatabaseLongTable();
						case 1:
							String table = Configuration.getResultDatabaseVeryLongTable();
							if(table == null)
							{
								table = Configuration.getResultDatabaseLongTable();
							}
							return table;					
						default:
							return Configuration.getResultDatabaseLongTable();
					}
				}
			}
			return Configuration.getResultDatabaseTable();
		}
		
		/**
		 * This gets the different timespans for determining the period of data covered in the graph in ascending order
		 * @return An array of long values representing the timespans
		 */
		public static long[] getTimeSpanLengths()
		{
			long[] values;
			List<Long> temp = new ArrayList<Long>();
			try
			{
				String[] timeSpans = Configuration.getTimespansForLongGraph().split(",");
				for(String timeSpan : timeSpans)
				{
					try
					{
						long tempVal = Long.parseLong(timeSpan.trim()) * 60 * 60 * 1000;
						if(tempVal >= DAY)
						{
							temp.add(new Long(tempVal));
						}
					}
					catch(NumberFormatException e)
					{
						
					}
				}
				values = new long[temp.size()];
				for(int i = 0; i < values.length; i++)
				{
					values[i] = temp.get(i).longValue();
				}
				Arrays.sort(values);
				return values;
			}
			catch(IOException e)
			{
				values = new long[0];
				return values;
			}
			catch(NullPointerException e)
			{
				values = new long[0];
				return values;
			}
		}
		
		/**
		 * Determines if the graph should display data over a long time period
		 * @param start The time the graph shows data from
		 * @param end The time the graph shows data to
		 * @return a value saying whether or not the graph data should cover a long time period
		 */
		public static boolean needsLongGraph(long start, long end)
		{
			long[] timeSpans = getTimeSpanLengths();
			for(long timeSpan : timeSpans)
			{
				if(end - start >= timeSpan)
				{
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Check that the displaying of long graphs has been enabled
		 * @return A boolean saying whether or not long graphs are enabled
		 */
		public static boolean longGraphIsAllowed()
		{
			try
			{
				String[] timeSpans = Configuration.getTimespansForLongGraph().split(",");
				for(String timeSpan : timeSpans)
				{
					long aTimeSpan = 0;
					try
					{
						aTimeSpan = Long.parseLong(timeSpan.trim());
					}
					catch(NumberFormatException e)
					{
						
					}
					// You can't display a long graph with time periods of less than one day.
					if(aTimeSpan >= 24)
					{
						return true;
					}
				}
				return false;
			}
			catch(IOException e)
			{
				return false;
			}
			catch(NullPointerException e)
			{
				return false;
			}
		}
}