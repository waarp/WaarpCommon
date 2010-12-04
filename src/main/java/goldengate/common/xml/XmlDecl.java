/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package goldengate.common.xml;


/**
 * XmlDecl to declare types, path and name for values from/to XML file/document
 * 
 * @author Frederic Bregier
 * 
 */
public class XmlDecl {
    private String name;

    private XmlType type;

    private String xmlPath;

    private XmlDecl[] subXml;

    private boolean isMultiple;

    public XmlDecl(String name, XmlType type, String xmlPath, boolean isMultiple) {
        this.name = name;
        this.type = type;
        this.xmlPath = xmlPath;
        this.isMultiple = isMultiple;
        this.subXml = null;
    }

    public XmlDecl(String name, XmlType type, String xmlPath, XmlDecl[] decls,
            boolean isMultiple) {
        this.name = name;
        this.type = type;
        this.xmlPath = xmlPath;
        this.isMultiple = isMultiple;
        this.subXml = decls;
    }

    /**
     * Get Java field name
     * 
     * @return the field name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the class type
     */
    public Class<?> getClassType() {
        return type.getClassType();
    }

    /**
     * @return the internal type
     */
    public XmlType getType() {
        return type;
    }

    /**
     * @return the xmlPath
     */
    public String getXmlPath() {
        return xmlPath;
    }

    /**
     * 
     * @return True if this Decl is a subXml
     */
    public boolean isSubXml() {
        return this.subXml != null;
    }

    /**
     * @return the subXml
     */
    public XmlDecl[] getSubXml() {
        return subXml;
    }

    /**
     * @return the subXml size
     */
    public int getSubXmlSize() {
        if (subXml == null) return 0;
        return subXml.length;
    }

    /**
     * @return the isMultiple
     */
    public boolean isMultiple() {
        return isMultiple;
    }

    /**
     * Check if two XmlDecl are compatible
     * 
     * @param xmlDecl
     * @return True if compatible
     */
    public boolean isCompatible(XmlDecl xmlDecl) {
        if (((isMultiple && xmlDecl.isMultiple) || ((!isMultiple) && (!xmlDecl.isMultiple))) &&
                ((isSubXml() && xmlDecl.isSubXml()) || ((!isSubXml()) && (!xmlDecl
                        .isSubXml())))) {
            if (!isSubXml()) {
                return type == xmlDecl.type;
            }
            if (subXml.length != xmlDecl.subXml.length) {
                return false;
            }
            for (int i = 0; i < subXml.length; i ++) {
                if (!subXml[i].isCompatible(xmlDecl.subXml[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String toString() {
        return "Decl: " + name + " Type: " + type.name() + " XmlPath: " +
                xmlPath + " isMultiple: " + isMultiple + " isSubXml: " +
                isSubXml();
    }
}
