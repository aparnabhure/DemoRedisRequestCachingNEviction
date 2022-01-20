/*
 *  Copyright (c) 2022 Broadcom. All rights reserved. The term "Broadcom" refers Broadcom Inc. and/or its subsidiaries.
 *  This software and all information contained therein is confidential and proprietary and shall not be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable license agreement, without the express written permission of Broadcom. All authorized reproductions must be marked with this language.
 *
 *  EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE LAW OR AS AGREED BY BROADCOM IN ITS APPLICABLE LICENSE AGREEMENT, BROADCOM PROVIDES THIS DOCUMENTATION "AS IS" WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NONINFRINGEMENT. IN NO EVENT WILL BROADCOM BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE USE OF THIS DOCUMENTATION, INCLUDING WITHOUT LIMITATION, LOST PROFITS, LOST INVESTMENT, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF BROADCOM IS EXPRESSLY ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH LOSS OR DAMAGE.
 *
 */
package com.example.DemoRedisRequestCachingNEviction.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTasks {
//0 50 23 * * ?
    //@Scheduled(cron = "1 0 0 * * ?")
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES, initialDelay = 2)
    public void cleanupCustomerProcessingState(){
        System.out.println("*** in processing state**");
    }
}
