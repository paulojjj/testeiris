package br.jus.tjse.config;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.dialect.Cache71Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.FirebirdDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
 
public class HibernateDialectResolver implements DialectResolver {
 
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(HibernateDialectResolver.class.getName());
	
	public static DialectResolutionInfo info;
	public static Dialect dialect;
	
	public static enum Database implements DialectResolver {
		DB2(DB2Dialect.class, "DB2", "DB2/NT64", "DB2/LINUXX8664"),
		FIREBIRD (new StartsWithDialectResolver(FirebirdDialect.class, "Firebird")),
		H2(H2Dialect.class, "H2"), 
		HSQL(HSQLDialect.class, "HSQL Database Engine"), 
		MYSQL(new MySQLDialectResolver()), 
		ORACLE(Oracle10gDialect.class, "Oracle"),
		POSTGRES(PostgreSQL9Dialect.class, "POSTGRESQL"),
		SQL_SERVER(SQLServer2012Dialect.class, "Microsoft SQL Server"),
		//org.hibernate.dialect.InterSystemsIRISDialect
		//IRIS(Cache71Dialect.class, "InterSystems IRIS"),
		CACHE(new CacheDialectResolver());
		
		private DialectResolver dialectResolver;
		
		Database(Class<? extends Dialect> dialectClass, String... databaseNames) {
			this.dialectResolver = new SimpleDialectResolver(dialectClass, databaseNames);
		}
		
		Database(DialectResolver resolver) {
			this.dialectResolver = resolver;
		}

		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			return dialectResolver.resolveDialect(info);
		}

	}
	
	public static abstract class SimpleStringDialectResolver implements DialectResolver {

		private final Comparator<String> comparator;
		private final List<String> databaseNames;
		private final Class<? extends Dialect> dialectClass;
		
		SimpleStringDialectResolver(Class<? extends Dialect> dialectClass, Comparator<String> comparator, String... databaseNames) {
			this.databaseNames = Arrays.asList(databaseNames);
			this.dialectClass = dialectClass;
			this.comparator = comparator;
		}
		
		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			for(String dbName : databaseNames) {
				if(comparator.compare(databaseName, dbName) == 0) {
					try {
						return dialectClass.newInstance();
					}
					catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}					
				}
			}
			return null;
		}
	}
	
	public static final class SimpleDialectResolver extends SimpleStringDialectResolver {
		SimpleDialectResolver(Class<? extends Dialect> dialectClass, String... databaseNames) {
			super(dialectClass, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
				
			}, databaseNames);
		}
	}
	
	public static final class StartsWithDialectResolver extends SimpleStringDialectResolver {
		StartsWithDialectResolver(Class<? extends Dialect> dialectClass, String... databaseNames) {
			super(dialectClass, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					if(o1.startsWith(o2) || o2.startsWith(o1)) {
						return 0;
					}
					return o1.compareTo(o2);
				}
				
			}, databaseNames);
		}
	}
	
	public static final class MySQLDialectResolver implements DialectResolver {
		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			if(!databaseName.equals("MySQL")) {
				return null;
			}
			int major = info.getDatabaseMajorVersion();
			if(major < 5) {
				return new MySQLDialect();
			}
			else {
				return new MySQL5Dialect();
			}
		}
	}
	
	public static final class MariaDBDialectResolver implements DialectResolver {

		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			if(!databaseName.startsWith("MariaDB")) {
				return null;
			}
			return new MySQL5Dialect();
		}
		
	}
	
	public static final class CacheDialectResolver implements DialectResolver {

		private static Class<?> classeCacheDialect;
		
		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			if(!databaseName.equals("Cache") && !databaseName.equals("InterSystems IRIS")) {
				return null;
			}
			
			
			if(classeCacheDialect == null) {
				try {
					classeCacheDialect = Class.forName("br.jus.tjse.dialect.CacheDialect");
				} catch (ClassNotFoundException e) {
					classeCacheDialect = Cache71Dialect.class;
				}
				
			}
			
			try {
				return (Dialect)classeCacheDialect.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public static interface DialectResolver {
		public Dialect resolveDialect(DialectResolutionInfo info);
	}
	
	public HibernateDialectResolver() {
	}
	
	@Override
	public Dialect resolveDialect(DialectResolutionInfo info) {
		Dialect dbDialect = lookupDialect(info);
		return dbDialect;
	}

	private Dialect lookupDialect(DialectResolutionInfo info) {
		HibernateDialectResolver.info = info;
		logger.info(String.format("Resolvendo dialeto do hibernate, databaseName: %s, version: %d.%d, driverName: %s, driverVersion: %d.%d", info.getDatabaseName(), info.getDatabaseMajorVersion(), info.getDatabaseMinorVersion(), info.getDriverName(), info.getDriverMajorVersion(), info.getDatabaseMinorVersion()));
		for(Database database : Database.values()) {
			Dialect dialect = database.resolveDialect(info);
			if(dialect != null) {
				HibernateDialectResolver.dialect = dialect;
				return dialect;
			}
		}
		logger.severe("Não foi possível determinar o dialeto do Hibernate");
		return null;
	}

}