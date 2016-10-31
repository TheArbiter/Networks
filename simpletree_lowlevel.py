#!/usr/bin/python

import sys
from mininet.node import Host, OVSSwitch, Controller
from mininet.link import Link

n = sys.argv[1]
c1 = Controller( 'c1', inNamespace=False )
i = 1
while( i <= n ):
    a1 = OVSSwitch( 'a%d', inNamespace=False) % (i)
    Link( c1, ai ) 
    i = i + 1
    
i = 1
count = 1
while( i <= n ** 2):                                                                                                         
    e1 = OVSSwitch( 'e%d', inNamespace=False ) % (i)
    Link( acount, ei ) 
    if i == n:
        count = count + 1    
    i = i + 1

i = 1
count = 1
while( i <= n ** 3) :    
    h1 = Host( 'h%d' ) % (i)
    Link( ecount, hi ) 
    hi.setIP( '10.0.0.1/24' )
    if i == n:
        count = count + 1    
    i = i + 1
                                                                                                                                                                                                                                                                        
c0.start()                                                                                                            
a1.start( [ c0 ] )
a2.start( [ c0 ] )                                                                                                    
print h1.IP
print h2.IP
print h3.IP
print h4.IP
print 'Pinging ...'
print h1.cmd( 'ping -c3 ', h2.IP() )     
print h1.cmd( 'ping -c3 ', h3.IP() )     
a1.stop()                               
a2.stop()                                                                              
c0.stop() 
