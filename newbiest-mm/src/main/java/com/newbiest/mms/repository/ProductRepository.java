package com.newbiest.mms.repository;

import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.mms.model.Product;
import org.springframework.stereotype.Repository;

/**
 * Created by guozhangLuo on 2020/8/11.
 */
@Repository
public interface ProductRepository extends IRepository<Product, Long> {

}