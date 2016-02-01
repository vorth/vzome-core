
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;


import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.Bivector3d;
import com.vzome.core.algebra.Vector3d;
import com.vzome.core.commands.Command;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.construction.Construction;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Panel;
import com.vzome.core.model.Strut;

public class PlaneSelectionTool extends ChangeSelection implements Tool
{
    private String name;
    
    private Bivector3d plane;
    
    private AlgebraicVector anchor;

	private boolean halfSpace = false;
	private boolean boundaryOpen = false;
	private boolean above = true;
	private boolean includeBalls = true;
	private boolean includeStruts = true;
	private boolean includePanels = true;
	private boolean includePartials = false;

    public PlaneSelectionTool( String name, Selection selection, AlgebraicField field )
    {
        super( selection, false );
        this .name = name;
    }

	public boolean isSticky()
    {
        return true;
    }

    public void perform() throws Failure
    {
        AlgebraicVector p1 = null, p2 = null, p3 = null;
        for (Manifestation man : mSelection) {
            unselect( man );
            if ( man instanceof Connector )
            {
                if ( p1 == null )
                {
                    p1 = man .getLocation();
                    continue;
                }
                if ( p2 == null )
                {
                    p2 = man .getLocation();
                    continue;
                }
                if ( p3 == null )
                {
                    p3 = man .getLocation();
                    continue;
                }
                else
                {
                    throw new Command.Failure( "half-space selection tool requires exactly three balls" );
                }
            }
        }
        
        if ( p3 == null )
        	throw new Command.Failure( "half-space selection tool requires exactly three balls" );

        Vector3d v1 = new Vector3d( p2 .minus( p1 ) );
        Vector3d v2 = new Vector3d( p3 .minus( p1 ) );
        plane = v1 .outer( v2 );
        anchor = p1;
    }

    public void prepare( ChangeManifestations applyTool ) {}

	public void complete( ChangeManifestations applyTool ) {}

    public boolean needsInput()
    {
    	return false;
    }

    public void performEdit( Construction c, ChangeManifestations applyTool ) {}

	public void performSelect( Manifestation man, ChangeManifestations applyTool )
	{
	    if ( man .isHidden() )
	        return;
	    if ( ! man .isRendered() )
	        return;
        if ( includePanels && man instanceof Panel )
            return;
        if ( includeBalls && man instanceof Connector )
        {
        	Connector c = (Connector) man;
        	int orientation = orient( c .getLocation() );
        	if ( !boundaryOpen && orientation == 0 )
        		applyTool .select( man );
        	else if ( halfSpace && above && orientation > 0 )
        		applyTool .select( man );
        	else if ( halfSpace && !above && orientation < 0 )
        		applyTool .select( man );
        }
        else if ( includeStruts && man instanceof Strut )
        {
        	int o1 = orient( ((Strut) man) .getLocation() );
        	int o2 = orient( ((Strut) man) .getEnd() );
        	if ( includePartials )
        	{
        		if ( !boundaryOpen && ( o1 == 0 || o2 == 0 ) )
        			applyTool .select( man );
            	else if ( halfSpace )
            	{
            		if ( above && ( o1 > 0 || o2 > 0 ) )
            			applyTool .select( man );
            		else if ( !above && ( o1 < 0 || o2 < 0 ) )
            			applyTool .select( man );
            	}
        	}
        	else
        	{
        		if ( !halfSpace && o1 == 0 && o2 == 0 )
        			applyTool .select( man );
            	else if ( halfSpace )
            	{
            		if ( boundaryOpen )
            		{
            			if ( above && ( o1 > 0 && o2 > 0 ) )
            				applyTool .select( man );
            			else if ( !above && ( o1 < 0 && o2 < 0 ) )
            				applyTool .select( man );
            		}
            		else
            		{
            			if ( above && ( o1 >= 0 && o2 >= 0 ) )
            				applyTool .select( man );
            			else if ( !above && ( o1 <= 0 && o2 <= 0 ) )
            				applyTool .select( man );
            		}
            	}
        	}
        }
        applyTool .redo();
	};
	
	private int orient( AlgebraicVector point )
	{
	    AlgebraicVector diff = point .minus( anchor );
		Vector3d v = new Vector3d( diff );
		AlgebraicNumber volume = plane .outer( v );
		if ( volume .isZero() )
			return 0;
		else
		{
			double volD = volume .evaluate();
			return (volD > 0d)? 1 : -1;
		}
	}

    public void redo()
    {
        // TODO manifest a symmetry construction... that is why this class extends ChangeConstructions
        // this edit is now sticky (not really undoable)
//        tools .addTool( this );
    }

    public void undo()
    {
        // this edit is now sticky (not really undoable)
//        tools .removeTool( this );
    }

    public String getName()
    {
        return name;
    }

    protected String getXmlElementName()
    {
        return "PlaneSelectionTool";
    }
    
    protected void getXmlAttributes( Element element )
    {
        element .setAttribute( "name", this.name );
    }

    protected void setXmlAttributes( Element element, XmlSaveFormat format ) throws Failure
    {
        this.name = element .getAttribute( "name" );
        this .includeBalls = ! "false" .equals( element .getAttribute( "balls" ) );
        this .includeStruts = ! "false" .equals( element .getAttribute( "struts" ) );
        this .includePanels = "true" .equals( element .getAttribute( "panels" ) );
    	this .includePartials = "any" .equals( element .getAttribute( "vertices" ) );
    	this .boundaryOpen = "true" .equals( element .getAttribute( "open" ) );
        String halfSpace = element .getAttribute( "halfSpace" );
        if ( "above" .equals( halfSpace ) )
        {
        	this .halfSpace = true;
        	this .above = true;
        }
        else if ( "below" .equals( halfSpace ) )
        {
        	this .halfSpace = true;
        	this .above = false;
        }
        else
        {
        	this .halfSpace = false;
        	this .boundaryOpen = false;  // note override of earlier default
        }
    }

    public String getCategory()
    {
        return "plane";
    }

    public String getDefaultName()
    {
        return "plane";
    }
}
