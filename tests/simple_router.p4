header_type ethernet_t {
    fields {
        dstAddr : 48;
        srcAddr : 48;
        etherType : 16;
    }
}

header_type ipv4_t {
    fields {
        version : 4;
        ihl : 4;
        diffserv : 8;
        totalLen : 16;
        identification : 16;
        flags : 3;
        fragOffset : 13;
        ttl : 8;
        protocol : 8;
        hdrChecksum : 16;
        srcAddr : 32;
        dstAddr: 32;
    }
}


header_type udp_t {
    fields {
        dstPort : 16;
        srcPort : 16;
	len : 16;
        checksum: 16;
    }
}

header udp_t udp;

header_type sr_t {
    fields {
    	port : 8;
        padding : 6;
        s : 1;
        f : 1;
    }
}

header sr_t sr[40];

header_type test_record_t {

	fields {
        	port : 16;
            id : 16;
        }
}

header test_record_t test_record;

parser start {
    return parse_ethernet;
}

#define ETHERTYPE_IPV4 0x0800

header ethernet_t ethernet;

parser parse_ethernet {
    extract(ethernet);
    return select(latest.etherType) {
        ETHERTYPE_IPV4 : parse_ipv4;
        default: ingress;
    }
}

header ipv4_t ipv4;

field_list ipv4_checksum_list {
        ipv4.version;
        ipv4.ihl;
        ipv4.diffserv;
        ipv4.totalLen;
        ipv4.identification;
        ipv4.flags;
        ipv4.fragOffset;
        ipv4.ttl;
        ipv4.protocol;
        ipv4.srcAddr;
        ipv4.dstAddr;
}

field_list_calculation ipv4_checksum {
    input {
        ipv4_checksum_list;
    }
    algorithm : csum16;
    output_width : 16;
}

calculated_field ipv4.hdrChecksum  {
    verify ipv4_checksum;
    update ipv4_checksum;
}

parser parse_ipv4 {
    extract(ipv4);
    return select(ipv4.protocol) {
        17 : parse_udp;
        default: ingress;
    }
}

parser parse_udp {
	extract(udp);
	return select(udp.dstPort) {
        11111 : parse_sr;
        default: ingress;
    }
}

parser parse_test_record {
    extract(test_record);
    return ingress;
}

parser parse_sr {
	extract(sr[next]);
	return select(latest.s) {
        0 : parse_sr;
        1 : ingress;
        default : parse_test_record;
    } 
}

action _drop() {
    drop();
}

header_type routing_metadata_t {
    fields {
        nhop_ipv4 : 32;
    }
}

metadata routing_metadata_t routing_metadata;

action set_nhop(nhop_ipv4, port) {
    modify_field(routing_metadata.nhop_ipv4, nhop_ipv4);
    modify_field(standard_metadata.egress_spec, port);
    modify_field(ipv4.ttl, ipv4.ttl - 1);
}

table ipv4_lpm {
    reads {
        ipv4.dstAddr : lpm;
    }
    actions {
        set_nhop;
        _drop;
    }
    size: 1024;
}

action record_test(id) {
    add_header(test_record);
    add(udp.len, udp.len, 4);
    modify_field(test_record.port, standard_metadata.egress_spec);
    modify_field(test_record.id, id);
}

table record {
    actions {
        record_test;
    }
}

action p4tester_forward() {
    modify_field(standard_metadata.egress_spec, sr[0].port);
    subtract(udp.len, udp.len, 2);
    remove_header(sr[0]);
}
table forward {
    actions {
        p4tester_forward;
    }
}
control p4tester {
    //if (sr[0].f == 1) {
        apply(record);
    //}
    apply(forward);
}

control ingress {
    if(valid(ipv4)) {
        apply(ipv4_lpm);
    }

    if (valid(sr[0])) {
        p4tester();
    }
}

control egress {
}


