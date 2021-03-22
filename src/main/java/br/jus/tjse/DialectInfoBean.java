package br.jus.tjse;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

@Named
@ViewScoped
public class DialectInfoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DialectInfoService dialectInfoService;
	
	private DialectResolutionInfo info;
	private String dialect;
	
	public DialectResolutionInfo getInfo() {
		if(info == null) {
			info = dialectInfoService.getInfo();
		}
		return info;
	}
	
	public String getDialect() {
		if(dialect == null) {
			dialect = dialectInfoService.getDialect() == null ? "null" : dialectInfoService.getDialect().getClass().getName();
		}
		return dialect;
	}
}
