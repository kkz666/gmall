package com.kkz.gmall.list.repository;

import com.kkz.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
