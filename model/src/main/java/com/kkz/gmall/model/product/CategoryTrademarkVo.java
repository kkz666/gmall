//
//
package com.kkz.gmall.model.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * BaseCategoryView
 * </p>
 *
 */
@Data
public class CategoryTrademarkVo {
	
	@ApiModelProperty(value = "三级分类编号")
	private Long category3Id;

	@ApiModelProperty(value = "品牌id")
	private List<Long> trademarkIdList;


}

