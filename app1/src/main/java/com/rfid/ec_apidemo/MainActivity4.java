package com.rfid.ec_apidemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.pc_rfid.api.EC_API;
import com.pc_rfid.api.RFdata;
import com.rfid.ec_apidemo.R;
import com.rfid.ec_apidemo.log.ALog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity implements View.OnClickListener {
    private final int LOG_CLEAR = 0;
    private final int LOG_SET = 1;
    private final int LOG_ADD = 2;
    private RFdata rf = null;
    private EditText et_log = null;

    private logHandler log = null;
    private boolean iscmd;//是否正在执行命令
    private EC_API ecApi = new EC_API();
    String strCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        rf = new RFdata();
        et_log = findViewById(R.id.edit_log);
        et_log.setFocusable(false);//禁止键盘输入


        log = new logHandler();
        iscmd = false;
        initReader();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unInitReader();
        log.removeCallbacksAndMessages(null);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_READ:
                if (iscmd)
                    return;
                String uid = read(ecApi);
                ALog.e("uid:" + uid);
                break;
            default:
                break;
        }
    }

    public void initReader() {
        //设置设备类型
        if (ecApi.EC_SetDriverType("52xx")) {
            log.Text(LOG_ADD, "设置设备类型成功\n");
        }
        iscmd = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.Text(LOG_CLEAR);
                log.Text(LOG_ADD, "枚举串口...\n");
                String[] all_com = EC_API.GetAllCOM();
                for (String value : all_com) {
                    log.Text(LOG_ADD, "【" + value + "】\n");
                }
                log.Text(LOG_ADD, "\n");
                for (String s : all_com) {
                    log.Text(LOG_ADD, "尝试与【" + s + "】通信...");
                    if (ecApi.EC_OpenCOM(s, 38400, "8E1")) {
                        //打开设备命令
                        if (ecApi.EC_OpenDevice(rf)) {//验证串口是否连接设备
                            log.Text(LOG_ADD, "（成功）\n与【" + s + "】建立连接。\n\n");
                            strCom = s;
                            runCmd(ecApi);//执行命令
                            iscmd = false;
                            log.removeMessages(110);
                            log.sendEmptyMessageDelayed(110, 1000L);
                            return;
                        }
                    }
                }
                ecApi.EC_Close();
                iscmd = false;
            }
        }).start();
    }

    public void unInitReader() {
        ecApi.EC_Close();
        iscmd = false;
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
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD, "打开设备成功\n\n");
        } else {
            log.Text(LOG_ADD, "打开设备失败\n");
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

    }


    /**
     * 盘点标签
     *
     * @param api    EC_API实例对象
     * @param antNum 要打开的天线口号(0~30，u单天线盘点)，0表示不打开（多天线盘点）
     * @return 标签列表
     */
    public List<Tag> ScanTab(EC_API api, int antNum) {
        List<Tag> tab_list = new ArrayList<Tag>();
        RFdata rf_1 = new RFdata();
        byte scanType = 0;
        if (antNum >= 1 && antNum <= 30) {
            //单标签盘点,打开指定天线口
            if (api.EC_OpenAnt_One(rf_1, (byte) antNum)) {
                log.showSendReceiveData(rf_1);
                log.Text(LOG_ADD, String.format("打开天线口%d成功！\n", antNum));
                scanType = 0x04;
            } else {
                return null;
            }

        }
        switch (api.GetLinkType()) {
            case EC_API.COM: {
                if (api.EC_InventoryTag(rf_1, scanType)) {
                    log.showSendData(rf_1.SendData);
                    //
                    while ((rf_1.RecvData[1] & 0xff) > 7) {
                        log.showReceiveData(rf_1.RecvData);
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
                    log.showReceiveData(rf_1.RecvData);
                }
            }
            break;
        }
        return tab_list;
    }


    public String read(EC_API api) {
        RFdata rf = new RFdata();
        byte[] data = null;
        byte[] UID = null;
        byte b_DSFID = 0X00;
        byte b_AFI = 0X00;
        //盘点命令
        List<Tag> tag_list = ScanTab(api, 0);
        if (tag_list != null) {
            log.Text(LOG_ADD, "盘点成功\n");
            int len = tag_list.size();
            log.Text(LOG_ADD, String.format("标签数量%d\n\n", len));
            if (len == 0) {
                log.Text(LOG_ADD, "检测完成！");
                return null;
            }
            UID = tag_list.get(0).UID;//记录第一个标签的UID，读写卡命令用到
            ALog.e("UID:" + ConvertUtils.bytes2HexString(UID));
            log.Text(LOG_ADD, "UID:" + ConvertUtils.bytes2HexString(UID) + "\n");
        } else {
            log.Text(LOG_ADD, "盘点失败\n");
            return null;
        }

        //打开天线口命令，读写标签之前要打开标签所在的天线口
        if (api.EC_OpenAnt_One(rf, (byte) 1)) {
            log.showSendReceiveData(rf);
            log.Text(LOG_ADD, "打开天线口1成功\n\n");
        } else {
            log.Text(LOG_ADD, "打开天线口1失败\n");
            return null;
        }

        //获取标签信息命令
        if (api.EC_GetOneTagInfo(rf, UID, (byte) 1)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "获取标签信息成功\n\n");
                //记录DSFID和AFI，
                b_DSFID = rf.RecvData[15];
                b_AFI = rf.RecvData[16];
            } else {
                log.Text(LOG_ADD, "获取标签信息失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "获取标签信息失败\n");
            return null;
        }

        //读单个数据块命令
        if (api.EC_ReadCardOneBlock(rf, UID, (byte) 1, (byte) 0, (byte) 0)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "读数据块0成功\n\n");
                //记录数据块
                data = new byte[4];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[8 + k];
                }
            } else {
                log.Text(LOG_ADD, "读数据块0失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "读数据块0失败\n");
            return null;
        }

        //写单个数据块命令
        if (data != null && api.EC_WriteCardOneBlock(rf, UID, (byte) 1, (byte) 0, data)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "写数据块0成功\n\n");
                data = null;
            } else {
                log.Text(LOG_ADD, "写数据块0失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "写数据块0失败\n");
            return null;
        }

        //读多个数据块命令
        if (api.EC_ReadCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                if (api.GetLinkType() != EC_API.NET) {
                    //非UDP传输方式，还有一个结束包
                    RFdata rf_1 = new RFdata();
                    if (api.EC_Receive(rf_1)) {
                        log.showReceiveData(rf_1.RecvData);

                    } else {
                        log.Text(LOG_ADD, "读数据块0~2失败\n");
                        return null;
                    }
                }
                log.Text(LOG_ADD, "读数据块0~2成功\n\n");
                //记录数据块
                data = new byte[12];
                for (int k = 0; k < 4; k++) {
                    data[k] = rf.RecvData[k + 17];
                    data[k + 4] = rf.RecvData[k + 17 + 5];
                    data[k + 8] = rf.RecvData[k + 17 + 10];
                }
            } else {
                log.Text(LOG_ADD, "读数据块0~2失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "读数据块0~2失败\n");
            return null;
        }

        //写多个数据块命令
        if (data != null && api.EC_WriteCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2, data)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "写数据块0~2成功\n\n");
            } else {
                log.Text(LOG_ADD, "写数据块0~2失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "写数据块0~2失败\n");
            return null;
        }

        //写DSFID命令
        if (api.EC_WriteOneTagDSFID(rf, UID, (byte) 1, b_DSFID)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "写DSFID成功\n\n");
            } else {
                log.Text(LOG_ADD, "写DSFID失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "写DSFID失败\n");
            return null;
        }

        //写AFI命令
        if (api.EC_WriteOneTagAFI(rf, UID, (byte) 1, b_AFI)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "写AFI成功\n\n");
            } else {
                log.Text(LOG_ADD, "写AFI失败\n");
                return null;
            }
        } else {
            log.Text(LOG_ADD, "写AFI失败\n");
            return null;
        }

        //检测EAS命令、启用EAS命令、禁用EAS命令
        boolean isEAS;//EAS状态
        if (api.EC_CheckOneTagEAS(rf, UID, (byte) 1)) {
            log.showSendReceiveData(rf);
            if (rf.RecvData[5] == 0) {
                log.Text(LOG_ADD, "检测EAS成功\n");
                if (rf.RecvData[1] == (byte) 0x0a) {//通过检测包的长度判断EAS是否禁用
                    log.Text(LOG_ADD, "EAS状态：关闭\n\n");
                    isEAS = false;
                } else {
                    log.Text(LOG_ADD, "EAS状态：开启\n\n");
                    isEAS = true;
                }
                //自动调节，通过检测的值决定先禁用还是先启用，不更改标签EAS的原始值
                for (int k = 0; k < 2; k++) {
                    if (isEAS) {
                        //禁用EAC
                        if (api.EC_BanOneTagEAS(rf, UID, (byte) 1)) {
                            log.showSendReceiveData(rf);
                            if (rf.RecvData[5] == 0) {
                                log.Text(LOG_ADD, "禁用EAS成功\n\n");
                                isEAS = false;
                                continue;
                            }
                        } else {
                            log.Text(LOG_ADD, "禁用EAS失败\n");
                            return null;
                        }

                    } else {
                        //启用EAC
                        if (api.EC_EnableOneTagEAS(rf, UID, (byte) 1)) {
                            log.showSendReceiveData(rf);
                            if (rf.RecvData[5] == 0) {
                                log.Text(LOG_ADD, "启用EAS成功\n\n");
                                isEAS = true;
                                continue;
                            }
                        } else {
                            log.Text(LOG_ADD, "启用EAS失败\n");
                            return null;
                        }

                    }
                }
                log.Text(LOG_ADD, "检测完成！\n\n");
            }
        } else {
            log.Text(LOG_ADD, "检测EAS失败\n");
            return null;
        }
        return ConvertUtils.bytes2HexString(UID).substring(0, 8);
    }

    /**
     * 子线程更新日志
     *
     * @author YC 2020/10/13
     * @version 1.10
     */
    public class logHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 110:
                    String uid = read(ecApi);
                    ALog.e("uid:" + uid);
                    log.removeMessages(110);
                    log.sendEmptyMessageDelayed(110, 300L);
                    break;
                case LOG_CLEAR://清空日志文本
                {
                    et_log.setText("");
                    ALog.e("");
                }
                break;
                case LOG_SET://设置日志文本
                {
                    String str = (String) msg.obj;
                    ALog.e(str);
                    if (str != null) {
                        et_log.setText(str);
                    }
                }
                break;
                case LOG_ADD://添加日志文本
                {
                    String str = (String) msg.obj;

                    if (str != null) {
                        ALog.e(str);
                        et_log.append(str);
                    }
                }
            }
        }

        /**
         * 日志文本更新
         *
         * @param what 动作
         * @param obj  字符串
         */
        public void Text(int what, Object obj) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = obj;
            sendMessage(msg);
        }

        public void Text(int what) {
            Text(what, null);
        }

        /**
         * 输出发送的数据
         *
         * @param sendData 数据
         */
        public void showSendData(byte[] sendData) {
            String data = HexToStr(sendData, (sendData[1] & 0xff) + 1, true);
            this.Text(LOG_ADD, "发送>>" + data + "\n");
        }

        /**
         * 输出接收的数据
         *
         * @param receiveData 数据
         */
        public void showReceiveData(byte[] receiveData) {
            String data = HexToStr(receiveData, (receiveData[1] & 0xff) + 1, true);
            this.Text(LOG_ADD, "接收<<" + data + "\n");
        }

        /**
         * 输出发送和接收的数据
         *
         * @param rfdata 数据
         */
        public void showSendReceiveData(RFdata rfdata) {
            showSendData(rfdata.SendData);
            showReceiveData(rfdata.RecvData);
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
}
