package com.nextlabs.sap.voltagesdk;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

public class SAPVoltageAPIHandler implements JCoServerFunctionHandler {

	private static final Log LOG = LogFactory.getLog(SAPVoltageAPIHandler.class);
	private Properties allProps;
	private String delimiter = "|";
	private String delimiterEscape = "\\|";

	public SAPVoltageAPIHandler(Properties allProps) {
		super();
		this.allProps = allProps;
		if (allProps != null) {
			delimiter = allProps.getProperty("delimiter", "|");
			delimiterEscape = "\\" + allProps.getProperty("delimiter", "|");
		}
	}

	@Override
	public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
			throws AbapException, AbapClassException {
		LOG.info("SAPVOLTAGEPLUGIN :: handleRequest() SAP Java VOLTAGE API request received");

		String userSSN = function.getImportParameterList().getString("IM_SSN");
		String userId = function.getImportParameterList().getString("IM_PERNR");
		String operation = function.getImportParameterList().getString("IM_OPERATION");
		LOG.info("SAPVOLTAGEPLUGIN :: handleRequest() userSSN from SAP " + userSSN);
		LOG.info("SAPVOLTAGEPLUGIN :: handleRequest() userId from SAP " + userId);
		LOG.info("SAPVOLTAGEPLUGIN :: handleRequest() operation from SAP " + operation);
		ProtectSSN pssn = new ProtectSSN();
		String resultSSN = "";
		try {
			pssn.init();
			LOG.info("SAPVOLTAGEPLUGIN ::  Voltage Initialized");
			if (operation.equalsIgnoreCase("HPV_ENCRYPT")) {
				resultSSN = pssn.getCipherSSN(userSSN, userId);
			} else if (operation.equalsIgnoreCase("HPV_DECRYPT")) {
				resultSSN = pssn.getPlainSSN(userSSN, userId);
			}

			function.getExportParameterList().setValue("EXP_SSN", resultSSN);

			LOG.info(
					"SAPVOLTAGEPLUGIN handleRequest() Finished converting ssn " + userSSN + " to new SSn " + resultSSN);
		} catch (Exception e) {
			LOG.info("SAPVOLTAGEPLUGIN error():" + e.getMessage());
			JCoTable resultTable = function.getExportParameterList().getTable("ET_RETURN");
			resultTable.appendRow();
			resultTable.setValue("TYPE", "F");
			resultTable.setValue("MESSAGE", "Voltage server was down");
		}
		JCoTable resultTable = function.getExportParameterList().getTable("ET_RETURN");
		resultTable.appendRow();
		resultTable.setValue("TYPE", "S");
		resultTable.setValue("MESSAGE", "Encryption done successfully");
	}

	public static void main(String[] args) {
		Properties props = new Properties();
		try {
			File file = new File("C:/P4/plugins/SAP_JCo_RM/main/lib/SAPJavaTRMAPIService.properties");
			LOG.info("Properties File Path:: " + file.getAbsolutePath());
			System.out.println("Properties File Path:: " + file.getAbsolutePath());
			if (file != null) {
				FileInputStream fis = new FileInputStream(file);
				props = new Properties();
				props.load(fis); // Can throw IOException
				fis.close();
			}
		} catch (Exception e) {
			LOG.error("Error parsing properties file ", e);
			props = null;
		}
		SAPVoltageAPIHandler handler = new SAPVoltageAPIHandler(props);

	}
}
