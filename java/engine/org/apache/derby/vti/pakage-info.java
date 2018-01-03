这个包实现的功能是?
 Support for Table Functions.表函数
Derby lets you declare functions which return ResultSets.
        You can then use these function results as tables in your queries.
        This in turn lets you do the following:

Migrate - Bulk-load data from an external
        database. The external data source could be any vendor's
        database.

Integrate - Transform live data from an existing
        legacy server and load the data into Derby. This lets users
        build new Derby-powered apps against subsets of legacy data,
        keep the data current, but limit the burden which the new apps
        place on the legacy server.

Snapshot - Copy a subset of server data to a laptop before travelling.

Federate - Join data from multiple external data
        sources. The external sources could be other relational databases
        or they could be non-relational data feeds.




Here is an example of how to declare and invoke a Table Function:

CREATE FUNCTION externalEmployees
        ()
        RETURNS TABLE
        (
        employeeId    INT,
        lastName       VARCHAR( 50 ),
        firstName      VARCHAR( 50 ),
        birthday         DATE
        )
        LANGUAGE JAVA
        PARAMETER STYLE DERBY_JDBC_RESULT_SET
        NO SQL
        EXTERNAL NAME 'com.acme.hrSchema.EmployeesTable.read'
        ;

        INSERT INTO employees
        SELECT s.*
        FROM TABLE (externalEmployees() ) s;


The Derby optimizer makes some assumptions about these Table Functions:



Cost - The optimizer hard-codes a guess about how expensive
        it is to materialize a Table Function.

Count - The optimizer also hard-codes a guess about how
        many rows a Table Function returns.

Repeatability - The optimizer assumes that the same results
        come back each time you invoke a Table Function.




Based on these assumptions, the optimizer decides where to place the
        Table Function in the join order. Using the interfaces in this package,
        you may override the optimizer's guesses and force the optimizer to
        choose a better join order.



./VTICosting.html">VTICosting - This interface
exposes methods which let you override the optimizer's guesses.
./VTIEnvironment.html">VTIEnvironment - This is a
        state variable, created by the optimizer and passed to the methods
        in ./VTICosting.html">VTICosting.
./VTICosting.html">VTICosting methods use this state
        variable to communicate with one another and learn more about the
        operating environment.

