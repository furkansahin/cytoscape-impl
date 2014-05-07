package org.cytoscape.ding.internal.charts.bar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;

public class BarChartEditor extends AbstractChartEditor<BarChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private JCheckBox stackedCkb;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public BarChartEditor(final BarChart chart, final CyApplicationManager appMgr) {
		super(chart, 10, true, true, true, true, false, true, appMgr);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getStackedCkb())
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getStackedCkb())
				);
		
		return p;
	}
	
	private JCheckBox getStackedCkb() {
		if (stackedCkb == null) {
			stackedCkb = new JCheckBox("Stacked");
			stackedCkb.setSelected(chart.get(BarChart.STACKED, Boolean.class, false));
			stackedCkb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chart.set(BarChart.STACKED, stackedCkb.isSelected());
					updateRangeMinMax(true);
				}
			});
		}
		
		return stackedCkb;
	}
}
