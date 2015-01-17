
//(c) Copyright 2006, Scott Vorthmann

package com.vzome.core.editor;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.construction.Point;

public class SymmetryCenterChange implements UndoableEdit
{
    private Point mOldCenter, mNewCenter;
    private final EditorModel mEditor;
	private Context context;

    public SymmetryCenterChange( EditorModel editor, Point newCenter )
    {
        mOldCenter = editor .getCenterPoint();
        mNewCenter = newCenter;
        mEditor = editor;
    }
    
    public boolean isVisible()
    {
    	return false;
    }

    public void redo()
    {
        mEditor .setCenterPoint( mNewCenter );
    }

    public void undo()
    {
        mEditor .setCenterPoint( mOldCenter );
    }

    public Element getXml( Document doc )
    {
        Element result = doc .createElement( "SymmetryCenterChange" );
        XmlSaveFormat .serializePoint( result, "new", mNewCenter );
        return result;
    }

    @Override
    public Element getDetailXml( Document doc )
    {
        return getXml( doc );
    }

    public void loadAndPerform( Element xml, XmlSaveFormat format, Context context ) throws Failure
    {
        if ( format .rationalVectors() )
        {
            mNewCenter = format .parsePoint( xml, "new" );
        }
        else
        {
            Map attrs = format .loadCommandAttributes( xml, true );
            mNewCenter = (Point) attrs .get( "new" );
        }
        
        context .performAndRecord( this );
    }

    public void perform()
    {
        redo();
    }

	public Context getContext()
	{
		return this .context;
	}

	public void setContext( Context context )
	{
		this .context = context;
	}

    public void releaseState()
    {
        setContext( null );
    }

    public boolean isDestructive()
    {
        return true;
    }

    public boolean isSticky()
    {
        return false;
    }
}
