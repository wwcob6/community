package com.sys.community.service;

import com.alibaba.fastjson.JSONObject;
import com.sys.community.dao.elasticsearch.DiscussPostRepository;
import com.sys.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
public class ESService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public List<DiscussPost> searchDiscussPost(String keyWord, int current, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title").field("content")
                .requireFieldMatch(false)
                .preTags("<span style='color:red'>").postTags("</span>");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyWord, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(current).size(limit)
                .highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse.getHits().getHits().length <= 0) {

        }
        List<DiscussPost> list = new LinkedList<>();
        if (searchResponse.getHits().getHits().length <= 0) {
            return list;
        }
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            HighlightField highlightField = hit.getHighlightFields().get("title");
            if (highlightField != null) discussPost.setTitle(highlightField.getFragments()[0].toString());
            highlightField = hit.getHighlightFields().get("content");
            if (highlightField != null) discussPost.setContent(highlightField.getFragments()[0].toString());
            // System.out.println(discussPost);
            list.add(discussPost);
        }
        return list;
    }
}
