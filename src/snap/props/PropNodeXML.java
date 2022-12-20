package snap.props;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import java.util.List;

/**
 * This utility class converts PropNode to/from XMLElement.
 */
public class PropNodeXML {

    /**
     * Converts a given PropNode to XML element.
     */
    public static XMLElement convertPropNodeToXML(PropNode aPropNode, String aNodeKey)
    {
        // Create XML for PropNode
        XMLElement xml = new XMLElement(aNodeKey);

        // If PropNode.NeedsClassDeclaration, add Class attribute to XML
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            if (!className.equals(aNodeKey))
                xml.add(PropArchiver.CLASS_KEY, className);
        }

        // Get configured Props
        String[] propNames = aPropNode.getPropNames();

        // Iterate over PropNames and add XML for each
        for (String propName : propNames) {

            // Get Node value and whether it is node and/or array
            Object nodeValue = aPropNode.getPropValue(propName);
            boolean isRelation = nodeValue instanceof PropNode || nodeValue instanceof PropNode[];
            boolean isArray = nodeValue != null && nodeValue.getClass().isArray();

            // Handle null
            if (nodeValue == null)
                xml.add(propName, "null");

            // Handle Relation prop
            else if (isRelation) {

                // Handle array
                if (isArray) {
                    PropNode[] arrayNodes = (PropNode[]) nodeValue;
                    XMLElement arrayXML = new XMLElement(propName);
                    for (PropNode arrayNode : arrayNodes) {
                        String className = arrayNode.getClassName();
                        XMLElement arrayNodeXML = convertPropNodeToXML(arrayNode, className);
                        arrayXML.addElement(arrayNodeXML);
                    }
                    xml.addElement(arrayXML);
                }

                // Handle simple relation
                else {
                    PropNode relNode = (PropNode) nodeValue;
                    XMLElement nodeXML = convertPropNodeToXML(relNode, propName);
                    xml.addElement(nodeXML);
                }
            }

            // Handle primitive array
            else if (isArray) {
                String arrayStr = aPropNode.getPropValueAsString(propName);
                XMLElement arrayXML = new XMLElement(propName);
                arrayXML.setValue(arrayStr);
                xml.addElement(arrayXML);
            }

            // Handle String (non-Node) prop
            else {
                String stringValue = aPropNode.getPropValueAsString(propName);
                xml.add(propName, stringValue);
            }
        }

        // Return
        return xml;
    }

    /**
     * Converts a given XML element to PropNode.
     */
    public static PropNode convertXMLToPropNode(XMLElement anElement)
    {
        // Create PropNode for XML element
        PropNode propNode = new PropNode();

        // Get attributes
        List<XMLAttribute> attributes = anElement.getAttributes();

        // Iterate over attributes and add to PropNode
        for (XMLAttribute attribute : attributes) {

            String propName = attribute.getName();
            propNode.setPropValue(propName, attribute.getValue());
        }

        // Get elements
        List<XMLElement> elements = anElement.getElements();

        // Iterate over elements and add to PropNode
        for (XMLElement element : elements) {

            // If child element is definitely an array element, convert to PropNode array and set that
            if (isDefinitelyArrayElement(element)) {
                PropNode[] arrayNodes = convertXMLToPropNodeArray(element);
                propNode.setPropValue(element.getName(), arrayNodes);
                continue;
            }

            // If element has value, assume it is primitive array
            String propName = element.getName();
            String xmlValue = element.getValue();
            if (xmlValue != null) {
                propNode.setPropValue(propName, xmlValue);
                continue;
            }

            // Get child xml as PropNode and set in parent
            PropNode relation = convertXMLToPropNode(element);
            relation.setXmlName(propName);
            propNode.setPropValue(propName, relation);
        }

        // Return xml
        return propNode;
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
     * Returns an array of propNodes
     */
    private static PropNode[] convertXMLToPropNodeArray(XMLElement anElement)
    {
        // Get elements and create PropNodes array
        List<XMLElement> elements = anElement.getElements();
        int elementCount = elements.size();
        PropNode[] propNodes = new PropNode[elementCount];

        // Iterate over elements, convert each and add to PropNode array
        for (int i = 0; i < elementCount; i++) {

            // Get child xml as PropNode and set in parent
            XMLElement element = elements.get(i);
            PropNode relation = convertXMLToPropNode(element);
            String propName = element.getName();
            relation.setXmlName(propName);

            // Add relation to array
            propNodes[i] = relation;
        }

        // Return
        return propNodes;
    }
}
