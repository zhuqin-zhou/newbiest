package com.newbiest.gc.trigger;

import com.newbiest.gc.service.GcService;

/**
 * Created by guozhangluo on 2020-08-07
 */
public class AsyncProductModelConversionThread extends GcTriggerThread {

    public static final String CRON_EXPRESS = "0 0/30 * * * ?";

    public AsyncProductModelConversionThread(GcService gcService) {
        super(gcService);
    }

    @Override
    public void execute() {
        this.gcService.asyncMesProductModelConversion();
    }
}