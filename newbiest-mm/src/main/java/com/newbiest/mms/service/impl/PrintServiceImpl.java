package com.newbiest.mms.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.newbiest.base.annotation.BaseJpaFilter;
import com.newbiest.base.exception.ClientException;
import com.newbiest.base.exception.ClientParameterException;
import com.newbiest.base.exception.ExceptionManager;
import com.newbiest.base.utils.*;
import com.newbiest.mms.exception.MmsException;
import com.newbiest.mms.model.*;
import com.newbiest.mms.print.DefaultPrintStrategy;
import com.newbiest.mms.print.IPrintStrategy;
import com.newbiest.mms.print.PrintContext;
import com.newbiest.mms.repository.*;
import com.newbiest.mms.service.MaterialLotUnitService;
import com.newbiest.mms.service.MmsService;
import com.newbiest.mms.service.PackageService;
import com.newbiest.mms.service.PrintService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoxunbo
 * @date 5/22/21
 */
@Service
@Slf4j
@Transactional
@BaseJpaFilter
@Data
@EnableAsync
public class PrintServiceImpl implements PrintService {

    @Autowired
    Map<String, IPrintStrategy> printStrategyMap;

    @Autowired
    WorkStationRepository workStationRepository;

    @Autowired
    LabelTemplateRepository labelTemplateRepository;

    @Autowired
    LabelTemplateParameterRepository labelTemplateParameterRepository;

    @Autowired
    MaterialRepository materialRepository;

    @Autowired
    MmsService mmsService;

    @Autowired
    DocumentLineRepository documentLineRepository;

    @Autowired
    MaterialLotRepository materialLotRepository;

    @Autowired
    MaterialLotUnitService materialLotUnitService;

    @Autowired
    PackageService packageService;

    public void print(PrintContext printContext) {
        print(DefaultPrintStrategy.DEFAULT_STRATEGY_NAME, printContext);
    }

    public void print(String strategyName, PrintContext printContext) {
        IPrintStrategy printStrategy = printStrategyMap.get(strategyName);
        if (printStrategy == null) {
            printStrategy = printStrategyMap.get(DefaultPrintStrategy.DEFAULT_STRATEGY_NAME);
        }
        if (log.isDebugEnabled()) {
            log.debug("Use context [" + printContext.toString() + "] to print");
        }
        printStrategy.print(printContext);
    }

    /**
     * 构建打印参数
     * @param labelTemplateName
     * @return
     * @throws ClientException
     */
    private PrintContext buildPrintContext(String labelTemplateName, String printCount) throws ClientException{
        try {
            WorkStation workStation = workStationRepository.findByIpAddress("localhost");
            if (workStation == null) {
                throw new ClientParameterException(MmsException.MM_WORK_STATION_IS_NOT_EXIST, "localhost");
            }
            LabelTemplate labelTemplate = labelTemplateRepository.findByName(labelTemplateName);
            if (labelTemplate == null) {
                throw new ClientParameterException(MmsException.MM_LBL_TEMPLATE_IS_NOT_EXIST, labelTemplateName);
            }
            if(!StringUtils.isNullOrEmpty(printCount)){
                labelTemplate.setPrintCount(Integer.parseInt(printCount));
            }
            List<LabelTemplateParameter> parameterList = labelTemplateParameterRepository.findByLblTemplateRrn(labelTemplate.getObjectRrn());
            labelTemplate.setLabelTemplateParameterList(parameterList);

            PrintContext printContext = new PrintContext();
            printContext.setLabelTemplate(labelTemplate);
            printContext.setWorkStation(workStation);
            return printContext;
        }catch (Exception e){
            throw ExceptionManager.handleException(e, log);
        }
    }

