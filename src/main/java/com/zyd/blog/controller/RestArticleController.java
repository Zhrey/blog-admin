package com.zyd.blog.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zyd.blog.business.entity.Article;
import com.zyd.blog.business.entity.User;
import com.zyd.blog.business.enums.QiniuUploadType;
import com.zyd.blog.business.enums.ResponseStatus;
import com.zyd.blog.business.service.BizArticleService;
import com.zyd.blog.business.service.BizArticleTagsService;
import com.zyd.blog.business.vo.ArticleConditionVO;
import com.zyd.blog.framework.object.PageResult;
import com.zyd.blog.framework.object.ResponseVO;
import com.zyd.blog.util.FileUtil;
import com.zyd.blog.util.ResultUtil;
import com.zyd.blog.util.SessionUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章管理
 *
 * @author zhrey
 * @website http://www.zhrey.cn
 * @date 2018/4/24 14:37
 */
@RestController
@RequestMapping("/article")
public class RestArticleController {
    @Autowired
    private BizArticleService articleService;
    @Autowired
    private BizArticleTagsService articleTagsService;

    @RequiresPermissions("articles")
    @PostMapping("/list")
    public PageResult list(ArticleConditionVO vo) {
        PageHelper.startPage(vo.getPageNumber() - 1, vo.getPageSize());
        PageInfo<Article> pageInfo = articleService.findPageBreakByCondition(vo);
        return ResultUtil.tablePage(pageInfo);
    }

    @PostMapping(value = "/remove")
    public ResponseVO remove(Long[] ids) {
        if (null == ids) {
            return ResultUtil.error(500, "请至少选择一条记录");
        }
        for (Long id : ids) {
            articleService.removeByPrimaryKey(id);
        }
        return ResultUtil.success("成功删除 [" + ids.length + "] 篇文章");
    }

    @PostMapping("/get/{id}")
    public ResponseVO get(@PathVariable Long id) {
        return ResultUtil.success(null, this.articleService.getByPrimaryKey(id));
    }

    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO edit(Article article, Long[] tags, MultipartFile file) {
        if (null == tags || tags.length <= 0) {
            return ResultUtil.error("请至少选择一个标签");
        }
        if (null != file) {
            String filePath = FileUtil.uploadToQiniu(file, QiniuUploadType.COVER_IMAGE, true);
            // 保存封面图片
            article.setCoverImage(filePath);
        }
        Long articleId = null;
        if (article.getId() != null) {
            articleService.updateSelective(article);
            articleId = article.getId();
        } else {
            User user = SessionUtil.getUser();
            article.setUserId(user.getId());
            articleService.insert(article);
            articleId = article.getId();
        }
        if (articleId != null) {
            articleTagsService.removeByArticleId(articleId);
            articleTagsService.insertList(tags, articleId);
        }
        return ResultUtil.success(ResponseStatus.SUCCESS);
    }

    @PostMapping("/update/{type}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO update(@PathVariable("type") String type, Long id) {
        Article article = articleService.getByPrimaryKey(id);
        article.setId(id);
        if ("top".equals(type)) {
            article.setTop(!article.isTop());
        } else {
            article.setRecommended(!article.getRecommended());
        }
        articleService.updateSelective(article);
        return ResultUtil.success(ResponseStatus.SUCCESS);
    }
}
