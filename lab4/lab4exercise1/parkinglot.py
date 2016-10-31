#!/usr/bin/python

"Creates a parking lot topology, \
    generates flows from senders to the receiver, \
    measures throughput of each flow"

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import lg, output
from mininet.node import CPULimitedHost
from mininet.link import TCLink
from mininet.util import irange, custom, quietRun, dumpNetConnections
from mininet.cli import CLI

from time import sleep, time
from multiprocessing import Process
from subprocess import Popen
import termcolor as T
import argparse

import sys
import os
from util.monitor import monitor_devs_ng

def cprint(s, color, cr=True):
    """Print in color
       s: string to print
       color: color to use"""
    if cr:
        print T.colored(s, color)
    else:
        print T.colored(s, color),

parser = argparse.ArgumentParser(description="Parking lot tests")
parser.add_argument('--bw', '-b',
                    type=float,
                    help="Bandwidth of network links",
                    required=True)

parser.add_argument('--dir', '-d',
                    help="Directory to store outputs",
                    default="results")

parser.add_argument('-n',
                    type=int,
                    help=("Number of senders in the parking lot topo."
                    "Must be >= 1"),
                    required=True)

parser.add_argument('--cli', '-c',
                    action='store_true',
                    help='Run CLI for topology debugging purposes')

parser.add_argument('--time', '-t',
                    dest="time",
                    type=int,
                    help="Duration of the experiment.",
                    default=60)

# Expt parameters
args = parser.parse_args()

if not os.path.exists(args.dir):
    os.makedirs(args.dir)

lg.setLogLevel('info')

# Topology to be instantiated in Mininet
class ParkingLotTopo(Topo):
    "Parking Lot Topology"

    def __init__(self, n=1, cpu=.1, bw=10, delay=None,
                 max_queue_size=None, **params):
        """Parking lot topology with one receiver
           and n clients.
           n: number of clients
           cpu: system fraction for each host
           bw: link bandwidth in Mb/s
           delay: link delay (e.g. 10ms)"""

        # Initialize topo
        Topo.__init__(self, **params)

        # Host and link configuration
        hconfig = {'cpu': cpu}
        lconfig = {'bw': bw, 'delay': delay,
                   'max_queue_size': max_queue_size }

        # Create the actual topology
        receiver = self.addHost('receiver')

        # Switch ports 1:uplink 2:hostlink 3:downlink
        uplink, hostlink, downlink = 1, 2, 3

	s = [] # Python list of switches
        h = [] # Python list of hosts

	# dynamically add all hosts and switches to network backbone first
        for i in range(n):
            switch_name = 's%s' % (i+1)
            host_name   = 'h%s' % (i+1)

            s.append( self.addSwitch(switch_name) )  # s[1] is switch1
            h.append( self.addHost(host_name) )      # h[1] is host1

            # Wire up hosts
            self.addLink(h[i], s[i], port1=0, port2=hostlink, **lconfig)
            
            # link to previous switch
            if i > 0:
                self.addLink(s[i-1], s[i], port1=downlink, port2=uplink, **lconfig)

        # Wire up receiver to first switch
        self.addLink(receiver, s[0], port1=0, port2=uplink, **lconfig)


def waitListening(client, server, port):
    "Wait until server is listening on port"
    if not 'telnet' in client.cmd('which telnet'):
        raise Exception('Could not find telnet')
    cmd = ('sh -c "echo A | telnet -e A %s %s"' %
           (server.IP(), port))
    while 'Connected' not in client.cmd(cmd):
        output('waiting for', server,
               'to listen on port', port, '\n')
        sleep(.5)

def progress(t):
    while t > 0:
        cprint('  %3d seconds left  \r' % (t), 'cyan', cr=False)
        t -= 1
        sys.stdout.flush()
        sleep(1)
    print

def start_tcpprobe():
    os.system("rmmod tcp_probe 1>/dev/null 2>&1; modprobe tcp_probe")
    Popen("cat /proc/net/tcpprobe > %s/tcp_probe.txt" % args.dir, shell=True)

def stop_tcpprobe():
    os.system("killall -9 cat; rmmod tcp_probe")

def run_parkinglot_expt(net, n):
    "Run experiment"

    seconds = args.time

    # Start the bandwidth and cwnd monitors in the background
    monitor = Process(target=monitor_devs_ng,
            args=('%s/bwm.txt' % args.dir, 1.0))
    monitor.start()
    start_tcpprobe()

    # Get receiver
    recvr = net.getNodeByName('receiver')
    
    # Start the receiver
    port = 5001
    recvr.cmd('iperf -s -p', port,
              '> %s/iperf_server.txt' % args.dir, '&')

    # get handle on each hosts using getNodeByName
    h = [] # Python list of hosts
    for i in range(n):
	h.append (net.getNodeByName('h%s' % (i+1)))

    # wait for ports on all iperf clients
    for i in range(n):
        waitListening(h[i], recvr, port)  

    # run iperf client on each client and generate a TCP data flow
    for i in range(n):
        node_name = 'h%s' % (i+1)
	h[i].sendCmd('iperf -c %s -p %s -t %d -i 1 -yc >%s/iperf_%s.txt' % (recvr.IP(), port, seconds, args.dir, node_name))


    # Count down time
    progress(seconds)

    # Wait for clients to complete
    # If you don't do this, iperf may keep running!
    output('Waiting for hosts to complete...\n')
    for i in range(n):
        h[i].waitOutput(verbose=True)

    recvr.cmd('kill %iperf')

    # Shut down monitors
    monitor.terminate()
    stop_tcpprobe()

def check_prereqs():
    "Check for necessary programs"
    prereqs = ['telnet', 'bwm-ng', 'iperf', 'ping']
    for p in prereqs:
        if not quietRun('which ' + p):
            raise Exception((
                'Could not find %s - make sure that it is '
                'installed and in your $PATH') % p)

def main():
    "Create and run experiment"
    start = time()

    topo = ParkingLotTopo(n=args.n)

    host = custom(CPULimitedHost, cpu=.15)  # 15% of system bandwidth
    link = custom(TCLink, bw=args.bw, delay='1ms',
                  max_queue_size=200)

    net = Mininet(topo=topo, host=host, link=link)

    net.start()

    cprint("*** Dumping network connections:", "green")
    dumpNetConnections(net)

    cprint("*** Testing connectivity", "blue")

    net.pingAll()

    if args.cli:
        # Run CLI instead of experiment
        CLI(net)
    else:
        cprint("*** Running experiment", "magenta")
        run_parkinglot_expt(net, n=args.n)

    net.stop()
    end = time()
    os.system("killall -9 bwm-ng")
    cprint("Experiment took %.3f seconds" % (end - start), "yellow")

if __name__ == '__main__':
    check_prereqs()
    main()
