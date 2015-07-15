import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.parser.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlParser {
	private ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();

	public void xmlParser(Logger logger, ArrayList<String> logs) {
		Object xmlJSONObj;

		try {
			for (int i = 0; i < logs.size(); i++) {
				StringBuilder sb = new StringBuilder();

				BufferedReader br = new BufferedReader(new FileReader(
						logs.get(i)));
				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					sb.append(sCurrentLine.trim());
				}

				br.close();

				if (validateXMLSchema(Constants.xsdPath, sb.toString()))
					xmlJSONObj = (JSONObject) XML.toJSONObject(sb.toString());
				else
					xmlJSONObj = (String) sb.toString();

				if (xmlJSONObj instanceof JSONObject) {
					ArrayList<JSONObject> list = parse((JSONObject) xmlJSONObj);

					for (int j = 0; j < list.size(); j++)
						logger.info(list.get(j).toString());
				} else {
					logger.info(xmlJSONObj.toString());
				}

			}
		} catch (IOException | JSONException | ParseException e) {
			e.printStackTrace();
		} 
	}

	private ArrayList<JSONObject> parse(JSONObject json) throws JSONException,
			ParseException {

		Iterator<String> keys = json.keys();
		JSONObject commonElements = new JSONObject();

		while (keys.hasNext()) {
			String key = keys.next();
			try {
				JSONObject originalJSON = json.getJSONObject(key.toString());
				parse(originalJSON);
				commonElements = originalJSON;
			} catch (JSONException e) {
				if (Constants.TESTCASE.equalsIgnoreCase(key.toString())) {
					JSONArray jsonarr = json.getJSONArray(key.toString());
					for (int n = 0; n < jsonarr.length(); n++) {
						JSONObject object = jsonarr.getJSONObject(n);

						JSONObject jsonFinal = new JSONObject();
						jsonFinal.put(key, object);

						jsonObjects.add(jsonFinal);

					}

				}
				

			}
		}
		commonElements.remove(Constants.TESTCASE);
		return merge(commonElements, jsonObjects);
	}

	private ArrayList<JSONObject> merge(JSONObject obj1,
			ArrayList<JSONObject> obj2) throws JSONException {
		ArrayList<JSONObject> arr = new ArrayList<JSONObject>();

		for (int i = 0; i < obj2.size(); i++) {
			JSONObject json = new JSONObject();
			json.append(Constants.TESTSUITE, obj1);
			json.append(Constants.TESTSUITE, obj2.get(i));
			arr.add(json);

		}

		return arr;

	}
	
	private boolean validateXMLSchema(String xsdPath, String xmlString){
        
        try {
            SchemaFactory factory = 
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlString)));
        } catch (IOException | SAXException e) {
        	e.printStackTrace();
            return false;
        }
        return true;
    }
}

