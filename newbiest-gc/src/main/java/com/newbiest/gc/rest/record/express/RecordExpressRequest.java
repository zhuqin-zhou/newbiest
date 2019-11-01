package com.newbiest.gc.rest.record.express;

import com.newbiest.msg.Request;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class RecordExpressRequest extends Request {

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_NAME = "RecordExpressNumber";

	private RecordExpressRequestBody body;

}
