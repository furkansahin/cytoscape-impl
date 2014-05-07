package org.cytoscape.ding.internal.charts;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public abstract class AbstractChartLayer<T extends Dataset> implements ImageCustomGraphicLayer {

	public static final int MAX_IMG_RESOLUTION = 3145728;
	public static final Color TRANSPARENT_COLOR = new Color(0x00, 0x00, 0x00, 0);
	
	/** Category ID -> list of values */
	protected final Map<String, List<Double>> data;
	protected final List<String> itemLabels;
	protected final List<String> domainLabels;
	protected final List<String> rangeLabels;
	protected final boolean showItemLabels;
	protected final boolean showDomainAxis;
	protected final boolean showRangeAxis;
	protected final List<Color> colors;
	protected DoubleRange range;
	
	protected Rectangle2D bounds;
	protected Rectangle2D scaledBounds;
	
	private JFreeChart chart;
	protected BufferedImage img;
	protected TexturePaint paint;
	
	protected AbstractChartLayer(final Map<String, List<Double>> data,
								 final List<String> itemLabels,
								 final List<String> domainLabels,
								 final List<String> rangeLabels,
								 final boolean showItemLabels,
								 final boolean showDomainAxis,
								 final boolean showRangeAxis,
								 final List<Color> colors,
								 final DoubleRange range,
								 final Rectangle2D bounds) {
		this.data = data;
		this.itemLabels = itemLabels;
		this.domainLabels = domainLabels;
		this.rangeLabels = rangeLabels;
		this.showItemLabels = showItemLabels;
		this.showDomainAxis = showDomainAxis;
		this.showRangeAxis = showRangeAxis;
		this.colors = colors;
		this.bounds = scaledBounds = bounds;
		this.range = range;
	}
	
	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	@Override
	public CustomGraphicLayer transform(final AffineTransform xform) {
		final Shape s = xform.createTransformedShape(bounds);
		System.out.println("[ transform ]: "+ s.getBounds2D());
		bounds = s.getBounds2D();
		
		return this;
	}

	@Override
	public TexturePaint getPaint(final Rectangle2D r) {
		System.out.println("> getPaint:  " + r);
		// If the bounds are the same as before, there is no need to recreate the "same" image again
		if (img == null || paint == null || !r.equals(scaledBounds)) {
			System.out.println("\tNEW IMAGE");
			// Recreate and cache Image and TexturePaint
			img = createImage(r);
			paint = new TexturePaintFactory(img).getPaint(
					new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
		}
		
		scaledBounds = r;
		
		return paint;
	}
	
	protected JFreeChart getChart() {
		if (chart == null) {
			final T dataset = createDataset();
			chart = createChart(dataset);
		}
		
		return chart;
	}

	protected BufferedImage createImage(final Rectangle2D r) {
		final Rectangle nr = validateBounds(r);
        
        return getChart().createBufferedImage(nr.width, nr.height, BufferedImage.TYPE_INT_ARGB, null);
	}
	
	protected Rectangle validateBounds(final Rectangle2D r) {
		double minScale = 1;
		final double minSize = 140;
		
		if ((r.getWidth() < minSize || r.getHeight() < minSize) && r.getWidth() > 4 && r.getHeight() > 4)
			minScale = minSize / Math.min(r.getWidth(), r.getHeight()); // Or the plot is not drawn/centered correctly;
		System.out.println("\tMIN SCALE =  " + minScale);
		
		int w = (int) Math.round(r.getWidth() * minScale);
		int h = (int) Math.round(r.getHeight() * minScale);
		
		// width * height needs to be less than Integer.MAX_VALUE,
		// but let's limit it to a much smaller resolution, to avoid performance issues and OutOfMemoryErrors
		final double resolution = (double)w * (double)h;
		
		if (resolution > MAX_IMG_RESOLUTION) {
			// (f*w)*(f*h) = MAX_IMG_RESOLUTION
			final double f = Math.sqrt(MAX_IMG_RESOLUTION / resolution); // new scale
			// The new scale may generate more images that are more pixelated, but that's fine,
			// because either the node size or the zoom level is exaggerated.
			w = (int) Math.round(w*f);
			h = (int) Math.round(h*f);
		}
		
		if (w <= 0) w = 1;
		if (h <= 0) h = 1;
		
		return new Rectangle(w, h);
	}
	
	protected abstract T createDataset();
	
	protected abstract JFreeChart createChart(final T dataset);
	
	public static CategoryDataset createCategoryDataset(final Map<String, List<Double>> data,
														final boolean listIsSeries,
														List<String> labels) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		if (listIsSeries && (labels == null || labels.isEmpty())) {
			int size = 0;
			
			for (final List<Double> values : data.values())
				size = Math.max(size, values.size());
			
			labels = createDefaultLabels(size);
		}
			
		int count = 0;
		
		for (String category : data.keySet()) {
			final List<Double> values = data.get(category);
			
			for (int i = 0; i < values.size(); i++) {
				final Double v = values.get(i);
				String k = "#" + (i+1); // row key
				
				if (listIsSeries) {
					if (labels != null && labels.size() > i)
						k = labels.get(i);
					
					dataset.addValue(v, category, k);
				} else {
					if (labels != null && labels.size() > count)
						category = labels.get(count); // Use label for category name
					
					dataset.addValue(v, k, category);
				}
			}
			
			count++;
		}
		
		return dataset;
	}
	
	// TODO minimumslice: The minimum size of a slice to be considered. All slices smaller than this are grouped together in a single "other" slice
	public static PieDataset createPieDataset(final List<Double> values, List<String> labels) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		
		if (values != null) {
			if (labels == null || labels.isEmpty())
				labels = createDefaultLabels(values.size());
			
			for (int i = 0; i < values.size(); i++) {
				final Double v = values.get(i);
				final String k = labels.size() > i ? labels.get(i) : "#"+(i+1);
				dataset.setValue(k, v);
			}
		}
		
		return dataset;
	}
	
	public static List<String> createDefaultLabels(final int size) {
		final List<String> labels = new ArrayList<String>(size);
		
		for (int i = 0; i < size; i++)
			labels.add("#" + (i+1));
		
		return labels;
	}
	
	public static DoubleRange calculateRange(final Collection<List<Double>> lists, final boolean stacked) {
		DoubleRange range = null;
		
		if (lists != null && !lists.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for (final List<Double> values : lists) {
				double sum = 0;
				
				if (values != null) {
					for (final double v : values) {
						if (stacked) {
							sum += v;
						} else {
							min = Math.min(min, v);
							max = Math.max(max, v);
						}
					}
					
					if (stacked) {
						min = Math.min(min, sum);
						max = Math.max(max, sum);
					}
				}
				
				if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY)
					range = new DoubleRange(min, max);
			}
		}
		
		return range;
	}
}
