package com.sys.community.quartz;

import com.sys.community.entity.DiscussPost;
import com.sys.community.service.DiscussPostService;
import com.sys.community.service.ESService;
import com.sys.community.service.LikeService;
import com.sys.community.util.CommunityConstant;
import com.sys.community.util.RedisUtil;
import com.sys.community.util.SensitiveFilter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ESService esService;
    // 论坛开启时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-06-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间失败");
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[没用需要刷新的帖子]");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数： " + operations.size());

        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }

        logger.info("[任务结束]");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在: id = "+ post.getId());
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        // 分数 = 权重 + 距离天数
        double score = Math.log10(Math.max(weight, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新分数
        discussPostService.updateScore(postId, score);
        // 搜索数据
        post.setScore(score);
        esService.saveDiscussPost(post);
    }
}
