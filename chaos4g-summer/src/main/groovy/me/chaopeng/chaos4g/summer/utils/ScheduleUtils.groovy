package me.chaopeng.chaos4g.summer.utils

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * me.chaopeng.chaos4g.summer.utils.ScheduleUtils
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class ScheduleUtils {
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);

    public static ScheduledExecutorService get() {
        scheduledExecutorService;
    }
}
