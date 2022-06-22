/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.FormatUtils;
import snap.util.XMLElement;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverXML extends PropArchiver {

    /**
     * Converts a PropObject to XML.
     */
    public XMLElement convertPropObjectToXML(PropObject aPropObject)
    {
        PropNode propNode = convertPropObjectToPropNode(aPropObject);
        XMLElement xml = convertPropNodeToXML(propNode);
        return xml;
    }

    /**
     * Converts a PropObject to XML.
     */
    public byte[] convertPropObjectToXMLBytes(PropObject aPropObject)
    {
        XMLElement xml = convertPropObjectToXML(aPropObject);
        byte[] xmlBytes = xml.getBytes();
        return xmlBytes;
    }

    /**
     * Returns XML for PropNode.
     */
    protected XMLElement convertPropNodeToXML(PropNode aPropNode)
    {
        // Create XML element for PropNode
        String xmlName = aPropNode.getClassName();
        XMLElement xml = new XMLElement(xmlName);

        // Get PropNode entries
        Set<Map.Entry<String,Object>> propValues = aPropNode.getPropValues();

        // Iterate over child entries and add XML attribute or child element
        for (Map.Entry<String,Object> entry : propValues) {
            String propName = entry.getKey();
            Object propValue = entry.getValue();
            addNameAndValueToXML(xml, propName, propValue);
        }

        // Return xml
        return xml;
    }

    /**
     * Adds XML for given prop name/value pair.
     */
    protected void addNameAndValueToXML(XMLElement xml, String propName, Object propValue)
    {
        // Handle null
        if (propValue == null) {
            xml.add(propName, propValue);
        }

        // Handle PropNode
        else if (propValue instanceof PropNode) {
            PropNode propNode = (PropNode) propValue;
            XMLElement propXML = convertPropNodeToXML(propNode);
            xml.addElement(propXML);
        }

        // Handle array
        else if (propValue instanceof Object[]) {

            // Get array
            Object[] array = (Object[]) propValue;

            // Create child XML
            XMLElement propXML = new XMLElement(propName);
            xml.addElement(propXML);

            // Handle PropNode array
            if (array.length > 0 && array[0] instanceof PropNode) {
                for (Object obj : array) {
                    PropNode childNode = (PropNode) obj;
                    XMLElement childXML = convertPropNodeToXML(childNode);
                    propXML.addElement(childXML);
                }
            }

            // Handle primitive array
            else {
                String valStr = Arrays.toString(array);
                propXML.setValue(valStr);
            }
        }

        // Handle primitive value
        else {

            // If float/double, format to avoid many decimals
            if (propValue instanceof Double || propValue instanceof Float)
                propValue = FormatUtils.formatNum((Number) propValue);

            // Add prop
            xml.add(propName, propValue);
        }
    }
}
