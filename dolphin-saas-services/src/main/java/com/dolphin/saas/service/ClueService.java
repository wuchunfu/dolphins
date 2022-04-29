package com.dolphin.saas.service;

public interface ClueService {
    // 企业线索入口 -- 前台API
    void saveReservationInfo(String userName, String phoneNumber, String company, Integer companySize) throws Exception;
}
