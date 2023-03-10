package com.kkz.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kkz.gmall.cart.client.CartFeignClient;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.common.util.HttpClientUtil;
import com.kkz.gmall.constant.MqConst;
import com.kkz.gmall.model.cart.CartInfo;
import com.kkz.gmall.model.enums.OrderStatus;
import com.kkz.gmall.model.enums.PaymentWay;
import com.kkz.gmall.model.enums.ProcessStatus;
import com.kkz.gmall.model.order.OrderDetail;
import com.kkz.gmall.model.order.OrderInfo;
import com.kkz.gmall.model.user.UserAddress;
import com.kkz.gmall.order.mapper.OrderDetailMapper;
import com.kkz.gmall.order.mapper.OrderInfoMapper;
import com.kkz.gmall.order.service.OrderService;
import com.kkz.gmall.product.client.ProductFeignClient;
import com.kkz.gmall.service.RabbitService;
import com.kkz.gmall.user.client.UserFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderService {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ThreadPoolExecutor executor;
    @Value("${ware.url}")
    private String wareUrl;
    @Autowired
    private RabbitService rabbitService;
    /**
     * ????????????Id ??????????????????
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //??????
        if(orderInfo != null){
            QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);
            List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);
            orderInfo.setOrderDetailList(orderDetailList);
        }
        return orderInfo;
    }

    /**
     * ????????????
     *
     * ??????????????????
     * order_info ???????????????  --???????????????
     * order_detail ???????????????  --???????????????
     *
     * ??????????????????????????????
     * ????????????:orderDetailList
     *
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(OrderInfo orderInfo) {
        //?????????
        orderInfo.sumTotalAmount();
        //????????????
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //????????????
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        //???????????????
        String outTradeNo = "kkz" + UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setOutTradeNo(outTradeNo);
        StringBuilder tradeBody = new StringBuilder();
        //????????????
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            tradeBody.append(orderDetail.getSkuName() + "   ");
        }
        //??????????????????
        if(tradeBody.toString().length() > 100){
            orderInfo.setTradeBody(tradeBody.toString().substring(0, 100));
        }else{
            orderInfo.setTradeBody(tradeBody.toString());
        }
        // ????????????
        orderInfo.setOperateTime(new Date());
        // ???????????? +1???
        Calendar calendar = Calendar.getInstance();
        // ????????????
        calendar.add(Calendar.DATE, 1);
        // ??????????????????
        orderInfo.setExpireTime(calendar.getTime());
        // ????????????????????????
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // ????????????
        orderInfoMapper.insert(orderInfo);
        // ??????????????????
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //????????????id
            orderDetail.setOrderId(orderInfo.getId());
            //??????
            orderDetailMapper.insert(orderDetail);
        }
        //???????????????
//        redisTemplate.delete(RedisConst.USER_KEY_PREFIX + orderInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX);
        //????????????
        rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(), MqConst.DELAY_TIME);
        //????????????id
        return orderInfo.getId();
    }
    /**
     * ????????????
     * @param orderInfo
     * @param request
     * @return
     */
    @Override
    public Result submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        //????????????id
        String userId = AuthContextHolder.getUserId(request);
        //???????????????out_of_trade_no
        String tradeNo = request.getParameter("tradeNo");
        boolean result = this.checkTradeCode(userId, tradeNo);
        if (!result) {
            return Result.fail().message("????????????????????????");
        }
        //????????????
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //??????????????????????????????
        List<CompletableFuture> completableFutureList = new ArrayList<>();
        //??????????????????????????????
        List<String> errorList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                // ????????????
                CompletableFuture<Void> sotckComplatebleFuturen = CompletableFuture.runAsync(() -> {
                    // ????????????
                    boolean flag = this.checkStock(String.valueOf(orderDetail.getSkuId()),
                            String.valueOf(orderDetail.getSkuNum()));
                    //??????
                    if (!flag) {
                        errorList.add(orderDetail.getSkuId() + orderDetail.getSkuName() + "???????????????");
                    }
                }, executor);
                // ????????????List
                completableFutureList.add(sotckComplatebleFuturen);

                CompletableFuture<Void> skuPriceComplatebleFuturn = CompletableFuture.runAsync(() -> {
                    // ??????????????????
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    // ??????
                    if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                        // ??????????????????
                        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
                        // ???????????????
                        // ???????????????????????????????????????????????????redis???
                        cartCheckedList.forEach(item -> {
                            // ??????????????????????????????
                            redisTemplate.boundHashOps(
                                    RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                                    .put(item.getSkuId().toString(), item);
                        });
                        errorList.add(orderDetail.getSkuId() + "???????????????");
                    }
                }, executor);
                // ????????????List
                completableFutureList.add(skuPriceComplatebleFuturn);
            }
        }
        //???????????????
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        //???????????????????????????????????????
        if(errorList.size()>0){
            // ??????????????????
            StringBuilder errorMessage = new StringBuilder();
            for (int i = 0; i < errorList.size(); i++) {
                errorMessage.append(errorList.get(i));
                if (i != errorList.size() - 1) {
                    errorMessage.append(",");
                }
            }
            return Result.fail().message(errorMessage.toString());
        }
        orderInfo.setUserId(Long.parseLong(userId));
        //????????????
        Long orderId = this.saveOrder(orderInfo);
        //???????????????
        this.deleteTradeCode(userId);
        return Result.ok(orderId);
    }

    /**
     * ??????
     * @param request
     * @return
     */
    @Override
    public Result trade(HttpServletRequest request) {
        //??????map?????? ??????
        Map<String, Object> resultMap = new HashMap<>();
        //????????????id
        String userId = AuthContextHolder.getUserId(request);
        //??????????????????
        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));
        //??????????????????
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        List<OrderDetail> orderDetailList = null;
        //??????
        if (!CollectionUtils.isEmpty(cartCheckedList)) {
            orderDetailList = cartCheckedList.stream().map(cartInfo -> {
                //????????????????????????
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                return orderDetail;
            }).collect(Collectors.toList());
        }
        //??????
