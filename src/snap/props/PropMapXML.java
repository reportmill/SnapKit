package snap.props;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import java.util.List;

/**
 * This utility class converts PropMap to/from XMLElement.
 */
public class PropMapXML {

    /**
     * Converts a given PropMap to XML element.
     */
    public static XMLElement convertPropMapToXML(PropMap propMap, String aNodeKey)
    {
        // Create XML for PropMap
        XMLElement xml = new XMLElement(aNodeKey);

        // If PropMap.NeedsClassDeclaration, add Class attribute to XML
        if (propMap.isNeedsClassDeclaration()) {
            String className = propMap.getClassName();
            if (!className.equals(aNodeKey))
                xml.add(PropArchiver.CLASS_KEY, className);
        }

        // Get configured Props
        String[] propNames = propMap.getPropNames();

        // Iterate over PropNames and add XML for each
        for (String propName : propNames) {

            // Get map value and whether it is prop map and/or array
            Object mapValue = propMap.getPropValue(propName);
            boolean isRelation = mapValue instanceof PropMap || mapValue instanceof PropMap[];
            boolean isArray = mapValue != null && mapValue.getClass().isArray();

            // Handle null
            if (mapValue == null)
                xml.add(propName, "null");

            // Handle Relation prop
            else if (isRelation) {

                // Handle array
                if (isArray) {
                    PropMap[] arrayNodes = (PropMap[]) mapValue;
                    XMLElement arrayXML = new XMLElement(propName);
                    for (PropMap arrayNode : arrayNodes) {
                        String className = arrayNode.getClassName();
                        XMLElement arrayNodeXML = convertPropMapToXML(arrayNode, className);
                        arrayXML.addElement(arrayNodeXML);
                    }
                    xml.addElement(arrayXML);
                }

                // Handle simple relation
                else {
                    PropMap relNode = (PropMap) mapValue;
                    XMLElement nodeXML = convertPropMapToXML(relNode, propName);
                    xml.addElement(nodeXML);
                }
            }

            // Handle primitive array
            else if (isArray) {
                String arrayStr = propMap.getPropValueAsString(propName);
                XMLElement arrayXML = new XMLElement(propName);
                arrayXML.setValue(arrayStr);
                xml.addElement(arrayXML);
            }

            // Handle String (non-Node) prop
            else {
                String stringValue = propMap.getPropValueAsString(propName);
                xml.add(propName, stringValue);
            }
        }

        // Return
        return xml;
    }

    /**
     * Converts a given XML element to PropMap.
     */
    public static PropMap convertXMLToPropMap(XMLElement anElement)
    {
        // Create PropMap for XML element
        PropMap propMap = new PropMap();

        // Get attributes
        List<XMLAttribute> attributes = anElement.getAttributes();

        // Iterate over attributes and add to PropMap
        for (XMLAttribute attribute : attributes) {

            String propName = attribute.getName();
            propMap.setPropValue(propName, attribute.getValue());
        }

        // Get elements
        List<XMLElement> elements = anElement.getElements();

        // Iterate over elements and add to PropMap
        for (XMLElement element : elements) {

            // If child element is definitely an array element, convert to PropMap array and set that
            if (isDefinitelyArrayElement(element)) {
                PropMap[] arrayNodes = convertXMLToPropMapArray(element);
                propMap.setPropValue(element.getName(), arrayNodes);
                continue;
            }

            // If element has value, assume it is primitive array
            String propName = element.getName();
            String xmlValue = element.getValue();
            if (xmlValue != null) {
                propMap.setPropValue(propName, xmlValue);
                continue;
            }

            // Get child xml as PropMap and set in parent
            PropMap relation = convertXMLToPropMap(element);
            relation.setXmlName(propName);
            propMap.setPropValue(propName, relation);
        }

        // Return xml
        return propMap;
    }

    /**
     * Returns whether given XML element is definitely an array. Only true if any child element has redundant name.
     */
    private static boolean isDefinitelyArrayElement(XMLElement anElement)
    {
        // If element has attributes or value, it isn't array
        if (anElement.getAttributeCount() > 0 || anElement.getValue() != null)
            return false;

        // If child elements don't have distinct names, it's an array
        List<XMLElement> elements = anElement.getElements();
        int elementCount = elements.size();
        long distinctCount = elements.stream().map(xml -> xml.getName()).distinct().count();
        return distinctCount != elementCount;
    }

    /**
     * Returns an array of PropMaps
     */
    private static PropMap[] convertXMLToPropMapArray(XMLElement anElement)
    {
        // Get elements and create PropMaps array
        List<XMLElement> elements = anElement.getElements();
        int elementCount = elements.size();
        PropMap[] propMaps = new PropMap[elementCount];

        // Iterate over elements, convert each and add to PropMap array
        for (int i = 0; i < elementCount; i++) {

            // Get child xml as PropMap and set in parent
            XMLElement element = elements.get(i);
            PropMap relation = convertXMLToPropMap(element);
            String propName = element.getName();
            relation.setXmlName(propName);

            // Add relation to array
            propMaps[i] = relation;
        }

        // Return
        return propMaps;
    }
}
