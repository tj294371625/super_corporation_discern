package com.chinadaas.task.impl;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.constant.TargetType;
import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.entity.TwoNodesEntity;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.NodeOperationService;
import com.chinadaas.task.FullTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 基于处理数据，执行业务逻辑
 * @createTime 2021.07.28
 */
@Slf4j
@Order(2)
@Component
public class ProcessTask implements FullTask {

    private final Executor parallelExecutor;
    private final EntIdListLoader entIdListLoader;
    private final ChainOperationService chainOperationService;
    private final NodeOperationService nodeOperationService;

    @Autowired
    public ProcessTask(Executor parallelExecutor,
                       EntIdListLoader entIdListLoader,
                       ChainOperationService chainOperationService,
                       NodeOperationService nodeOperationService) {

        this.parallelExecutor = parallelExecutor;
        this.entIdListLoader = entIdListLoader;
        this.chainOperationService = chainOperationService;
        this.nodeOperationService = nodeOperationService;
    }

    private ParentToSourceCalTask parentToSourceCalTask;
    private CtrlCalTask ctrlCalTask;

    @PostConstruct
    public void init() {
        this.parentToSourceCalTask = new ParentToSourceCalTask();
        this.ctrlCalTask = new CtrlCalTask();
    }

    @Override
    public void run() {
        Set<String> allEntIds = chainOperationService.fullSourceEntId();

        final Consumer<String> processTask = (entId) -> {
//            parentToSourceCalTask.cal()
        };

        parallelExecutor.execute("", processTask);
    }

    private class ParentToSourceCalTask {

        public SuperCorporationModel cal(SuperCorporationModel superCorporationModel) {
            String currentQueryId = superCorporationModel.getCurrentQueryId();

            ChainEntity chainEntity = chainOperationService.chainQuery(currentQueryId, ModelType.PARENT);

            String sourceEntId = chainEntity.getSourceEntId();
            String targetEntId = chainEntity.getTargetEntId();
            String targetType = chainEntity.getTargetType();
            long parent2SourceLayer = chainEntity.getTarget2SourceLayer();
            List<TwoNodesEntity> twoNodesUseInvList;
            List<TwoNodesEntity> twoNodesUseGroupParentList = Lists.newArrayList();

            if (ChainConst.UNKNOWN_ID.equals(targetEntId)
                    || TargetType.NON_EXIST.toString().equals(targetType)) {
                return superCorporationModel;
            }

            if (TargetType.DISCLOSURE.toString().equals(targetType)) {
                // todo: 上市披露逻辑
            }

            twoNodesUseInvList = nodeOperationService.sourceToTargetUseInv(sourceEntId, targetEntId, parent2SourceLayer);
            superCorporationModel.setSourceProperty(Neo4jResultParseUtils.obtainSpecialNode(currentQueryId, twoNodesUseInvList));
            superCorporationModel.setParentProperty(Neo4jResultParseUtils.obtainSpecialNode(currentQueryId, twoNodesUseInvList));
            return superCorporationModel.calSourceToParent(twoNodesUseInvList, twoNodesUseGroupParentList);
        }
    }

    private class CtrlCalTask {

        public SuperCorporationModel cal(SuperCorporationModel superCorporationModel) {
            String currentQueryId = superCorporationModel.getCurrentQueryId();

            ChainEntity parentEntity = chainOperationService.chainQuery(currentQueryId, ModelType.PARENT);
            ChainEntity finCtrlEntity = chainOperationService.chainQuery(currentQueryId, ModelType.FIN_CTRL);

            String parentId = parentEntity.getTargetEntId();
            String finCtrlId = finCtrlEntity.getTargetEntId();
            long parent2SourceLayer = parentEntity.getTarget2SourceLayer();
            long ctrlToSourceLayer = finCtrlEntity.getTarget2SourceLayer();
            String parentType = parentEntity.getTargetType();
            String ctrlType = finCtrlEntity.getTargetType();

            // zs: 1.不存在母公司，但存在控股股东
            if (ChainConst.UNKNOWN_ID.equals(parentId)
                    && !ChainConst.UNKNOWN_ID.equals(finCtrlId)) {

                List<TwoNodesEntity> controlPathList;
                if (TargetType.ENT.toString().equals(ctrlType)) {
                    controlPathList = nodeOperationService.sourceToTargetUseInv(currentQueryId, finCtrlId, ctrlToSourceLayer);
                } else {
                    controlPathList = nodeOperationService.sourceToPersonUseInv(currentQueryId, finCtrlId, ctrlToSourceLayer);
                }

                superCorporationModel.setFinCtrlProperty(Neo4jResultParseUtils.obtainSpecialNode(finCtrlId, controlPathList));
                return superCorporationModel.sourceToControlNoParent(controlPathList);
            }

            // zs: 2.不存在最终控股股东，但存在母公司，将母公司作为最终控股股东
            if (ChainConst.UNKNOWN_ID.equals(finCtrlId)
                    && !ChainConst.UNKNOWN_ID.equals(parentId)) {

                final Function<LinkWrapper, LinkWrapper> replaceGroupParentLink = (groupParentLink) -> {
                    long fromId = groupParentLink.getFrom();
                    long toId = groupParentLink.getTo();
                    return nodeOperationService.groupParentMappingTenInvMerge(fromId, toId);
                };

                return superCorporationModel.parentReplaceControl(replaceGroupParentLink);
            }

            // zs: 3.存在母公司和最终控股股东
            if (ChainConst.UNKNOWN_ID.equals(finCtrlId)
                    && ChainConst.UNKNOWN_ID.equals(parentId)) {

                List<TwoNodesEntity> parentToControlPathList;
                if (TargetType.ENT.toString().equals(ctrlType)) {
                    parentToControlPathList = nodeOperationService.sourceToTargetUseInv(parentId, finCtrlId, ctrlToSourceLayer);
                } else {
                    parentToControlPathList = nodeOperationService.sourceToPersonUseInv(parentId, finCtrlId, ctrlToSourceLayer);
                }

                /*
                 * 当母公司不是通过上市披露找到的，则直接通过teninvmerge关系查询输入企业到最终控股股东的path
                 * 当母公司是通过上市披露找到的，则输入企业到最终控股股东的path需要merge
                 * */
                Long sourceToControlLayer = parent2SourceLayer + ctrlToSourceLayer;
                List<TwoNodesEntity> sourceToControlPathList = Lists.newArrayList();
                if (!TargetType.DISCLOSURE.toString().equals(parentType)) {
                    if (TargetType.ENT.toString().equals(ctrlType)) {
                        sourceToControlPathList = nodeOperationService.sourceToTargetUseInv(currentQueryId, finCtrlId, sourceToControlLayer);
                    } else {
                        sourceToControlPathList = nodeOperationService.sourceToPersonUseInv(currentQueryId, finCtrlId, sourceToControlLayer);
                    }
                }

                superCorporationModel.setFinCtrlProperty(Neo4jResultParseUtils.obtainSpecialNode(finCtrlId, parentToControlPathList));
                return superCorporationModel.sourceToControlHaveParent(sourceToControlPathList, parentToControlPathList, parentType);
            }

            // zs: 不存在母公司和最终控股股东
            return superCorporationModel;
        }
    }

}
