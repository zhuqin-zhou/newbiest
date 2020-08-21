package com.newbiest.gc.rest.stockout.wltStockout;

import com.newbiest.base.exception.ClientException;
import com.newbiest.gc.rest.stockout.StockOutRequest;
import com.newbiest.gc.rest.stockout.StockOutRequestBody;
import com.newbiest.gc.rest.stockout.StockOutResponse;
import com.newbiest.gc.rest.stockout.StockOutResponseBody;
import com.newbiest.gc.service.GcService;
import com.newbiest.mms.dto.MaterialLotAction;
import com.newbiest.mms.model.MaterialLot;
import com.newbiest.msg.Request;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gc")
@Slf4j
@Api(value="/gc", tags="gc客制化接口", description = "GalaxyCore客制化接口")
public class WltStockOutController {

    @Autowired
    GcService gcService;

    @ApiOperation(value = "WltStockOut", notes = "Wlt/CP发货")
    @ApiImplicitParam(name="request", value="request", required = true, dataType = "WltStockOutRequest")
    @RequestMapping(value = "/wltStockOut", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public WltStockOutResponse execute(@RequestBody WltStockOutRequest request) throws Exception {
        WltStockOutResponse response = new WltStockOutResponse();
        response.getHeader().setTransactionId(request.getHeader().getTransactionId());

        WltStockOutResponseBody responseBody = new WltStockOutResponseBody();
        WltStockOutRequestBody requestBody = request.getBody();
        String actionType = requestBody.getActionType();

        if (WltStockOutRequest.ACTION_WLTSTOCKOUT.equals(actionType)) {
            gcService.wltStockOut(requestBody.getDocumentLine(), requestBody.getMaterialLotActions());
        } else if(WltStockOutRequest.ACTION_VALIDATION_WLTMLOT.equals(actionType)){
            boolean falg = gcService.validationWltStockOutMaterialLot(requestBody.getQueryMaterialLot(), requestBody.getMaterialLotActions());
            responseBody.setFalg(falg);
        } else {
            throw new ClientException(Request.NON_SUPPORT_ACTION_TYPE + requestBody.getActionType());
        }

        response.setBody(responseBody);
        return response;
    }
}