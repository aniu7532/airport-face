package com.arcsoft.arcfacedemo.entity;

/**
 * RFID 标签读取结果，用于实体卡刷卡识别。
 */
public class Tag {
    /** 标签唯一标识 UID，8 字节 */
    public byte[] UID=null;
    /** 读取该标签的天线口编号 */
    public int Ant=0;
    /** 标签 DSFID 值 */
    public byte DSFID;

    /** 初始化 UID 缓冲区 */
    public Tag(){
        UID=new byte[8];
    }

    /** 将 UID 字节数组格式化为十六进制字符串 */
    public String getUID(){
        String str_uid=null;
        int len=UID.length;
        for(int i=0;i<len;i++){
            str_uid+=String.format("%02X",UID[i]);
        }
        return str_uid;
    }

    /** 根据 UID 特征识别 RFID 标签芯片型号 */
    public String getTagType(){
        String uid_type="未知";
        if(UID[7]==(byte)0xe0&&UID[6]==(byte)0x04){
            switch(UID[5]){
                case (byte)0x01:
                {
                    if((UID[4]&0x18)==0x10)
                    {
                        uid_type="ICODE SLIX";
                    }
                    else if((UID[4]&0x18)==0x00)
                    {
                        uid_type="ICODE SLI";
                    }
                    else if((UID[4]&0x18)==0x08)
                    {
                        uid_type="ICODE SLIX2";
                    }
                }
                break;
                case (byte)0x02:
                {
                    uid_type="ICODE SLIX-S";
                }
                break;
                case (byte)0x03:
                {
                    uid_type="ICODE SLIX-L";
                }
                break;
            }
        }
        return uid_type;
    }
}