    @Override
    public void printReceiveWltCpLotLabel(List<MaterialLot> materialLotList, String printCount) throws ClientException {
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_WLT_CP_BOX_LABEL, printCount);
            for(MaterialLot materialLot : materialLotList){
                Map<String, Object> parameterMap = buildWltCpPrintParameter(materialLot);
                printContext.setBaseObject(materialLot);
                printContext.setParameterMap(parameterMap);
                print(printContext);
            }
        } catch (Exception e){
            throw ExceptionManager.handleException(e, log);
        }
    }

    private Map<String,Object> buildWltCpPrintParameter(MaterialLot materialLot) throws ClientException{
        try {
            Map<String, Object> parameterMap = Maps.newHashMap();
            parameterMap.put("DEVICEID", materialLot.getMaterialName());
            parameterMap.put("QTY", materialLot.getCurrentQty().toString());
            parameterMap.put("WAFERGRADE", materialLot.getGrade());
            parameterMap.put("LOCATION", materialLot.getReserved6());
            parameterMap.put("SUBCODE", materialLot.getReserved1());
            List<MaterialLotUnit> materialLotUnitList = materialLotUnitService.getUnitsByMaterialLotId(materialLot.getMaterialLotId());

            if(CollectionUtils.isNotEmpty(materialLotUnitList)){
                Integer waferQty = materialLotUnitList.size();
                parameterMap.put("WAFERQTY", waferQty.toString());
                String waferIdList1 = "";
                String waferIdList2 = "";

                for(int j = 0; j <  materialLotUnitList.size() ; j++){
                    String[] waferIdList = materialLotUnitList.get(j).getUnitId().split(StringUtils.SPLIT_CODE);
                    String waferSeq = waferIdList[1] + ",";
                    if(j < 8){
                        waferIdList1 = waferIdList1 + waferSeq;
                    } else {
                        waferIdList2 = waferIdList2 + waferSeq;
                    }
                }
                if(!StringUtils.isNullOrEmpty(waferIdList1)){
                    parameterMap.put("WAFERID1", waferIdList1);
                } else {
                    parameterMap.put("WAFERID1", StringUtils.EMPTY);
                }
                if(!StringUtils.isNullOrEmpty(waferIdList2)){
                    parameterMap.put("WAFERID2", waferIdList2);
                } else {
                    parameterMap.put("WAFERID2", StringUtils.EMPTY);
                }
            }
            return parameterMap;
        } catch (Exception e){
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * 打印WLT或者CP批次标签（产线接收入库）
     * @param materialLot
     * @throws ClientException
     */
    @Override
    public void printWltOrCpLabel(MaterialLot materialLot, String printCount) throws ClientException {
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_WLT_CP_BOX_LABEL, printCount);
            Map<String, Object> parameterMap = buildWltCpPrintParameter(materialLot);
            printContext.setBaseObject(materialLot);
            printContext.setParameterMap(parameterMap);
            print(printContext);
        } catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * 斜标签补打
     * @param materialLotList
     * @throws ClientException
     */
    public void printMaterialLotObliqueBoxLabel(List<MaterialLot> materialLotList, String expressNumber) throws ClientException{
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_OBLIQUE_BOX_LABEL, "");
            List<MaterialLot> expressNumberInfoList = materialLotList.stream().filter(materialLot -> StringUtils.isNullOrEmpty(materialLot.getExpressNumber())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(expressNumberInfoList)){
                throw new ClientParameterException(MmsException.MATERIAL_LOT_NOT_RECORD_EXPRESS, expressNumberInfoList.get(0).getMaterialLotId());
            }

            Integer seq = 1;
            Integer numfix = materialLotList.size();
            //按照称重的先后排序打印标签
            List<MaterialLot> materialLots = Lists.newArrayList();
            List<MaterialLot> mLotList = Lists.newArrayList();
            for(MaterialLot materialLot : materialLotList){
                if(StringUtils.isNullOrEmpty(materialLot.getWeightSeq())){
                    materialLots.add(materialLot);
                } else {
                    mLotList.add(materialLot);
                }
            }
            if(CollectionUtils.isNotEmpty(mLotList)){
                mLotList = mLotList.stream().sorted(Comparator.comparing(MaterialLot::getWeightSeq)).collect(Collectors.toList());
                materialLots.addAll(mLotList);
            }
            for (MaterialLot materialLot : materialLots){
                Map<String, Object> parameterMap =  Maps.newHashMap();
                if (StringUtils.isNullOrEmpty(materialLot.getReserved18())){
                    parameterMap.put("CSNAME", materialLot.getShipper());
                }else{
                    parameterMap.put("CSNAME", materialLot.getReserved18());
                }
                parameterMap.put("NUMCHANG", seq.toString());
                parameterMap.put("NUMFIX", numfix.toString());
                if(StringUtils.isNullOrEmpty(expressNumber)){
                    parameterMap.put("EXNUM", materialLot.getExpressNumber());
                }else {
                    parameterMap.put("EXNUM", expressNumber);
                }
                ++seq;
                printContext.setParameterMap(parameterMap);
                print(printContext);
            }

        } catch (Exception e){
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * RW的CST标签打印
     * @param materialLotList
     * @param printCount
     * @throws ClientException
     */
    public void printRwLotCstLabel(List<MaterialLot> materialLotList, String printCount) throws ClientException{
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_RW_LOT_CST_LABEL, printCount);
            for(MaterialLot materialLot : materialLotList){
                Map<String, Object> parameterMap = buildRwLotCsttPrintParameter(materialLot);
                printContext.setBaseObject(materialLot);
                printContext.setParameterMap(parameterMap);
                print(printContext);
            }
        } catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * 获取RW完成品接收打印参数
     * @param materialLot
     */
    private Map<String, Object> buildRwLotCsttPrintParameter(MaterialLot materialLot) throws ClientException{
        try {
            Map<String, Object> parameterMap = Maps.newHashMap();
            List<MaterialLotUnit> materialLotUnitList = materialLotUnitService.getUnitsByMaterialLotId(materialLot.getMaterialLotId());
            String productId = materialLot.getMaterialName();
            parameterMap.put("PRODUCTID", productId.substring(0, productId.lastIndexOf("-")));
            parameterMap.put("CSTID", materialLot.getLotId());
            parameterMap.put("WAFERQTY", materialLot.getCurrentSubQty().toString());
            parameterMap.put("LOCATION", materialLot.getReserved6());
            parameterMap.put("SUBCODE", materialLot.getReserved1());
            parameterMap.put("LOIID", materialLot.getLotId());
            parameterMap.put("LOTCST", materialLot.getLotCst());
            parameterMap.put("PCODE", materialLot.getPcode());
            parameterMap.put("QTY", materialLot.getCurrentQty().toString());
            int i = 1;
            for(MaterialLotUnit materialLotUnit : materialLotUnitList){
                parameterMap.put("frameID" + i, materialLotUnit.getUnitId());
                parameterMap.put("chipQty" + i, materialLotUnit.getCurrentQty().toString());
                i++;
            }
            for (int j = i; j <= 13; j++) {
                parameterMap.put("frameID" + j, StringUtils.EMPTY);
                parameterMap.put("chipQty" + j, StringUtils.EMPTY);
            }
            return parameterMap;
        } catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * RW的LotCst标签补打
     * @param materialLot
     * @param printCount
     * @throws ClientException
     */
    @Override
    public void rePrintRwLotCstLabel(MaterialLot materialLot, String printCount) throws ClientException {
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_RW_LOT_CST_LABEL, printCount);
            Map<String, Object> parameterMap = buildRwLotCsttPrintParameter(materialLot);
            printContext.setBaseObject(materialLot);
            printContext.setParameterMap(parameterMap);
            print(printContext);
        }catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }

    /**
     * 打印COB装箱标签(一箱只有一包)
     * @param materialLot
     * @throws ClientException
     */
    @Override
    public void printCobBoxLabel(MaterialLot materialLot) throws ClientException {
        try {
            PrintContext printContext = buildPrintContext(LabelTemplate.PRINT_COB_BOX_LABEL, "");
            materialLot = mmsService.getMLotByMLotId(materialLot.getMaterialLotId());
            Map<String, Object> parameterMap = Maps.newHashMap();
            parameterMap.put("BOXID", materialLot.getMaterialLotId());
            parameterMap.put("SUBCODE", materialLot.getReserved1());
            parameterMap.put("LOCATION", materialLot.getReserved6());
            parameterMap.put("DEVICEID", materialLot.getMaterialName());
            parameterMap.put("CHIPNUM", materialLot.getCurrentQty().toPlainString());

            List<MaterialLot> packageDetailLots = packageService.getPackageDetailLots(materialLot.getObjectRrn());
            if(CollectionUtils.isNotEmpty(packageDetailLots)){
                //COB箱号，一箱只装一个真空包
                MaterialLot packedLot = packageDetailLots.get(0);
                parameterMap.put("CSTID", packedLot.getLotId());
                parameterMap.put("FRAMEQTY", packedLot.getCurrentSubQty().toPlainString());

                List<MaterialLotUnit> materialLotUnitList = materialLotUnitService.getUnitsByMaterialLotId(packedLot.getMaterialLotId());

                if(CollectionUtils.isNotEmpty(materialLotUnitList) && materialLotUnitList.size() > 13){
                    throw new ClientParameterException(MmsException.MATERIALLOT_WAFER_QTY_MORE_THAN_THIRTEEN, materialLot.getMaterialLotId());
                }

                int i = 1;
                if (CollectionUtils.isNotEmpty(materialLotUnitList)){
                    for(MaterialLotUnit materialLotUnit : materialLotUnitList){
                        parameterMap.put("FRAMEID" + i, materialLotUnit.getUnitId());
                        parameterMap.put("CHIPQTY" + i, materialLotUnit.getCurrentQty().toPlainString());
                        i++;
                    }
                }

                for (int j = i; j <= 13; j++) {
                    parameterMap.put("FRAMEID" + j, StringUtils.EMPTY);
                    parameterMap.put("CHIPQTY" + j, StringUtils.EMPTY);
                }
            } else {
                throw new ClientParameterException(MmsException.MATERIALLOT_PACKED_DETIAL_IS_NULL, materialLot.getMaterialLotId());
            }
            printContext.setBaseObject(materialLot);
            printContext.setParameterMap(parameterMap);
            print(printContext);
        } catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }
}