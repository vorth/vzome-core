//(c) Copyright 2006, Scott Vorthmann

package com.vzome.core.editor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzome.core.commands.Command;
import com.vzome.core.commands.XmlSaveFormat;


public interface UndoableEdit
{
    public void perform() throws Command.Failure;
    
    public void undo();
    
    public void redo() throws Command.Failure;
    
    public boolean isVisible();

    public Element getXml( Document doc );
    
    public void loadAndPerform( Element xml, XmlSaveFormat format, Context context ) throws Command.Failure;
    
//    public interface History
//    {
//        void performAndRecord( UndoableEdit edit );
//    }
//    
    public interface Context
    {
    	UndoableEdit createEdit( String type, XmlSaveFormat format );
    	
    	void performAndRecord( UndoableEdit edit );
    }

	public void setContext( Context context );
	
	public Context getContext();
	
	public void releaseState();

	/**
	 * True when this edit must cause a persistent history branch.
	 * @return
	 */
    public boolean isSticky();

    /**
     * True when this edit invalidates redoable edits.
     * @return
     */
    public boolean isDestructive();

    public Element getDetailXml( Document doc );
}