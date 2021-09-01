package com.chinadaas.task.subtask;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.constant.TargetType;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.Neo4jResultParseUtils;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.entity.TwoNodesEntity;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.NodeOperationService;
import com.chinadaas.service.SuperCorporationService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.28
 */
@Slf4j
@Component
public class SuperCorporationProcessTask {

    private final Executor parallelExecutor;
    private final RecordHandler recordHandler;
    private final ChainOperationService chainOperationService;
    private final NodeOperationService nodeOperationService;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public SuperCorporationProcessTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                                       RecordHandler recordHandler,
                                       ChainOperationService chainOperationService,
                                       NodeOperationService nodeOperationService,
                                       SuperCorporationService superCorporationService) {

        this.parallelExecutor = parallelExecutor;
        this.recordHandler = recordHandler;
        this.chainOperationService = chainOperationService;
        this.nodeOperationService = nodeOperationService;
        this.superCorporationService = superCorporationService;
    }

    public void generateSuperCorporation() {
        log.info("generate super corporation task start run...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> processTask = (entId) -> {
            SuperCorporationModel superCorporationModel = new SuperCorporationModel(entId);
            calPartOfParent(superCorporationModel);
            calPartOfCtrl(superCorporationModel);
            dataStorage(superCorporationModel);
        };

        parallelExecutor.execute("generate super corporation task", processTask);

        log.info("end the generate super corporation task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    private void calPartOfParent(SuperCorporationModel superCorporationModel) {
        String currentQueryId = superCorporationModel.getCurrentQueryId();

        ChainEntity chainEntity = chainOperationService.chainQuery(currentQueryId, ModelType.PARENT);
        if (Objects.isNull(chainEntity)) {
            return;
        }

        String targetEntId = chainEntity.getTargetEntId();
        String targetType = chainEntity.getTargetType();
        long parent2SourceLayer = chainEntity.getTarget2SourceLayer();
        List<TwoNodesEntity> twoNodesUseInvList;
        List<TwoNodesEntity> twoNodesUseGroupParentList = Lists.newArrayList();

        // zs: 不存在母公司
        if (ChainConst.UNKNOWN_ID.equals(targetEntId)
                || TargetType.NON_EXIST.toString().equals(targetType)) {

            return;
        }

        // zs: 上市披露
        if (TargetType.DISCLOSURE.toString().equals(targetType)) {
            String middleEntId = chainOperationService.obtainEntBeforeDisclosure(currentQueryId, targetEntId);
            twoNodesUseInvList = nodeOperationService.sourceToTargetUseInv(currentQueryId, middleEntId, parent2SourceLayer - 1);
            twoNodesUseGroupParentList = nodeOperationService.sourceToTargetUseGroupParent(middleEntId, targetEntId);
            superCorporationModel.setSourceProperty(Neo4jResultParseUtils.obtainSpecialNode(currentQueryId, twoNodesUseInvList));
            superCorporationModel.setParentProperty(Neo4jResultParseUtils.obtainSpecialNode(targetEntId, twoNodesUseGroupParentList));
            superCorporationModel.calSourceToParent(twoNodesUseInvList, twoNodesUseGroupParentList, targetEntId);

            return;
        }

        // zs: 决策权&单一大股东
        twoNodesUseInvList = nodeOperationService.sourceToTargetUseInv(currentQueryId, targetEntId, parent2SourceLayer);
        superCorporationModel.setSourceProperty(Neo4jResultParseUtils.obtainSpecialNode(currentQueryId, twoNodesUseInvList));
        superCorporationModel.setParentProperty(Neo4jResultParseUtils.obtainSpecialNode(targetEntId, twoNodesUseInvList));
        superCorporationModel.calSourceToParent(twoNodesUseInvList, twoNodesUseGroupParentList, targetEntId);
    }

    private void calPartOfCtrl(SuperCorporationModel superCorporationModel) {
        String currentQueryId = superCorporationModel.getCurrentQueryId();

        ChainEntity parentEntity = chainOperationService.chainQuery(currentQueryId, ModelType.PARENT);
        if (Objects.isNull(parentEntity)) {
            return;
        }
        String parentId = parentEntity.getTargetEntId();

        // zs: 最终控股股东两种情况
        ChainEntity finCtrlEntity;
        if (ChainConst.UNKNOWN_ID.equals(parentId)) {
            finCtrlEntity = chainOperationService.chainQuery(currentQueryId, ModelType.FIN_CTRL);
        } else {
            finCtrlEntity = chainOperationService.chainQuery(parentId, ModelType.FIN_CTRL);
        }
        if (Objects.isNull(finCtrlEntity)) {
            return;
        }

        String finCtrlId = finCtrlEntity.getTargetEntId();
        long parent2SourceLayer = parentEntity.getTarget2SourceLayer();
        long ctrl2SourceLayer = finCtrlEntity.getTarget2SourceLayer();
        String parentType = parentEntity.getTargetType();
        String ctrlType = finCtrlEntity.getTargetType();

        // zs: 1.不存在母公司，但存在控股股东
        if (ChainConst.UNKNOWN_ID.equals(parentId)
                && !ChainConst.UNKNOWN_ID.equals(finCtrlId)) {

            List<TwoNodesEntity> controlPathList;
            if (TargetType.ENT.toString().equals(ctrlType)) {
                controlPathList = nodeOperationService.sourceToTargetUseInv(currentQueryId, finCtrlId, ctrl2SourceLayer);
            } else {
                controlPathList = nodeOperationService.sourceToPersonUseInv(currentQueryId, finCtrlId, ctrl2SourceLayer);
            }

            superCorporationModel.setSourceProperty(Neo4jResultParseUtils.obtainSpecialNode(currentQueryId, controlPathList));
            superCorporationModel.setFinCtrlProperty(Neo4jResultParseUtils.obtainSpecialNode(finCtrlId, controlPathList));
            superCorporationModel.sourceToControlNoParent(controlPathList);
            return;
        }

        // zs: 2.不存在最终控股股东，但存在母公司，将母公司作为最终控股股东
        if (ChainConst.UNKNOWN_ID.equals(finCtrlId)
                && !ChainConst.UNKNOWN_ID.equals(parentId)) {

            final Function<LinkWrapper, LinkWrapper> replaceGroupParentLink = (groupParentLink) -> {
                long fromId = groupParentLink.getFrom();
                long toId = groupParentLink.getTo();
                return nodeOperationService.groupParentMappingTenInvMerge(fromId, toId);
            };

            superCorporationModel.parentReplaceControl(replaceGroupParentLink);
            return;
        }

        // zs: 3.存在母公司和最终控股股东
        if (!ChainConst.UNKNOWN_ID.equals(finCtrlId)
                && !ChainConst.UNKNOWN_ID.equals(parentId)) {

            List<TwoNodesEntity> parentToControlPathList;
            if (TargetType.ENT.toString().equals(ctrlType)) {
                parentToControlPathList = nodeOperationService.sourceToTargetUseInv(parentId, finCtrlId, ctrl2SourceLayer);
            } else {
                parentToControlPathList = nodeOperationService.sourceToPersonUseInv(parentId, finCtrlId, ctrl2SourceLayer);
            }

            /*
             * 当母公司不是通过上市披露找到的，则直接通过teninvmerge关系查询输入企业到最终控股股东的path
             * 当母公司是通过上市披露找到的，则输入企业到最终控股股东的path需要merge
             * */

            long sourceToControlLayer = parent2SourceLayer + ctrl2SourceLayer;
            List<TwoNodesEntity> sourceToControlPathList = Lists.newArrayList();
            if (!TargetType.DISCLOSURE.toString().equals(parentType)) {
                if (TargetType.ENT.toString().equals(ctrlType)) {
                    sourceToControlPathList = nodeOperationService.sourceToTargetUseInv(currentQueryId, finCtrlId, sourceToControlLayer);
                } else {
                    sourceToControlPathList = nodeOperationService.sourceToPersonUseInv(currentQueryId, finCtrlId, sourceToControlLayer);
                }
            }

            superCorporationModel.setFinCtrlProperty(Neo4jResultParseUtils.obtainSpecialNode(finCtrlId, parentToControlPathList));
            superCorporationModel.sourceToControlHaveParent(sourceToControlPathList, parentToControlPathList, parentType);
        }

        // zs: 4.不存在母公司和最终控股股东
    }

    private void dataStorage(SuperCorporationModel superCorporationModel) {
        if (ModelStatus.NO_RESULT.equals(superCorporationModel.getResultStatus())) {
            return;
        }
        SuperCorporationEntity superCorporationEntity = AssistantUtils.modelTransferToEntityOfSC(superCorporationModel);


        superCorporationService.insertSuperCorporation(superCorporationEntity);
        recordHandler.recordSuperCorporation(superCorporationEntity);
    }
}
