package com.newbiest.gc.repository;

import com.newbiest.base.exception.ClientException;
import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.gc.model.ErpSo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpSoRepository extends IRepository<ErpSo, Long> {

    List<ErpSo> findByTypeAndSynStatusNotIn(String type, List<String> asyncStatus);

    @Query("update ErpSo p set p.synStatus=:synStatus, p.errorMemo = :errorMemo where p.seq in (:seqList)")
    @Modifying
    void updateSynStatusAndErrorMemoBySeq(@Param("synStatus") String synStatus,@Param("errorMemo") String errorMemo,@Param("seqList") List<Long> seqList) throws ClientException;
}