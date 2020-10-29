// http://jigsaw.w3.org/Doc/Programmer/api/org/w3c/util/DateParser.html
// InvalidDateException.java
// $Id: InvalidDateException.java,v 1.1 2005/01/07 16:30:51 dbarashev Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package net.sourceforge.ganttproject.util;

/**
 * @version $Revision: 1.1 $
 * @author Benoît Mahé (bmahe@w3.org)
 */
public class InvalidDateException extends Exception {

    public InvalidDateException(String msg) {
        super(msg);
    }

}
