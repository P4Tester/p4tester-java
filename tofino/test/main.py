
import pd_base_tests

import random
from ptf import config
from ptf.testutils import *
from ptf.thriftutils import *

from p4tester.p4_pd_rpc.ttypes import *
from res_pd_rpc.ttypes import *
from mirror_pd_rpc.ttypes import *
from conn_mgr_pd_rpc.ttypes import *
from mc_pd_rpc.ttypes import *
from devport_mgr_pd_rpc.ttypes import *
from ptf_port import *
from pal_rpc.ttypes import *

PROGRAM_NAME = "p4tester"
NUM_DEL_ENTRIES = 100

mask_map = [
    '0.0.0.0',
    '128.0.0.0',
    '192.0.0.0',
    '224.0.0.0',
    '240.0.0.0',
    '248.0.0.0',
    '252.0.0.0',
    '254.0.0.0',
    '255.0.0.0',
    '255.128.0.0',
    '255.192.0.0',
    '255.224.0.0',
    '255.240.0.0',
    '255.248.0.0',
    '255.252.0.0',
    '255.254.0.0',
    '255.255.0.0',
    '255.255.128.0',
    '255.255.192.0',
    '255.255.224.0',
    '255.255.240.0',
    '255.255.248.0',
    '255.255.252.0',
    '255.255.254.0',
    '255.255.255.0',
    '255.255.255.128',
    '255.255.255.192',
    '255.255.255.224',
    '255.255.255.240',
    '255.255.255.248',
    '255.255.255.252',
    '255.255.255.254',
    '255.255.255.255'
]

def mirror_session(mir_type, mir_dir, sid, egr_port=0, egr_port_v=False,
                   egr_port_queue=0, packet_color=0, mcast_grp_a=0,
                   mcast_grp_a_v=False, mcast_grp_b=0, mcast_grp_b_v=False,
                   max_pkt_len=0, level1_mcast_hash=0, level2_mcast_hash=0,
                   cos=0, c2c=0, extract_len=0, timeout=0, int_hdr=[]):
    return MirrorSessionInfo_t(mir_type,
                               mir_dir,
                               sid,
                               egr_port,
                               egr_port_v,
                               egr_port_queue,
                               packet_color,
                               mcast_grp_a,
                               mcast_grp_a_v,
                               mcast_grp_b,
                               mcast_grp_b_v,
                               max_pkt_len,
                               level1_mcast_hash,
                               level2_mcast_hash,
                               cos,
                               c2c,
                               extract_len,
                               timeout,
                               int_hdr,
                               len(int_hdr))


