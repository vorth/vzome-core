
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;


import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command;
import com.vzome.core.construction.AnchoredSegment;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.Segment;
import com.vzome.core.construction.SegmentEndPoint;
import com.vzome.core.construction.SymmetryTransformation;
import com.vzome.core.construction.Transformation;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;

public class RotationTool extends SymmetryTool
{
    public String getDefaultName()
    {
        return "rotation around Z axis";
    }

    public String getCategory()
    {
        return "rotation";
    }

    public RotationTool( String name, Symmetry symmetry, Selection selection, RealizedModel realized, Point originPoint )
    {
        super( name, symmetry, selection, realized, originPoint );
    }

    public void perform() throws Command.Failure
    {
        Point center = null;
        Segment axisStrut = null;
        boolean correct = true;
        if ( ! isAutomatic() )
            for (Manifestation man : mSelection) {
                unselect( man );
                if ( man instanceof Connector )
                {
                    if ( center != null )
                    {
                        correct = false;
                        break;
                    }
                    center = (Point) ((Connector) man) .getConstructions() .next();
                }
                else if ( man instanceof Strut )
                {
                    if ( axisStrut != null )
                    {
                        correct = false;
                        break;
                    }
                    axisStrut = (Segment) ((Strut) man) .getConstructions() .next();
                }
        }
        
        if ( axisStrut == null )
        {
            if ( isAutomatic() )
            {
                center = originPoint;
                AlgebraicField field = symmetry .getField();
                AlgebraicVector zAxis = field .basisVector( 3, AlgebraicVector .Z );
                AlgebraicNumber len = field .createPower( 2 );  // does not matter
                axisStrut = new AnchoredSegment( symmetry .getAxis( zAxis ), len, center );
            }
            else
                correct = false;
        }
        else if ( center == null )
            center = new SegmentEndPoint( axisStrut );
        
        if ( ! correct )
            throw new Command.Failure( "rotation tool requires a single axis strut,\n" +
                                        "and optionally a separate center point" );

        AlgebraicVector vector = axisStrut .getOffset();
        vector = axisStrut .getField() .projectTo3d( vector, true );
        Axis axis = symmetry .getAxis( vector );
        int rotation = axis .getRotation();
        if ( rotation == Symmetry .NO_ROTATION )
            throw new Command.Failure( "selected strut is not an axis of rotation" );
        this .transforms = new Transformation[ 1 ];
        transforms[ 0 ] = new SymmetryTransformation( symmetry, rotation, center );
    }

    protected String getXmlElementName()
    {
        return "RotationTool";
    }
}
