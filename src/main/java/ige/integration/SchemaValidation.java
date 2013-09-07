package ige.integration;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;



 
public class SchemaValidation {
 
    public static void main(String[] args) {
         
      System.out.println("requestPayload.xml validates against requestPayload.xsd? "+validateXMLSchema("C:\\requestPayload.xsd", "C:\\requestPayload.xml"));
      System.out.println("responsePayload.xml validates against responsePayload.xsd? "+validateXMLSchema("C:\\responsePayload.xsd", "C:\\responsePayload.xml"));
      //System.out.println("employee.xml validates against Employee.xsd? "+validateXMLSchema("Employee.xsd", "employee.xml"));
       
      }
     
    public static boolean validateXMLSchema(String xsdPath, String xmlPath){
         
        try {
            SchemaFactory factory = 
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }
}