class DCINTTest(pd_base_tests.ThriftInterfaceDataPlane):
    def __init__(self):
        pd_base_tests.ThriftInterfaceDataPlane.__init__(self,
                                                        [PROGRAM_NAME])

    # The setUp() method is used to prepare the test fixture. Typically
    # you would use it to establich connection to the Thrift server.
    #
    # You can also put the initial device configuration there. However,
    # if during this process an error is encountered, it will be considered
    # as a test error (meaning the test is incorrect),
    # rather than a test failure
    def setUp(self):
        pd_base_tests.ThriftInterfaceDataPlane.setUp(self)

        self.sess_hdl = self.conn_mgr.client_init()
        self.dev      = 0
        self.dev_tgt  = DevTarget_t(self.dev, hex_to_i16(0xFFFF))
        self.entries = {}
        # self.mc_sess_hdl = self.mc.mc_create_session()

        print("\nConnected to Device %d, Session %d" % (
            self.dev, self.sess_hdl))

    # This method represents the test itself. Typically you would want to
    # configure the device (e.g. by populating the tables), send some
    # traffic and check the results.
    #
    # For more flexible checks, you can import unittest module and use
    # the provided methods, such as unittest.assertEqual()
    #
    # Do not enclose the code into try/except/finally -- this is done by
    # the framework itself
    def addTableEntry(self, table_name, action_name, match_fields, action_parameters, priority=None):
        command_str = "%s_%s_match_spec_t"%(PROGRAM_NAME, table_name)
        match_spec = eval(command_str)(*match_fields)
        action_spec = None
        if len(action_parameters) > 0:
            command_str = "%s_%s_action_spec_t"%(PROGRAM_NAME, action_name)
            action_spec = eval(command_str)(*action_parameters)

        if action_spec is None:
            if priority is None:
                command_str = "self.client.%s_table_add_with_%s(self.sess_hdl, self.dev_tgt, match_spec)" % (table_name, action_name)
            else:
                command_str = "self.client.%s_table_add_with_%s(self.sess_hdl, self.dev_tgt, match_spec, priority)" % (table_name, action_name)
        else:
            if priority is None:
                command_str = "self.client.%s_table_add_with_%s(self.sess_hdl, self.dev_tgt, match_spec, action_spec)" % (table_name, action_name)
            else:
                command_str = "self.client.%s_table_add_with_%s(self.sess_hdl, self.dev_tgt, match_spec, action_spec, priority)" % (table_name, action_name)
        if table_name not in self.entries:
            self.entries[table_name] = []
        self.entries[table_name].append(eval(command_str))
        self.conn_mgr.complete_operations(self.sess_hdl)

    def delTableEntry(self, table, enrty_list):
        delete_func = "self.client." + table + "_table_delete"
        for entry in enrty_list:
            exec delete_func + "(self.sess_hdl, self.dev, entry)"
        self.conn_mgr.complete_operations(self.sess_hdl)

    def setDefaultEntry(self, table_name, action_name, action_parameters):
        command_str = ''
        action_spec = None
        if len(action_parameters) > 0:
            command_str = "%s_%s_action_spec_t"%(PROGRAM_NAME, action_name)
            action_spec = eval(command_str)(*action_parameters)

        if action_spec is None:
            command_str = "self.client.%s_set_default_action_%s(self.sess_hdl, self.dev_tgt)" % (table_name, action_name)
        else:
            command_str = "self.client.%s_set_default_action_%s(self.sess_hdl, self.dev_tgt, action_spec)" % (table_name, action_name)
        eval(command_str)
        self.conn_mgr.complete_operations(self.sess_hdl)

    def addMirrorSession(self, sid, dst_port):
        info = mirror_session(MirrorType_e.PD_MIRROR_TYPE_NORM,
                              Direction_e.PD_DIR_INGRESS,
                              sid,
                              dst_port,
                              True)
        self.mirror.mirror_session_create(self.sess_hdl, self.dev_tgt, info)
        self.conn_mgr.complete_operations(self.sess_hdl)

    def setupPort(self, port, an):
        speed = pal_port_speed_t.BF_SPEED_10G
        fec = pal_fec_type_t.BF_FEC_TYP_NONE
        self.pal.pal_port_add(self.dev, port, speed, fec)
        if an is not None:
            self.pal.pal_port_an_set(self.dev, port, an)
        self.pal.pal_port_enable(self.dev, port)

    def runTest(self):
        print "Populate table entries!"

        # for port in [128, 129, 130, 131, 0, 1, 2, 3]:
        #    self.setupPort(port, None)

        # for port in [136, 137, 138, 139, 8, 9, 10, 11]:
        #    self.setupPort(port, pal_autoneg_policy_t.BF_AN_FORCE_DISABLE)
        f = open("ip.txt")
        for l in f:
            tmp = l.strip('\n').split(' ')
            ip = tmp[0]
            prefix = int(tmp[1])
            print ip, prefix
            # hex_to_i16(prefix) ipv4Addr_to_i32(ip),1
            self.addTableEntry("ipv4_lpm", "set_nhop", (ipv4Addr_to_i32(ip),ipv4Addr_to_i32(mask_map[prefix])), (), prefix)
        f.close()
        print "delete %d entries"%NUM_DEL_ENTRIES
        entry_list = random.sample(self.entries["ipv4_lpm"], NUM_DEL_ENTRIES)
        self.delTableEntry("ipv4_lpm", entry_list)

    # Use this method to return the DUT to the initial state by cleaning
    # all the configuration and clearing up the connection
    def tearDown_(self):
        try:
            print("Clearing table entries")
            for table in self.entries.keys():
                delete_func = "self.client." + table + "_table_delete"
                for entry in self.entries[table]:
                    exec delete_func + "(self.sess_hdl, self.dev, entry)"
        except:
            print("Error while cleaning up. ")
            print("You might need to restart the driver")
        finally:
            self.conn_mgr.complete_operations(self.sess_hdl)
            self.conn_mgr.client_cleanup(self.sess_hdl)
            print("Closed Session %d" % self.sess_hdl)
            # pd_base_tests.ThriftInterfaceDataPlane.tearDown(self)
