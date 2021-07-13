package com.sys.community.dao;

import com.sys.community.entity.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPost(int userId, int offset, int limit);

    int selectDiscussPostRows(@Param("userId") int userId);

    @Insert({
            "insert into discuss_post(user_id, title, content, type, status, create_time, comment_count, score)",
            "values(#{userId},#{title},#{content},#{type}, #{status}, #{createTime}, #{commentCount}, #{score})"
    })
    int insertDiscussPost(DiscussPost discussPost);

    @Select({
            "select id,user_id, title, content, type, status, create_time, comment_count, score",
            "from discuss_post",
            "where id=#{id}"
    })
    DiscussPost selectDiscussPostById(int id);

    @Update({
            "update discuss_post",
            "set comment_count = #{commentCount}",
            "where id = #{id}"
    })
    int updateCommentCount(int id, int commentCount);
}
