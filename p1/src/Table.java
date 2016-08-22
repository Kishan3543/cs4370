
/****************************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 */

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.Boolean.*;
import static java.lang.System.out;

/****************************************************************************************
 * This class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operators are provided: project,
 * select, union, minus and join.  The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operators.
 */
public class Table implements Serializable
{
    // relative path for storage directory 
    private static final String DIR = "store" + File.separator;

    // file extension for database files
    private static final String EXT = ".dbf";

    // counter for naming temp variables
    private static int count = 0;
	
	// table name
    private final String name;

	// array  of attribute names
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    // primary key
    private final String [] key;

    // index into tuples 
    private final Map <KeyType, Comparable []> index;

    //----------------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Construct an empty table from the meta-data specifications.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types )
     * @param _key        the primary key
     */  
    public Table( String _name, String [] _attribute, Class [] _domain, String [] _key )
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <>();
		index     = new TreeMap <>();       
    } 

    /************************************************************************************
     * Construct a table from the meta-data specifications and data in _tuples list.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types )
     * @param _key        the primary key
     * @param _tuple      the list of tuples containing the data
     */  
    public Table( String _name, String [] _attribute, Class [] _domain, String [] _key,
                  List <Comparable []> _tuples )
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = _tuples;
        index     = new TreeMap <>();  
    } 

    /************************************************************************************
     * Construct an empty table from the raw string specifications.
     *
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types )
     */
    public Table( String name, String attributes, String domains, String _key )
    {
        this( name, attributes.split( " " ), findClass (domains.split( " " ) ), _key.split( " " ) );

        out.println( "DDL> create table " + name + " ( " + attributes + " )" );
    } 

    //----------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     *
     * #usage movie.project( "title year studioNo" )
     *
     * @param attributes  the attributes to project onto
     * @return  a table of projected tuples
     */
    public Table project(String attributes )
    {
        out.println( "RA> " + name + ".project( " + attributes + " )" );
        String [] attrs     = attributes.split( " " );
        Class []  colDomain = extractDom( match ( attrs ), domain );
        String [] newKey    = ( Arrays.asList( attrs ).containsAll( Arrays.asList( key ) ) ) ? key : attrs;

        List <Comparable []> rows = new ArrayList <>();

        //  T O   B E   I M P L E M E N T E D 

        return new Table( name + count++, attrs, colDomain, newKey, rows );
    }

    /************************************************************************************
     * Select the tuples satisfying the given predicate (Boolean function).
     *
     * #usage movie.select(t -> t[movie.col( "year" )].equals (1977) )
     *
     * @param predicate  the check condition for tuples
     * @return  a table with tuples satisfying the predicate
     */
    public Table select(Predicate <Comparable []> predicate)
    {
        out.println( "RA> " + name + ".select( " + predicate + " )" );

        return new Table( name + count++, attribute, domain, key,
                   tuples.stream().filter( t -> predicate.test( t ) ).collect( Collectors.toList() ) );
    } 

    /************************************************************************************
     * Select the tuples satisfying the given key predicate (key = value).  Use an index
     * (Map) to retrieve the tuple with the given key value.
     *
     * @param keyVal  the given key value
     * @return  a table with the tuple satisfying the key predicate
     */
    public Table select( KeyType keyVal )
    {
        out.println( "RA> " + name + ".select( " + keyVal + " )" );

        List <Comparable []> rows = new ArrayList <> ();
    
        Comparable[] temp = index.get( keyVal );
        	
        if( temp != null )
		{
			rows.add( temp );
		}

        return new Table( name + count++, attribute, domain, key, rows );
    } 

    /************************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     *
     * #usage movie.union (show)
     *
     * @param table2  the rhs table in the union operation
     * @return  a table representing the union
     */
    public Table union( Table table2 )
    {
        out.println( "RA> " + name + ".union ( " + table2.name + " )" );
        
        List <Comparable []> rows = new ArrayList <> ();
        
        Table result = new Table( name + count++, attribute, domain, key, rows );
    	
    	if( !this.compatible( table2 ) ) 
    	{
    		out.println( "Tables are incompatible.");
    		return this;
    	}
    	else
    	{
    		for( int i = 0; i < this.tuples.size(); i++ )
    		{
    			result.insert( this.tuples.get( i ) );
    		}
    		
    		for( int i = 0; i < table2.tuples.size(); i++ )
    		{
    			Comparable[] current   = ( Comparable[] ) table2.tuples.get( i );
    			Comparable[] keyvalue = new Comparable[ table2.key.length ];
    			
    			int[] columns = match( result.key );
    			
    			for( int j = 0; j < keyvalue.length; j++ )
    			{
    				keyvalue[ j ] = current[ columns[ j ] ];
    			}
    			
    			if( !( result.index.containsKey( new KeyType( keyvalue ) ) ) )
    			{
    				result.insert( current );
    			}
    		}
    	}	
    	
    	return result;
    }

    /************************************************************************************
     * Take the difference of this table and table2.  Check that the two tables are
     * compatible.
     *
     * #usage movie.minus (show)
     *
     * @param table2  The rhs table in the minus operation
     * @return  a table representing the difference
     */
    public Table minus( Table table2 )
    {
        out.println( "RA> " + name + ".minus ( " + table2.name + " )" );
        if( ! compatible( table2 ) ) return null;
        
        List <Comparable []> rows = new ArrayList <> ();
        
        Table result = new Table( name + count++, attribute, domain, key, rows );

        if( !this.compatible( table2 ) ) 
    	{
    		out.println( "Tables are incompatible.");
    		return this;
    	}
    	else
    	
    	for( Comparable[] tuple : tuples )
    	{
    		boolean flag = false;
    		
    		for( Comparable[] tuple2 : table2.tuples )
    		{
    			if( tuple2 == tuple )
    			{
    				flag = true;
    				break;
    			}
    		}
    		
    		if( !flag )
    		{
    			result.insert( tuple );
    		}
    	}
/*    	{	
    		for( Comparable[] tuple : tuples )
    		{
    			Comparable[] keyvalue = new Comparable[ table2.key.length ];
    			
    			int[] columns = match( result.key );
    			
    			for( int i = 0; i < keyvalue.length; i++ )
    			{
    				keyvalue[ i ] = tuple[ columns[ i ] ];
    			}
    			
    			if( !( table2.index.containsKey( new KeyType( keyvalue ) ) ) )
    			{
    				result.insert( tuple );
    			}
    		}
		} */
		return result;
	}
    /************************************************************************************
     * Join this table and table2 by performing an "equi-join".  Tuples from both tables
     * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
     * names by append "2" to the end of any duplicate attribute name.
     *
     * #usage movie.join ( "studioNo", "name", studio)
     *
     * @param attribute1  the attributes of this table to be compared (Foreign Key )
     * @param attribute2  the attributes of table2 to be compared (Primary Key )
     * @param table2      the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join( String attributes1, String attributes2, Table table2 )
    {
        out.println( "RA> " + name + ".join ( " + attributes1 + ", " + attributes2 + ", "
                                               + table2.name + " )" );

        String [] t_attrs = attributes1.split( " " );
        String [] u_attrs = attributes2.split( " " );

        List <Comparable []> rows = new ArrayList <>();

        //  T O   B E   I M P L E M E N T E D 

        return new Table( name + count++, ArrayUtil.concat( attribute, table2.attribute ),
                                          ArrayUtil.concat( domain, table2.domain ), key, rows );
    } // join

    /************************************************************************************
     * Join this table and table2 by performing an "natural join".  Tuples from both tables
     * are compared requiring common attributes to be equal.  The duplicate column is also
     * eliminated.
     *
     * #usage movieStar.join (starsIn)
     *
     * @param table2  the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join( Table table2 )
    {
        out.println( "RA> " + name + ".join ( " + table2.name + " )" );

        List <Comparable []> rows = new ArrayList <>();

        //  T O   B E   I M P L E M E N T E D 

        // FIX - eliminate duplicate columns
        return new Table( name + count++, ArrayUtil.concat( attribute, table2.attribute ),
                                          ArrayUtil.concat( domain, table2.domain ), key, rows );
    } // join

    /************************************************************************************
     * Return the column position for the given attribute name.
     *
     * @param attr  the given attribute name
     * @return  a column position
     */
	public int col( String attr )
	{
		for( int i = 0; i < attribute.length; i++ ) 
		{
			if( attr.equals( attribute [ i ] ) ) 
			{
				return i;
			}
		} 

		return -1;
	} // col

    /************************************************************************************
     * Insert a tuple to the table.
     *
     * #usage movie.insert( "'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     *
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert( Comparable [] tup )
    {
        out.println( "DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )" );

        if( typeCheck( tup ) ) 
        {
            tuples.add( tup );
            
            Comparable [] keyVal = new Comparable[ key.length ];
        	int []        cols   = match( key );
        	
            for( int j = 0; j < keyVal.length; j++ ) 
            {
            	keyVal[ j ] = tup[ cols[ j ] ];
            }
            
            index.put( new KeyType( keyVal ), tup );
            
            return true;
            
        } 
        else 
        {
            return false;
        } 
    } 

    /************************************************************************************
     * Get the name of the table.
     *
     * @return  the table's name
     */
    public String getName() { return name; }

    /************************************************************************************
     * Print this table.
     */
    public void print()
    {
        out.println( "\n Table " + name);
        out.print( "|-" );
        for( int i = 0; i < attribute.length; i++ ) out.print( "---------------" );
        out.println( "-|" );
        out.print( "| " );
        for( String a : attribute ) out.printf ( "%15s", a);
        out.println( " |" );
        out.print( "|-" );
        for( int i = 0; i < attribute.length; i++ ) out.print( "---------------" );
        out.println( "-|" );
        for( Comparable [] tup : tuples ) {
            out.print( "| " );
            for( Comparable attr : tup) out.printf ( "%15s", attr);
            out.println( " |" );
        } 
        out.print( "|-" );
        for( int i = 0; i < attribute.length; i++ ) out.print( "---------------" );
        out.println( "-|" );
    } // print

    /************************************************************************************
     * Print this table's index (Map).
     */
    public void printIndex()
    {
        out.println( "\n Index for " + name );
        out.println( "-------------------"  );
        
        for( Map.Entry <KeyType, Comparable []> e : index.entrySet() ) 
        {
            out.println( e.getKey() + " -> " + Arrays.toString( e.getValue() ) );
        } 
        
        out.println( "-------------------" );
    } // printIndex

    /************************************************************************************
     * Load the table with the given name into memory. 
     *
     * @param name  the name of the table to load
     */
    public static Table load( String name )
    {
        Table tab = null;
        try {
            ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT) );
            tab = (Table) ois.readObject();
            ois.close();
        } catch (IOException ex) {
            out.println( "load: IO Exception" );
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            out.println( "load: Class Not Found Exception" );
            ex.printStackTrace();
        } 
        return tab;
    } // load

    /************************************************************************************
     * Save this table in a file.
     */
    public void save()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT) );
            oos.writeObject(this );
            oos.close();
        } catch (IOException ex) {
            out.println( "save: IO Exception" );
            ex.printStackTrace();
        } 
    } // save

    //----------------------------------------------------------------------------------
    // Private Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e., have
     * the same number of attributes each with the same corresponding domain.
     *
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     */
    private boolean compatible( Table table2 )
    {
        if( domain.length != table2.domain.length) {
            out.println( "compatible ERROR: table have different arity" );
            return false;
        } 
        for( int j = 0; j < domain.length; j++ ) 
        {
            if( domain[ j ] != table2.domain[ j ] ) 
            {
                out.println( "compatible ERROR: tables disagree on domain " + j);
                return false;
            } 
        } 
        return true;
    } // compatible

    /************************************************************************************
     * Match the column and attribute names to determine the domains.
     *
     * @param column  the array of column names
     * @return  an array of column index positions
     */
    private int [] match( String [] column )
    {
        int [] colPos = new int[ column.length ];

        for( int j = 0; j < column.length; j++ ) 
        {
            boolean matched = false;
            for( int k = 0; k < attribute.length; k++ ) 
            {
                if( column[ j ].equals( attribute[ k ] ) ) 
                {
                    matched 	= true;
                    colPos[ j ] = k;
                }
            }
            if(  ! matched) 
            {
                out.println( "match: domain not found for " + column [j]);
            }
        } 

        return colPos;
    }

    /************************************************************************************
     * Extract the attributes specified by the column array from tuple t.
     *
     * @param t       the tuple to extract from
     * @param column  the array of column names
     * @return  a smaller tuple extracted from tuple t 
     */
    private Comparable [] extract( Comparable [] t, String [] column )
    {
        Comparable [] tup = new Comparable[ column.length ];
        int [] colPos 	  = match( column );
        
        for( int j = 0; j < column.length; j++ )
        {
        	tup[ j ] = t[ colPos[ j ] ];
        }
        
        return tup;
    } // extract

    /************************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the type of
     * each value to ensure it is from the right domain. 
     *
     * @param t  the tuple as a list of attribute values
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     */
    private boolean typeCheck( Comparable [] t )
    { 
        //  T O   B E   I M P L E M E N T E D 

        return true;
    } // typeCheck

    /************************************************************************************
     * Find the classes in the "java.lang" package with given names.
     *
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  an array of Java classes
     */
    private static Class [] findClass( String [] className )
    {
        Class [] classArray = new Class[ className.length ];

        for( int i = 0; i < className.length; i++ ) 
        {
            try {
                classArray[ i ] = Class.forName ( "java.lang." + className[ i ] );
            } catch (ClassNotFoundException ex ) {
                out.println( "findClass: " + ex );
            } 
        } 

        return classArray;
    } // findClass

    /************************************************************************************
     * Extract the corresponding domains.
     *
     * @param colPos the column positions to extract.
     * @param group  where to extract from
     * @return  the extracted domains
     */
    private Class [] extractDom( int [] colPos, Class [] group )
    {
        Class [] obj = new Class[ colPos.length ];

        for( int j = 0; j < colPos.length; j++ ) 
        {
            obj[ j ] = group[ colPos[ j ] ];
        } 

        return obj;
    }

} // Table class

