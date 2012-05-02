package org.cytoscape.internal.shutdown;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.io.FileOutputStream;


public class ConfigDirPropertyWriter implements CyShutdownListener {
	private final DialogTaskManager taskManager;
	private final Map<CyProperty, Map> configDirProperties;
	private final CyApplicationConfiguration config;
	private static final Logger logger = LoggerFactory.getLogger(ConfigDirPropertyWriter.class);

	public ConfigDirPropertyWriter(final DialogTaskManager taskManager, final CyApplicationConfiguration config)
	{
		this.taskManager = taskManager;
		this.config = config;
		configDirProperties = new HashMap<CyProperty, Map>();
	}

	public void handleEvent(final CyShutdownEvent event) {
		
		for (final Map.Entry<CyProperty, Map> keyAndValue : configDirProperties.entrySet()) {
			final String propertyName = (String)keyAndValue.getValue().get("cyPropertyName");
			final String propertyFileName;
			if(propertyName.endsWith(".props"))
				propertyFileName = propertyName;
			else
				propertyFileName = propertyName + ".props";
			
			final File outputFile = new File(config.getConfigurationDirectoryLocation(), propertyFileName);
			
			final Properties props = (Properties) keyAndValue.getKey().getProperties();

			try {
				FileOutputStream out = new FileOutputStream(outputFile);
				props.store(out, null);
				out.close();
			}
			catch(Exception e){
				logger.error("Error in wring properties file!");
			}
		}
	}

	public void addCyProperty(final CyProperty newCyProperty, final Map properties) {
		if (newCyProperty.getSavePolicy() == CyProperty.SavePolicy.CONFIG_DIR
				|| newCyProperty.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.put(newCyProperty, properties);
	}

	public void removeCyProperty(final CyProperty oldCyProperty, final Map properties) {
		if (oldCyProperty.getSavePolicy() == CyProperty.SavePolicy.CONFIG_DIR
		    || oldCyProperty.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.remove(oldCyProperty);
		
	}
}
