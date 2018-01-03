package org.apache.derby.vti;

/**
 * <p>
 * Interface describing a table function which can be given information about the context
 * in which it runs.
 * </p>
 */
public interface AwareVTI
{
    /////////////////////////////////////////////////////////////////
    //
    //  PUBLIC BEHAVIOR
    //
    /////////////////////////////////////////////////////////////////

    /** Get the table function context */
    public  VTIContext  getContext();

    /** Set the table function context */
    public  void    setContext( VTIContext context );
    
}
