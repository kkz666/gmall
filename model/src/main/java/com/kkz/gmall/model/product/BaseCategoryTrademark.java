//
//
package com.kkz.gmall.model.product;

import com.kkz.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * BaseCategoryView
 * </p>
 *
 */
@Data
@ApiModel(description = "BaseCategoryTrademark")
@TableName("base_category_trademark")
public class BaseCategoryTrademark extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "三级分类编号")
	@TableField("category3_id")
	private Long category3Id;

	@ApiModelProperty(value = "品牌id")
	@TableField("trademark_id")
	private Long trademarkId;

	@TableField(exist = false)
	private BaseTrademark baseTrademark;
}

