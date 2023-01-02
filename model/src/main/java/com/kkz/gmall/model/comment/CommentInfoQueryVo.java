package com.kkz.gmall.model.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(description = "CommentInfo")
public class CommentInfoQueryVo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "用户名称")
	private Long userId;

	@ApiModelProperty(value = "skuid")
	private Long skuId;

	@ApiModelProperty(value = "商品id")
	private Long spuId;

	@ApiModelProperty(value = "订单编号")
	private Long orderId;

	@ApiModelProperty(value = "评价 1 好评 2 中评 3 差评")
	private Integer appraise;

	@ApiModelProperty(value = "评价内容")
	private String commentTxt;

	@ApiModelProperty(value = "创建时间")
	private Date createTime;

	@ApiModelProperty(value = "修改时间")
	private Date operateTime;

}

