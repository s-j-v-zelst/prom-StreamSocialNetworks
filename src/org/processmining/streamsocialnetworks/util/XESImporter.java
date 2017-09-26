package org.processmining.streamsocialnetworks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.impl.ProgressBarImpl;
import org.processmining.plugins.log.XContextMonitoredInputStream;

public class XESImporter {

	public static XLog importXLog(File f) {
		GlobalContext gc = new CLIContext();
		PluginContext pc = new CLIPluginContext(gc, "dummy");
		try {
			return importFromStream(pc, new FileInputStream(f), f.getName(), f.length(),
					XFactoryRegistry.instance().currentDefault());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static XLog importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes, XFactory factory) throws Exception {
		//	System.out.println("Open file");
		XParser parser = new XesXmlParser(factory);
		Collection<XLog> logs = null;
		Exception firstException = null;
		String errorMessage = "";
		try {
			logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, new ProgressBarImpl(context)));
		} catch (Exception e) {
			logs = null;
			firstException = e;
			errorMessage = errorMessage + e;
		}
		if (logs == null) {
			throw new Exception("Could not open log file, possible cause: "
					/* + errorMessage, */ + firstException);
		}
		if (logs.size() == 0) {
			throw new Exception("No processes contained in log!");
		}

		XLog log = logs.iterator().next();
		if (XConceptExtension.instance().extractName(log) == null) {
			XConceptExtension.instance().assignName(log, "Anonymous log imported from " + filename);
		}
		return log;

	}

}
