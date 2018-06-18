sudo ip netns add h1
sudo ip link set heth netns h1
sudo ip netns exec h1 ifconfig lo up
sudo ip netns exec h1 ifconfig heth promisc up
