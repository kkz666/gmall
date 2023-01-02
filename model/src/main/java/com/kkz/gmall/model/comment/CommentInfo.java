package com.kkz.gmall.model.comment;

import com.kkz.gmall.model.base.BaseMongoEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * CommentInfo
 */
@Data
@ApiModel(description = "商品评论")
@Document("商品评论")
public class CommentInfo extends BaseMongoEntity {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "用户名称")
	@TableField("user_id")
	private Long userId;

	@ApiModelProperty(value = "用户昵称")
	@TableField("nick_name")
	private String nickName;

	@ApiModelProperty(value = "用户头像")
	@TableField("head_img")
	private String headImg;

	@ApiModelProperty(value = "skuid")
	@TableField("sku_id")
	private Long skuId;

	@ApiModelProperty(value = "商品id")
	@TableField("spu_id")
	private Long spuId;

	@ApiModelProperty(value = "订单编号")
	@TableField("order_id")
	private Long orderId;

	@ApiModelProperty(value = "商品评分")
	@TableField("appraise")
	private Integer appraise;

	@ApiModelProperty(value = "评价内容")
	@TableField("comment_txt")
	private String commentTxt;

}

