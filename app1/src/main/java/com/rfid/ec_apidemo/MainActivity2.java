package com.rfid.ec_apidemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.pc_rfid.api.EC_API;
import com.pc_rfid.api.RFdata;
import com.rfid.ec_apidemo.R;
import com.rfid.ec_apidemo.log.ALog;

import java.util.ArrayList;
import java.util.List;

/*
 * @author JAY 2021/07/03
 * @version 1.3
 */
public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    private RFdata rf = null;
    private Button btn_com = null;
    private Button btn_InfraredStart = null;
    private boolean iscmd;//是否正在执行命令
    private int InfraredScanState;//停止红外数据接收
    private EC_API ecApi = new EC_API();
    String comName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        rf = new RFdata();


        btn_com = findViewById(R.id.btn_COM);
        btn_InfraredStart = findViewById(R.id.btn_InfraredStart);
        btn_com.setOnClickListener(this);
        btn_InfraredStart.setOnClickListener(this);


        iscmd = false;
        InfraredScanState = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ecApi.EC_Close();
    }


    @Override
    public void onClick(View v) {

        //设置设备类型
        if (ecApi.EC_SetDriverType("52xx")) {
            ALog.e("设置设备类型成功\n");
        }

        if (iscmd)
            return;
        switch (v.getId()) {
            case R.id.btn_COM: {
                iscmd = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ALog.e("枚举串口...\n");
                        String[] all_com = EC_API.GetAllCOM();
                        for (int i = 0; i < all_com.length; i++) {
                            ALog.e("【" + all_com[i] + "】\n");
                        }
                        //int i=5;
                        for (int i = 0; i < all_com.length; i++) {
                            ALog.e("尝试与【" + all_com[i] + "】通信...");
                            if (ecApi.EC_OpenCOM(all_com[i], 38400, "8E1")) {
                                //打开设备命令
                                if (ecApi.EC_OpenDevice(rf)) {//验证串口是否连接设备
                                    comName = all_com[i];
                                    ToastUtils.showShort("（成功）与【" + all_com[i] + "】建立连接。");
                                    ALog.e("（成功）\n与【" + all_com[i] + "】建立连接。");
                                    runCmd(ecApi);//执行命令
                                    //ecApi.EC_Close();
                                    iscmd = false;
                                    return;
                                }
                            }
                            ecApi.EC_Close();
                            ALog.e("（失败）\n");
                            if (i == all_com.length - 1)
                                ALog.e("所有串口均未连接设备,或串口被占用\n");
                        }
                        ecApi.EC_Close();
                        iscmd = false;
                    }
                }).start();
            }
            break;
            case R.id.btn_InfraredStart://开始红外检测
            {
                switch (InfraredScanState) {
                    case 0: {
                        InfraredScanState = 1;
                        btn_InfraredStart.setText("停止");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ALog.e("开始检测...\n");
                                if (ecApi.EC_OpenCOM(comName, 38400, "8E1")) {
                                    //接收数据
                                    RFdata rf = new RFdata();
                                    while (InfraredScanState == 1) {
                                        rf.RecvData[0] = 0x00;
                                        ecApi.EC_Receive(rf);
                                        if (rf.RecvData[0] == (byte) 0xAA) {
                                            ALog.e("--有人--" + "\n");
                                        } else if (rf.RecvData[0] == (byte) 0xBB) {
                                            ALog.e("");
                                        } else {
                                            //ALog.e( String.format("%02X", rf.RecvData[0]));
                                        }
                                    }
                                } else {
                                    ALog.e("打开串口失败\n");
                                }
                                InfraredScanState = 0;
                                ecApi.EC_Close();
                            }
                        }).start();
                    }
                    break;
                    case 1: {
                        InfraredScanState = 2;//开始准备退出
                        btn_InfraredStart.setText("接收红外检测");
                    }
                    break;
                }
            }
            break;
        }
    }

    /**
     * 执行命令
     */
    private void runCmd(EC_API api) {
        RFdata rf = new RFdata();
        byte[] data = null;
        byte[] UID = null;
        byte b_DSFID = 0X00;
        byte b_AFI = 0X00;
        boolean isUploadAntNum = false;//上传天线编号

        //打开设备命令
        if (api.EC_OpenDevice(rf)) {
            showSendReceiveData(rf);
            ALog.e("打开设备成功");
        } else {
            ALog.e("打开设备失败\n");
            return;
        }

        //获取设备信息命令
        if (api.EC_GetDeviceInfoVersion(rf)) {
            showSendReceiveData(rf);
            ALog.e("获取设备信息成功");
        } else {
            ALog.e("获取设备信息失败\n");
            return;
        }
        /*//不常用的命令示例
        //读配置块3命令
        if(api.EC_ReadCfgBlock(rf, (byte) 3)){
            log.showSendReceiveData(rf);
            ALog.e("读配置块3成功ALog.e(");
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
            ALog.e("读配置块3失败\n");
            return;
        }

        //写配置块3命令
        if(data!=null&&api.EC_WriteCfgBlock(rf,data, (byte) 3)){
            log.showSendReceiveData(rf);
            ALog.e("写配置块3成功ALog.e(");
            data=null;
        }else{
            ALog.e("写配置块3失败\n");
            return;
        }

        //保存配置块命令
        if(api.EC_SaveCfgBlock(rf, (byte) 0)){
            log.showSendReceiveData(rf);
            ALog.e("保存配置块成功ALog.e(");
        }else{
            ALog.e("保存配置块失败\n");
            return;
        }

        //噪音检测命令
        if(api.EC_NoiseCheck(rf,(byte)0)){
            log.showSendReceiveData(rf);
            ALog.e("噪音检测成功ALog.e(");
        }else{
            ALog.e("噪音检测失败\n");
            return;
        }

        //打开射频命令
        if(api.EC_Open_CloseRFPower(rf,(byte)1)){
            log.showSendReceiveData(rf);
            ALog.e("打开射频成功ALog.e(");
        }else{
            ALog.e("打开射频失败\n");
            return;
        }

        //关闭射频命令
        if(api.EC_Open_CloseRFPower(rf,(byte)0)){
            log.showSendReceiveData(rf);
            ALog.e("关闭射频成功ALog.e(");
        }else{
            ALog.e("关闭射频失败\n");
            return;
        }

        //蜂鸣器闪烁命令
        if(api.EC_HardwareControl(rf,(byte)1,(byte)0x80,(byte)0)){
            log.showSendReceiveData(rf);
            ALog.e("蜂鸣器闪烁成功ALog.e(");
        }else{
            ALog.e("蜂鸣器闪烁失败\n");
            return;
        }

        //继电器闪烁命令
        if(api.EC_HardwareControl(rf,(byte)1,(byte)1,(byte)0)){
            log.showSendReceiveData(rf);
            ALog.e("继电器闪烁成功ALog.e(");
        }else{
            ALog.e("继电器闪烁失败\n");
            return;
        }
        */

        //盘点命令
        List<Tag> tag_list = ScanTab(api, 0, isUploadAntNum);
        if (tag_list != null) {
            ALog.e("盘点成功 ");
            int len = tag_list.size();
            ALog.e(String.format("标签数量%d", len));
            if (len == 0) {
                ALog.e("检测完成！");
                return;
            }
            UID = tag_list.get(0).UID;//记录第一个标签的UID，读写卡命令用到
            ALog.e("UID:" + ConvertUtils.bytes2HexString(UID));
        } else {
            ALog.e("盘点失败 ");
            return;
        }

        //打开天线口命令，读写标签之前要打开标签所在的天线口
        if (api.EC_OpenAnt_One(rf, (byte) 1)) {
            showSendReceiveData(rf);
            ALog.e("打开天线口1成功");
        } else {
            ALog.e("打开天线口1失败 ");
            return;
        }

        //获取标签信息命令
        if (api.EC_GetOneTagInfo(rf, UID, (byte) 1)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("获取标签信息成功");
                //记录DSFID和AFI，
                b_DSFID = rf.RecvData[15];
                b_AFI = rf.RecvData[16];
            } else {
                ALog.e("获取标签信息失败 ");
                return;
            }
        } else {
            ALog.e("获取标签信息失败 ");
            return;
        }

        //读单个数据块命令
        if (api.EC_ReadCardOneBlock(rf, UID, (byte) 1, (byte) 0, (byte) 0)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("读数据块0成功");
                //记录数据块
                data = new byte[4];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[8 + k];
                }
            } else {
                ALog.e("读数据块0失败 ");
                return;
            }
        } else {
            ALog.e("读数据块0失败 ");
            return;
        }

        //写单个数据块命令
        if (data != null && api.EC_WriteCardOneBlock(rf, UID, (byte) 1, (byte) 0, data)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("写数据块0成功");
                data = null;
            } else {
                ALog.e("写数据块0失败 ");
                return;
            }
        } else {
            ALog.e("写数据块0失败 ");
            return;
        }

        //读多个数据块命令
        if (api.EC_ReadCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                if (api.GetLinkType() != EC_API.NET) {
                    //非UDP传输方式，还有一个结束包
                    RFdata rf_1 = new RFdata();
                    if (api.EC_Receive(rf_1)) {
                        ALog.e("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1, true));
                    } else {
                        ALog.e("读数据块0~2失败 ");
                        return;
                    }
                }
                ALog.e("读数据块0~2成功");
                //记录数据块
                data = new byte[12];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[k + 17];
                    data[k + 4] = rf.RecvData[k + 17 + 5];
                    data[k + 8] = rf.RecvData[k + 17 + 10];
                }
            } else {
                ALog.e("读数据块0~2失败 ");
                return;
            }
        } else {
            ALog.e("读数据块0~2失败 ");
            return;
        }

        //写多个数据块命令
        if (data != null && api.EC_WriteCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2, data)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("写数据块0~2成功");
            } else {
                ALog.e("写数据块0~2失败 ");
                return;
            }
        } else {
            ALog.e("写数据块0~2失败 ");
            return;
        }

        //写DSFID命令
        if (api.EC_WriteOneTagDSFID(rf, UID, (byte) 1, b_DSFID)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("写DSFID成功");
            } else {
                ALog.e("写DSFID失败 ");
                return;
            }
        } else {
            ALog.e("写DSFID失败 ");
            return;
        }

        //写AFI命令
        if (api.EC_WriteOneTagAFI(rf, UID, (byte) 1, b_AFI)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("写AFI成功");
            } else {
                ALog.e("写AFI失败 ");
                return;
            }
        } else {
            ALog.e("写AFI失败 ");
            return;
        }

        //检测EAS命令、启用EAS命令、禁用EAS命令
        boolean isEAS;//EAS状态
        if (api.EC_CheckOneTagEAS(rf, UID, (byte) 1)) {
            showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                ALog.e("检测EAS成功 ");
                if (rf.RecvData[1] == (byte) 0x0a) {//通过检测包的长度判断EAS是否禁用
                    ALog.e("EAS状态：关闭");
                    isEAS = false;
                } else {
                    ALog.e("EAS状态：开启");
                    isEAS = true;
                }
                //自动调节，通过检测的值决定先禁用还是先启用，不更改标签EAS的原始值
                for (int k = 0; k < 2; k++) {
                    if (isEAS) {
                        //禁用EAC
                        if (api.EC_BanOneTagEAS(rf, UID, (byte) 1)) {
                            showSendReceiveData(rf);
                            if (rf.RecvData[5] == 0) {
                                ALog.e("禁用EAS成功");
                                isEAS = false;
                                continue;
                            }
                        } else {
                            ALog.e("禁用EAS失败 ");
                            return;
                        }

                    } else {
                        //启用EAC
                        if (api.EC_EnableOneTagEAS(rf, UID, (byte) 1)) {
                            showSendReceiveData(rf);
                            if (rf.RecvData[5] == 0) {
                                ALog.e("启用EAS成功");
                                isEAS = true;
                                continue;
                            }
                        } else {
                            ALog.e("启用EAS失败 ");
                            return;
                        }

                    }
                }
                ALog.e("检测完成！");
            }
        } else {
            ALog.e("检测EAS失败");
            return;
        }
    }

    /**
     * 盘点标签
     *
     * @param api            EC_API实例对象
     * @param antNum         要打开的天线口号(0~30，u单天线盘点)，0表示不打开（多天线盘点）
     * @param isUploadAntNum 是否开启了上传天线编号（只有网络盘点用到这个参数）
     * @return 标签列表
     */
    public List<Tag> ScanTab(EC_API api, int antNum, boolean isUploadAntNum) {
        List<Tag> tab_list = new ArrayList<Tag>();
        RFdata rf_1 = new RFdata();
        byte scanType = 0;
        if (antNum >= 1 && antNum <= 30) {
            //单标签盘点,打开指定天线口
            if (api.EC_OpenAnt_One(rf_1, (byte) antNum)) {
                showSendReceiveData(rf_1);
                ALog.e(String.format("打开天线口%d成功！ ", antNum));
                scanType = 0x04;
            } else {
                return null;
            }

        }
        switch (api.GetLinkType()) {
            case EC_API.COM: {
                if (api.EC_InventoryTag(rf_1, scanType)) {
                    ALog.e("发送<<" + HexToStr(rf_1.SendData, (rf_1.SendData[1] & 0xff) + 1, true));
                    while ((rf_1.RecvData[1] & 0xff) > 7) {
                        ALog.e("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1, true));
                        //从数据包中获取DSFID
                        Tag tag_info = new Tag();
                        tag_info.DSFID = rf_1.RecvData[6];
                        //获取天线口，设置了上传天线编号才能获取到天线口
                        if (antNum == 0) {
                            if (rf_1.RecvData[1] == 0x11) {//通过包的长度判断是否包含天线口数据，设置了上传天线编号数据包多一个字节
                                tag_info.Ant = rf_1.RecvData[15] & 0xff;
                            } else {
                                tag_info.Ant = 1;
                            }
                        } else {
                            tag_info.Ant = antNum;
                        }
                        //获取uid
                        for (int i = 0; i < 8; i++) {
                            tag_info.UID[i] = rf_1.RecvData[i + 7];
                        }
                        tab_list.add(tag_info);
                        //继续接收,直到接收到结束包为止
                        if (!api.EC_Receive(rf_1))
                            break;
                    }
                    ALog.e("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1, true));
                }
            }
            break;
        }
        return tab_list;
    }


    /**
     * 输出发送和接收的数据
     *
     * @param rfdata 数据
     */
    public void showSendReceiveData(RFdata rfdata) {
        String data = HexToStr(rfdata.SendData, (rfdata.SendData[1] & 0xff) + 1, true);
        ALog.e("发送>>" + data);
        data = HexToStr(rfdata.RecvData, (rfdata.RecvData[1] & 0xff) + 1, true);
        ALog.e("接收<<" + data);
    }


    /**
     * 字节数组转字符串
     *
     * @param data  字节数组
     * @param len   要转换的长度
     * @param ishex 是否转换成16进制形式的字符串
     * @return
     */
    private String HexToStr(byte[] data, int len, boolean ishex) {
        String strData = "";
        for (int i = 0; i < len; i++) {
            if (ishex) {
                strData = strData + String.format("%02X ", data[i]);//十六进制
            } else {
                strData = strData + String.format("%d ", data[i] & 0xff);//十进制
            }
        }
        return strData;
    }
}
