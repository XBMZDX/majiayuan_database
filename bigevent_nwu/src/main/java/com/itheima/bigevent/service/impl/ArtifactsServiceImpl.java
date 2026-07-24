package com.itheima.bigevent.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.bigevent.mapper.ArtifactsMapper;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.service.ArtifactDetectionStatusService;
import com.itheima.bigevent.service.ArtifactsService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArtifactsServiceImpl implements ArtifactsService {

    @Autowired
    private ArtifactsMapper artifactsMapper;
    @Autowired
    private ArtifactDetectionStatusService artifactDetectionStatusService;

    @PostConstruct
    public void clearLegacyManualTestingStatus() {
        artifactsMapper.clearManualTestingStatus();
    }

    /**
     * 新增文物 — 自动分配序号（填补空缺或末尾追加），事务保证原子性
     */
    @Override
    @Transactional
    public void add(artifacts artifact) {
        // 此字段不再允许人工维护，展示值统一由检测分析记录实时生成。
        artifact.setTestingStatus(null);
        // 设置创建时间和更新时间
        artifact.setCreateTime(LocalDateTime.now());
        artifact.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        artifact.setCreatedBy(userId);

        // 自动分配序号：优先填补空缺，无空缺则追加到末尾
        if (artifact.getSerialNumber() == null) {
            Integer gapSerial = artifactsMapper.findFirstGapSerialNumber();
            if (gapSerial != null && gapSerial > 0) {
                artifact.setSerialNumber(gapSerial); // 填补空缺
            } else {
                // 无空缺，追加到末尾
                Integer maxSerial = artifactsMapper.getMaxSerialNumber();
                artifact.setSerialNumber(maxSerial + 1);
            }
        }

        // 调用Mapper层方法新增数据
        artifactsMapper.add(artifact);
    }

    /**
     * 删除文物 — 同时重排后续序号以保持连续，事务保证原子性
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        // 先获取被删除记录的序号
        Integer deletedSerialNumber = artifactsMapper.getSerialNumberById(id);
        // 删除记录
        artifactsMapper.deleteById(id);
        // 将大于被删序号的记录全部减1，消除空缺
        if (deletedSerialNumber != null) {
            artifactsMapper.decrementAfterDelete(deletedSerialNumber);
        }
    }

    /**
     * 批量删除文物 — 删除后整体重排序号，事务保证原子性
     */
    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        // 批量删除
        artifactsMapper.batchDelete(ids);
        // 全部删除后整体重新编号，保持序号连续
        artifactsMapper.initRowNum();
        artifactsMapper.renumberAllSerialNumbers();
    }

    @Override
    public void update(artifacts artifact) {
        // 忽略任何客户端传入的手工检测情况。
        artifact.setTestingStatus(null);
        // 设置更新时间
        artifact.setUpdateTime(LocalDateTime.now());

        // 调用Mapper层方法修改数据
        artifactsMapper.update(artifact);
    }

    @Override
    public PageBean<artifacts> list(Integer pageNum, Integer pageSize, String keyword, String newArtifactName, String newArtifactCode, String material1, String excavationRelic, String completeness) {
        // 创建PageBean对象
        PageBean<artifacts> pageBean = new PageBean<>();

        // 调用PageHelper的startPage方法开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 调用Mapper层方法查询数据
        List<artifacts> artifactsList = artifactsMapper.list(keyword, newArtifactName, newArtifactCode, material1, excavationRelic, completeness);

        // 使用PageInfo获取分页信息
        PageInfo<artifacts> pageInfo = new PageInfo<>(artifactsList);
        artifactDetectionStatusService.decorate(pageInfo.getList());

        // 填充PageBean对象
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());

        return pageBean;
    }
}
