package snap.data;
import java.util.*;
import snap.util.StringUtils;

/**
 * Some useful methods for DataSite.
 */
public class DataSiteUtils {

/**
 * Returns a string of datasite tables.
 */
public static String showTables(DataSite aDataSite)
{
    StringBuffer sb = new StringBuffer();
    for(Entity entity : aDataSite.getSchema().getEntities())
        sb.append(entity.getName()).append("\n");
    return sb.toString();
}

/**
 * Execute select command.
 */
public static String executeSelect(DataSite aDataSite, String aCommand)
{
    // Get from index
    int from = StringUtils.indexOfIC(aCommand, "from");
    if(from<0)
        return "Syntax error";
    
    // Get entity
    String entityName = aCommand.substring(from + 4).trim();
    Entity entity = aDataSite.getSchema().getEntity(entityName);
    if(entity==null)
        return "Table not found";
    
    // Get properties
    List <Property> properties = new ArrayList();
    String props[] = aCommand.substring(0, from).split(",");
    for(String prop : props) {
        if(prop.trim().equals("*")) {
            properties.addAll(entity.getProperties());
            break;
        }
        Property property = entity.getProperty(prop.trim());
        if(property!=null)
            properties.add(property);
    }
    
    // Create string buffer
    StringBuffer sb = new StringBuffer();
    
    // Append headers
    for(Property prop : properties)
        sb.append(prop.getName()).append("\t");
    if(properties.size()>0) sb.delete(sb.length()-1, sb.length());
    sb.append("\n");

    // Get rows and append values
    List <Row> rows = aDataSite.getRows(new Query(entity));
    for(Row row : rows) {
        for(Property prop : properties)
            sb.append(row.get(prop.getName())).append("\t");
        if(properties.size()>0) sb.delete(sb.length()-1, sb.length());
        sb.append("\n");
    }
    
    // Return string
    return sb.toString();
}

}