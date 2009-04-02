package org.aptivate.pmGraph.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.Layer;

/**
 * Abstract Class which contains the method which checks if the values of a
 * JFreechart correspond with the rows and values array passed. It can be used
 * whenever we try to chech the values of a graph then it is used for the port
 * grap and for the Ip graphs.
 * 
 * @author Noe A. Rodriguez Gonzalez.
 * 
 */
public abstract class GraphTestBase extends TestCase
{
	protected TestUtils m_testUtils;

	public GraphTestBase() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException,
			IOException
	{

		m_testUtils = new TestUtils();
	}

	protected void checkChartData(long values[][], String[] rows,
			JFreeChart chart)
	{

		XYPlot plot = (XYPlot) chart.getPlot();
		assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
		Collection markers = plot.getRangeMarkers(Layer.FOREGROUND);
		Iterator i = markers.iterator();
		assertEquals(i.next(), new ValueMarker(0));
		assertFalse(i.hasNext());

		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		assertEquals("Throughput (kb/s)", yAxis.getLabel());
		DefaultTableXYDataset dataset = (DefaultTableXYDataset) plot
				.getDataset();

		// Check if there are a series per each por for upload and another for
		// download data.

		for (int n = 0; n < rows.length; n++)
		{
			assertTrue("missing item " + rows[n],
					dataset.getSeriesCount() > (n << 1));
			assertEquals(rows[n] + "<down>", dataset.getSeries(n << 1).getKey());
			assertEquals(rows[n] + "<up>", dataset.getSeries((n << 1) + 1)
					.getKey());
		}

		assertEquals(rows.length * 2, dataset.getSeriesCount());
		Map<String, XYSeries> series = new HashMap<String, XYSeries>();

		// check the number of elements of each serie.
		for (int n = 0; n < dataset.getSeriesCount(); n++)
		{
			XYSeries s = dataset.getSeries(n);
			assertEquals(4, s.getItemCount());
			assertEquals(m_testUtils.t1.getTime(), s.getX(0));
			assertEquals(m_testUtils.t2.getTime(), s.getX(1));
			assertEquals(m_testUtils.t3.getTime(), s.getX(2));
			assertEquals(m_testUtils.t4.getTime(), s.getX(3));
			series.put(s.getKey().toString(), s);
		}

		for (int n = 0; n < rows.length; n++)
		{
			XYSeries s = series.get(rows[n] + "<up>"); // check Up series
			// values.
			for (int j = 0; j < (values[2 * n]).length; j++)
			{
				assertEquals(values[2 * n][j], s.getY(j));
			}
			s = series.get(rows[n] + "<down>"); // check down series values.
			for (int j = 0; j < (values[2 * n + 1]).length; j++)
			{
				assertEquals(values[2 * n + 1][j], s.getY(j));
			}
		}
	}

}