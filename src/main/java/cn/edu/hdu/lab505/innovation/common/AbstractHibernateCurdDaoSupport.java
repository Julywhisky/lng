package cn.edu.hdu.lab505.innovation.common;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;


/**
 * $Id: $
 */
public abstract class AbstractHibernateCurdDaoSupport<T> extends
        HibernateDaoSupport implements ICurdDaoSupport<T> {

    protected Class<?> entityClass;

    /**
     * 为了能自动注入HibernateDaoSupport的sessionFactory
     * ！！权宜之计
     *
     * @param sessionFactory
     */
    @Autowired(required = false)
    public void setSuperSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    public AbstractHibernateCurdDaoSupport() {
        Type type = getClass().getGenericSuperclass();
        Type trueType = ((ParameterizedType) type).getActualTypeArguments()[0];
        this.entityClass = (Class<T>) trueType;
    }


    public T get(Serializable id) {
        return (T) getHibernateTemplate().get(entityClass, id);
    }


    public void insert(T entity) {
        getHibernateTemplate().save(entity);
    }


    public void update(T entity) {
        getHibernateTemplate().update(entity);
    }


    public void merge(T entity) {
        getHibernateTemplate().merge(entity);
    }

    public void saveOrUpdate(T entity) {
        getHibernateTemplate().saveOrUpdate(entity);
    }


    public void delete(T entity) {
        getHibernateTemplate().delete(entity);
    }


    public List<T> findAll() {
        return (List<T>) getHibernateTemplate().loadAll(entityClass);
    }


    public Long getCount(final DetachedCriteria detachedCriteria) {
        return getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Long>() {
            public Long doInHibernate(Session session) throws HibernateException {
                Criteria criteria = detachedCriteria.getExecutableCriteria(session);
                CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
                Projection projection = criteriaImpl.getProjection();
                //logger.debug("SQL:"+Projections.rowCount());
                Long totalCount = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
                criteria.setProjection(projection);
                if (projection == null) {
                    criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
                }
                return totalCount;
            }
        });
    }

    public Long getCount() {
        final DetachedCriteria dc = DetachedCriteria.forClass(entityClass);
        return getCount(dc);
    }


    public List<T> find(final DetachedCriteria detachedCriteria) {
        return (List<T>) getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    public Page<T> findPage(final DetachedCriteria detachedCriteria, final int start, final int limit) {
        Page<T> page = new Page<T>();
        Long totalCount = getCount(detachedCriteria);
        page.setTotal(totalCount.longValue());
        if (totalCount > 0) {
            page.setList((List<T>) getHibernateTemplate().findByCriteria(detachedCriteria, start, limit));
        } else {
            page.setList(new ArrayList<T>(0));
        }
        return page;
    }

    public Page<T> findPage(final int start, final int limit) {
        final DetachedCriteria dc = DetachedCriteria.forClass(entityClass);
        return findPage(dc, start, limit);
    }

    // 执行插入，更新或删除SQL
    public int bulkUpdateBySQL(final String sql, final Object... params) {
        return getHibernateTemplate().executeWithNativeSession(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) {
                session.flush();
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        sqlQuery.setParameter(i, params[i]);
                    }
                }
                return new Integer(sqlQuery.executeUpdate());
            }
        });
    }

    //执行查询SQL
    public List findBySQL(final String sql, final Object... params) {
        return getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List>() {
            public List<Object> doInHibernate(Session session) {
                session.flush();
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        sqlQuery.setParameter(i, params[i]);
                    }
                }
                return sqlQuery.list();
            }
        });
    }
}
