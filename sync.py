#!/usr/bin/env python

import sys, os, time

if len(sys.argv) != 2:
  print "usage: python prog <hostfile>"
  sys.exit(1)

hostfile = sys.argv[1]

# full path.
sync_path = "~/petuum-java-cmu-605"

with open(hostfile, "r") as f:
  ips = f.readlines()

for ip in ips:
  cmd = "rsync -avhce \"ssh -i 15619primer.pem -o StrictHostKeyChecking=no\" " \
      + sync_path + " " + ip.strip() + ":~/"
  print cmd
  time.sleep(2)
  os.system(cmd)
