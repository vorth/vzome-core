package com.vzome.core.editor;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.Segment;
import com.vzome.core.construction.SegmentEndPoint;
import com.vzome.core.construction.SegmentJoiningPoints;
import com.vzome.core.construction.TransformedPoint;
import com.vzome.core.construction.Translation;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.RealizedModel;
import com.vzome.core.model.Strut;

/**
 * Copied and adapted from AffineHeptagon.
 * 
 * @author Scott Vorthmann
 */
public class AffinePolygonMesh extends ChangeManifestations
{
    @Override
    public void perform() throws Command.Failure
    {
        // Accepts exactly 2 struts as parameters
        // Any selected balls or panels are unselected and ignored
        String errorMsg = "Affine polygon mesh command requires two selected struts with a common vertex.";

        Strut strut1 = null, strut2 = null;
        for (Manifestation man : mSelection) {
            unselect(man);
            if (man instanceof Strut) {
                if (strut1 == null) {
                    strut1 = Strut.class.cast(man);
                } else if (strut2 == null) {
                    strut2 = Strut.class.cast(man);
                } else {
                    // too many struts
                    fail(errorMsg);
                }
            }
        }

        if (strut1 == null || strut2 == null) {
            // not enough struts
            fail(errorMsg);
        }

        AlgebraicField field = strut1.getLocation().getField();

        redo();  // Get the unselects out of the way, in case anything needs to be re-selected later

        // Before we start, be sure the balls at both ends of both struts have not been deleted or hidden.
        // It's safe to restore them all without testing if they already exist.
        Segment s1 = Segment.class.cast(strut1.getConstructions().next());
        Segment s2 = Segment.class.cast(strut2.getConstructions().next());
        manifestConstruction( new SegmentEndPoint(s1, true) );
        manifestConstruction( new SegmentEndPoint(s1, false) );
        manifestConstruction( new SegmentEndPoint(s2, true) );
        manifestConstruction( new SegmentEndPoint(s2, false) );
        redo();

        // find the offsets and the endpoint vectors: v0, v1, v2
        AlgebraicVector offset1 = s1.getOffset();
        AlgebraicVector offset2 = s2.getOffset();
        AlgebraicVector v0 = null, v1 = null, v2 = null;
        {
            AlgebraicVector s1s = s1.getStart();
            AlgebraicVector s1e = s1.getEnd();
            AlgebraicVector s2s = s2.getStart();
            AlgebraicVector s2e = s2.getEnd();
            // reverse the offset direction(s) if necessary so the two offsets have a common starting point at v0
            if (s1s.equals(s2s)) {
                v0 = s1s;
                v1 = s1e;
                v2 = s2e;
            } else if (s1e.equals(s2s)) {
                v0 = s1e;
                v1 = s1s;
                v2 = s2e;
                offset1 = offset1.negate();
            } else if (s1e.equals(s2e)) {
                v0 = s1e;
                v1 = s1s;
                v2 = s2s;
                offset2 = offset2.negate();
                offset1 = offset1.negate();
            } else if (s1s.equals(s2e)) {
                v0 = s1s;
                v1 = s1e;
                v2 = s2s;
                offset2 = offset2.negate();
            } else {
                fail(errorMsg);
            }
        }

        // find the corresponding connectors and points now that we've juggled them into the right order
        Connector c0 = null, c1 = null, c2 = null;
        Point p1 = null, p2 = null;
        for (Manifestation man : mManifestations) {
            if (man instanceof Connector) {
                AlgebraicVector loc = man.getLocation();
                if (loc.equals(v0)) {
                    c0 = Connector.class.cast(man);
                } else if (loc.equals(v1)) {
                    c1 = Connector.class.cast(man);
                    p1 = (Point) man.getConstructions().next();
                } else if (loc.equals(v2)) {
                    c2 = Connector.class.cast(man);
                    p2 = (Point) man.getConstructions().next();
                }
            }
        }

        /*
        Hexagon vertex layout:
        
                 [p5]
                /    \
              /        \
            /            \
          /                \
        [p4]              [p3]
         |  \            /  |
         |    \        /    |
         |      \    /      |
         |        \/        |
         |        /\        |
         | 2*o1 /    \ 2*o2 |
         |    /        \    |
         |  /            \  |
        (p2)              [p1]
          \                /
            \            /
          o2  \        / o1
                \    /
                 [p0]
        */

        // translate the three initial points to make 4 new ones
        final AlgebraicNumber two = field .createAlgebraicNumber( 2 );

        // construct p3 = p2 + (two * offset1)
        Point p3 = new TransformedPoint(new Translation( offset1.scale( two ) ), p2);
        // construct p4 = p1 + (two * offset2)
        Point p4 = new TransformedPoint(new Translation( offset2.scale( two ) ), p1);
        // construct p5 = p4 + (offset1)
        Point p5 = new TransformedPoint(new Translation( offset1 ), p4);

        // now, construct and select struts p1-p3, p3-p5, p5-p6, p6-p4 and p4-p2
        select( manifestConstruction( new SegmentJoiningPoints(p1, p3) ) );
        select( manifestConstruction( new SegmentJoiningPoints(p3, p5) ) );
        select( manifestConstruction( new SegmentJoiningPoints(p5, p4) ) );
        select( manifestConstruction( new SegmentJoiningPoints(p4, p2) ) );

        // now select all 7 of the connectors, new and old in a sequential order
        // which will allow the user to easiliy make a panel or a centroid as a next step.
        // Note that this is different from the final selection of AffinePentagon
        select(c0);
        select(c1);
        select( manifestConstruction( p3 ) );
        select( manifestConstruction( p5 ) );
        select( manifestConstruction( p4 ) );
        select(c2);

        redo();
    }

    public AffinePolygonMesh(Selection selection, RealizedModel realized) {
        super(selection, realized, false);
    }

    @Override
    protected String getXmlElementName() {
        return "AffinePolygonMesh";
    }

}
