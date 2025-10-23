package com.rfid.ec_apidemo;

import android.annotation.SuppressLint;

/**
 * IP信息
 */
public class IPInfo {
    public byte[] ipaddr=null;//IP地址
    public byte[] subnetMask=null;//子网掩码
    public byte[] gateway=null;//网关
    private String MAC=null;
    public IPInfo(){
        ipaddr=new byte[4];
        subnetMask=new byte[4];
        gateway=new byte[4];
        MAC="";
    }
    public IPInfo(byte []data){
        ipaddr=new byte[4];
        subnetMask=new byte[4];
        gateway=new byte[4];
        MAC="";
        DataParse(data);
    }

    /**
     * 数据解析
     * @param data 数据
     * @return 解析成功返回true
     */
    public boolean DataParse(byte []data){
        if(data==null || data[1]!=0x19 || data.length<0x1A)
            return false;
        for(int i=0;i<4;i++) {
            ipaddr[i] = data[6 + i];
            subnetMask[i] = data[10 + i];
            gateway[i] = data[14 + i];
        }
        MAC = String.format("%02X-%02X-%02X-%02X-%02X-%02X", data[18], data[19], data[20], data[21], data[22], data[23]);
        return true;
    }

    /**
     * 获取IP信息
     * @return IP信息字符串
     */
    public String get_info(){
        return "IP:" + getIP() + "  子网掩码：" + getSubnetMask() + "  网关：" + getGateway() + "  MAC:" + MAC;
    }

    public String getIP(){
        if(ipaddr==null)
            return "0.0.0.0";
        return String.format("%d.%d.%d.%d", ipaddr[0] & 0xff, ipaddr[1] & 0xff, ipaddr[2] & 0xff, ipaddr[3] & 0xff);
    }

    public String getSubnetMask(){
        if(subnetMask==null)
            return "0.0.0.0";
        return String.format("%d.%d.%d.%d", subnetMask[0] & 0xff, subnetMask[1] & 0xff, subnetMask[2] & 0xff, subnetMask[3] & 0xff);
    }

    public String getGateway(){
        if(gateway==null)
            return "0.0.0.0";
        return String.format("%d.%d.%d.%d", gateway[0] & 0xff, gateway[1] & 0xff, gateway[2] & 0xff, gateway[3] & 0xff);
    }

    public String getMAC(){
        return MAC;
    }
}
