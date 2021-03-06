package org.apache.derbyBuild;

import org.apache.derby.iapi.services.cache.ClassSize;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class implements a program that catalogs the size estimate coefficients of various classes.
 * 这个类根据预估的变换类的程度进行编目
 * @see ClassSize#getSizeCoefficients.
 * The program is invoked as:
 * java -DWS=<i>work-space</i> [-DclassDir=<i>class-dir</i>] [-Dout=<i>out-file</i> [-Dprefix[.<i>x</i>=<i>package-prefix</i>]] [-Dverbose=true] org.apache.derby.iapi.services.cache.ClassSizeCrawler <i>class-or-interface</i> ...<br>
 * This program gets the size coefficients for each class in the <i>class-or-interface</i> list,
 * and for each class that implements an interface in the list. If there is an interface in the list
 * this program crawls through the classes hierarcy, starting at points specified by the prefix
 * properties, looking for classes that implement the interfaces.
 * 这个程序先get到在class-or-interface列表中到每个类到大小系数，都实现了那个接口。如果列表中有接口，这个程序会爬继承类，
 * （在prefix properties这个点？？？？，查找每个实现了接口都类）
 * If the <i>class-or-interface</i> list is empty then this program searches for implementations
 * of org.apache.derby.iapi.types.DataValueDescriptor, and at least one prefix property
 * must be specified
 * 如果class-or-interface列表为空，这个类就查找DataValueDescriptor的实现，至少有一个prefix property属性被指定。
 * The catalog is written as a java source file
 * into <i>out-file</i>, by default
 * <i>work-space</i>/java/org.apache.derby.iapi.services.cache.ClassSizeCatalogImpl.java.
 * 目录会以java源文件的形式写到out-file，默认为ClassSizeCatalogImpl.java。
 * <i>work-space</i> is the directory containing the java and classes directories. $WS in the
 * standard development environment. This property is required.
 * 这的工作空间是包含java文件和class文件的目录。
 * <i>class-dir</i> is the directory containing the compiled classes. By default it is <i>work-space</i>/classes.
 * class-dir是包含编译后的class文件的目录，默认为work-space/classes
 * <i>package-prefix</i> is the first part of a package name. e.g. "com.ibm.db2j.impl". At least
 * one prefix property must be specified if there is an interface in the list.
 * 包前缀是包名的前面部分
 * For example:<br>
 * <pre>
 * <code>
 * java -DWS=$WS \
 *      -Dprefix.1=org.apache.derby.iapi.types \
 *      org.apache.derby.iapi.services.cache.ClassSizeCrawler \
 *        org.apache.derby.iapi.types.DataValueDescriptor \
 *        java.math.BigDecimal \
 *        org.apache.derby.impl.services.cache.Generic.CachedItem
 *</code>
 *</pre>
 */
public class ClassSizeCrawler
{
    public static void main( String[] arg)
    {
       //初始化classAndInterfaceList默认值
        String[] classAndInterfaceList = {"org.apache.derby.iapi.types.DataValueDescriptor"};
       //如果参数里有值，则进行赋值
        if(arg.length > 0)
            classAndInterfaceList = arg;
        Class[] interfaceList = new Class[classAndInterfaceList.length];
        int interfaceCount = 0;//接口计数器
        Class[] classList = new Class[classAndInterfaceList.length];
        int classCount = 0;//类计数器
        //todo 怎么做到让垃圾收集器卸载的？
        Class classSizeClass = ClassSize.class; // Make sure that the garbage collector does not unload it
        ClassSize.setDummyCatalog();//todo 这个方法是干什么的？
        /* Most of the classes we will catalog invoke ClassSize.estimateBaseFromCatalog in
         * their static initializer. This dummy the catalog out so that this will not generate
         * errors. We will not actually use the classes, just examine their fields.
         */
        /**
         * 我们归类的大多数类静态初始化时调用了ClassSize.estimateBAseFromCatalog方法来达到使目录dummy，也就不会产生错误。
         * 我们不会用到这些类，只会用它们的变量。
         */
            
            
        for( int i = 0; i < classAndInterfaceList.length; i++)
        {
            Class cls = null;
            try
            {
                cls = Class.forName( classAndInterfaceList[i]);
            }
            catch( ClassNotFoundException cnfe)
            {
                System.err.println( "*** Could not find class " + classAndInterfaceList[i]);
                System.exit(1);
            }
            if( cls.isInterface())
                interfaceList[ interfaceCount++] = cls;
            else
                classList[ classCount++] = cls;
        }
        //WS是干什么的？
        String WS = System.getProperty( "WS");
        if( WS == null)
        {
            System.err.println( "*** WS is not set.");
            System.exit(1);
        }
        //根目录
        StringBuffer baseDir = new StringBuffer( System.getProperty( "classDir", ""));
        if( baseDir.length() == 0)
        {
            baseDir.append( WS);
            baseDir.append( '/');
            baseDir.append( "classes");
        }
        int baseDirLength = baseDir.length();

        StringBuffer packagePrefix = new StringBuffer( );

        Hashtable<String, int[]> classSizes = new Hashtable<String, int[]>();

        ClassSizeCrawler crawler = new ClassSizeCrawler(interfaceList, interfaceCount, classSizes);

        if( interfaceCount > 0)
        {
            boolean gotPrefix = false;
            // Crawl through the class hierarchies for classes implementing the interfaces
            //爬取实现了接口的类
            for( Enumeration e = System.getProperties().propertyNames();//propertyNames方法是做什么的？
                 e.hasMoreElements();)//这个是干嘛的？
            {
                String propertyName = (String) e.nextElement();
                if( propertyName.equals( "prefix") || propertyName.startsWith( "prefix."))
                {
                    gotPrefix = true;
                    packagePrefix.setLength( 0);
                    packagePrefix.append( System.getProperty( propertyName));
                    baseDir.setLength( baseDirLength);
                    if( packagePrefix.length() > 0)
                    {
                        baseDir.append( '/');
                        for( int offset = 0; offset < packagePrefix.length(); offset++)
                        {
                            char c = packagePrefix.charAt( offset);
                            if( c == '.')
                                baseDir.append( '/');
                            else
                                baseDir.append( c);
                        }
                    }
                    crawler.crawl( new File( baseDir.toString()), packagePrefix);
                }
            }
            if( ! gotPrefix)
            {
                System.err.println( "*** Could not search the class hierarchy because no starting");
                System.err.println( "    prefixes where specified.");
                System.exit(1);
            }
        }
        for( int i = 0; i < classCount; i++)
            crawler.addClass( classList[i]);

        baseDir.setLength( baseDirLength);
        String outputFileName =
          System.getProperty( "out", WS + "/java/org.apache.derby.iapi.services.cache.ClassSizeCatalogImpl.java");
        try
        {
            PrintWriter out = new PrintWriter( new FileWriter( outputFileName));
            out.print( "/*\n\n" +

                       "   Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                       "   contributor license agreements.  See the NOTICE file distributed with\n" +
                       "   this work for additional information regarding copyright ownership.\n" +
                       "   The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                       "   (the \"License\"); you may not use this file except in compliance with\n" +
                       "   the License.  You may obtain a copy of the License at\n" +
                       "\n" +
                       "      http://www.apache.org/licenses/LICENSE-2.0\n" +
                       "\n" +
                       "   Unless required by applicable law or agreed to in writing, software\n" +
                       "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                       "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                       "   See the License for the specific language governing permissions and\n" +
                       "   limitations under the License.\n" +
                       " */\n");
            out.print( "package org.apache.derby.iapi.services.cache;\n" +
                       "class ClassSizeCatalogImpl extends ClassSizeCatalog\n" +
                       "{\n" +
                       "    public ClassSizeCatalogImpl()\n" +
                       "    {\n");
            for( Enumeration e = classSizes.keys();
                 e.hasMoreElements();)
            {
                String className = (String) e.nextElement();
                int[] coeff = (int[]) classSizes.get( className);
                out.print( "        put( \"" + className + "\", new int[]{" + coeff[0] + "," + coeff[1] + "});\n");
            }
            out.print("    }\n" +
                      "}\n");
            out.flush();
            out.close();
        }
        catch( IOException ioe)
        {
            System.err.println( "*** Cannot write to " + outputFileName);
            System.err.println( "   " + ioe.getMessage());
            System.exit(1);
        }
    } // end of main

    private Class<?>[] interfaceList; // Search for classes that implement these interfaces
    private int interfaceCount;
    private Hashtable<String, int[]> classSizes;
    private boolean verbose = false;

    private ClassSizeCrawler( Class[] interfaceList,
                              int interfaceCount,
                              Hashtable<String, int[]> classSizes)
    {
        this.interfaceList = interfaceList;
        this.classSizes = classSizes;
        this.interfaceCount = interfaceCount;
        verbose = Boolean.parseBoolean( System.getProperty( "verbose", "false"));
    }

    private void crawl( File curDir, StringBuffer className)
    {
        if( verbose)
            System.out.println( "Searching directory " + curDir.getPath());

        try
        {
            if( ! curDir.isDirectory())
            {
                System.err.println( "*** " + curDir.getPath() + " is not a directory.");
                System.exit(1);
            }
        }
        catch( SecurityException se)
        {
            System.err.println( "Cannot access " + curDir.getPath());
            System.exit(1);
        }
        String[] filenames = curDir.list( );
        if( className.length() != 0)
            className.append( ".");

        int classNameLength = className.length();
        for( int fileIdx = 0; fileIdx < filenames.length; fileIdx++)
        {
            if( filenames[fileIdx].endsWith( ".class"))
            {
                // Strip off the ".class" suffix
                //取消.class后缀
                String s = filenames[fileIdx].substring( 0, filenames[fileIdx].length() - 6);
                className.append( s);
                Class<?> targetClass = null;
                String targetClassName = className.toString();
                try
                {
                    targetClass = Class.forName( targetClassName);
                    if( !targetClass.isInterface())
                    {
                        for( int interfaceIdx = 0; interfaceIdx < interfaceCount; interfaceIdx++)
                        {
                            if( interfaceList[interfaceIdx].isAssignableFrom( targetClass))
                                addClass( targetClass);
                        }
                    }
                }
                catch( ClassNotFoundException cnfe)
                {
                    System.err.println( "Could not find class " + targetClassName);
                    System.exit(1);
                }
                catch( Throwable t){}
                className.setLength( classNameLength);
            }
            else
            {
                File nextDir = new File( curDir, filenames[fileIdx]);
                if( nextDir.isDirectory())
                {
                    className.append( filenames[fileIdx]);
                    crawl( nextDir, className);
                    className.setLength( classNameLength);
                }
            }
        }
    } // end of crawl

    private void addClass( Class targetClass)
    {
        int[] coefficients = ClassSize.getSizeCoefficients( targetClass);
        if( verbose)
            System.out.println( targetClass.getName() + " " + coefficients[0] + ", " + coefficients[1]);
        classSizes.put( targetClass.getName(), coefficients);
    } // end of addClass
} // end of ClassSizeCrawler
