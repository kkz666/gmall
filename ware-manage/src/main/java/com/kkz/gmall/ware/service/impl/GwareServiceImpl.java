package com.kkz.gmall.ware.service.impl;


import com.alibaba.fastjson.JSON;
import com.kkz.gmall.ware.bean.WareInfo;
import com.kkz.gmall.ware.bean.WareOrderTask;
import com.kkz.gmall.ware.bean.WareOrderTaskDetail;
import com.kkz.gmall.ware.bean.WareSku;
import com.kkz.gmall.ware.constant.MqConst;
import com.kkz.gmall.ware.enums.TaskStatus;
import com.kkz.gmall.ware.mapper.WareInfoMapper;
import com.kkz.gmall.ware.mapper.WareOrderTaskDetailMapper;
import com.kkz.gmall.ware.mapper.WareOrderTaskMapper;
import com.kkz.gmall.ware.mapper.WareSkuMapper;
import com.kkz.gmall.ware.service.GwareService;
import com.kkz.gmall.ware.util.HttpclientUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class GwareServiceImpl implements GwareService {

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private WareInfoMapper wareInfoMapper;

    @Autowired
    private WareOrderTaskMapper wareOrderTaskMapper;

    @Autowired
    private WareOrderTaskDetailMapper wareOrderTaskDetailMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${order.split.url}")
    private String ORDER_URL;

    public Integer getStockBySkuId(String skuid) {
        Integer stock = wareSkuMapper.selectStockBySkuid(skuid);

        return stock;
    }

    public boolean hasStockBySkuId(String skuid, Integer num) {
        Integer stock = getStockBySkuId(skuid);

        if (stock == null || stock < num) {
            return false;
        }
        return true;
    }


    public List<WareInfo> getWareInfoBySkuid(String skuid) {
        List<WareInfo> wareInfos = wareInfoMapper.selectWareInfoBySkuid(skuid);
        return wareInfos;
    }

    public List<WareInfo> getWareInfoList() {
        List<WareInfo> wareInfos = wareInfoMapper.selectList(null);
        return wareInfos;
    }


    public void addWareInfo() {
        WareInfo wareInfo = new WareInfo();
        wareInfo.setAddress("1123");
        wareInfo.setAreacode("123123");
        wareInfo.setName("123123");
        wareInfoMapper.insert(wareInfo);

        WareSku wareSku = new WareSku();
        wareSku.setId(wareInfo.getId());
        wareSku.setWarehouseId("991");
        wareSkuMapper.insert(wareSku);
    }

    public Map<String, List<String>> getWareSkuMap(List<String> skuIdlist) {
        QueryWrapper<WareSku> queryWrapper = new QueryWrapper();
        queryWrapper.in("sku_id", skuIdlist);
        List<WareSku> wareSkuList = wareSkuMapper.selectList(queryWrapper);

        Map<String, List<String>> wareSkuMap = new HashMap<>();

        for (WareSku wareSku : wareSkuList) {
            List<String> skulistOfWare = wareSkuMap.get(wareSku.getWarehouseId());
            if (skulistOfWare == null) {
                skulistOfWare = new ArrayList<>();
            }
            skulistOfWare.add(wareSku.getSkuId());
            // 1 - 33  | 2 - 35 36
            wareSkuMap.put(wareSku.getWarehouseId(), skulistOfWare);
        }
        return wareSkuMap;

    }


    public List<Map<String, Object>> convertWareSkuMapList(Map<String, List<String>> wareSkuMap) {
        List<Map<String, Object>> wareSkuMapList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : wareSkuMap.entrySet()) {
            Map<String, Object> skuWareMap = new HashMap<>();
            String wareid = entry.getKey();
            skuWareMap.put("wareId", wareid);
            List<String> skuids = entry.getValue();
            skuWareMap.put("skuIds", skuids);
            wareSkuMapList.add(skuWareMap);
        }
        return wareSkuMapList;
    }

    public void addWareSku(WareSku wareSku) {
        wareSkuMapper.insert(wareSku);
    }

    public List<WareSku> getWareSkuList() {
        List<WareSku> wareSkuList = wareSkuMapper.selectWareSkuAll();
        return wareSkuList;
    }

    public WareOrderTask getWareOrderTask(String taskId) {
        WareOrderTask wareOrderTask = wareOrderTaskMapper.selectById(taskId);

        QueryWrapper<WareOrderTaskDetail> queryWrapper = new QueryWrapper();
        queryWrapper.eq("task_id", taskId);
        List<WareOrderTaskDetail> details = wareOrderTaskDetailMapper.selectList(queryWrapper);
        wareOrderTask.setDetails(details);
        return wareOrderTask;
    }


    /***
     * ????????????  ???????????????????????????
     * @param taskExample
     */
    @Transactional
    public void deliveryStock(WareOrderTask taskExample) {
        String trackingNo = taskExample.getTrackingNo();
        WareOrderTask wareOrderTask = getWareOrderTask(taskExample.getId());
        wareOrderTask.setTaskStatus(TaskStatus.DELEVERED.name());
        List<WareOrderTaskDetail> details = wareOrderTask.getDetails();
        for (WareOrderTaskDetail detail : details) {
            WareSku wareSku = new WareSku();
            wareSku.setWarehouseId(wareOrderTask.getWareId());
            wareSku.setSkuId(detail.getSkuId());
            wareSku.setStock(detail.getSkuNum());
            wareSkuMapper.deliveryStock(wareSku);
        }

        wareOrderTask.setTaskStatus(TaskStatus.DELEVERED.name());
        wareOrderTask.setTrackingNo(trackingNo);
        wareOrderTaskMapper.updateById(wareOrderTask);
        sendToOrder(wareOrderTask);

        sendToOrder(wareOrderTask);
    }


    public void sendToOrder(WareOrderTask wareOrderTask) {
//        Connection conn = activeMQUtil.getConn();
//
//        Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
//        Destination destination = session.createQueue("SKU_DELIVER_QUEUE");
//        MessageProducer producer = session.createProducer(destination);
//        MapMessage mapMessage = new ActiveMQMapMessage();
//        mapMessage.setString("orderId", wareOrderTask.getOrderId());
//        mapMessage.setString("status", wareOrderTask.getTaskStatus().toString()); //????????? ??????
//        mapMessage.setString("trackingNo", wareOrderTask.getTrackingNo());
//
//        producer.send(mapMessage);
//        session.commit();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<WareOrderTask> checkOrderSplit(WareOrderTask wareOrderTask) {
        List<WareOrderTaskDetail> details = wareOrderTask.getDetails();
        List<String> skulist = new ArrayList<>();
        for (WareOrderTaskDetail detail : details) {
            skulist.add(detail.getSkuId());
        }
        Map<String, List<String>> wareSkuMap = getWareSkuMap(skulist);
        if (wareSkuMap.size() == 1) {
            Map.Entry<String, List<String>> entry = wareSkuMap.entrySet().iterator().next();
            String wareid = entry.getKey();
            wareOrderTask.setWareId(wareid);
        } else {
            //????????????
            List<Map<String, Object>> wareSkuMapList = convertWareSkuMapList(wareSkuMap);
            String jsonString = JSON.toJSONString(wareSkuMapList);
            Map<String, String> map = new HashMap<>();
            map.put("orderId", wareOrderTask.getOrderId());
            map.put("wareSkuMap", jsonString);
            // http://order.gmall.com/orderSplit?orderId=xxx&wareSkuMap=xxx
            String resultJson = HttpclientUtil.doPost(ORDER_URL, map);
            List<WareOrderTask> wareOrderTaskList = JSON.parseArray(resultJson, WareOrderTask.class);
            if (wareOrderTaskList.size() >= 2) {
                for (WareOrderTask subOrderTask : wareOrderTaskList) {
                    subOrderTask.setTaskStatus(TaskStatus.DEDUCTED.name());
                    saveWareOrderTask(subOrderTask);
                }
                updateStatusWareOrderTaskByOrderId(wareOrderTask.getOrderId(), TaskStatus.SPLIT);
                return wareOrderTaskList;
            } else {
                throw new RuntimeException("????????????!!");
            }
        }
        return null;
    }


    public WareOrderTask saveWareOrderTask(WareOrderTask wareOrderTask) {
        wareOrderTask.setCreateTime(new Date());

        QueryWrapper<WareOrderTask> queryWrapper = new QueryWrapper();
        queryWrapper.in("order_id", wareOrderTask.getOrderId());
        WareOrderTask wareOrderTaskOrigin = wareOrderTaskMapper.selectOne(queryWrapper);
        if (wareOrderTaskOrigin != null) {
            return wareOrderTaskOrigin;
        }

        wareOrderTaskMapper.insert(wareOrderTask);

        List<WareOrderTaskDetail> wareOrderTaskDetails = wareOrderTask.getDetails();
        for (WareOrderTaskDetail wareOrderTaskDetail : wareOrderTaskDetails) {
            wareOrderTaskDetail.setTaskId(wareOrderTask.getId());
            wareOrderTaskDetailMapper.insert(wareOrderTaskDetail);
        }
        return wareOrderTask;

    }

    public void updateStatusWareOrderTaskByOrderId(String orderId, TaskStatus taskStatus) {
        QueryWrapper<WareOrderTask> queryWrapper = new QueryWrapper();
        queryWrapper.in("order_id", orderId);
        WareOrderTask wareOrderTask = new WareOrderTask();
        wareOrderTask.setTaskStatus(taskStatus.name());
        wareOrderTaskMapper.update(wareOrderTask, queryWrapper);
    }

    public void sendSkuDeductMQ(WareOrderTask wareOrderTask) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", wareOrderTask.getOrderId());
        map.put("status", wareOrderTask.getTaskStatus().toString());
        this.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_ORDER, MqConst.ROUTING_WARE_ORDER, JSON.toJSONString(map));
    }

    @Transactional
    public void lockStock(WareOrderTask wareOrderTask) {
        List<WareOrderTaskDetail> wareOrderTaskDetails = wareOrderTask.getDetails();
        String comment = "";
        for (WareOrderTaskDetail wareOrderTaskDetail : wareOrderTaskDetails) {

            WareSku wareSku = new WareSku();
            wareSku.setWarehouseId(wareOrderTask.getWareId());
            wareSku.setStockLocked(wareOrderTaskDetail.getSkuNum());
            wareSku.setSkuId(wareOrderTaskDetail.getSkuId());

            int availableStock = wareSkuMapper.selectStockBySkuidForUpdate(wareSku); //?????????????????? ??????????????? ????????????????????????
            if (availableStock - wareOrderTaskDetail.getSkuNum() < 0) {
                comment += "???????????????????????????" + wareOrderTaskDetail.getSkuName() + "????????????????????????" + availableStock + ",????????????" + wareOrderTaskDetail.getSkuNum();
            }
        }

        if (comment.length() > 0) {   //???????????? ?????????????????????????????????
            wareOrderTask.setTaskComment(comment);
            wareOrderTask.setTaskStatus(TaskStatus.OUT_OF_STOCK.name());
            updateStatusWareOrderTaskByOrderId(wareOrderTask.getOrderId(), TaskStatus.OUT_OF_STOCK);

        } else {   //????????????  ???????????????
            for (WareOrderTaskDetail wareOrderTaskDetail : wareOrderTaskDetails) {

                WareSku wareSku = new WareSku();
                wareSku.setWarehouseId(wareOrderTask.getWareId());
                wareSku.setStockLocked(wareOrderTaskDetail.getSkuNum());
                wareSku.setSkuId(wareOrderTaskDetail.getSkuId());

                wareSkuMapper.incrStockLocked(wareSku); //  ??????????????? ????????????????????????

            }
            wareOrderTask.setTaskStatus(TaskStatus.DEDUCTED.name());
            updateStatusWareOrderTaskByOrderId(wareOrderTask.getOrderId(), TaskStatus.DEDUCTED);
        }

        sendSkuDeductMQ(wareOrderTask);
        return;
    }

    public List<WareOrderTask> getWareOrderTaskList(WareOrderTask wareOrderTask) {
        List<WareOrderTask> wareOrderTasks = null;
        if (wareOrderTask == null) {
            wareOrderTasks = wareOrderTaskMapper.selectList(null);
        } else {
            wareOrderTasks = wareOrderTaskMapper.selectList(null);
        }
        return wareOrderTasks;
    }

    /**
     * ????????????
     *
     * @param exchange   ?????????
     * @param routingKey ?????????
     * @param message    ??????
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return true;
    }

}
