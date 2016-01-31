package com.vzome.core.render;

import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.OrbitSet;
import com.vzome.core.math.symmetry.Symmetry;

public interface OrbitSource
{
	Symmetry getSymmetry();
	    	
    Axis getAxis( AlgebraicVector vector );
    
    Color getColor( Direction orbit );

	OrbitSet getOrbits(); 
	
	Shapes getShapes();
}