package com.rfid.ec_apidemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.pc_rfid.api.OnUsbListener;
import com.pc_rfid.api.EC_API;
import com.pc_rfid.api.EC_USB;
import com.pc_rfid.api.RFdata;
import com.pc_rfid.api.TagInfo;
import com.rfid.ec_apidemo.R;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/*
 * @author JAY 2021/07/03
 * @version 1.3
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final int LOG_CLEAR=0;
    private final int LOG_SET=1;
    private final int LOG_ADD=2;
    private RFdata rf=null;
    private EditText et_log=null;
    private Button btn_com=null;
    private Button btn_usb=null;
    private Button btn_net=null;
    private Button btn_usbtocom=null;
    private Button btn_InfraredStart=null;
    private Button btn_BatchReadMultiBlocks=null;
    private Spinner sn_comName=null;
    private logHandler log=null;
    private boolean iscmd;//是否正在执行命令
    private int InfraredScanState;//停止红外数据接收
    private EC_API ecApi=new EC_API();
   // private UsbDevice[] list_dev=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rf=new RFdata();
        et_log=findViewById(R.id.edit_log);
        et_log.setFocusable(false);//禁止键盘输入


        btn_com=findViewById(R.id.btn_COM);
        btn_usb=findViewById(R.id.btn_USB);
        btn_net=findViewById(R.id.btn_NET);
        btn_usbtocom=findViewById(R.id.btn_USBTOCOM);
        btn_InfraredStart=findViewById(R.id.btn_InfraredStart);
        btn_BatchReadMultiBlocks=findViewById(R.id.btn_BatchReadMultiBlocks);
        sn_comName=findViewById(R.id.tab_rftype_comName);
        btn_com.setOnClickListener(this);
        btn_usb.setOnClickListener(this);
        btn_net.setOnClickListener(this);
        btn_usbtocom.setOnClickListener(this);
        btn_InfraredStart.setOnClickListener(this);
        btn_BatchReadMultiBlocks.setOnClickListener(this);


        log=new logHandler();
        iscmd=false;
        InfraredScanState=0;
        //枚举串口
        addDropDownList(sn_comName ,EC_API.GetAllCOM(),0);

        EC_API.USB_init(this);//程序开始时执行，在USB监听及读写之前要先执行，否则操作无效
        //USB插拔监听
        EC_API.setOnUsbListener(new OnUsbListener() {
            @Override
            public void onStateChanged(UsbDevice usbDevice, boolean b) {
                Log.d(TAG, String.format("VID=%04X,PID=%04X",usbDevice.getVendorId(),usbDevice.getProductId()));
                if(b){
                    //播放系统通知提示音
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                    Toast.makeText(MainActivity.this,"USB：检测到设备接入",Toast.LENGTH_SHORT).show();
                    //这里可以直接利用EC_API.EC_OpenUSB(usbDevice)打开设备
                }else{
                    Toast.makeText(MainActivity.this,"USB：设备断开连接",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //初始USB转串口
        EC_API.USBTOCOM_init((UsbManager)getSystemService(Context.USB_SERVICE), this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ecApi.EC_Close();
        EC_API.USB_exit();//程序结束时执行，释放USB资源
    }

    // 键盘(USB主动读卡)监听
    public boolean dispatchKeyEvent(KeyEvent event){
        Log.d(TAG, event.toString());
        if(event.getDeviceId()!=-1){//区分外接键盘，-1是系统软键盘
            if(event.getAction()==KeyEvent.ACTION_DOWN) {
                char pressedKey = (char) event.getUnicodeChar();//转字符
                log.Text(LOG_ADD,String.format("%c",pressedKey));
            }
            return true;//拦截，不向下传输
        }
        return super.dispatchKeyEvent(event);
    }

    private void addDropDownList(Spinner sn,String []list,int index)
    {
        if(list.length>0) {
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sn.setAdapter(typeAdapter);
            sn.setSelection(index);
        }
    }

    @Override
    public void onClick(View v) {

        //设置设备类型
        if(ecApi.EC_SetDriverType("52xx")) {
            log.Text(LOG_ADD,"设置设备类型成功\n");
        }

        if(iscmd)
            return;
        switch(v.getId()){
            case R.id.btn_COM:
            {
                iscmd=true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.Text(LOG_CLEAR);
                        log.Text(LOG_ADD,"枚举串口...\n");
                        String []all_com=EC_API.GetAllCOM();
                        for(int i=0;i<all_com.length;i++) {
                            log.Text(LOG_ADD,"【"+all_com[i]+"】\n");
                        }
                        log.Text(LOG_ADD,"\n");
                        //int i=5;
                        for(int i=0;i<all_com.length;i++){
                            log.Text(LOG_ADD,"尝试与【"+all_com[i]+"】通信...");
                            if(ecApi.EC_OpenCOM(all_com[i],38400,"8E1")){
                                //打开设备命令
                                if(ecApi.EC_OpenDevice(rf)){//验证串口是否连接设备
                                    log.Text(LOG_ADD,"（成功）\n与【"+all_com[i]+"】建立连接。\n\n");
                                    runCmd(ecApi);//执行命令
                                    //ecApi.EC_Close();
                                    iscmd = false;
                                    return;
                                }
                            }
                            ecApi.EC_Close();
                            log.Text(LOG_ADD,"（失败）\n");
                            if(i==all_com.length-1)
                                log.Text(LOG_ADD,"所有串口均未连接设备,或串口被占用\n");
                        }
                        ecApi.EC_Close();
                        iscmd=false;
                    }
                }).start();
            }
            break;
            case R.id.btn_USB:
            {
                iscmd=true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.Text(LOG_CLEAR);
                        /**
                         * 打开USB方案一.
                         * 默认使用VID:0xFFFE,PID:0x0091，打开枚举到的第一个设备
                         * 请确保已经执行EC_API.USB_init(this);
                         * 最简版
                         */
                        /*if(EC_API.EC_OpenUSB()){
                            log.Text(LOG_ADD,"打开USB成功\n\n");
                            runCmd();
                        }
                        EC_API.EC_Close();*/

                        /**
                         * 打开USB方案二
                         * 指定VID和PID，打开枚举到的第一个设备
                         * 请确保已经执行EC_API.USB_init(this);
                         * 简化版
                         */
                       /* if(EC_API.EC_OpenUSB(0xFFFE,0x0091)){
                            log.Text(LOG_ADD,"成功打开USB\n\n");
                            runCmd();
                        }
                        EC_API.EC_Close();*/

                        /**
                         * 打开USB方案三
                         * 指定VID和PID，可选择打开的设备
                         * 请确保已经执行EC_API.USB_init(this);
                         */
                        log.Text(LOG_ADD,"枚举USB...\n");
                        UsbDevice[] list_dev=EC_API.ScanUSB(0xFFFE,0x0091);
                        if(list_dev!=null&&list_dev.length>0){
                            for(int i=0;i<list_dev.length;i++) {
                                log.Text(LOG_ADD,"【"+list_dev[i].getDeviceName()+"】\n");
                            }
                            if(ecApi.EC_OpenUSB(list_dev[0])){
                                //打开第一个USB
                                log.Text(LOG_ADD,"成功打开【"+list_dev[0].getDeviceName()+"】\n\n");
                                runCmd(ecApi);
                            }
                            //ecApi.EC_Close();//关闭USB
                        }else{
                            log.Text(LOG_ADD,"没有发现符合的USB设备\n");
                        }

                        iscmd=false;
                    }
                }).start();
            }
            break;
            case R.id.btn_NET:
            {
                iscmd=true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.Text(LOG_CLEAR);
                        //网络搜索设备
                        List<IPInfo> ip_list=ScanIP(ecApi);
                        if(ip_list.size()>0){
                            log.Text(LOG_ADD, "设备数量"+ip_list.size()+"\n\n");

                            //网络修改设备IP、子网掩码、网关
                            IPInfo ip_1=ip_list.get(0);
                            if(ecApi.EC_OpenNET(ip_1.getIP(),6688)) {//配置网络
                                log.Text(LOG_ADD, "配置网络【"+ip_1.getIP()+":6688】成功\n");
                                //这里演示用法，IP信息不做改动，把获取的信息设置回去
                                if(ecApi.EC_NetWriteDeviceInfo(rf,ip_1.ipaddr,ip_1.subnetMask,ip_1.gateway)){//EC_NetWriteDeviceInfo为修改网络配置示例，可以不用
                                    log.Text(LOG_ADD, "IP修改成功\n\n");
                                    //设备IP信息本质上没有改动，所以不需要重新配置网络
                                    runCmd(ecApi);//读卡器示例
                                    //run59XXCmd(ecApi);//安全门示例
                                }else{
                                    log.Text(LOG_ADD, "IP修改失败\n\n");
                                }
                            }
                        } else{
                            log.Text(LOG_ADD,"未连接设备\n");
                        }
                        ecApi.EC_Close();
                        iscmd=false;
                    }
                }).start();

            }
            break;
            case R.id.btn_USBTOCOM://usb转串口
            {
                iscmd=true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.Text(LOG_CLEAR);
                        boolean isOpen = false;
                        if (ecApi.EC_OpenUSBTOCOM(38400, "8E1", "CH34x")) {
                            log.Text(LOG_ADD,"尝试与CH34x转接线通信...");
                            isOpen = true;
                        }
                        else if (ecApi.EC_OpenUSBTOCOM(38400, "8E1", "pl2303g")){
                            log.Text(LOG_ADD,"尝试与pl2303g转接线通信...");
                            isOpen = true;
                        }
                        else if (ecApi.EC_OpenUSBTOCOM(38400, "8E1", "d2xx")){
                            log.Text(LOG_ADD,"尝试与ftdi转接线通信...");
                            isOpen = true;
                        }
                        else if (ecApi.EC_OpenUSBTOCOM(38400, "8E1", "pl2303")){
                            log.Text(LOG_ADD,"尝试与pl2303转接线通信...");
                            isOpen = true;
                        }
                        if(isOpen) {
                            //打开设备命令
                            if (ecApi.EC_OpenDevice(rf)) {//验证串口是否连接设备
                                log.Text(LOG_ADD, "（成功）\n与USB转串口设备建立连接。\n\n");
                                runCmd(ecApi);//执行命令
                                iscmd = false;
                                return;
                            }
                        }
                        else {
                            log.Text(LOG_ADD,"未找到USB转串口设备\n");
                        }/**/
                        ecApi.EC_Close();
                        iscmd = false;
                    }
                }).start();
            }
            break;
            case R.id.btn_InfraredStart://开始红外检测
            {
                switch(InfraredScanState){
                    case 0:
                    {
                        InfraredScanState = 1;
                        btn_InfraredStart.setText("停止");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                log.Text(LOG_CLEAR);
                                log.Text(LOG_ADD,"开始检测...\n");
                                String comname = sn_comName.getSelectedItem().toString();
                                if(ecApi.EC_OpenCOM(comname,38400,"8E1")){
                                    //接收数据
                                    RFdata rf=new RFdata();
                                    while (InfraredScanState==1){
                                        rf.RecvData[0] = 0x00;
                                        ecApi.EC_Receive(rf);
                                        if (rf.RecvData[0] == (byte)0xAA) {
                                            log.Text(LOG_SET, "--有人--" + "\n");
                                        }
                                        else if (rf.RecvData[0] == (byte)0xBB){
                                            log.Text(LOG_CLEAR);
                                        }
                                        else {
                                            //log.Text(LOG_ADD, String.format("%02X", rf.RecvData[0]));
                                        }
                                    }
                                }
                                else {
                                    log.Text(LOG_ADD,"打开串口失败\n");
                                }
                                InfraredScanState = 0;
                                ecApi.EC_Close();
                            }
                        }).start();
                    }
                    break;
                    case 1:
                    {
                        InfraredScanState=2;//开始准备退出
                        btn_InfraredStart.setText("接收红外检测");
                    }
                    break;
                }
            }
            break;
            case R.id.btn_BatchReadMultiBlocks://批量读多个数据块
            {
                log.Text(LOG_CLEAR);
                /**
                 * 批量盘点并读多个数据块以及AFI、EAS
                 * antennaCollection  天线集合
                 * readerType  读写器类型：微功率0x00， 中、大功率0x01
                 * readDataType  只盘点标签不读数据:-1, blocksdata:0, afi:1, eas:2, blocksdata & afi:3, blocksdata & eas:4, afi & eas:5, blocksdata & afi & eas:6
                 * readSecSta  读取安全位 0 or 1
                 * blkAddr  开始块地址
                 * numOfBlksToRead  块数量 从0开始计算
                 * tagDataList  读取到的标签数据列表
                 * return 读取数据失败标签数量
                 */
                byte[] antCollection = new byte[]{0x01};//天线编号集合，双天线为：{0x01， 0x02}
                Vector<TagInfo> tagDataList=new Vector<TagInfo>();
                ecApi.ISO15693_BatchReadMultiBlocks(antCollection, (byte)1,0, (byte)0, (byte)0, (byte)2, tagDataList);

                if(tagDataList.size() > 0) {
                    int num = 0;
                    for(TagInfo tagInfo: tagDataList) {
                        num++;
                        log.Text(LOG_ADD, "No. " + num
                                + "\nantid: " + String.format("%02X", tagInfo.antid)
                                + "\ntagid: " + tagInfo.getUID()
                                + "\ndsfid: " + String.format("%02X", tagInfo.dsfid)
                                + "\nafi: " + String.format("%02X", tagInfo.afi)
                                + "\neas: " + String.format("%02X", tagInfo.eas)
                                + "\ndata: " + tagInfo.getBlocksData() + "\n\n");
                    }
                    tagDataList.clear();
                }
            }
            break;
        }
    }

    /**
     * 执行命令
     */
    private void runCmd(EC_API api){
        RFdata rf=new RFdata();
        byte []data=null;
        byte []UID=null;
        byte b_DSFID=0X00;
        byte b_AFI=0X00;
        boolean isUploadAntNum=false;//上传天线编号

        //打开设备命令
        if(api.EC_OpenDevice(rf)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"打开设备成功\n\n");
        }else{
            log.Text(LOG_ADD,"打开设备失败\n");
            return;
        }

        //获取设备信息命令
        if(api.EC_GetDeviceInfoVersion(rf)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"获取设备信息成功\n\n");
        }else{
            log.Text(LOG_ADD,"获取设备信息失败\n");
            return;
        }
