/*

   Derby - Class org.apache.derby.client.net.OpenSocketAction

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package org.apache.derby.client.net;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.derby.iapi.services.info.JVMInfo;

public class OpenSocketAction implements java.security.PrivilegedExceptionAction {
    private String server_;
    private int port_;
    private int clientSSLMode_;

    public OpenSocketAction(String server, int port, int clientSSLMode) {
        server_ = server;
        port_ = port;
        clientSSLMode_ = clientSSLMode;
    }

    public Object run() 
        throws java.net.UnknownHostException, 
               java.io.IOException,
               java.security.NoSuchAlgorithmException,
               java.security.KeyManagementException,
               java.security.NoSuchProviderException,
               java.security.KeyStoreException,
               java.security.UnrecoverableKeyException,
               java.security.cert.CertificateException
    {
        
        SocketFactory sf;
        switch (clientSSLMode_) {
        case org.apache.derby.jdbc.ClientBaseDataSource.SSL_BASIC:
            sf = NaiveTrustManager.getSocketFactory();
            break;
        case org.apache.derby.jdbc.ClientBaseDataSource.SSL_PEER_AUTHENTICATION:
            sf = (SocketFactory)SSLSocketFactory.getDefault();
            break;
        case org.apache.derby.jdbc.ClientBaseDataSource.SSL_OFF:
            sf = SocketFactory.getDefault();
            break;
        default: 
            // Assumes cleartext for undefined values
            sf = SocketFactory.getDefault();
            break;
        }
        
        //DERBY-6778(SSL tests are failing on 10.8 codeline with 
        // IBM jdk 1.4.2 after poodle security backport)
        //In order to work around the IBM jdk 1.4.2 issue, do not
        // try to disable SSLv3 and SSLV2Hello protocols for this 
        // specific jdk. 
        //The SSLv3 and SSLV2Hello protocols can lead to poodle 
        // security issue and that is why they are getting disabled 
        // for all the other jdks as per DERBY-6764.
        boolean SSLclient = false;//are we working with SSL enabled client
        if ((clientSSLMode_ == org.apache.derby.jdbc.ClientBaseDataSource.SSL_BASIC ||
                clientSSLMode_ == org.apache.derby.jdbc.ClientBaseDataSource.SSL_PEER_AUTHENTICATION))
        	SSLclient = true;
        
        boolean ibmJdk142 = false;//are we working with IBM jdk 1.4.2
        if (JVMInfo.isIBMJVM() && JVMInfo.JDK_ID == JVMInfo.J2SE_142)
        	ibmJdk142 = true;

        if (SSLclient && !ibmJdk142) {
        	//DERBY-6764(analyze impact of poodle security alert on Derby 
        	// client - server ssl support)
        	//If SSLv3 and/or SSLv2Hello is one of the enabled protocols,  
        	// then we want to remove it from the list of enabled protocols  
        	// because of poodle security breach
        	SSLSocket sSocket = (SSLSocket)sf.createSocket(server_, port_);
        	String[] enabledProtocols = sSocket.getEnabledProtocols();

            //If SSLv3 and/or SSLv2Hello is one of the enabled protocols, 
            // then remove it from the list of enabled protocols because of 
            // its security breach.
            String[] supportedProtocols = new String[enabledProtocols.length];
            int supportedProtocolsCount  = 0;
            for ( int i = 0; i < enabledProtocols.length; i++ )
            {
                if (!(enabledProtocols[i].toUpperCase().indexOf ("SSLV3") >=0 ||
                    enabledProtocols[i].toUpperCase().indexOf("SSLV2HELLO") >=0 )) {
                	supportedProtocols[supportedProtocolsCount] = 
                			enabledProtocols[i];
                	supportedProtocolsCount++;
                }
            }
            if(supportedProtocolsCount < enabledProtocols.length) {
            	String[] newEnabledProtocolsList = null;
            	//We found that SSLv3 and or SSLv2Hello is one of the enabled 
            	// protocols for this jvm. Following code will remove it from 
            	// enabled list.
            	newEnabledProtocolsList = 
            			new String[supportedProtocolsCount];
            	System.arraycopy(supportedProtocols, 0, 
            			newEnabledProtocolsList, 0, 
            			supportedProtocolsCount);
            	sSocket.setEnabledProtocols(newEnabledProtocolsList);
            }
            return sSocket;
        } else
            return sf.createSocket(server_, port_);
    }

}
