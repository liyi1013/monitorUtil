package util

import (
	"net"
)

// GetLocalIP 获取本机的ip地址（ipv4）
func GetLocalIP() (ip string, err error) {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return
	}
	for _, addr := range addrs {
		ipAddr, ok := addr.(*net.IPNet)
		if !ok {
			continue
		}
		if ipAddr.IP.IsLoopback() {
			continue
		}
		if !ipAddr.IP.IsGlobalUnicast() {
			continue
		}
		//println(ipAddr.IP.String())
		if ipAddr.IP.To4() != nil {
			ip = ipAddr.IP.String()
			return ip, nil
		}
	}
	return
}
