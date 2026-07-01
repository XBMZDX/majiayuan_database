package com.itheima.bigevent.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.bigevent.mapper.ArtifactsMapper;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.artifacts;
import com.itheima.bigevent.service.ArtifactsService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArtifactsServiceImpl implements ArtifactsService {

    @Autowired
    private ArtifactsMapper artifactsMapper;

    @Override
    public void add(artifacts artifact) {
        // 设置创建时间和更新时间
        artifact.setCreateTime(LocalDateTime.now());
        artifact.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        artifact.setCreatedBy(userId);

        // 设置默认审核状态
        artifact.setVerificationStatus("pending");

        // 调用Mapper层方法新增数据
        artifactsMapper.add(artifact);
    }

    @Override
    public void deleteById(Integer id) {
        // 调用Mapper层方法根据ID删除数据
        artifactsMapper.deleteById(id);
    }

    @Override
    public void update(artifacts artifact) {
        // 设置更新时间
        artifact.setUpdateTime(LocalDateTime.now());

        // 调用Mapper层方法修改数据
        artifactsMapper.update(artifact);
    }

    @Override
    public PageBean<artifacts> list(Integer pageNum, Integer pageSize, String name, String artifactCode, String siteName, String relicName, String category, String material, String era) {
        // 创建PageBean对象
        PageBean<artifacts> pageBean = new PageBean<>();

        // 调用PageHelper的startPage方法开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 调用Mapper层方法查询数据
        List<artifacts> artifactsList = artifactsMapper.list(name, artifactCode, siteName, relicName, category, material, era);

        // 使用PageInfo获取分页信息
        PageInfo<artifacts> pageInfo = new PageInfo<>(artifactsList);

        // 填充PageBean对象
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());

        return pageBean;
    }
}
