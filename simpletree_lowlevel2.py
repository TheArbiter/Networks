#!/usr/bin/python

from mininet.node import Host, OVSSwitch, Controller
from mininet.link import Link

h1 = Host( 'h1' )                                                                                                     
h2 = Host( 'h2' )
h3 = Host( 'h3' )                                                                                                     
h4 = Host( 'h4' )
h5 = Host( 'h5' )                                                                                                     
h6 = Host( 'h6' )
h7 = Host( 'h7' )                                                                                                     
h8 = Host( 'h8' )
e1 = OVSSwitch( 'e1', inNamespace=False )
e2 = OVSSwitch( 'e2', inNamespace=False )                                                                             
e3 = OVSSwitch( 'e3', inNamespace=False )
e4 = OVSSwitch( 'e4', inNamespace=False )                                                                             
a1 = OVSSwitch( 'a1', inNamespace=False )
a2 = OVSSwitch( 'a2', inNamespace=False )                                                                             
c1 = Controller( 'c1', inNamespace=False )                                                                            
Link( h1, e1 )                                                                                                        
Link( h2, e1 )
Link( h3, e2 )                                                                                                        
Link( h4, e2 )
Link( h5, e3 )                                                                                                        
Link( h6, e3 )
Link( h7, e4 )                                                                                                        
Link( h8, e4 )
h1.setIP( '10.0.0.1/24' )  
h2.setIP( '10.0.0.2/24' )  
h3.setIP( '10.0.0.3/24' )  
h4.setIP( '10.0.0.4/24' )  
h5.setIP( '10.0.0.5/24' )  
h6.setIP( '10.0.0.6/24' )  
h7.setIP( '10.0.0.7/24' )  
h8.setIP( '10.0.0.8/24' )  
c1.start()                                                                                                            
a1.start( [ c1 ] )
a2.start( [ c1 ] )                                                                                                    
print h1.IP
print h2.IP
print h3.IP
print h4.IP
print h5.IP
print h6.IP
print h7.IP
print h8.IP
print 'Pinging ...'
print h1.cmd( 'ping -c3 ', h2.IP() )     
print h1.cmd( 'ping -c3 ', h3.IP() )     
a1.stop()                               
a2.stop()                                                                           
c1.stop() 
