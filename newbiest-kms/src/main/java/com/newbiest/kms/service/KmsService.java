package com.newbiest.kms.service;

import com.newbiest.base.exception.ClientException;
import com.newbiest.base.service.FileStrategyService;
import com.newbiest.base.utils.SessionContext;
import com.newbiest.kms.model.Question;
import com.newbiest.kms.model.QuestionLine;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by guoxunbo on 2019-07-29 11:20
 */
public interface KmsService extends FileStrategyService {

    Question saveQuestion(Question question, SessionContext sc) throws ClientException;
    Question closeQuestion(Question question, SessionContext sc) throws ClientException;
    Question deleteQuestion(Question question, SessionContext sc) throws ClientException;
    Question watchQuestion(Question question, SessionContext sc) throws ClientException;

    List<QuestionLine> getQuestionLineByQuestionRrn(Long questionRrn) throws ClientException;

}