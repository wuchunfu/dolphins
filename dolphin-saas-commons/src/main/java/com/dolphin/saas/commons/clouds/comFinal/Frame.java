package com.dolphin.saas.commons.clouds.comFinal;

import java.util.Map;

public interface Frame {

    Frame setVal(Map<String, Object> paramets);

    void initService() throws Exception;

    void execService() throws Exception;

    void finishService() throws Exception;

    void run() throws Exception;

    Frame runner() throws Exception;

    Map<String, Object> refval() throws Exception;
}
