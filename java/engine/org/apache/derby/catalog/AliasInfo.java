package org.apache.derby.catalog;//目录

/**
 *
 * An interface for describing an alias in Derby systems.
 * 
 * In a Derby system, an alias can be one of the following:
 * <ul>
 * <li>method alias
 * <li>UDT alias
 * <li>class alias
 * <li>synonym
 * <li>user-defined aggregate
 * </ul>
 *
 */
public interface AliasInfo//别名,好多重复,为什么合并成一个呢?都有什么意义??
{
	/**
	 * Public statics for the various alias types as both char and String.
	 */
	public static final char ALIAS_TYPE_UDT_AS_CHAR		= 'A';//userdefinedtype用户定义类型
	public static final char ALIAS_TYPE_AGGREGATE_AS_CHAR		= 'G';//aggregate聚合函数?
	public static final char ALIAS_TYPE_PROCEDURE_AS_CHAR		= 'P';//procedure存储过程
	public static final char ALIAS_TYPE_FUNCTION_AS_CHAR		= 'F';//function函数
	public static final char ALIAS_TYPE_SYNONYM_AS_CHAR             = 'S';	//synonym同义词

	public static final String ALIAS_TYPE_UDT_AS_STRING		= "A";
	public static final String ALIAS_TYPE_AGGREGATE_AS_STRING		= "G";
	public static final String ALIAS_TYPE_PROCEDURE_AS_STRING		= "P";
	public static final String ALIAS_TYPE_FUNCTION_AS_STRING		= "F";
	public static final String ALIAS_TYPE_SYNONYM_AS_STRING  		= "S";

	/**
	 * Public statics for the various alias name spaces as both char and String.
	 */
	public static final char ALIAS_NAME_SPACE_UDT_AS_CHAR	= 'A';
	public static final char ALIAS_NAME_SPACE_AGGREGATE_AS_CHAR	= 'G';
	public static final char ALIAS_NAME_SPACE_PROCEDURE_AS_CHAR	= 'P';
	public static final char ALIAS_NAME_SPACE_FUNCTION_AS_CHAR	= 'F';
	public static final char ALIAS_NAME_SPACE_SYNONYM_AS_CHAR       = 'S';

	public static final String ALIAS_NAME_SPACE_UDT_AS_STRING	= "A";
	public static final String ALIAS_NAME_SPACE_AGGREGATE_AS_STRING	= "G";
	public static final String ALIAS_NAME_SPACE_PROCEDURE_AS_STRING	= "P";
	public static final String ALIAS_NAME_SPACE_FUNCTION_AS_STRING	= "F";
	public static final String ALIAS_NAME_SPACE_SYNONYM_AS_STRING   = "S";

	/**
	 * Get the name of the static method that the alias 
	 * represents at the source database.  (Only meaningful for
	 * method aliases )
	 *
	 * @return The name of the static method that the alias 
	 * represents at the source database.
	 */
	public String getMethodName();

	/**
	 * Return true if this alias is a Table Function.
	 */
	public boolean isTableFunction();

}
