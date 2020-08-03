package com.newbiest.gc.rest.async;

import com.newbiest.msg.Request;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class GcAsyncRequest extends Request {

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_NAME = "AsyncManager";

	public static final String ACTION_ASYNC_RETEST_ISSUE_ORDER = "AsyncReTestIssueOrder";
	public static final String ACTION_ASYNC_WAFER_ISSUE_ORDER = "AsyncWaferIssueOrder";

	public static final String ACTION_ASYNC_RECEIVE_ORDER = "AsyncReceiveOrder";
	public static final String ACTION_ASYNC_SHIP_ORDER = "AsyncShipOrder";

	//TODO 暂时根据同步的表定义得名字  后续自行根据业务需求修改
	public static final String ACTION_ASYNC_MOUTA_ORDER = "AsyncMoutaOrder"; //同步ETM_MATERIAL_OUTA
	public static final String ACTION_ASYNC_SOA_ORDER = "AsyncSoaOrder"; //同步ETM_SOA
	public static final String ACTION_ASYNC_SOB_ORDER = "AsyncSobOrder"; //同步ETM_SOB

	public static final String ACTION_ASYNC_MATERIAL = "AsyncMaterial";
	public static final String ACTION_ASYNC_PRODUCT = "AsyncProduct";
	public static final String ACTION_ASYNC_WAFERTYPE = "AsyncWaferType";
	public static final String ACTION_ASYNC_PRODUCTSUBCODE = "AsyncProductSubcode";

	private GcAsyncRequestBody body;

}
