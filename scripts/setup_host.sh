sudo ip netns add h1
sudo ip link set heth h1
sudo ip netns exec h1 ifconffig lo up
sudo ip netns exec h1 ifconffig heth promisc up
