package com.nwuer.core.pojo;

import com.nwuer.core.common.Const;
import com.nwuer.core.common.util.DateParseForCronExpressionUtil;
import com.nwuer.core.common.util.TokenCache;
import com.nwuer.core.dto.JobDto;
import com.nwuer.core.dto.UserDto;
import com.nwuer.core.service.MailSendService;
import com.nwuer.core.service.impl.JobService;
import com.nwuer.core.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Job: 表示一个任务,要执行的具体内容
 * @author vividzc
 * @date 2018/6/14 23:03
 */
@Slf4j
@Component
public class PowerYourselfJob extends BaseJob {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private JobService jobService;
    @Autowired
    private MailSendService mailSendService;
    @Override
    public void execute(JobExecutionContext context) {
        log.info("PowerYouself执行:"+new Date().toString());
        JobDetail jobDetail = context.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        String userId = jobDetail.getKey().getGroup();
        //得到UserDto
        UserDto userDto = userService.selectById(userId);
        //得到任务信息
        JobDto jobDto = jobService.selectById(jobName);
        //发送邮件
        Map<String,String> map = new HashMap<>();
        map.put("title",jobDto.getTitle());
        map.put("beganTime", DateParseForCronExpressionUtil.getDateString(jobDto.getBeganTime()));
        map.put("endTime",DateParseForCronExpressionUtil.getDateString(jobDto.getEndTime()));
        map.put("remindTime",DateParseForCronExpressionUtil.getDateString(jobDto.getRemindTime()));
        map.put("desc",jobDto.getNotation());
        map.put("domain",Const.URL_PREFIX);
        map.put("done_url", Const.URL_PREFIX+"/user/job/done/"+jobName);
        map.put("delete_url", Const.URL_PREFIX+"/user/job/delete/"+jobName);
        map.put("job_id",jobName);
        map.put("user_id",userId);

        TokenCache.setKey(Const.JOB_TOKEN_PREFIX+jobName,jobName);
        mailSendService.sendEmail(userDto.getEmail(),map);
    }
}
