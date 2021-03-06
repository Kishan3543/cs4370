
/************************************************************************************
    @file BpTreeMap.java

    @author  John Miller
*/

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.ceil;
import static java.lang.System.out;

/************************************************************************************
    The BpTreeMap class provides B+Tree maps.  B+Trees are used as multi-level index
    structures that provide efficient access for both point queries and range queries.
    All keys will be at the leaf level with leaf nodes linked by references.
    Internal nodes will contain divider keys such that each divider key corresponds to
    the largest key in its left subtree (largest left).  Keys in left subtree are "<=",
    while keys in right substree are ">".
*/
public class BpTreeMap <K extends Comparable <K>, V>
    extends AbstractMap <K, V>
    implements Serializable, Cloneable, SortedMap <K, V>
{
    /** The debug flag
    */
    private static final boolean DEBUG = true;

    /** The maximum fanout (number of children) for a B+Tree node.
        May wish to increase for better performance for Program 3.
    */
    private static final int ORDER = 5;

    /** The maximum fanout (number of children) for a big B+Tree node.
    */
    private static final int BORDER = ORDER + 1;

    /** The ceiling of half the ORDER.
    */
    private static final int MID = ( int ) ceil ( ORDER / 2.0 );

    /** The class for type K.
    */
    private final Class <K> classK;

    /** The class for type V.
    */
    private final Class <V> classV;

    /********************************************************************************
        This inner class defines nodes that are stored in the B+tree map.
    */
    private class Node
    {
        boolean   isLeaf;                             
        int       nKeys;
        K []      key;
        Object [] ref;

        /****************************************************************************
            Construct a node.
            @param p       the order of the node (max refs)
            @param isLeaf  whether the node is a leaf
        */
        @SuppressWarnings ( "unchecked" )
        Node( int p, boolean _isLeaf )
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = ( K [] ) Array.newInstance( classK, p - 1 );

            if( isLeaf )
            {
                ref = new Object[ p ];
            }
            else
            {
                ref = ( Node[] ) Array.newInstance( Node.class, p );
            }
        }

        /****************************************************************************
            Copy keys and ref from node n to this node.
            @param n     the node to copy from
            @param from  where in n to start copying from
            @param num   the number of keys/refs to copy
        */
        void copy( Node n, int from, int num )
        {
            nKeys = num;
            for( int i = 0; i < num; i++ )
            {
                key[ i ] = n.key[ from + i ];
                ref[ i ] = n.ref[ from + i ];
            }
            ref[ num ] = n.ref[ from + num ];
        }

        /****************************************************************************
            Find the "<=" match position in this node.
            @param k  the key to be matched.
            @return  the position of match within node, where nKeys indicates no match
        */
        int find( K k )
        {
            for( int i  = 0; i < nKeys; i++ ) 
            {
                if( k.compareTo( key[ i ] ) <= 0 )
                {
                    return i;
                }
            }
            return nKeys;
        } 

        /****************************************************************************
            Overriding toString method to print the Node. Prints out the keys.
        */
        @Override
        public String toString() { return Arrays.deepToString( key ); }

    }

    /** The root of the B+Tree
    */
    private Node root;

    /** The first (leftmost) leaf in the B+Tree
    */
    private final Node firstLeaf;

    /** A big node to hold all keys and references/pointers before splitting
    */
    private final Node bn;

    /** Flag indicating whether a split at the level below has occured that needs to be handled
    */
    private boolean hasSplit = false;

    /** The counter for the number nodes accessed (for performance testing)
    */
    private int count = 0;

    /** The counter for the total number of keys in the B+Tree Map
    */
    private int keyCount = 0;

    /********************************************************************************
        Construct an empty B+Tree map.
        @param _classK  the class for keys (K)
        @param _classV  the class for values (V)
    */
    public BpTreeMap( Class <K> _classK, Class <V> _classV )
    {
        classK    = _classK;
        classV    = _classV;
        root      = new Node ( ORDER, true );
        firstLeaf = root;
        bn        = new Node ( BORDER, true );
    }

    /********************************************************************************
        Return null to use the natural order based on the key type.  This requires the
        key type to implement Comparable.
    */
    public Comparator <? super K> comparator() { return null; }

    /********************************************************************************
        Return a set containing all the entries as pairs of keys and values.
        @return  the set view of the map
    */
    public Set <Map.Entry <K, V> > entrySet()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <>();

        Node current = root;

        while( !current.isLeaf )
        {
            current = ( Node ) current.ref[ 0 ];
        }

        while( current != null )
        {
            for( int i = 0; i < current.nKeys; i++ )
            {
                enSet.add( new SimpleEntry( current.key[ i ], current.ref[ i ] ) );
            }

            current = ( Node ) current.ref[ ORDER - 1 ];
        }

        return enSet;
    }

    /********************************************************************************
        Given the key, look up the value in the B+Tree map.
        @param key  the key used for look up
        @return  the value associated with the key or null if not found
    */
    @SuppressWarnings ( "unchecked" )
    public V get( Object key )
    {
        return find( ( K ) key, root );
    }

    /********************************************************************************
        Put the key-value pair in the B+Tree map.
        @param key    the key to insert
        @param value  the value to insert
        @return  null, not the previous value for this key
    */
    public V put( K key, V value )
    {
        insert( key, value, root );
        keyCount++;
        return null;
    }

    /********************************************************************************
        Return the first (smallest) key in the B+Tree map.
        @return  the first key in the B+Tree map.
    */
    public K firstKey()
    {
        if( size() == 0 )
        {
            throw new NoSuchElementException();
        }

        return firstLeaf.key[ 0 ];
    }

    /********************************************************************************
        Return the last (largest) key in the B+Tree map.
        @return  the last key in the B+Tree map.
    */
    public K lastKey()
    {
        if( size() == 0 )
        {
            throw new NoSuchElementException();
        }

        Node current = root;

        while( !current.isLeaf )
        {
            current = ( Node ) current.ref[ current.nKeys ];
        }        

        return current.key[ current.nKeys - 1 ]; 
    } 

    /********************************************************************************
        Return the portion of the B+Tree map where key < toKey.
        @return  the submap with keys in the range [ firstKey, toKey)
    */
    public SortedMap <K, V> headMap( K toKey ) 
    { 
        return subMap( firstKey(), toKey ); 
    }

    /********************************************************************************
        Return the portion of the B+Tree map where fromKey <= key.
        @return  the submap with keys in the range [ fromKey, lastKey]
    */
    public SortedMap <K, V> tailMap( K fromKey )
    {   
        // each of these methods seeming so easy makes it seem like i missed something
        return subMap( fromKey, lastKey() );
    }

    /********************************************************************************
        Return the portion of the B+Tree map whose keys are between fromKey and toKey,
        i.e., fromKey <= key < toKey.
        @return  the submap with keys in the range [ fromKey, toKey)
    */
    public SortedMap <K, V> subMap( K fromKey, K toKey )
    {
        BpTreeMap <K, V> submap = new BpTreeMap <> ( classK, classV );

        K start = fromKey == null ? firstKey() : fromKey;
        K end   = toKey   == null ? lastKey()  : toKey;

        Set <Entry<K, V>> entries = this.entrySet();

        for( Entry<K, V> entry : entries )
        {
            if( entry.getKey().compareTo( start ) >= 0 &&
                entry.getKey().compareTo( end   ) <= 0 )
            {
                submap.put( entry.getKey(), entry.getValue() );
            }
        }

        return submap;
    } 

    /********************************************************************************
        Return the size (number of keys) in the B+Tree.
        @return  the size of the B+Tree
    */
    public int size()
    {
        int sum = 0;
        Node n  = root;

        while( !n.isLeaf )
        {
            n = ( Node ) n.ref[ 0 ];
        }

        while( n != null )
        {
            sum = sum + n.nKeys;
            n   = ( Node ) n.ref[ n.nKeys ];
        }

        return sum;
    }

    /********************************************************************************
        Print the B+Tree using a pre-order traveral and indenting each level.
        @param n      the current node to print
        @param level  the current level of the B+Tree
    */
    @SuppressWarnings( "unchecked" )
    private void print( Node n, int level )
    {
        if( n == root )
        {
            out.println( "BpTreeMap" );
        }
        out.println( "-------------------------------------------" );

        for( int j = 0; j < level; j++ )
        {
            out.print( "\t" );
        }
        out.print( "[  . " );
        for( int i = 0; i < n.nKeys; i++ )
        {
            out.print( n.key[ i ] + " . " );
        }
        out.println( "]" );
        if( ! n.isLeaf )
        {
            for( int i = 0; i <= n.nKeys; i++ )
            {
                print( ( Node ) n.ref[ i ], level + 1 );
            }
        }

        if( n == root )
        {
            out.println( "-------------------------------------------" );
        }
    } 

    /********************************************************************************
        Recursive helper function for finding a key in B+trees.
        @param key  the key to find
        @param n    the current node
    */
    @SuppressWarnings ( "unchecked" )
    private V find( K key, Node n )
    {
        count++;
        int i = n.find( key );

        if( i < n.nKeys )
        {
            K k_i = n.key[ i ];

            if( n.isLeaf )
            {
                return( key.compareTo ( k_i ) == 0 ) ? ( V ) n.ref[ i ] : null;
            }
            else
            {
                return find( key, ( Node ) n.ref[ i ] );
            }
        }
        else
        {
            return( n.isLeaf ) ? null : find( key, ( Node ) n.ref[ n.nKeys ] );
        }
    }

    /********************************************************************************
        Recursive helper function for inserting a key in B+trees.
        @param key  the key to insert
        @param ref  the value/node to insert
        @param n    the current node
        @return  the node inserted into (may wish to return more information)
    */
    @SuppressWarnings ( "unchecked" )
    private Node insert( K key, V ref, Node n )
    {
        out.println( "=============================================================" );
        out.println( "insert: key = " + key );
        out.println( "=============================================================" );

        Node rt = null;

        if( n.isLeaf )                                                      
        {  
            if( n.nKeys < ORDER - 1 )                                       
            {
                wedge( key, ref, n, n.find( key ), true );
                hasSplit = false;          
            }
            else                                                       
            {
                rt = split( key, ref, n, true );     
                n.ref[ n.nKeys ] = rt;

                if( n == root && rt != null )
                {
                    root = makeRoot( n, n.key[ n.nKeys - 1 ], rt );
                }
                else if( rt != null )
                {
                    hasSplit = true;        
                }
            }
        }
        else           
        {
            int i = n.find( key );   

            rt = insert( key, ref, ( Node ) n.ref[ i ] );   

            if( DEBUG )
            {
                out.println( "insert: handle internal node level" );
            }

            Node lt = ( Node ) n.ref[ i ];
            
            if( hasSplit )
            {
                if( n.nKeys < ORDER - 1 )
                {
                    wedge( lt.key[ lt.nKeys - 1 ], rt, n, i, false );
                    hasSplit = false;
                }
                else
                {
                    Node srt = split( lt.key[ n.nKeys - 1 ], rt, n, false );
                    hasSplit = true;

                    if( n == root )
                    {
                        root = this.makeRoot( n, n.key[ n.nKeys - 1 ], srt );
                        n.nKeys--;
                    }
                    return srt;
                }
            }
        }
        if( DEBUG )
        {
            print( root, 0 );
        }
        return rt;                                      
    }
    /********************************************************************************
        Make a new root, linking to left and right child node, seperated by a divider key.
        @param ref0  the reference to the left child node
        @param key0  the divider key - largest left
        @param ref1  the reference to the right child node
        @return  the node for the new root
    */
    private Node makeRoot( Node ref0, K key0, Node ref1 )
    {
        Node nr     = new Node( ORDER, false );               
        nr.nKeys    = 1;
        nr.ref[ 0 ] = ref0;                                            
        nr.key[ 0 ] = key0;                                             
        nr.ref[ 1 ] = ref1;                                             
        return nr;
    } 

    /********************************************************************************
        Wedge the key-ref pair into node n.  Shift right to make room if needed.
        @param key   the key to insert
        @param ref   the value/node to insert
        @param n     the current node
        @param i     the insertion position within node n
        @param left  whether to start from the left side of the key
        @return boolean indicate whether wedge succeeded (i.e no duplicate)
    */
    private boolean wedge( K key, Object ref, Node n, int i, boolean left )
    {

        if( i < n.nKeys && key.compareTo( n.key[ i ] ) == 0 )
        {
            out.println( "BpTreeMap.insert: attempt to insert duplicate key = " + key );
            return false;
        }

        n.ref[ n.nKeys + 1 ] = n.ref[ n.nKeys ];

        for( int j = n.nKeys; j > i; j-- )
        {
            n.key[ j ] = n.key[ j - 1 ];

            if( left || j > i + 1 )
            {
                n.ref[ j ] = n.ref[ j - 1 ];
            }
        } 

        n.key[ i ] = key;

        if( left )
        {
            n.ref[ i ] = ref;
        }
        else
        {
            n.ref[ i + 1 ] = ref;
        }

        n.nKeys++;
        return true;
    }

    /********************************************************************************
        Split node n and return the newly created right sibling node rt.  The bigger half
        should go in the current node n, with the remaining going in rt.
        @param key  the new key to insert
        @param ref  the new value/node to insert
        @param n    the current node
        @return  the right sibling node (may wish to provide more information)
    */
    private Node split( K key, Object ref, Node n, boolean left )
    {
        bn.copy ( n, 0, ORDER - 1 );                            

        if( wedge( key, ref, bn, bn.find( key ), left ) )                 
        {
            n.copy ( bn, 0, MID );                 
            Node rt = new Node ( ORDER, n.isLeaf );
            rt.copy ( bn, MID, ORDER - MID );                         
            return rt;
        }
        return null;                                                     
    }

    /********************************************************************************
        The main method used for testing.
        @param  the command-line arguments (args[ 0 ] gives number of keys to insert)
    */
    public static void main( String [] args )
    {
        int totalKeys    = 14;
        boolean RANDOMLY = false;

        BpTreeMap <Integer, Integer> bpt = new BpTreeMap <> ( Integer.class, Integer.class );
        if( args.length == 1 )
        {
            totalKeys = Integer.valueOf ( args[ 0 ] );
        }

        if( RANDOMLY )
        {
            Random rng = new Random();
            for( int i = 1; i <= totalKeys; i += 2 )
            {
                bpt.put ( rng.nextInt ( 2 * totalKeys ), i * i );
            }
        }
        else
        {
            for( int i = 1; i <= totalKeys; i += 2 )
            {
                bpt.put ( i, i * i );
            }
        }

        bpt.print( bpt.root, 0 );
        for( int i = 0; i <= totalKeys; i++ )
        {
            out.println( "key = " + i + " value = " + bpt.get ( i ) );
        } 
        out.println( "-------------------------------------------" );
        out.println( "Average number of nodes accessed = " + bpt.count / ( double ) totalKeys );
    }

}

