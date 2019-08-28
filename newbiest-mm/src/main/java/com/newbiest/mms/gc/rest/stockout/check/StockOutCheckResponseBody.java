package com.newbiest.mms.gc.rest.stockout.check;

import com.newbiest.mms.gc.model.StockOutCheck;
import com.newbiest.msg.ResponseBody;
import lombok.Data;

import java.util.List;


@Data
public class StockOutCheckResponseBody extends ResponseBody {

	private static final long serialVersionUID = 1L;

	private List<StockOutCheck> stockOutCheckList;

}
