package org.apache.derby.vti;

/**
 * <p>
 * Context parameter which is passed to an AwareVTI.
 * </p>
 */
public class VTIContext
{
    /////////////////////////////////////////////////////////////////
    //
    //  CONSTANTS
    //
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    //
    //  STATE
    //
    /////////////////////////////////////////////////////////////////

    private String  _vtiSchema;
    private String  _vtiTable;
    private String  _statementText;
    
    /////////////////////////////////////////////////////////////////
    //
    //  CONSTRUCTOR
    //
    /////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Construct from pieces.
     * </p>
     *
     * @param   vtiSchema   Name of the schema holding the table function.
     * @param   vtiTable      Name of the table function.
     * @param   statementText   Text of the statement which is invoking the table function.
     */
    public  VTIContext
        (
         String vtiSchema,
         String vtiTable,
         String statementText
         )
    {
        _vtiSchema = vtiSchema;
        _vtiTable = vtiTable;
        _statementText = statementText;
    }
    
    /////////////////////////////////////////////////////////////////
    //
    //  PUBLIC BEHAVIOR
    //
    /////////////////////////////////////////////////////////////////

    /** Return the name of the schema holding the table function */
    public  String  vtiSchema() { return _vtiSchema; }

    /** Return the unqualified table function name */
    public  String  vtiTable()  { return _vtiTable; }

    /** Return the text of the statement which invoked the table function */
    public  String  statementText() { return _statementText; }
    
}
