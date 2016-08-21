
/************************************************************************************
 * @file OrderList.java
 *
 * @author  John Miller
 *
 * compile: javac OrderList.java
 */

import java.util.ArrayList;
import java.util.List;
import static java.lang.System.out;

/************************************************************************************
 * The `OrderList` class provides methods for sorting a collection of tuples.
 */
public class OrderList
{
    /** The list of tuples
     */
    private final static List <Comparable> tuples = new ArrayList <> ();

    /** The array of strings holding the months the year
     */
    private final static String [] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    static { for ( String m : months ) tuples.add ( m ); }

    /************************************************************************************
     * Swap elements in the tuple list at positions 'i' and 'j'.
     *
     * @param i  the first position 
     * @param j  the second position
     */
    private static void swap ( int i, int j )
    {	
    	Comparable temp = tuples.get( i );
    	tuples.set( i, tuples.get( j ) );
    	tuples.set( j, temp );
    } // swap

    /************************************************************************************
     * Find the index of the minimum element in the tuple list from position 'i' to the end.
     *
     * @param i  the starting point
     */
    @SuppressWarnings("unchecked")
    public static int findMin( int i )
    {
    	int min = -1;
        int j   = -1;
            if( tuples.get( j ).compareTo( tuples.get( min ) ) < 0 ) 
            {
            	min = j; 
            }
        return -1;
        
    } // findMin

    /************************************************************************************
     * Sort the elements in the tuple list into ascending (technically non-decreasing) order
     * using selection sort (put smallest in first position, next smallest in second, etc.).
     */
    @SuppressWarnings("unchecked")
    public static void selSort ()
    {	
    	for( int i = 0; i < tuples.size() - 1; i++ )
    	{
    		for( int j = i + 1; j < tuples.size(); j++ )
    		{
    			if( tuples.get( j ).compareTo( tuples.get( i ) ) < 0 ) 
    			{
    				swap( j, i );
    			}
    		}
    	}
    } 

    /************************************************************************************
     * Print the tuple list.
     */
    public static void print () { out.println ( "tuples = " + tuples ); }
    /************************************************************************************
     * The 'main' method calls 'selSort'.
     *
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        print ();
        // swap example
        swap( 2, 3 );
        out.println( "After swap:" );
        print();
        selSort();
        out.println( "\nAfter selection sort:" );
        print();
    }        
}

