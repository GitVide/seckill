package com.github.chenjie.service;

import com.github.chenjie.conf.ConfigC;
import com.github.chenjie.model.BusinessException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketTimeoutException;

/**
 * @author wangxiaodong
 */
public class SecKillRunnable implements Runnable{

    private final Logger logger = LogManager.getLogger(SecKillService.class);
    /**
     * 是否刷新st
     */
    private boolean resetSt;
    /**
     * httpService
     */
    private HttpService httpService;
    /**
     * 疫苗id
     */
    private Integer vaccineId;
    /**
     * 开始时间
     */
    private long startDate;

    public SecKillRunnable(boolean resetSt, HttpService httpService, Integer vaccineId, long startDate) {
        this.resetSt = resetSt;
        this.httpService = httpService;
        this.vaccineId = vaccineId;
        this.startDate = startDate;
    }

    @Override
    public void run() {
        do {
            long id = Thread.currentThread().getId();
            try {
                //获取加密参数st
                if(resetSt){
                    logger.info("Thread ID：{}，请求获取加密参数st", id);
                    ConfigC.st = httpService.getSt(vaccineId.toString());
                    logger.info("Thread ID：{}，成功获取加密参数st", id);
                }
                logger.info("Thread ID：{}，秒杀请求", id);
                httpService.secKill(vaccineId.toString(), "1", ConfigC.memberId.toString(),
                        ConfigC.idCard, ConfigC.st);
                ConfigC.success = true;
                logger.info("Thread ID：{}，抢购成功", id);
                break;
            } catch (BusinessException e) {
                logger.info("Thread ID: {}, 抢购失败: {}", id, e.getErrMsg());
                if(e.getErrMsg().contains("没抢到")){
                    ConfigC.success = false;
                    break;
                }
            } catch (ConnectTimeoutException | SocketTimeoutException socketTimeoutException ){
                logger.error("Thread ID: {},抢购失败: 超时了", Thread.currentThread().getId());
            }catch (Exception e) {
                logger.warn("Thread ID: {}，未知异常", Thread.currentThread().getId());
            }finally {
                //如果离开始时间10分钟后，或者已经成功抢到则不再继续
                if (System.currentTimeMillis() > startDate + 1000 * 60 *10 || ConfigC.success != null) {
                    break;
                }
            }
        } while (true);
    }
}
