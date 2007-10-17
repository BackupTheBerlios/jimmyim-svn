package jimmy.util;

import java.util.Hashtable;
import java.util.Vector;

public class XmlNode
{
  public static final String ROOT = "ROOT";
  
  public String name;
  public String value;
  public Hashtable attribs = new Hashtable();
  public Vector childs = new Vector();
  
  private XmlNode(){}
  
  private XmlNode(String block)
  {
    name = getName(block);
    parseAttributes(block);
    parseChilds(removeCurrTag(block));
  }
  
  public static XmlNode parse(String in)
  {
    if (in == null || in.equals(""))
      return null;
    System.out.println(in);
    
    in = removeXmlHeader(in).trim();
    
    XmlNode xml = new XmlNode();
    xml.parseChilds(in);
    xml.name = ROOT;
    
    return xml;
  }

  private void parseAttributes(String in)
  {
    int st = in.indexOf(" ");
    int end = in.indexOf(">");
    
    if (st < 0 || st > end) return;
    
    String attrib = in.substring(
        st + 1, 
        in.indexOf(">")).trim();
    attrib = attrib.endsWith("/") ? attrib.substring(0, attrib.length() - 1) : attrib;
    
    while (!attrib.equals(""))
    {
      String name = attrib.substring(0, attrib.indexOf("="));
      int valI = attrib.indexOf("\"");
      int valI2 = 0;
      if (valI < 0)
      {
        valI = attrib.indexOf("'");
        valI2 = attrib.indexOf("'", valI + 1);
      }
      else
        valI2 = attrib.indexOf("\"", valI + 1);
      
      String value = attrib.substring(
          valI + 1,
          valI2);
      
      attribs.put(name, value);
      attrib = attrib.substring(name.length() + value.length() + 3, attrib.length()).trim();
    }
  }

  private void parseChilds(String in)
  {
    if (!Utils.stringContains(in, "<"))
    {
      value = in;
      return;
    }
    
    while (!in.equals(""))
    {
      String block = getNextNode(in, getName(in));
      childs.addElement(new XmlNode(block));
      
      in = in.substring(block.length(), in.length()).trim();
    }
  }

  private String removeCurrTag(String in)
  {
    int st = in.indexOf(">");
    if (in.length() == st + 1) return "";
    int end = in.length();
    
    end = in.charAt(st - 1) != '/' ? in.indexOf("</" + name) : end;
    
    return in.substring(
        st + 1, 
        end).trim();
  }
  
  public Vector getNodesByName(String name)
  {
    Vector v = new Vector();
    
    for (int i = 0; i < childs.size(); i++)
    {
      if (((XmlNode)childs.elementAt(i)).name.equals(name))
        v.addElement(childs.elementAt(i));
      copyVector(((XmlNode)childs.elementAt(i)).getNodesByName(name), v);
    }
    
    return v;
  }
  
  public XmlNode getFirstNode(String name)
  {
    Vector v = getNodesByName(name);
    return v.size() == 0 ? null : (XmlNode)v.elementAt(0);
  }
  
  public boolean contains(String name)
  {
    return getNodesByName(name).size() != 0;
  }

  private static String removeXmlHeader(String in)
  {
    if (in.startsWith("<?"))
      return in.substring(in.indexOf("?>") + 2, in.length());
    return in;
  }
  
  private static String getNextNode(String in, String name)
  {
    if (in.charAt(in.indexOf(">") - 1) == '/')
      return in.substring(0, in.indexOf(">") + 1);
    else if (Utils.stringContains(in, "</" + name + ">"))
      return in.substring(0, in.indexOf("</" + name + ">") + name.length() + 3);
    else return in.substring(0, in.indexOf(">") + 1);
  }

  private static String getName(String in)
  {
    int end = in.indexOf(">");
    int space = in.indexOf(" ");
    end = end < space || space < 0 ? end : space;
    end = in.charAt(end - 1) == '/' ? end - 1 : end;
    
    return in.substring(
        1, 
        end);
  }
  
  private static void copyVector(Vector from, Vector to)
  {
    for (int i = 0; i < from.size(); i++)
      to.addElement(from.elementAt(i));
  }
}