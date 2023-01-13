package com.kkz.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kkz.gmall.list.repository.GoodsRepository;
import com.kkz.gmall.list.service.SearchService;
import com.kkz.gmall.model.list.*;
import com.kkz.gmall.model.product.BaseAttrInfo;
import com.kkz.gmall.model.product.BaseCategoryView;
import com.kkz.gmall.model.product.BaseTrademark;
import com.kkz.gmall.model.product.SkuInfo;
import com.kkz.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient highLevelClient;

    @Override
    public void upperGoods(Long skuId) {
        // 创建封装数据对象
        Goods goods = new Goods();
        // 设置skuInfo信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo != null) {
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
            goods.setCreateTime(new Date());
            // 设置品牌信息
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            if (trademark != null) {
                goods.setTmId(skuInfo.getTmId());
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }
            // 设置分类信息
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            if (categoryView != null) {
                goods.setCategory1Id(categoryView.getCategory1Id());
                goods.setCategory2Id(categoryView.getCategory2Id());
                goods.setCategory3Id(categoryView.getCategory3Id());
                goods.setCategory1Name(categoryView.getCategory1Name());
                goods.setCategory2Name(categoryView.getCategory2Name());
                goods.setCategory3Name(categoryView.getCategory3Name());
            }
        }
        // 设置平台属性
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        if (!CollectionUtils.isEmpty(attrList)) {
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                //属性id
                searchAttr.setAttrId(baseAttrInfo.getId());
                //属性名称
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                //属性值名称
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            //添加到goods
            goods.setAttrs(searchAttrList);
        }
        // 将数据添加到es
        goodsRepository.save(goods);
    }
    /**
     * 商品下架
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * 更新商品的热度排名
     *
     * @param skuId 1.保存到redis作为累计
     *              redis的类型 ：
     *              hostScore  skuId:21   1
     *              <p>
     *              2.累计到10整数，修改es
     */
    @Override
    public void incrHotScore(Long skuId) {
        //定义key
        String hotKey = "hotScore";
        //累计数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        //当累计到整10的时候修改到es
        if (hotScore % 10 == 0) {
            //获取skuId对应的商品
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //修改，没有update方法，save覆盖方式id
            goods.setHotScore(Math.round(hotScore));
            //修改
            goodsRepository.save(goods);
        }
    }
    /**
     * 商品搜索
     *
     * @param searchParam
     * @return
     */
    @Override
    @SneakyThrows
    public SearchResponseVo search(SearchParam searchParam) {
        //第一步：封装条件对象
        SearchRequest searchRequest = this.buildQuery(searchParam);
        //第二步：执行查询
        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //第三步：根据返回的响应对象获取结果
        SearchResponseVo searchResponseVo = this.parseSearchResponseVo(searchResponse);
        //设置每页条数
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //设置当前页
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //总页数(向上取整)
        Long totalPage =
                (searchResponseVo.getTotal() +
                        searchResponseVo.getPageSize() - 1) / searchResponseVo.getPageSize();
        searchResponseVo.setTotalPages(totalPage);
        return searchResponseVo;
    }
    /**
     * 搜索结果集封装
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResponseVo(SearchResponse searchResponse) {
        // 创建对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 获取所有的聚合数据封装
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        // 获取品牌的聚合结果
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(buckets)){
            // 获取品牌集合数据
            List<SearchResponseTmVo> responseTmVoList = buckets.stream().map(bucket -> {
                // 创建品牌对象
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                // 封装id
                long tmId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(tmId);
                // 封装name
                Map<String, Aggregation> tmSubAggregation = ((Terms.Bucket) bucket).getAggregations().asMap();
                // 获取品牌名称聚合对象
                ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmSubAggregation.get("tmNameAgg");
                // 品牌名称
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
                // 封装logoUrl
                ParsedStringTerms  tmLogoUrlAgg = (ParsedStringTerms) tmSubAggregation.get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
                return searchResponseTmVo;
            }).collect(Collectors.toList());
            // 设置品牌信息到响应对象
            searchResponseVo.setTrademarkList(responseTmVoList);
        }

        // 封装平台属性集合数据
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        // 获取id子聚合
        Map<String, Aggregation> attrSubAggregation = attrAgg.getAggregations().asMap();
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrSubAggregation.get("attrIdAgg");
        // 获取聚合数据
        // attrIdAgg的buckets
        List<? extends Terms.Bucket> subBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(subBuckets)){
            // 获取平台属性结果集
            List<SearchResponseAttrVo> responseAttrVoList = subBuckets.stream().map(subBucket -> {
                // 创建平台封装对象
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                // 封装平台属性id
                long attrId = ((Terms.Bucket) subBucket).getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);
                // 获取子聚合数据
                Map<String, Aggregation> subsbuAggregation = ((Terms.Bucket) subBucket).getAggregations().asMap();
                // 封装平台属性名
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subsbuAggregation.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                // 封装平台属性值
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subsbuAggregation.get("attrValueAgg");
                // 获取属性值的结果集
                // attrValueAgg的buckets
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrValueAggBuckets)){
                    //获取属性值集合
                    List<String> attrValueList = attrValueAggBuckets.stream().map(attrValueBucket -> {
                        return ((Terms.Bucket) attrValueBucket).getKeyAsString();
                    }).collect(Collectors.toList());
                    //设置属性值集合
                    searchResponseAttrVo.setAttrValueList(attrValueList);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            //设置到响应对象中
            searchResponseVo.setAttrsList(responseAttrVoList);
        }
        //封装商品goods数据
        SearchHit[] hits = searchResponse.getHits().getHits();
        //定义集合接收goods数据
        List<Goods> goodsList = new ArrayList<>();
        //判断
        if(hits != null && hits.length > 0){
            for (SearchHit hit : hits) {
                Goods goods = JSONObject.parseObject(hit.getSourceAsString(), Goods.class);
                //获取高亮数据
                if(hit.getHighlightFields().get("title") != null){
                    //获取高亮
                    HighlightField title = hit.getHighlightFields().get("title");
                    goods.setTitle(title.getFragments()[0].toString());
                }
                goodsList.add(goods);
            }
        }
        //设置商品集合数据
        searchResponseVo.setGoodsList(goodsList);
        //设置总记录数据
        long total = searchResponse.getHits().getTotalHits().value;
        searchResponseVo.setTotal(total);
        return searchResponseVo;
    }
    /**
     * 封装查询条件
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildQuery(SearchParam searchParam) {
        // 创建查询请求对象 参数：索引库
        SearchRequest searchRequest = new SearchRequest("goods");
        // 创建提交构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 创建多条件对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 判断是否有关键字条件
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            // 设置关键字条件到多条件对象
            boolQueryBuilder.must(title);
        }
        // 过滤品牌 trademark=2:华为
        String trademark = searchParam.getTrademark();
        //判断
        if (!StringUtils.isEmpty(trademark)) {
            //split
            String[] split = trademark.split(":");
            //判断
            if (split != null && split.length == 2) {
                //构建过滤品牌
                TermQueryBuilder tmId = QueryBuilders.termQuery("tmId", split[0]);
                //添加 到多条件对象
                boolQueryBuilder.filter(tmId);
            }
        }
        //分类
        if (searchParam.getCategory1Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        //分类
        if (searchParam.getCategory2Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        //分类
        if (searchParam.getCategory3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        //平台属性  23:4G:运行内存
        String[] props = searchParam.getProps();
        //判断数组是否为空
        if (props != null && props.length > 0) {
            for (String prop : props) {
                //prop 23:4G:运行内存
                //平台属性Id 平台属性值名称 平台属性名
                //split StringUtils.split
                String[] split = prop.split(":");
                //判断
                if(split != null && split.length == 3){
                    //创建多条件对象
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //子多条件对象
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                    //nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    //添加到最外层多条件对象
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }
        //添加条件到构建对象
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        //计算索引
        int index = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(searchParam.getPageSize());
        //排序 1:desc    1:hotScore 2:price
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            //定义字段
            String field = null;
            //switch
            switch (split[0]){
                case "1":
                    field = "hotScore";
                    break;
                case "2":
                    field = "price";
                    break;
            }
            searchSourceBuilder.sort(field, split[1].equals("asc") ? SortOrder.ASC : SortOrder.DESC);
        }else{
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }
        //聚合--品牌
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(tmIdAgg);

        //聚合--平台属性
        NestedAggregationBuilder nestedAgg = AggregationBuilders.nested("attrAgg", "attrs");
        //一级子聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //添加到父聚合
        nestedAgg.subAggregation(attrIdAgg);
        //二级子聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        searchSourceBuilder.aggregation(nestedAgg);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 指定字段
        highlightBuilder.field("title");
        // 指定前缀
        highlightBuilder.preTags("<span style=color:red>");
        // 执行后缀
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 结果过滤
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);
        // 将构建对象添加到请求中
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl:==" + searchSourceBuilder.toString());
        return searchRequest;
    }
}
