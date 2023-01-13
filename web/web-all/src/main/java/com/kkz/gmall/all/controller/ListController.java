package com.kkz.gmall.all.controller;

import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.list.client.ListFeignClient;
import com.kkz.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {
    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model){
        Result<Map<String,Object>> result = listFeignClient.list(searchParam);
        model.addAllAttributes(result.getData());
        //设置搜索条件回显
        model.addAttribute("searchParam",searchParam);
        //拼接urlParam
        String urlParam = this.makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        //面包屑--品牌
        String tradeMarkParam = this.makeTradeMark(searchParam.getTrademark());
        model.addAttribute("trademarkParam",tradeMarkParam);
        //面包屑--平台属性
        List<Map<String,String>> propsParamList = this.makeProps(searchParam.getProps());
        model.addAttribute("propsParamList",propsParamList);
        //排序处理
        Map<String,String> orderMap = this.makeOrder(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        return "list/index";
    }
    /**
     * 排序处理
     * order=1:desc --map
     * @param order
     * @return
     */
    private Map<String, String> makeOrder(String order) {
        Map<String, String> resultMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
//            order=1:desc
            String[] split = order.split(":");
            //判断
            if(split != null && split.length == 2){
                resultMap.put("type", split[0]);
                resultMap.put("sort", split[1]);
            }
        }else{
            resultMap.put("type", "1");
            resultMap.put("sort", "desc");
        }
        return resultMap;
    }

    /**
     * 构建平台属性面包屑集合数据
     * @param props
     * @return
     */
    private List<Map<String, String>> makeProps(String[] props) {
        //创建集合封装数据
        List<Map<String, String>> propsList=new ArrayList<>();
        //判断
        if(props != null && props.length > 0){
            for (String prop : props) {
                //prop props=23:4G:运行内存
                String[] split = prop.split(":");
                //判断
                if(split != null && split.length == 3){
                    Map<String,String> resultMap = new HashMap<>();
                    resultMap.put("attrId", split[0]);
                    resultMap.put("attrName", split[2]);
                    resultMap.put("attrValue", split[1]);
                    propsList.add(resultMap);
                }
            }
        }
        return propsList;
    }
    /**
     * 面包屑--品牌
     *
     * @param trademark
     * @return
     */
    private String makeTradeMark(String trademark) {
        //判断
        if(!StringUtils.isEmpty(trademark)){
            //trademark=2:小米
            String[] split = trademark.split(":");
            //判断
            if(split != null &&split.length == 2){
                return "品牌 : "+split[1];
            }
        }
        return "";
    }
    /**
     * 拼接路径
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder builder = new StringBuilder();
        //入口一拼接关键字
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            builder.append("keyword=").append(searchParam.getKeyword());
        }
        //入口二拼接分类
        if(searchParam.getCategory1Id() != null){
            builder.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if(searchParam.getCategory2Id() != null){
            builder.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if(searchParam.getCategory3Id() != null){
            builder.append("category3Id=").append(searchParam.getCategory3Id());
        }
        //品牌
        if(!StringUtils.isEmpty(searchParam.getTrademark())){
            //处理=2:华为
            if(builder.length() > 0){
                builder.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        //平台属性
        String[] props = searchParam.getProps();
        //判断
        if(props != null &&props.length>0){
            for (String prop : props) {
                if(builder.length() > 0){
                    builder.append("&props=").append(prop) ;
                }
            }
        }
        return "list.html?"+builder.toString();
    }
}
