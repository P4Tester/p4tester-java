#! /bin/python
import os 

def create_link(peer1, peer2):
    os.popen("ip link add %s type veth peer name %s"%(peer1, peer2))

def main():
    create_link('heth', 's0_0')
    create_link('s0_1', 's1_0')
    create_link('s0_2', 's2_0')
    create_link('s0_3', 's3_0')
    create_link('s0_4', 's4_0')
    create_link('s0_5', 's5_0')
    create_link('s0_6', 's6_0')
    create_link('s0_7', 's7_0')

if __name__ == "__main__":
    main()
