## Jython and vZome

In the branch where this file originates, I've added some bits to the ``build.gradle`` file that capture required JARs to the ``build/output/jythonPath`` folder.  That done, you must set the ``JYTHONPATH`` environment variable as follows (in tcsh):

```
setenv JYTHONPATH `pwd`/build/output/jythonPath/vecmath-1.6.0-pre11.jar:`pwd`/build/libs/vzome-core-0.4.jar
```
That seems sufficient to get as far as the defect below.

The problem with the defect is the classloader in use.  We're using the thread context classloader, and that seems incorrect for Jython.  If I recall correctly, I changed to that classloader either to support use in Android (Cardboard), or in the exporter servlet.  (See the ``servlet`` Git branch.)


```
[mini:~/vZome/vzome-core] vorth% ~/jython2.5.3/bin/jython
Jython 2.5.3 (2.5:c56500f08d34+, Aug 13 2012, 14:48:36) 
[Java HotSpot(TM) 64-Bit Server VM (Oracle Corporation)] on java1.8.0_45
Type "help", "copyright", "credits" or "license" for more information.
>>> import com.vzome.api                                                             
>>> v = com.vzome.api.Application()                                                  
>>> import java.io                                                                   
>>> fis = java.io. FileInputStream( "/Users/vorth/Documents/testSetItemColor.vZome" )
>>> doc = v.loadDocument( fis )                                                      
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
	at com.vzome.core.viewing.ScriptedShapes.buildConnectorShape(ScriptedShapes.java:58)
	at com.vzome.core.viewing.ExportedVEFShapes.buildConnectorShape(ExportedVEFShapes.java:78)
	at com.vzome.core.viewing.AbstractShapes.getConnectorShape(AbstractShapes.java:72)
	at com.vzome.core.render.RenderedModel.resetAttributes(RenderedModel.java:406)
	at com.vzome.core.render.RenderedModel.resetAttributes(RenderedModel.java:316)
	at com.vzome.core.render.RenderedModel.manifestationAdded(RenderedModel.java:138)
	at com.vzome.core.model.RealizedModel.privateShow(RealizedModel.java:157)
	at com.vzome.core.model.RealizedModel.show(RealizedModel.java:149)
	at com.vzome.core.editor.EditorModel.<init>(EditorModel.java:27)
	at com.vzome.core.editor.DocumentModel.<init>(DocumentModel.java:184)
	at com.vzome.core.editor.Application.loadDocument(Application.java:499)
	at com.vzome.api.Application.loadDocument(Application.java:42)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)

java.lang.IllegalStateException: java.lang.IllegalStateException: missing script: com/vzome/core/parts/default/connector.zomic
>>> exit()
```
