package com.sys.community;

import com.alibaba.fastjson.JSONObject;
import com.sys.community.dao.DiscussPostMapper;
import com.sys.community.dao.elasticsearch.DiscussPostRepository;
import com.sys.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class ESTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /*@Autowired
    private ElasticsearchTemplate elasticsearchTemplate;*/

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    /*@Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(101, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(102, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(103, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(111, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(112, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(131, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(132, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(133, 0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(134, 0,100));
    }
*/
    @Test
    public void testUpdate() {
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人，使劲灌水");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(231);
    }

    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
        //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0).size(10);// 从那条开始查询，查询总条数

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSONString(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(discussPost);
            list.add(discussPost);
        }


    }

    @Test
    public void testHighlightSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title").field("content")
                .requireFieldMatch(false)
                .preTags("<span style='color:red'>").postTags("</span>");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0).size(10)
                .highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse.getHits().getHits().length <= 0) {
            return;
        }
        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            HighlightField highlightField = hit.getHighlightFields().get("title");
            if (highlightField != null) discussPost.setTitle(highlightField.getFragments()[0].toString());
            highlightField = hit.getHighlightFields().get("content");
            if (highlightField != null) discussPost.setContent(highlightField.getFragments()[0].toString());
            // System.out.println(discussPost);
            list.add(discussPost);
            }
        }
    }
