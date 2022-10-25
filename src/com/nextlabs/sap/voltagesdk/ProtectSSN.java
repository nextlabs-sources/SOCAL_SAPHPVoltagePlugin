package com.nextlabs.sap.voltagesdk;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.crypt.Encryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.voltage.vibesimple.AuthMethod;
import com.voltage.vibesimple.VibeSimpleSOAPStub;
import com.voltage.vibesimple.VibeSimple_ServiceLocator;

public class ProtectSSN {
	VibeSimpleSOAPStub service;
	URL serviceURL;
	AuthMethod authMethod;
	String authInfo;
	private static final Log LOG = LogFactory.getLog(ProtectSSN.class);

	public void init() {
		PropertyLoader loader = new PropertyLoader();
		Properties prop = loader.loadProperties();
		ReversibleEncryptor re = new ReversibleEncryptor();
		String endpointurl = prop.getProperty("Voltage_SOAP_Endpoint");
		String propAuthMethod = prop.getProperty("Voltage_AuthMethod");
		String propauthinfo = re.decrypt(prop.getProperty("Voltage_AuthInfo"));

		try {
			serviceURL = new URL(endpointurl);
			service = (VibeSimpleSOAPStub) new VibeSimple_ServiceLocator().getVibeSimpleSOAP(serviceURL);
			if (propAuthMethod.equalsIgnoreCase("SharedSecret")) {
				authMethod = AuthMethod.SharedSecret;
			} else if (propAuthMethod.equalsIgnoreCase("UserPassword")) {
				authMethod = AuthMethod.UserPassword;
			} else if (propAuthMethod.equalsIgnoreCase("Certificate")) {
				authMethod = AuthMethod.Certificate;
			}
			authInfo = propauthinfo;
		} catch (MalformedURLException e) {
			LOG.info("SAPVOLTAGEPLUGIN ::URL is incorrect or Malformed: " + e.getMessage());
		} catch (ServiceException e) {
			LOG.info("SAPVOLTAGEPLUGIN ::Service is not initialized properly : " + e.getMessage());
		}
		LOG.info("SAPVOLTAGEPLUGIN ::Voltage Service initialized Successfully");
	}

	public String getCipherSSN(String plainSSN, String userid) throws RemoteException {
		String cipherSSN = service.protectSocialSecurityNumber(plainSSN, userid, null, authMethod, authInfo);
		LOG.info("SAPVOLTAGEPLUGIN ::Cipher SSN: " + cipherSSN);
		return cipherSSN;
	}

	public String getPlainSSN(String cipherSSN, String userid) throws RemoteException {
		String plainSSN = service.accessSocialSecurityNumber(cipherSSN, userid, null, false, authMethod, authInfo);
		LOG.info("SAPVOLTAGEPLUGIN ::Plain SSN: " + plainSSN);
		return plainSSN;
	}

	public static void main(String args[]) {
		ProtectSSN pssn = new ProtectSSN();
		//pssn.init();
			ReversibleEncryptor e = new ReversibleEncryptor();
			System.out.println(e.encrypt("voltage123"));
		//	String cipherssn = pssn.getCipherSSN("111-11-1111", "abc@yahoo.iin");
		//	pssn.getPlainSSN(cipherssn, "abc@yahoo.iin");
	}
}
