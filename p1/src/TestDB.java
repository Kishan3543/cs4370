// ========================================================
// @file TestDB.java
//
// @author Jake Sciotto
// ========================================================

import static java.lang.System.out;

// ========================================================
// TestDB.java is a driver file to test other Tables and 
// make sure methods in other files are working correctly.
//
//
// ========================================================

class TestDB
{
	public static void main( String[] args )
	{
		out.println();
	
		Table movie 	= new Table ( "movie", "title year length genre studioName producerNo", "String Integer Integer String String Integer", "title year" );

        Table cinema 	= new Table ( "cinema", "title year length genre studioName producerNo", "String Integer Integer String String Integer", "title year" );

        Table movieStar = new Table ( "movieStar", "name address gender birthdate", "String String Character String", "name" );

        Table starsIn 	= new Table ( "starsIn", "movieTitle movieYear starName", "String Integer String", "movieTitle movieYear starName" );

        Table movieExec = new Table ( "movieExec", "certNo name address fee", "Integer String String Float", "certNo" );

        Table studio    = new Table ( "studio", "name address presNo", "String String Integer", "name" );
        
        // add films to movie table
        Comparable [] film0 = { "Star_Wars", 	1977, 	124, 	"sciFi", 	 	"Fox", 			12345 };
        Comparable [] film1 = { "Star_Wars_2", 	1980, 	124, 	"sciFi", 	 	"Fox", 			12345 };
        Comparable [] film2 = { "Rocky", 		1985, 	200, 	"action",	 	"Universal", 	12125 };
        Comparable [] film3 = { "Rambo", 		1978, 	100, 	"action", 	 	"Universal", 	32355 };
        Comparable [] film4 = { "Training_Day", 2001,	122, 	"thriller", 	"Warner Bros", 	22222 };
        
        out.println();
        
        movie.insert( film0 );
        movie.insert( film1 );
        movie.insert( film2 );
        movie.insert( film3 );
        movie.insert( film4 );
        //movie.insert( film4 );
        movie.print();
        
        out.println();
        
        cinema.insert( film2 );
        cinema.insert( film3 );
        cinema.insert( film4 );
    
       	cinema.print();
       	
       	Comparable [] star0 = { "Carrie_Fisher", 	"Hollywood", 		'F', 	"9/9/99" };
        Comparable [] star1 = { "Mark_Hamill", 		"Brentwood", 		'M', 	"8/8/88" };
        Comparable [] star2 = { "Harrison_Ford", 	"Beverly_Hills", 	'M', 	"7/7/77" };
        
        out.println();
        
        movieStar.insert( star0 );
        movieStar.insert( star1 );
        movieStar.insert( star2 );
        
        movieStar.print();
       	
       	// not even sure what the fuck this does
       	movie.save();
       	cinema.save();
        movieStar.save();
        
        movie.print();
        cinema.print();


// --------------------------------------------------------
// :: TEST 1 -> PROJECT
// --------------------------------------------------------

		out.println();
        Table t_project = movie.project ( "title year" );
        t_project.print();

// --------------------------------------------------------
// :: TEST 2 -> SELECT: equals && 
// --------------------------------------------------------

		out.println();
        Table t_select = movie.select ( t -> t [ movie.col( "title" )].equals ( "Star_Wars" ) &&
                                             t [ movie.col( "year"  )].equals (	1977 ) );
        t_select.print();

// --------------------------------------------------------
// :: TEST 3 -> SELECT: <
// --------------------------------------------------------

		out.println();
		Table t_select2 = movie.select ( t -> (Integer) t [ movie.col( "year" ) ]  < 1980 );
		t_select2.print();

// --------------------------------------------------------
// :: TEST 4 -> SELECT: INDEXED
// --------------------------------------------------------

        out.println();
        Table t_iselect = movieStar.select ( new KeyType ( "Harrison_Ford" ));
        t_iselect.print();

// --------------------------------------------------------
// :: TEST 5 -> UNION
// --------------------------------------------------------
		out.println();
		Table t_union = movie.union( cinema );
		t_union.print();

// --------------------------------------------------------
// :: TEST 6 -> MINUS
// --------------------------------------------------------
		out.println();
		Table t_minus = movie.minus( cinema );
		t_minus.print();

// --------------------------------------------------------
// :: TEST 7 -> EQUI-JOIN
// --------------------------------------------------------
// --------------------------------------------------------
// :: TEST 8 -> NATURAL JOIN
// --------------------------------------------------------



// --------------------------------------------------------
// :: TEST X ->
// --------------------------------------------------------
// --------------------------------------------------------
// :: TEST Y -> 
// --------------------------------------------------------
// --------------------------------------------------------
// :: 
// --------------------------------------------------------
// --------------------------------------------------------
// :: 
// --------------------------------------------------------

	}
}