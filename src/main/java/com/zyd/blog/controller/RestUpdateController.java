package com.zyd.blog.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zyd.blog.business.entity.UpdateRecorde;
import com.zyd.blog.business.enums.ResponseStatus;
import com.zyd.blog.business.service.SysUpdateRecordeService;
import com.zyd.blog.business.vo.UpdateRecordeConditionVO;
import com.zyd.blog.framework.object.PageResult;
import com.zyd.blog.framework.object.ResponseVO;
import com.zyd.blog.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统更新日志
 *
 * @author zhrey
 * @website http://www.zhrey.cn
 * @date 2018/4/24 14:37
 */
@RestController
@RequestMapping("/update")
public class RestUpdateController {
    @Autowired
    private SysUpdateRecordeService updateRecordeService;

    @PostMapping("/list")
    public PageResult list(UpdateRecordeConditionVO vo) {
        PageHelper.startPage(vo.getPageNumber() - 1, vo.getPageSize());
        PageInfo<UpdateRecorde> pageInfo = updateRecordeService.findPageBreakByCondition(vo);
        return ResultUtil.tablePage(pageInfo);
    }

    @PostMapping(value = "/add")
    public ResponseVO add(UpdateRecorde updateRecorde) {
        updateRecordeService.insert(updateRecorde);
        return ResultUtil.success("成功");
    }

    @PostMapping(value = "/remove")
    public ResponseVO remove(Long[] ids) {
        if (null == ids) {
            return ResultUtil.error(500, "请至少选择一条记录");
        }
        for (Long id : ids) {
            updateRecordeService.removeByPrimaryKey(id);
        }
        return ResultUtil.success("成功删除 [" + ids.length + "] 个更新记录");
    }

    @PostMapping("/get/{id}")
    public ResponseVO get(@PathVariable Long id) {
        return ResultUtil.success(null, this.updateRecordeService.getByPrimaryKey(id));
    }

    @PostMapping("/edit")
    public ResponseVO edit(UpdateRecorde updateRecorde) {
        try {
            updateRecordeService.updateSelective(updateRecorde);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("更新记录修改失败！");
        }
        return ResultUtil.success(ResponseStatus.SUCCESS);
    }

}
