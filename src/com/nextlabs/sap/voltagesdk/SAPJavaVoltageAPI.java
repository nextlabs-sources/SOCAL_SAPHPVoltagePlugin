package com.nextlabs.sap.voltagesdk;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProvider;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerState;

public class SAPJavaVoltageAPI implements IServiceProvider {

	private static final Log LOG = LogFactory.getLog(SAPJavaVoltageAPI.class);

	@Override
	public void init() throws Exception {

		LOG.info("SAPJavaRMAPI init() started.");
		PropertyLoader loader = new PropertyLoader();
		Properties prop = loader.loadProperties();
		ProtectSSN ssn = new ProtectSSN();
		ssn.init();
		try {
			LOG.info("SAPVOLTAGEAPI: sample ssn check " + ssn.getCipherSSN("111-11-1111", "abc@yahoo.co.id"));
		} catch (Exception e) {
			LOG.error(e);
		}

		setupServer(prop);
	}

	static void setupServer(Properties allProps) {

		LOG.info("SAPJavaRMAPI :: setupServer() started");
		String[] serv_prefixes = allProps.getProperty("server_prefix").split(";");
		int noOfServers = serv_prefixes.length;
		LOG.info("No of Server instances to be created : " + noOfServers);
		JCoServer[] servers = new JCoServer[noOfServers];

		for (int i = 0; i < noOfServers; i++) {
			String serverName = serv_prefixes[i];
			try {
				LOG.info("SAPJavaRMAPI :: Setting up server - " + serverName);

				servers[i] = JCoServerFactory.getServer(serverName);

				// Register one function handler for each rmapi_hanlder

				String[] handlers = allProps.getProperty("rmapi_handler", "").split(",");
				DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();

				for (String handler : handlers) {
					LOG.info("SAPJavaRMAPI :: Creating handler" + handler + " object for " + serverName);
					JCoServerFunctionHandler functionHandler = new SAPVoltageAPIHandler(allProps);

					factory.registerHandler(handler.trim(), functionHandler);
				}

				LOG.info("SAPJavaRMAPI :: Server state for server " + serverName + " is " + servers[i].getState());

				// Force to restart to register handler
				if (servers[i].getState() == JCoServerState.STARTED || servers[i].getState() == JCoServerState.ALIVE) {

					LOG.info("SAPJavaRMAPI :: Stopping server " + serverName);
					servers[i].stop();

					int k = 0;

					while (servers[i].getState() != JCoServerState.STOPPED
							|| servers[i].getState() == JCoServerState.ALIVE || k < 10) {
						LOG.debug(
								"SAPJavaRMAPI :: Server " + serverName + " current state is " + servers[i].getState());
						Thread.currentThread();
						Thread.sleep(200);
						k++;
					}

					servers[i].setCallHandlerFactory(factory);

					servers[i].setRepository(JCoDestinationManager.getDestination(serverName).getRepository());

					servers[i].start();

				} else {
					LOG.info("SAPJavaRMAPI :: Server state is STOPPED. Starting server");

					servers[i].setCallHandlerFactory(factory);

					servers[i].setRepository(JCoDestinationManager.getDestination(serverName).getRepository());

					servers[i].start();
				}

				LOG.info("SAPJavaRMAPI :: Server - " + serverName + " started ");

			} catch (JCoException e) {
				LOG.error("SAPJavaRMAPI :: Unable to create the server " + serverName + " because of error", e);
			} catch (Exception ee) {
				LOG.error("SAPJavaRMAPI :: Unable to create the server " + serverName + " because of error", ee);
			}
		}
	}

	public static void main(String[] args) {
		SAPJavaVoltageAPI saprmapi = new SAPJavaVoltageAPI();
		try {
			saprmapi.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
