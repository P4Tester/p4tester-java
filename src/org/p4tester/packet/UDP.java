/**
*    Copyright 2011, Big Switch Networks, Inc.
*    Originally created by David Erickson, Stanford University
*
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package org.p4tester.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.TransportPort;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class UDP extends BasePacket {
    public static Map<TransportPort, Class<? extends IPacket>> decodeMap;


    protected short sourcePort;
    protected short destinationPort;
    protected short length;
    protected short checksum;

    /**
     * @return the sourcePort
     */
    public short getSourcePort() {
        return sourcePort;
    }


    /**
     * @param sourcePort the sourcePort to set
     */
    public UDP setSourcePort(short sourcePort) {
        this.sourcePort = sourcePort;
        return this;
    }

    /**
     * @return the destinationPort
     */
    public short getDestinationPort() {
        return destinationPort;
    }


    /**
     * @param destinationPort the destinationPort to set
     */
    public UDP setDestinationPort(short destinationPort) {
        this.destinationPort = destinationPort;
        return this;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @return the checksum
     */
    public short getChecksum() {
        return checksum;
    }

    /**
     * @param checksum the checksum to set
     */
    public UDP setChecksum(short checksum) {
        this.checksum = checksum;
        return this;
    }

    @Override
    public void resetChecksum() {
        this.checksum = 0;
        super.resetChecksum();
    }

    /**
     * Serializes the packet. Will compute and set the following fields if they
     * are set to specific values at the time serialize is called:
     *      -checksum : 0
     *      -length : 0
     */
    public byte[] serialize() {
        byte[] payloadData = null;
        if (payload != null) {
            payload.setParent(this);
            payloadData = payload.serialize();
        }

        //this.length = (short) (8 + ((payloadData == null) ? 0
        //        : payloadData.length));

        byte[] data = new byte[8];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putShort((short)this.sourcePort); // UDP packet port numbers are 16 bit
        bb.putShort((short)this.destinationPort);
        bb.putShort(this.length);
        bb.putShort(this.checksum);
        if (payloadData != null)
            bb.put(payloadData);

        if (this.parent != null && this.parent instanceof IPv4)
            ((IPv4)this.parent).setProtocol(IpProtocol.UDP);

        // compute checksum if needed
        /*
        if (this.checksum == 0) {
            bb.rewind();
            int accumulation = 0;

            // compute pseudo header mac
            if (this.parent != null && this.parent instanceof IPv4) {
                IPv4 ipv4 = (IPv4) this.parent;
                accumulation += ((ipv4.getSourceAddress() >> 16) & 0xffff)
                        + (ipv4.getSourceAddress() & 0xffff);
                accumulation += ((ipv4.getDestinationAddress() >> 16) & 0xffff)
                        + (ipv4.getDestinationAddress() & 0xffff);
                accumulation += ipv4.getProtocol().getIpProtocolNumber() & 0xff;
                accumulation += this.length & 0xffff;
            }

            for (int i = 0; i < this.length / 2; ++i) {
                accumulation += 0xffff & bb.getShort();
            }
            // pad to an even number of shorts
            if (this.length % 2 > 0) {
                accumulation += (bb.get() & 0xff) << 8;
            }

            accumulation = ((accumulation >> 16) & 0xffff)
                    + (accumulation & 0xffff);
            this.checksum = (short) (~accumulation & 0xffff);
            bb.putShort(6, this.checksum);
        }*/
        return data;
    }

    public void setLength(short length) {
        this.length = length;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 5807;
        int result = super.hashCode();
        result = prime * result + checksum;
        result = prime * result + destinationPort;
        result = prime * result + length;
        result = prime * result + sourcePort;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof UDP))
            return false;
        UDP other = (UDP) obj;
        if (checksum != other.checksum)
            return false;
        if (destinationPort != other.destinationPort)
            return false;
        if (length != other.length)
            return false;
        if (sourcePort != other.sourcePort)
            return false;
        return true;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length)
            throws PacketParsingException {
        ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        this.sourcePort = (short) (bb.getShort() & 0xffff); // short will be signed, pos or neg
        this.destinationPort = (short) (bb.getShort() & 0xffff); // convert range 0 to 65534, not -32768 to 32767
        this.length = bb.getShort();
        this.checksum = bb.getShort();
        // Grab a snapshot of the first four bytes of the UDP payload.
        // We will use these to see if the payload is SPUD, without
        // disturbing the existing byte buffer's offsets.



        this.payload = new Data();
        this.payload = payload.deserialize(data, bb.position(), bb.limit()-bb.position());
        this.payload.setParent(this);
        return this;
    }
}
