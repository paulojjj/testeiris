package br.jus.tjse;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import br.jus.tjse.config.HibernateDialectResolver;

@Stateless
public class DialectInfoService {

	@PersistenceContext
	EntityManager em;

	public DialectResolutionInfo getInfo() {
		return HibernateDialectResolver.info;
	}
	
	public Dialect getDialect() {
		return HibernateDialectResolver.dialect;
	}

}