//        int skuNum = orderDetailList.size();
        // ?????????2???????????????
        int skuNum = 0;
        for (OrderDetail orderDetail : orderDetailList) {
            skuNum += orderDetail.getSkuNum();
        }
        //?????????
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        //???????????????--?????????????????????????????????????????????
        orderInfo.sumTotalAmount();
        resultMap.put("userAddressList", userAddressListByUserId);
        resultMap.put("detailArrayList", orderDetailList);
        resultMap.put("totalNum", skuNum);
        resultMap.put("totalAmount", orderInfo.getTotalAmount());
        //???????????????
        String tradeNo = this.getTradeNo(userId);
        //???????????????out_of_trade_no
        resultMap.put("tradeNo", tradeNo);
        return Result.ok(resultMap);
    }
    /**
     * ???????????????
     * 1.?????????????????????
     * 2.??????????????????redis
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString().replaceAll("-","");
        //?????????redis
        String tradeNoKey = "user:"+userId+":tradeno";
        //??????
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }
    /**
     * ???????????????
     * @param userId
     * @param tradeNoCode
     * @return
     */
    @Override
    public boolean checkTradeCode(String userId, String tradeNoCode) {
        //??????redis??????tradeNo
        String tradeNoKey = "user:" + userId + ":tradeno";
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        //??????
        if(StringUtils.isEmpty(redisTradeNo)){
            return false;
        }
        return redisTradeNo.equals(tradeNoCode);
    }

    /**
     * ???????????????
     * @param userId
     */
    @Override
    public void deleteTradeCode(String userId) {
        //??????redis??????tradeNo
        String tradeNoKey="user:" + userId + ":tradeno";
        redisTemplate.delete(tradeNoKey);
    }

    /**
     * ????????????
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, String skuNum) {
        //http??????http://localhost:9001/hasStock?skuId=22&num=200
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    /**
     * ????????????
     * @param page
     * @param limit
     * @param request
     * @return
     */
    @Override
    public IPage<OrderInfo> getOrderPageByUserId(Long page, Long limit, HttpServletRequest request) {
        //????????????id
        String userId = AuthContextHolder.getUserId(request);
        //??????????????????
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);

        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectOrderPageByUserId(orderInfoPage, userId);
        List<OrderInfo> records = orderInfoIPage.getRecords();
        records.stream().forEach(item -> {
            //??????????????????
            item.setOrderStatusName(OrderStatus.getStatusNameByStatus(item.getOrderStatus()));
        });
        return orderInfoIPage;
    }
    /**
     * ??????????????????
     * @param orderId
     */
    @Override
    public void execExpiredOrder(Long orderId, String flag) {
        //????????????
        this.updateOrderStatus(orderId, ProcessStatus.CLOSED);
        if ("2".equals(flag)) {
            //??????????????????????????????
            this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,
                    MqConst.ROUTING_PAYMENT_CLOSE, orderId);
        }
    }
    /**
     * ??????????????????
     * @param orderId
     * @param processStatus
     */
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        //??????????????????
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        //????????????????????????
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }
    /**
     * ????????????????????????
     * @param orderInfo
     */
    @Override
    public void sendOrderStatus(OrderInfo orderInfo) {
        //????????????????????????
        this.updateOrderStatus(orderInfo.getId(), ProcessStatus.NOTIFIED_WARE);
        //??????????????????
        String strJson = this.initWareOrder(orderInfo);
        //????????????
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK, strJson);
    }
    /**
     * ?????? ??????
     * @param orderInfo
     * @return
     */
    private String initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("orderId", orderInfo.getId());
        resultMap.put("consignee", orderInfo.getConsignee());
        resultMap.put("consigneeTel", orderInfo.getConsigneeTel());
        resultMap.put("orderComment", orderInfo.getOrderComment());
        resultMap.put("orderBody", orderInfo.getTradeBody());
        resultMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        resultMap.put("paymentWay", "2");
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //??????
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            List<OrderDetail> listMap = orderDetailList.stream().map(orderDetail -> {
                Map<String, Object> orderDetailMap = new HashMap<>();
                orderDetailMap.put("skuId", orderDetail.getSkuId());
                orderDetailMap.put("skuNum", orderDetail.getSkuNum());
                orderDetailMap.put("skuName", orderDetail.getSkuName());
                return orderDetail;
            }).collect(Collectors.toList());
            //??????????????????
            resultMap.put("details", listMap);
        }
        return JSON.toJSONString(resultMap);
    }
    /**
     * ????????????
     * @param orderInfo
     * @return
     */
    @Override
    public Map<String, Object> initWareOrderMap(OrderInfo orderInfo) {
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("orderId", orderInfo.getId());
        resultMap.put("consignee", orderInfo.getConsignee());
        resultMap.put("consigneeTel", orderInfo.getConsigneeTel());
        resultMap.put("orderComment", orderInfo.getOrderComment());
        resultMap.put("orderBody", orderInfo.getTradeBody());
        resultMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        resultMap.put("paymentWay", "2");
        // ????????????
        resultMap.put("wareId", orderInfo.getWareId());
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //??????
        if(!CollectionUtils.isEmpty(orderDetailList)){
            List<OrderDetail> listMap = orderDetailList.stream().map(orderDetail -> {
                Map<String, Object> orderDetailMap = new HashMap<>();
                orderDetailMap.put("skuId", orderDetail.getSkuId());
                orderDetailMap.put("skuNum", orderDetail.getSkuNum());
                orderDetailMap.put("skuName", orderDetail.getSkuName());
                return orderDetail;
            }).collect(Collectors.toList());
            //??????????????????
            resultMap.put("details", listMap);
        }
        return resultMap;
    }
    /**
     * ??????
     * @param orderId
     * @param wareSkuMap
     * @return
     *
     *
     * wareSkuMap
     *  [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     *
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        //??????????????????????????????
        List<OrderInfo> resultList = new ArrayList<>();
        //????????????????????????
        //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        //?????????????????????
        OrderInfo orderOrigin = this.getOrderInfoById(Long.parseLong(orderId));
        //????????????
        if(!CollectionUtils.isEmpty(mapList)){
            for (Map map : mapList) {
                //{"wareId":"1","skuIds":["2","10"]}
                // ????????????id
                String wareId = (String) map.get("wareId");
                // ??????
                List<String> skuIds = (List<String>) map.get("skuIds");
                // ???????????????
                OrderInfo subOrderInfo = new OrderInfo();
                // ????????????????????????
                BeanUtils.copyProperties(orderOrigin, subOrderInfo);
                // ??????id
                subOrderInfo.setId(null);
                // ????????????
                subOrderInfo.setWareId(wareId);
                // ???????????????
                subOrderInfo.setParentOrderId(orderOrigin.getId());
                // ??????????????????
                // ??????????????????????????????
                List<OrderDetail> orderDetailList = orderOrigin.getOrderDetailList();
                // ?????????????????????
                List<OrderDetail> orderDetails = new ArrayList<>();
                // ????????????
                StringBuilder builder = new StringBuilder();
                // ??????
                if (!CollectionUtils.isEmpty(orderDetailList)) {
                    for (OrderDetail orderDetail : orderDetailList) {
                        //??????????????????????????????????????????
                        for (String skuId : skuIds) {
                            //??????
                            if (Long.parseLong(skuId) == orderDetail.getSkuId().longValue()) {
                                orderDetails.add(orderDetail);
                                builder.append(orderDetail.getSkuName() + " ");
                            }
                        }
                    }
                }
                subOrderInfo.setOrderDetailList(orderDetails);
                // ?????????--??????????????????????????????
                subOrderInfo.sumTotalAmount();
                // ??????????????????
                subOrderInfo.setTradeBody(builder.toString());
                // ???????????????????????????
                this.saveOrder(subOrderInfo);
                resultList.add(subOrderInfo);
            }
        }
        //?????????????????????
        this.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.SPLIT);
        return resultList;
    }
}
