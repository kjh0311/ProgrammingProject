// 작성자: 김진희
// 목록에 보여줄 이름, MAC을 담는 클래스

package com.kjh.blescanner.domain;

public class BluetoothDeviceData {
    private String name, mac, uuids[];

    public BluetoothDeviceData(String name, String mac, String[] uuids) {
        this.name = name;
        this.mac = mac;
        this.uuids = uuids;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public String[] getUuids() {
        return uuids;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setUuids(String[] uuids) {
        this.uuids = uuids;
    }
}
