package com.vzome.core.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.TreeMap;

import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.construction.Point;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.RealizedModel;

public class Tools extends TreeMap<String, Tool>
{
	private AlgebraicField field;
	private Selection selection;
	private RealizedModel model;
	private Point origin;
	private DocumentModel doc;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    
	public Tools( DocumentModel doc, Selection selection, RealizedModel model, Point origin, AlgebraicField field )
	{
		super();
		this.doc = doc;
		this.selection = selection;
		this.model = model;
		this.origin = origin;
		this.field = field;
	}

	public UndoableEdit createEdit( Element xml )
	{
		UndoableEdit edit = null;
        String toolName = xml .getAttribute( "name" );
		switch ( xml .getLocalName() ) {

        case "BookmarkTool":
        	edit = new BookmarkTool( toolName, this.selection, this .model );
        	break;

        case "InversionTool":
        	edit = new InversionTool( toolName, this.selection, this .model, this.origin );
        	break;

        case "MirrorTool":
        	edit = new MirrorTool( toolName, this.selection, this .model, this.origin );
        	break;

        case "TranslationTool":
        	edit = new TranslationTool( toolName, this.selection, this .model, this.origin );
        	break;

        case "LinearMapTool":
        	edit = new LinearMapTool( toolName, this.selection, this .model, this.origin, true );
        	break;

        case "LinearTransformTool":
        	edit = new LinearMapTool( toolName, this.selection, this .model, this.origin, false );
        	break;

        case "RotationTool":
        	edit = new RotationTool( toolName, null, this .selection, this .model, this.origin );
        	break;

        case "ScalingTool":
        	edit = new ScalingTool( toolName, null, this .selection, this .model, this.origin );
        	break;

        case "SymmetryTool":
        	edit = new SymmetryTool( toolName, null, this .selection, this .model, this.origin );
        	break;

        case "ModuleTool":
        	edit = new ModuleTool( toolName, this .selection, this .model );
        	break;

        case "PlaneSelectionTool":
        	edit = new PlaneSelectionTool( toolName, this .selection, this .field );
        	break;

        case "ToolApplied":
			edit = new ApplyTool( this, this.selection, this.model, false );
        	break;

        case "ApplyTool":
			edit = new ApplyTool( this, this.selection, this.model, true );
        	break;
		}
		if ( edit instanceof Tool ) {
			this .put( toolName, (Tool) edit );
			this .pcs .firePropertyChange( "tool.instances", null, toolName );
		}
		return edit;
	}

	public void createTool( String name, String group, Symmetry symmetry )
	{
        Selection toolSelection = this .selection;

        if ( "default" .equals( group ) )
        {
            name = name .substring( "default." .length() );
            int nextDot = name .indexOf( "." );
            group = name .substring( 0, nextDot );
            toolSelection = new Selection();
        }
        
        Tool tool = null;
        switch ( group ) {

        case "bookmark":
        	tool = new BookmarkTool( name, toolSelection, this .model );
        	break;

        case "point reflection":
            tool = new InversionTool( name, toolSelection, this .model, this.origin );
        	break;

        case "mirror":
            tool = new MirrorTool( name, toolSelection, this .model, this.origin );
        	break;

        case "translation":
            tool = new TranslationTool( name, toolSelection, this .model, this.origin );
        	break;

        case "linear map":
            tool = new LinearMapTool( name, toolSelection, this .model, this.origin, false );
        	break;

        case "rotation":
            tool = new RotationTool( name, symmetry, this .selection, this .model, this.origin );
        	break;

        case "scaling":
        	tool = new ScalingTool( name, symmetry, this .selection, this .model, this.origin );
        	break;

        case "tetrahedral":
            tool = new SymmetryTool( name, symmetry, this .selection, this .model, this.origin );
        	break;

        case "module":
            tool = new ModuleTool( name, this .selection, this .model );
        	break;

        case "plane":
            tool = new PlaneSelectionTool( name, this .selection, this .field );
        	break;

		default:
        	tool = new SymmetryTool( name, symmetry, this .selection, this .model, this.origin );
		}
        
        this .doc .performAndRecord( (UndoableEdit) tool );
        this .put( name, tool );
		this .pcs .firePropertyChange( "tool.instances", null, name );
	}

	public void applyTool( Tool tool, int modes )
	{
		UndoableEdit edit = new ApplyTool( this, this .selection, this .model, tool, modes, true );
        this .doc .performAndRecord( edit );
	}	
	
    public void addPropertyListener( PropertyChangeListener listener )
    {
        pcs .addPropertyChangeListener( listener );
    }

    public void removePropertyListener( PropertyChangeListener listener )
    {
        pcs .removePropertyChangeListener( listener );
    }
}
