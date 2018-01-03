package org.apache.derby.diag;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.shared.common.reference.SQLState;
import org.apache.derby.iapi.services.context.Context;
import org.apache.derby.iapi.services.context.ContextService;
import org.apache.derby.iapi.sql.conn.LanguageConnectionContext;
import org.apache.derby.iapi.sql.dictionary.DataDictionary;

/**
 * Utility methods for the package of diagnostic vtis.//vti是virtual table interface的缩写
 */
abstract    class   DiagUtil
{
    /**
     * Raise an exception if we are running with SQL authorization turned on
     * but the current user isn't the database owner. This method is used
     * to restrict access to VTIs which disclose sensitive information.
     * See DERBY-5395.
     */
    static void    checkAccess()   throws StandardException
    {
        LanguageConnectionContext lcc = (LanguageConnectionContext)
            getContextOrNull(LanguageConnectionContext.CONTEXT_ID);
        DataDictionary  dd = lcc.getDataDictionary();

        if ( dd.usesSqlAuthorization() )
        {
            String  databaseOwner = dd.getAuthorizationDatabaseOwner();
            String  currentUser = lcc.getStatementContext().getSQLSessionContext().getCurrentUser();

            if ( !databaseOwner.equals( currentUser ) )
            {
                throw StandardException.newException( SQLState.DBO_ONLY );
            }
        }
    }

    
    /**
     * Privileged lookup of a Context. Must be private so that user code
     * can't call this entry point.
     */
    private  static  Context    getContextOrNull( final String contextID )
    {
        if ( System.getSecurityManager() == null )
        {
            return ContextService.getContextOrNull( contextID );
        }
        else
        {
            return AccessController.doPrivileged
                (
                 new PrivilegedAction<Context>()
                 {
                     public Context run()
                     {
                         return ContextService.getContextOrNull( contextID );
                     }
                 }
                 );
        }
    }

}


