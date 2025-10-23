package com.rfid.ec_apidemo;

/**
 * 标签信息
 */
public class Tag {
    public byte[] UID=null;
    public int Ant=0;//盘点自哪个天线口
    public byte DSFID;//DSFID值

    public Tag(){
        UID=new byte[8];
    }
    public String getUID(){
        String str_uid=null;
        int len=UID.length;
        for(int i=0;i<len;i++){
            str_uid+=String.format("%02X",UID[i]);
        }
        return str_uid;
    }

    /*
     * 获取标签类型
     * */
    public String getTagType(){
        String uid_type="未知";//Unknown
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
                        uid_type="ICODE SLI";//不支持密码
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
                    uid_type="ICODE SLIX-L";//不支持密码
                }
                break;
            }
        }
        return uid_type;
    }
}
