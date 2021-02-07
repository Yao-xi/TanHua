package com.yx.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.server.mapper.QuestionMapper;
import com.yx.tanhua.server.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    
    
    /**
     * 根据用户id查询问题
     * @param userId 用户id
     *
     * @return {@link Question} 问题
     */
    public Question queryQuestion(Long userId) {
        QueryWrapper<Question> queryWrapper =
            new QueryWrapper<Question>().eq("user_id", userId);
        return this.questionMapper.selectOne(queryWrapper);
    }
}