package mysquare.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Utility {

	InputStream inputStream;

	public HashMap<String, String> getProperties() throws IOException {
		Properties prop = new Properties();
		HashMap<String, String> properties = new HashMap<String, String>();

		try {
			FileInputStream ip = new FileInputStream(configFilePath());
			if (ip != null) {
				prop.load(ip);
				System.out.println(System.getProperty("os.name"));
			} else {
				throw new FileNotFoundException("Property file not found.");
			}

			properties.put("dbSource", prop.getProperty("DB_PATH"));
			properties.put("dbDriver", prop.getProperty("DB_DRIVER"));
			properties.put("mpAccessToken", prop.getProperty("MP_ACCESS_TOKEN", ""));
			properties.put("mpPayerEmail", prop.getProperty("MP_PAYER_EMAIL_PADRAO", ""));

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		return properties;
	}

	/**
	 * Windows keeps the original fixed path the production install relies on.
	 * Any other OS (used for local dev/testing on Linux/macOS) reads from the
	 * user's home directory instead, since "C:/..." isn't a real path there.
	 */
	private static String configFilePath() {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("win")) {
			return "C:/ims_files/config.properties";
		}
		return System.getProperty("user.home") + "/ims_files/config.properties";
	}

	public String[] getProductList() {
		ArrayList<String> productList = Db.fetchPList();
	    String[] productArr = productList.toArray(new String[productList.size()]);
	    return productArr;
	}
	
	public String[] getColourList() {
		ArrayList<String> colourList = Db.fetchCList();
	    String[] colourArr = colourList.toArray(new String[colourList.size()]);
	    return colourArr;
   	}
	
	public String[] getWeightList() {
		ArrayList<String> weightList = Db.fetchWList();
		String[] weightArr = weightList.toArray(new String[weightList.size()]);
		return weightArr;
	}
}
