package com.github.chenjie.ui;

import com.github.chenjie.conf.ConfigC;
import com.github.chenjie.model.Member;
import com.github.chenjie.model.VaccineList;
import com.github.chenjie.service.HttpService;
import com.github.chenjie.service.SecKillService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2020-08-14.
 * 控制台模式
 * @author wangxiaodong
 */
public class ConsoleMode {

    private final Logger log = LogManager.getLogger(ConsoleMode.class);

    private ExecutorService service = Executors.newFixedThreadPool(100);

    private HttpService httpService = new HttpService();

    private SecKillService secKillService = new SecKillService();

    public void start() throws IOException, ParseException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        log.info("请输入tk：");
        ConfigC.tk = sc.nextLine().trim();
        log.info("请输入Cookie：");
        calCookie(sc.nextLine().trim());
        log.info("获取接种人员......");
        List<Member> members = httpService.getMembers();
        for (int i = 0; i < members.size(); i++) {
            log.info("{}-{}-{}", i, members.get(i).getName(), members.get(i).getIdCardNo());
        }
        log.info("请输入接种人员序号：");
        int no = Integer.parseInt(sc.nextLine());
        ConfigC.memberId = members.get(no).getId();
        ConfigC.idCard = members.get(no).getIdCardNo();

        log.info("获取疫苗列表......");
        List<VaccineList> vaccineList = httpService.getVaccineList();
        for (int i = 0; i < vaccineList.size(); i++) {
            VaccineList item = vaccineList.get(i);
            log.info("{}-{}-{}-{}-{}", i, item.getName(), item.getVaccineName(), item.getAddress(), item.getStartTime());
        }
        log.info("请输入疫苗序号：");
        no = Integer.parseInt(sc.nextLine());
        int code = vaccineList.get(no).getId();
        String startTime = vaccineList.get(no).getStartTime();
        log.info("按回车键开始秒杀：");
        sc.nextLine();
        secKillService.startSecKill(code, startTime, null);
    }
    private void calCookie(String cookie){
        String[] s = cookie.replaceAll(" ", "").split(";");
        for (String s1 : s) {
            ConfigC.cookie.put(s1.split("=")[0], s1);
        }
    }
}