/*//不常用的命令示例
        //读配置块3命令
        if(api.EC_ReadCfgBlock(rf, (byte) 3)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"读配置块3成功\n\n");
            //记录配置块数据，保证写配置时不更改配置
            data=new byte[8];
            for(int k=0;k<8;k++)
            {
                data[k]=rf.RecvData[k+6];
            }
            //记录上传天线编号标识
            if((data[0]>>>4&0x01)==1)
            {
                isUploadAntNum=true;//网络盘点解析天线口号用到
            }
        }else{
            log.Text(LOG_ADD,"读配置块3失败\n");
            return;
        }

        //写配置块3命令
        if(data!=null&&api.EC_WriteCfgBlock(rf,data, (byte) 3)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"写配置块3成功\n\n");
            data=null;
        }else{
            log.Text(LOG_ADD,"写配置块3失败\n");
            return;
        }

        //保存配置块命令
        if(api.EC_SaveCfgBlock(rf, (byte) 0)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"保存配置块成功\n\n");
        }else{
            log.Text(LOG_ADD,"保存配置块失败\n");
            return;
        }

        //噪音检测命令
        if(api.EC_NoiseCheck(rf,(byte)0)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"噪音检测成功\n\n");
        }else{
            log.Text(LOG_ADD,"噪音检测失败\n");
            return;
        }

        //打开射频命令
        if(api.EC_Open_CloseRFPower(rf,(byte)1)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"打开射频成功\n\n");
        }else{
            log.Text(LOG_ADD,"打开射频失败\n");
            return;
        }

        //关闭射频命令
        if(api.EC_Open_CloseRFPower(rf,(byte)0)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"关闭射频成功\n\n");
        }else{
            log.Text(LOG_ADD,"关闭射频失败\n");
            return;
        }

        //蜂鸣器闪烁命令
        if(api.EC_HardwareControl(rf,(byte)1,(byte)0x80,(byte)0)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"蜂鸣器闪烁成功\n\n");
        }else{
            log.Text(LOG_ADD,"蜂鸣器闪烁失败\n");
            return;
        }

        //继电器闪烁命令
        if(api.EC_HardwareControl(rf,(byte)1,(byte)1,(byte)0)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"继电器闪烁成功\n\n");
        }else{
            log.Text(LOG_ADD,"继电器闪烁失败\n");
            return;
        }
*/

        //盘点命令
        List<Tag> tag_list=ScanTab(api,0,isUploadAntNum);
        if(tag_list!=null){
            log.Text(LOG_ADD,"盘点成功\n");
            int len=tag_list.size();
            log.Text(LOG_ADD,String.format("标签数量%d\n\n",len));
            if(len==0){
                log.Text(LOG_ADD,"检测完成！");
                return;
            }
            UID=tag_list.get(0).UID;//记录第一个标签的UID，读写卡命令用到
        }else{
            log.Text(LOG_ADD,"盘点失败\n");
            return;
        }

        //打开天线口命令，读写标签之前要打开标签所在的天线口
        if(api.EC_OpenAnt_One(rf,(byte)1)){
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD,"打开天线口1成功\n\n");
        }else{
            log.Text(LOG_ADD,"打开天线口1失败\n");
            return;
        }

        //获取标签信息命令
        if(api.EC_GetOneTagInfo(rf,UID,(byte)1)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0) {
                log.Text(LOG_ADD,"获取标签信息成功\n\n");
                //记录DSFID和AFI，
                b_DSFID = rf.RecvData[15];
                b_AFI = rf.RecvData[16];
            }else{
                log.Text(LOG_ADD,"获取标签信息失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"获取标签信息失败\n");
            return;
        }

        //读单个数据块命令
        if(api.EC_ReadCardOneBlock(rf,UID,(byte)1,(byte)0,(byte)0)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0) {
                log.Text(LOG_ADD,"读数据块0成功\n\n");
                //记录数据块
                data = new byte[4];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[8 + k];
                }
            }else{
                log.Text(LOG_ADD,"读数据块0失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"读数据块0失败\n");
            return;
        }

        //写单个数据块命令
        if(data!=null&&api.EC_WriteCardOneBlock(rf,UID,(byte)1,(byte)0,data)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0) {
                log.Text(LOG_ADD,"写数据块0成功\n\n");
                data = null;
            }else{
                log.Text(LOG_ADD,"写数据块0失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"写数据块0失败\n");
            return;
        }

        //读多个数据块命令
        if(api.EC_ReadCardMultBlock(rf,UID,(byte)1,(byte)0,(byte)2)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0) {
                if (api.GetLinkType() != EC_API.NET) {
                    //非UDP传输方式，还有一个结束包
                    RFdata rf_1 = new RFdata();
                    if (api.EC_Receive(rf_1)) {
                        log.showReceiveData(rf_1.RecvData);

                    } else {
                        log.Text(LOG_ADD,"读数据块0~2失败\n");
                        return;
                    }
                }
                log.Text(LOG_ADD,"读数据块0~2成功\n\n");
                //记录数据块
                data = new byte[12];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[k + 17];
                    data[k + 4] = rf.RecvData[k + 17 + 5];
                    data[k + 8] = rf.RecvData[k + 17 + 10];
                }
            }else{
                log.Text(LOG_ADD,"读数据块0~2失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"读数据块0~2失败\n");
            return;
        }

        //写多个数据块命令
        if(data!=null&&api.EC_WriteCardMultBlock(rf,UID,(byte)1,(byte)0,(byte)2,data)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0) {
                log.Text(LOG_ADD,"写数据块0~2成功\n\n");
            }else{
                log.Text(LOG_ADD,"写数据块0~2失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"写数据块0~2失败\n");
            return;
        }

        //写DSFID命令
        if(api.EC_WriteOneTagDSFID(rf,UID,(byte)1,b_DSFID)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0){
                log.Text(LOG_ADD,"写DSFID成功\n\n");
            }else{
                log.Text(LOG_ADD,"写DSFID失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"写DSFID失败\n");
            return;
        }

        //写AFI命令
        if(api.EC_WriteOneTagAFI(rf,UID,(byte)1,b_AFI)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0){
                log.Text(LOG_ADD,"写AFI成功\n\n");
            }else{
                log.Text(LOG_ADD,"写AFI失败\n");
                return;
            }
        }else{
            log.Text(LOG_ADD,"写AFI失败\n");
            return;
        }

        //检测EAS命令、启用EAS命令、禁用EAS命令
        boolean isEAS;//EAS状态
        if(api.EC_CheckOneTagEAS(rf,UID,(byte)1)){
            log.showSendReceiveData(rf);
            if(rf.RecvData[5]==0){
                log.Text(LOG_ADD,"检测EAS成功\n");
                if(rf.RecvData[1]==(byte)0x0a){//通过检测包的长度判断EAS是否禁用
                    log.Text(LOG_ADD,"EAS状态：关闭\n\n");
                    isEAS=false;
                }else{
                    log.Text(LOG_ADD,"EAS状态：开启\n\n");
                    isEAS=true;
                }
                //自动调节，通过检测的值决定先禁用还是先启用，不更改标签EAS的原始值
                for(int k=0;k<2;k++){
                    if(isEAS){
                        //禁用EAC
                        if(api.EC_BanOneTagEAS(rf,UID,(byte)1)){
                            log.showSendReceiveData(rf);
                            if(rf.RecvData[5]==0){
                                log.Text(LOG_ADD,"禁用EAS成功\n\n");
                                isEAS=false;
                                continue;
                            }
                        }else{
                            log.Text(LOG_ADD,"禁用EAS失败\n");
                            return;
                        }

                    }else{
                        //启用EAC
                        if(api.EC_EnableOneTagEAS(rf,UID,(byte)1)){
                            log.showSendReceiveData(rf);
                            if(rf.RecvData[5]==0){
                                log.Text(LOG_ADD,"启用EAS成功\n\n");
                                isEAS=true;
                                continue;
                            }
                        }else{
                            log.Text(LOG_ADD,"启用EAS失败\n");
                            return;
                        }

                    }
                }
                log.Text(LOG_ADD,"检测完成！\n\n");
            }
        }else{
            log.Text(LOG_ADD,"检测EAS失败\n");
            return;
        }
    }

    private void run59XXCmd(EC_API api) {
        RFdata rf = new RFdata();
        byte[] data = null;
        byte[] UID = null;
        byte b_DSFID = 0X00;
        byte b_AFI = 0X00;

        //打开设备命令
        if (api.EC_OpenDevice(rf)) {
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD, "打开设备成功\n\n");
        } else {
            log.Text(LOG_ADD, "打开设备失败\n");
            log.showSendReceiveData(rf);
            return;
        }

        //获取设备信息命令
        if (api.EC_GetDeviceInfoVersion(rf)) {
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD, "获取设备信息成功\n\n");
        } else {
            log.Text(LOG_ADD, "获取设备信息失败\n");
            return;
        }

        //安全门获取和删除记录
        //NumFlag - 序号
        //Flag - 0x00只获取记录;0x01删除上一次被取走的记录，再获取新的记录;0x02开始读卡的第一个数据包。
        if (api.EC_DoorTakeRecords(rf, (short) 0,(byte)0)) {
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD, "安全门获取和删除记录\n\n");
        } else {
            log.Text(LOG_ADD, "安全门获取和删除记录\n");
            return;
        }

    }

    /**
     * 盘点标签
     * @param api EC_API实例对象
     * @param antNum 要打开的天线口号(0~30，u单天线盘点)，0表示不打开（多天线盘点）
     * @param isUploadAntNum 是否开启了上传天线编号（只有网络盘点用到这个参数）
     * @return 标签列表
     */
    public List<Tag> ScanTab(EC_API api,int antNum,boolean isUploadAntNum){
        List<Tag> tab_list=new ArrayList<Tag>();
        RFdata rf_1=new RFdata();
        byte scanType=0;
        if(antNum>=1&&antNum<=30)
        {
            //单标签盘点,打开指定天线口
            if(api.EC_OpenAnt_One(rf_1,(byte)antNum)){
                log.showSendReceiveData(rf_1);
                log.Text(LOG_ADD,String.format("打开天线口%d成功！\n",antNum));
                scanType=0x04;
            }else{
                return null;
            }

        }
        switch(api.GetLinkType())
        {
            case EC_API.NET:
            {
                Vector<byte[]> list_pack =new Vector<byte[]>();
                if(api.EC_NETinventoryTag(list_pack,rf_1,scanType)){
                    log.showSendData(rf_1.SendData);//输出发送数据
                    int len=list_pack.size();
                    for(int i=0;i<len;i++){
                        byte[] pack=list_pack.get(i);
                        int packLen=pack[1]&0xff;
                        if(packLen!=9){
                            //数据包含天线编号
                            int pos=8;//标签信息开始的位置
                            while(pos+9<packLen){//如果下标pos往后的pack数据长度小于一个标签信息的长度，说明标签读取完了
                                Tag tag_info=new Tag();
                                //获取DSFID
                                tag_info.DSFID=pack[pos];
                                //获取uid
                                for(int k=0;k<8;k++){
                                    tag_info.UID[k]=pack[k+pos+1];
                                }
                                //获取天线口，设置了上传天线编号才能获取到天线口
                                if (isUploadAntNum) {
                                    if(antNum==0) {
                                        tag_info.Ant = pack[pos + 9] & 0xff;
                                    }else {
                                        tag_info.Ant = antNum;
                                    }
                                    pos = pos + 10;//下一个标签的开始位置（因为信息包含了一个字节的天线口）
                                } else {
                                    if(antNum==0) {
                                        tag_info.Ant = 1;
                                    }else {
                                        tag_info.Ant = antNum;
                                    }
                                    pos = pos + 9;//下一个标签的开始位置
                                }
                                tab_list.add(tag_info);
                            }
                        }
                        log.showReceiveData(pack);//输出接收数据
                    }
                }else{
                    return null;
                }
            }
            break;
            case EC_API.USB://USB和串口盘点方式一样
            case EC_API.COM:
            {
                if(api.EC_InventoryTag(rf_1,scanType)){
                    log.showSendData(rf_1.SendData);
                    //
                    while((rf_1.RecvData[1]&0xff)>7){
                        log.showReceiveData(rf_1.RecvData);
                        //从数据包中获取DSFID
                        Tag tag_info=new Tag();
                        tag_info.DSFID=rf_1.RecvData[6];
                        //获取天线口，设置了上传天线编号才能获取到天线口
                        if(antNum==0) {
                            if (rf_1.RecvData[1] == 0x11) {//通过包的长度判断是否包含天线口数据，设置了上传天线编号数据包多一个字节
                                tag_info.Ant = rf_1.RecvData[15] & 0xff;
                            } else {
                                tag_info.Ant = 1;
                            }
                        }else{
                            tag_info.Ant = antNum;
                        }
                        //获取uid
                        for(int i=0;i<8;i++){
                            tag_info.UID[i]=rf_1.RecvData[i+7];
                        }
                        tab_list.add(tag_info);
                        //继续接收,直到接收到结束包为止
                        if(!api.EC_Receive(rf_1))
                            break;
                    }
                    log.showReceiveData(rf_1.RecvData);
                }
            }
            break;
        }
        return tab_list;
    }

    /**
     * 网络广播搜索设备（同网段）
     * @param api EC_API实例对象
     * @return 设备IP列表
     */
    public List<IPInfo> ScanIP(EC_API api){
        List<IPInfo> IP_list=new ArrayList<IPInfo>();
        log.Text(LOG_ADD,"正在搜索设备...\n");
        if(api.EC_OpenNET("255.255.255.255",6688)) {//配置网络，广播搜索
            //搜索ip
            if (api.EC_NetScanDeviceInfo(rf)) {
                IPInfo ip_1=new IPInfo(rf.RecvData);//解析
                IP_list.add(ip_1);
                log.showSendReceiveData(rf);
                log.Text(LOG_ADD, ip_1.get_info() + "\n");
                //成功接收了一个包，接着继续尝试接收，直到超时（2秒）
                while (api.EC_Receive(rf)) {
                    IPInfo ip_2=new IPInfo(rf.RecvData);//解析
                    IP_list.add(ip_2);
                    log.showReceiveData(rf.RecvData);
                    log.Text(LOG_ADD, ip_2.get_info() + "\n");
                }
            }
        }
        return IP_list;
    }

    /**
     * 子线程更新日志
     * @author YC 2020/10/13
     * @version 1.10
     */
    public class logHandler extends Handler{

        @Override
        public void handleMessage( Message msg) {
            switch(msg.what){
                case LOG_CLEAR://清空日志文本
                {
                    et_log.setText("");
                }
                break;
                case LOG_SET://设置日志文本
                {
                    String str= (String) msg.obj;
                    if(str!=null) {
                        et_log.setText(str);
                    }
                }
                break;
                case LOG_ADD://添加日志文本
                {
                    String str= (String) msg.obj;
                    if(str!=null) {
                        et_log.append(str);
                    }
                }
            }
        }

        /**
         * 日志文本更新
         * @param what 动作
         * @param obj 字符串
         */
        public void Text(int what,Object obj){
            Message msg=Message.obtain();
            msg.what=what;
            msg.obj=obj;
            sendMessage(msg);
        }
        public void Text(int what){
            Text(what,null);
        }

        /**
         * 输出发送的数据
         * @param sendData 数据
         */
        public void showSendData(byte []sendData)
        {
            String data=HexToStr(sendData,(sendData[1]&0xff)+1,true);
            this.Text(LOG_ADD,"发送>>"+data+"\n");
        }

        /**
         * 输出接收的数据
         * @param receiveData 数据
         */
        public void showReceiveData(byte[] receiveData)    {
            String data=HexToStr(receiveData,(receiveData[1]&0xff)+1,true);
            this.Text(LOG_ADD,"接收<<"+data+"\n");
        }

        /**
         * 输出发送和接收的数据
         * @param rfdata 数据
         */
        public void showSendReceiveData(RFdata rfdata)
        {
            showSendData(rfdata.SendData);
            showReceiveData(rfdata.RecvData);
        }

        /**
         * 字节数组转字符串
         * @param data 字节数组
         * @param len 要转换的长度
         * @param ishex 是否转换成16进制形式的字符串
         * @return
         */
        private String HexToStr(byte []data,int len,boolean ishex)
        {
            String strData="";
            for(int i = 0; i < len; i++) {
                if(ishex) {
                    strData = strData + String.format("%02X ", data[i]);//十六进制
                }else{
                    strData = strData + String.format("%d ", data[i]&0xff);//十进制
                }
            }
            return strData;
        }
    }
}
