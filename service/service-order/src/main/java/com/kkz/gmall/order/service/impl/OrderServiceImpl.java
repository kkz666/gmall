package com.kkz.gmall.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kkz.gmall.cart.client.CartFeignClient;
import com.kkz.gmall.common.constant.RedisConst;
import com.kkz.gmall.common.result.Result;
import com.kkz.gmall.common.util.AuthContextHolder;
import com.kkz.gmall.common.util.HttpClientUtil;
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
import com.kkz.gmall.user.client.UserFeignClient;
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
public class OrderServiceImpl implements OrderService {
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

    /**
     * 提交订单
     *
     * 涉及到的表：
     * order_info 订单详情表  --订单的说明
     * order_detail 订单明细表  --商品的说明
     *
     * 一个订单多个订单明细
     * 关联关系:orderDetailList
     *
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(OrderInfo orderInfo) {
        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //付款方式
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        //订单交易号
        String outTradeNo = "kkz" + UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setOutTradeNo(outTradeNo);
        StringBuilder tradeBody = new StringBuilder();
        //订单描述
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            tradeBody.append(orderDetail.getSkuName() + "   ");
        }
        //设置订单描述
        if(tradeBody.toString().length() > 100){
            orderInfo.setTradeBody(tradeBody.toString().substring(0, 100));
        }else{
            orderInfo.setTradeBody(tradeBody.toString());
        }
        // 操作时间
        orderInfo.setOperateTime(new Date());
        // 失效时间 +1天
        Calendar calendar = Calendar.getInstance();
        // 天数加一
        calendar.add(Calendar.DATE, 1);
        // 设置超时时间
        orderInfo.setExpireTime(calendar.getTime());
        // 设置订单进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 保存订单
        orderInfoMapper.insert(orderInfo);
        // 保存订单明细
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //设置订单id
            orderDetail.setOrderId(orderInfo.getId());
            //添加
            orderDetailMapper.insert(orderDetail);
        }
        //删除购物车
//        redisTemplate.delete(RedisConst.USER_KEY_PREFIX + orderInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX);
        //返回订单id
        return orderInfo.getId();
    }
    /**
     * 提交订单
     * @param orderInfo
     * @param request
     * @return
     */
    @Override
    public Result submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //校验流水号
        String tradeNo = request.getParameter("tradeNo");
        boolean result = this.checkTradeCode(userId, tradeNo);
        if(!result){
            return Result.fail().message("不能重复提交订单");
        }

        //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //定义集合收集异步对象
        List<CompletableFuture> completableFutureList = new ArrayList<>();
        //定义集合收集错误信息
        List<String> errorList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                // 异步编排

                CompletableFuture<Void> sotckComplatebleFuturen = CompletableFuture.runAsync(() -> {
                    // 验证库存
                    boolean flag = this.checkStock(String.valueOf(orderDetail.getSkuId()),
                            String.valueOf(orderDetail.getSkuNum()));
                    //处理
                    if (!flag) {
                        errorList.add(orderDetail.getSkuId() + orderDetail.getSkuName() + "库存不足！");
                    }
                }, executor);
                // 加入异步List
                completableFutureList.add(sotckComplatebleFuturen);

                CompletableFuture<Void> skuPriceComplatebleFuturn = CompletableFuture.runAsync(() -> {
                    // 获取实时价格
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    // 比较
                    if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                        // 设置新的内容
                        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
                        // 更新到缓存
                        // 加入购物车的时候已经将商品信息加到redis中
                        cartCheckedList.forEach(item -> {
                            // 将实时价格更新到缓存
                            redisTemplate.boundHashOps(
                                    RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX)
                                    .put(item.getSkuId().toString(), item);
                        });
                        errorList.add(orderDetail.getSkuId() + "价格有变动");
                    }
                }, executor);
                // 加入异步List
                completableFutureList.add(skuPriceComplatebleFuturn);
            }
        }
        //声明为一组
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        //错误集合信息决定是否下订单
        if(errorList.size()>0){
            // 拼接错误消息
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
        //保存订单
        Long orderId = this.saveOrder(orderInfo);
        //删除流水号
        this.deleteTradeCode(userId);
        return Result.ok(orderId);
    }

    /**
     * 结算
     * @param request
     * @return
     */
    @Override
    public Result trade(HttpServletRequest request) {
        //创建map封装 数据
        Map<String, Object> resultMap = new HashMap<>();
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //查询地址列表
        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));
        //封装购物列表
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        List<OrderDetail> orderDetailList = null;
        //判断
        if (!CollectionUtils.isEmpty(cartCheckedList)) {
            orderDetailList = cartCheckedList.stream().map(cartInfo -> {
                //创建订单明细对象
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                return orderDetail;
            }).collect(Collectors.toList());
        }
        //数量
//        int skuNum = orderDetailList.size();
        // 数量为2算两个商品
        int skuNum = 0;
        for (OrderDetail orderDetail : orderDetailList) {
            skuNum += orderDetail.getSkuNum();
        }
        //总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        //计算总金额--在计算之前必须设置订单明细集合
        orderInfo.sumTotalAmount();
        resultMap.put("userAddressList", userAddressListByUserId);
        resultMap.put("detailArrayList", orderDetailList);
        resultMap.put("totalNum", skuNum);
        resultMap.put("totalAmount", orderInfo.getTotalAmount());
        //携带流水号
        String tradeNo = this.getTradeNo(userId);
        //存储流水号
        resultMap.put("tradeNo", tradeNo);
        return Result.ok(resultMap);
    }
    /**
     * 生成流水号
     * 1.生成后返回页面
     * 2.生成后存储到redis
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString().replaceAll("-","");
        //存储到redis
        String tradeNoKey = "user:"+userId+":tradeno";
        //存储
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }
    /**
     * 校验流水号
     * @param userId
     * @param tradeNoCode
     * @return
     */
    @Override
    public boolean checkTradeCode(String userId, String tradeNoCode) {
        //获取redis中的tradeNo
        String tradeNoKey = "user:" + userId + ":tradeno";
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        //判断
        if(StringUtils.isEmpty(redisTradeNo)){
            return false;
        }
        return redisTradeNo.equals(tradeNoCode);
    }

    /**
     * 删除流水号
     * @param userId
     */
    @Override
    public void deleteTradeCode(String userId) {
        //获取redis中的tradeNo
        String tradeNoKey="user:" + userId + ":tradeno";
        redisTemplate.delete(tradeNoKey);
    }

    /**
     * 校验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, String skuNum) {
        //http请求http://localhost:9001/hasStock?skuId=22&num=200
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    /**
     * 我的订单
     * @param page
     * @param limit
     * @param request
     * @return
     */
    @Override
    public IPage<OrderInfo> getOrderPageByUserId(Long page, Long limit, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //处理分页参数
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);

        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectOrderPageByUserId(orderInfoPage, userId);
        List<OrderInfo> records = orderInfoIPage.getRecords();
        records.stream().forEach(item -> {
            //设置订单状态
            item.setOrderStatusName(OrderStatus.getStatusNameByStatus(item.getOrderStatus()));
        });
        return orderInfoIPage;
    }
}
