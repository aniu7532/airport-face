package com.arcsoft.arcfacedemo.util;

import java.lang.reflect.Type;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.LeadingPeople;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static String[] fromString(String value) {
        if (value == null) {
            return null;
        }
        return value.split(",");
    }

    @TypeConverter
    public static String fromStringArray(String[] array) {
        if (array == null) {
            return null;
        }
        return String.join(",", array);
    }

    @TypeConverter
    public static byte[] fromByteArray(String value) {
        if (value == null) {
            return null;
        }
        String[] byteStrings = value.split(",");
        byte[] byteArray = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            byteArray[i] = Byte.parseByte(byteStrings[i]);
        }
        return byteArray;
    }

    @TypeConverter
    public static String fromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(b).append(",");
        }
        return sb.toString();
    }

    @TypeConverter
    public static String someObjectArrayToJson(LeadingPeople[] someObjectArray) {
        if (someObjectArray == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(someObjectArray);
    }

    @TypeConverter
    public static LeadingPeople[] jsonToSomeObjectArray(String json) {
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<LeadingPeople[]>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static LongTermPass convertToLongTermPass(LongPassCard longPassCard) {
        LongTermPass longTermPass = new LongTermPass();
        longTermPass.id = longPassCard.id;
        longTermPass.applyId = longPassCard.applyId;
        longTermPass.idCode = longPassCard.idCode;
        longTermPass.cardId = longPassCard.cardId;
        longTermPass.score = longPassCard.score;
        longTermPass.status = longPassCard.status;
        longTermPass.type = longPassCard.type;
        longTermPass.userId = longPassCard.userId;
        longTermPass.companyId = longPassCard.companyId;
        longTermPass.orgId = longPassCard.orgId;
        longTermPass.nickname = longPassCard.nickname;
        longTermPass.companyName = longPassCard.companyName;
        longTermPass.orgName = longPassCard.orgName;
        longTermPass.expiryDate = longPassCard.expiryDate;
        longTermPass.areaRootIds = longPassCard.areaRootIds;
        longTermPass.areaRootCodes = longPassCard.areaRootCodes;
        longTermPass.areaIds = longPassCard.areaIds;
        longTermPass.areaCodes = longPassCard.areaCodes;
        longTermPass.startDate = longPassCard.startDate;
        longTermPass.setleadingPeople(longPassCard.leadingPeople); // 使用类型转换器设置 leadingPeople
        longTermPass.photo = longPassCard.photo;
        longTermPass.photoBytes = longPassCard.photoBytes;
        longTermPass.leadingPeopleId = longPassCard.leadingPeopleId;
        longTermPass.idNo = longPassCard.idNo;
        longTermPass.checkPhoto = longPassCard.checkPhoto;
        longTermPass.checkPhotoBytes = longPassCard.checkPhotoBytes;
        longTermPass.unitName = longPassCard.unitName;
        longTermPass.templateType = longPassCard.templateType;
        longTermPass.isBlacklist = longPassCard.isBlacklist;
        longTermPass.isWithhold = longPassCard.isWithhold;
        longTermPass.isWithdraw = longPassCard.isWithdraw;
        longTermPass.withholdStartDate = longPassCard.withholdStartDate;
        longTermPass.withholdEndDate = longPassCard.withholdEndDate;
        longTermPass.cardIdLong = longPassCard.cardIdLong;
        longTermPass.areaDisplayCode = longPassCard.areaDisplayCode;
        longTermPass.businessScope = longPassCard.businessScope;
        longTermPass.sex = longPassCard.sex;
        longTermPass.updateTime = longPassCard.updateTime;
        return longTermPass;
    }

    public static LongPassCard convertToLongPassCard(LongTermPass longTermPass1) {
        LongPassCard longPassCard = new LongPassCard();
        longPassCard.id = longTermPass1.id;
        longPassCard.applyId = longTermPass1.applyId;
        longPassCard.idCode = longTermPass1.idCode;
        longPassCard.cardId = longTermPass1.cardId;
        longPassCard.score = longTermPass1.score;
        longPassCard.status = longTermPass1.status;
        longPassCard.type = longTermPass1.type;
        longPassCard.userId = longTermPass1.userId;
        longPassCard.companyId = longTermPass1.companyId;
        longPassCard.orgId = longTermPass1.orgId;
        longPassCard.nickname = longTermPass1.nickname;
        longPassCard.companyName = longTermPass1.companyName;
        longPassCard.orgName = longTermPass1.orgName;
        longPassCard.expiryDate = longTermPass1.expiryDate;
        longPassCard.areaRootIds = longTermPass1.areaRootIds;
        longPassCard.areaRootCodes = longTermPass1.areaRootCodes;
        longPassCard.areaIds = longTermPass1.areaIds;
        longPassCard.areaCodes = longTermPass1.areaCodes;
        longPassCard.startDate = longTermPass1.startDate;
        // longPassCard.setleadingPeople(longTermPass1.leadingPeople); // 使用类型转换器设置 leadingPeople
        longPassCard.photo = longTermPass1.photo;
        longPassCard.photoBytes = longTermPass1.photoBytes;
        longPassCard.leadingPeopleId = longTermPass1.leadingPeopleId;
        longPassCard.idNo = longTermPass1.idNo;
        longPassCard.checkPhoto = longTermPass1.checkPhoto;
        longPassCard.checkPhotoBytes = longTermPass1.checkPhotoBytes;
        longPassCard.unitName = longTermPass1.unitName;
        longPassCard.templateType = longTermPass1.templateType;
        longPassCard.isBlacklist = longTermPass1.isBlacklist;
        longPassCard.isWithhold = longTermPass1.isWithhold;
        longPassCard.isWithdraw = longTermPass1.isWithdraw;
        longPassCard.withholdStartDate = longTermPass1.withholdStartDate;
        longPassCard.withholdEndDate = longTermPass1.withholdEndDate;
        longPassCard.cardIdLong = longTermPass1.cardIdLong;
        longPassCard.areaDisplayCode = longTermPass1.areaDisplayCode;
        longPassCard.businessScope = longTermPass1.businessScope;
        longPassCard.sex = longTermPass1.sex;
        longPassCard.updateTime = longTermPass1.updateTime;
        return longPassCard;
    }

    // 在线图片转化为字节数组
    // public static List<LongPassCard> convertImageUrlToByte(List<LongPassCard> longPassCards) {
    // for (LongPassCard longPassCard : longPassCards) {
    // longPassCard.checkPhotoBytes = ImageUtils.getImageBytes(longPassCard.checkPhoto);
    // longPassCard.photoBytes = ImageUtils.getImageBytes(longPassCard.photo);
    // }
    // return longPassCards;
    // }

}
