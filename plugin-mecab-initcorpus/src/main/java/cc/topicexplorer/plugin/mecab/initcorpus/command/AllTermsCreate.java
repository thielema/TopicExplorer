package cc.topicexplorer.plugin.mecab.initcorpus.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import cc.topicexplorer.commands.TableCreateCommand;

public class AllTermsCreate  extends TableCreateCommand {
	private static final Logger logger = Logger.getLogger(AllTermsCreate.class);
	private ArrayList<String> queries = new ArrayList<String>();

	@Override
	public void createTable() {
		runSqlFillQueriesSimple();
	}

	@Override
	public void setTableName() {
		this.tableName = "ALL_TERMS";
	}

	@Override
	public Set<String> getAfterDependencies() {
		return Sets.newHashSet();
	}

	@Override
	public Set<String> getBeforeDependencies() {
		return Sets.newHashSet("DocumentTermFill", "PosTypeFill");
	}

	@Override
	public Set<String> getOptionalAfterDependencies() {
		return Sets.newHashSet();
	}

	@Override
	public Set<String> getOptionalBeforeDependencies() {
		return Sets.newHashSet();
	}
	private void generateFillQueriesSimple() {
		queries.add(
				"CREATE TABLE `" + this.tableName + "` AS "
				+ "SELECT DOCUMENT_TERM.TERM, COUNT(DISTINCT DOCUMENT_TERM.DOCUMENT_ID) AS COUNT, p2.POS "
				+ "FROM DOCUMENT_TERM, POS_TYPE p1, POS_TYPE p2 "
				+ "WHERE p1.POS=DOCUMENT_TERM.WORDTYPE_CLASS AND p1.LOW>=p2.LOW "
				+ "AND p1.HIGH<=p2.HIGH "
				+ "GROUP BY DOCUMENT_TERM.TERM, p2.POS"
				);
	}
	private void runSqlFillQueriesSimple() {
		
		try {
			for (Integer i=0;i<queries.size();i++) {
				logger.info("Start Query " + i.toString());
				this.database.executeUpdateQuery(queries.get(i));
				logger.info("Finished Query " + i.toString());
			}
			 
		} catch (SQLException e) {
			logger.error("Table " + this.tableName + " could not be created.");
			throw new RuntimeException(e);
		}

	}

	private void generateFillQueriesIterateDocuments() {
		
	}

	private void runSqlFillQueriesIterateDocuments() {
		
	}

	private void generateFillQueriesIteratePos() {
		
	}

	private void runSqlFillQueriesIteratePos() {
		
	}


}