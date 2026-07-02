package com.itheima.bigevent.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.bigevent.mapper.RelicsMapper;
import com.itheima.bigevent.pojo.PageBean;
import com.itheima.bigevent.pojo.relics;
import com.itheima.bigevent.service.RelicsService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class RelicsServiceImpl implements RelicsService {

    @Autowired
    private RelicsMapper relicsMapper;

    @Override
    public void add(relics relic) {
        // 设置创建时间和更新时间
        relic.setCreateTime(LocalDateTime.now());
        relic.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        relic.setCreatedBy(userId);

        // 调用Mapper层方法新增数据
        relicsMapper.add(relic);
    }

    @Override
    public void deleteById(Integer id) {
        // 调用Mapper层方法根据ID删除数据
        relicsMapper.deleteById(id);
    }

    @Override
    public void update(relics relic) {
        // 设置更新时间
        relic.setUpdateTime(LocalDateTime.now());

        // 调用Mapper层方法修改数据
        relicsMapper.update(relic);
    }
    @Override
    public PageBean<relics> list(Integer pageNum, Integer pageSize, String name, String relicCode, String siteName, String type) {
        // 创建PageBean对象
        PageBean<relics> pageBean = new PageBean<>();

        // 调用PageHelper的startPage方法开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 调用Mapper层方法查询数据
        List<relics> relicsList = relicsMapper.list(name, relicCode, siteName, type);

        // 使用PageInfo获取分页信息
        PageInfo<relics> pageInfo = new PageInfo<>(relicsList);

        // 填充PageBean对象
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());

        return pageBean;
    }

    @Override
    public void batchImport(List<relics> relicsList) {
        // 获取当前登录用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        
        // 设置每个遗迹的创建时间、更新时间和创建人ID
        LocalDateTime now = LocalDateTime.now();
        for (relics relic : relicsList) {
            relic.setCreateTime(now);
            relic.setUpdateTime(now);
            relic.setCreatedBy(userId);
        }
        
        // 批量导入数据
        if (!relicsList.isEmpty()) {
            relicsMapper.batchInsert(relicsList);
        }
    }

    @Override
    public List<String> getNames() {
        return relicsMapper.getNames();
    }
}
