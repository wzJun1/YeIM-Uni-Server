package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.pojo.YeIMPushConfig;
import cn.wzjun1.yeimServer.service.PushService;
import com.alibaba.fastjson.JSONObject;
import com.getui.push.v2.sdk.ApiHelper;
import com.getui.push.v2.sdk.GtApiConfiguration;
import com.getui.push.v2.sdk.api.PushApi;
import com.getui.push.v2.sdk.common.ApiResult;
import com.getui.push.v2.sdk.dto.req.Audience;
import com.getui.push.v2.sdk.dto.req.Settings;
import com.getui.push.v2.sdk.dto.req.message.PushChannel;
import com.getui.push.v2.sdk.dto.req.message.PushDTO;
import com.getui.push.v2.sdk.dto.req.message.PushMessage;
import com.getui.push.v2.sdk.dto.req.message.android.AndroidDTO;
import com.getui.push.v2.sdk.dto.req.message.android.GTNotification;
import com.getui.push.v2.sdk.dto.req.message.android.ThirdNotification;
import com.getui.push.v2.sdk.dto.req.message.android.Ups;
import com.getui.push.v2.sdk.dto.req.message.ios.Alert;
import com.getui.push.v2.sdk.dto.req.message.ios.Aps;
import com.getui.push.v2.sdk.dto.req.message.ios.IosDTO;
import com.github.yitter.idgen.YitIdHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wzjun1
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {

    @Autowired
    YeIMPushConfig yeIMPushConfig;

    @Override
    public void pushSingleByDeviceId(String deviceId, String pushTitle, String pushContent) {
        if (yeIMPushConfig.getType().equals("getui")) {
            gtSinglePush(deviceId, pushTitle, pushContent);
        }
    }

    private void gtSinglePush(String deviceId, String pushTitle, String pushContent) {

        GtApiConfiguration apiConfiguration = new GtApiConfiguration();
        //填写应用配置，参数在“Uni Push”下的“应用配置”页面中获取
        apiConfiguration.setAppId(yeIMPushConfig.getGt().getAppId());
        apiConfiguration.setAppKey(yeIMPushConfig.getGt().getAppKey());
        apiConfiguration.setMasterSecret(yeIMPushConfig.getGt().getMasterSecret());
        apiConfiguration.setDomain("https://restapi.getui.com/v2/");
        // 实例化ApiHelper对象，用于创建接口对象
        ApiHelper apiHelper = ApiHelper.build(apiConfiguration);
        // 创建对象，建议复用。目前有PushApi、StatisticApi、UserApi
        PushApi pushApi = apiHelper.creatApi(PushApi.class);
        //根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数，requestid需要每次变化唯一
        pushDTO.setRequestId(System.currentTimeMillis() + "-" + YitIdHelper.nextId());
        Settings settings = new Settings();
        pushDTO.setSettings(settings);
        //消息有效期，走厂商消息必须设置该值
        settings.setTtl(3600000);
        //在线走个推通道时推送的消息体
        PushMessage pushMessage = new PushMessage();
        pushDTO.setPushMessage(pushMessage);

        //GTNotification和transmission二选一
//        GTNotification gtNotification = new GTNotification();
//        gtNotification.setTitle(pushTitle);
//        gtNotification.setBody(pushContent);
//        gtNotification.setClickType("startapp");
//        gtNotification.setChannelId(yeIMPushConfig.getOppoChannelId());
//        gtNotification.setChannelName("聊天离线通知");
//        gtNotification.setChannelLevel("4");
        //pushMessage.setNotification(gtNotification);

        JSONObject transmission = new JSONObject();
        transmission.put("title", pushTitle);
        transmission.put("content", pushContent);
        transmission.put("payload", "nothing");
        pushMessage.setTransmission(transmission.toJSONString());

        // 设置接收人信息
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(deviceId);
        //设置离线推送时的消息体
        PushChannel pushChannel = new PushChannel();
        //安卓离线厂商通道推送的消息体
        AndroidDTO androidDTO = new AndroidDTO();
        Ups ups = new Ups();
        ups.addOption("HO", "/android/notification/importance", "NORMAL");
        ups.addOption("HW", "/message/android/category", "IM");
        ups.addOption("HW", "/message/android/notification/badge/add_num", 1);
        ups.addOption("HW", "/message/android/notification/importance", "HIGH");
        ups.addOption("VV", "/classification", 1);
        ups.addOption("XM", "/extra.channel_id", "high_system");
        ups.addOption("XM", "/extra.notify_foreground", "1");
        ups.addOption("OP", "/channel_id", yeIMPushConfig.getOppoChannelId());
        ups.addOption("OPG", "/channel_id", yeIMPushConfig.getOppoChannelId());
        ThirdNotification thirdNotification = new ThirdNotification();
        thirdNotification.setTitle(pushTitle);
        thirdNotification.setBody(pushContent);
        thirdNotification.setClickType("startapp");
        ups.setNotification(thirdNotification);
        androidDTO.setUps(ups);
        pushChannel.setAndroid(androidDTO);
        //ios离线apn通道推送的消息体
        Alert alert = new Alert();
        alert.setTitle(pushTitle);
        alert.setBody(pushContent);
        Aps aps = new Aps();
        aps.setContentAvailable(0);
        aps.setSound("default");
        aps.setAlert(alert);
        IosDTO iosDTO = new IosDTO();
        iosDTO.setAps(aps);
        iosDTO.setType("notify");
        pushChannel.setIos(iosDTO);
        pushDTO.setPushChannel(pushChannel);
        // 进行cid单推
        ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
        //System.out.println(JSONObject.toJSONString(apiResult));
    }

}




