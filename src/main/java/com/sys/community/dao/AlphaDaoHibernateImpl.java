package com.sys.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaDaoHibernateImpl")
public class AlphaDaoHibernateImpl implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}
