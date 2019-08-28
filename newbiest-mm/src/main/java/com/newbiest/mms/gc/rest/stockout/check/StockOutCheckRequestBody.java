package com.newbiest.mms.gc.rest.stockout.check;

import com.newbiest.mms.model.MaterialLot;
import com.newbiest.msg.RequestBody;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel("具体请求操作信息")
public class StockOutCheckRequestBody extends RequestBody {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "操作类型", example = "GetCheckList/Judge")
	private String actionType;

	@ApiModelProperty(value = "物料批次")
	private MaterialLot materialLot;

	@ApiModelProperty(value = "检查结果")
	private String checkResult;
}
