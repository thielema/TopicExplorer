package cc.topicexplorer.plugin.text.preprocessing.tables.topic;

/** MIT-JOOQ-START 
 import static jooq.generated.Tables.TOPIC;
 import static jooq.generated.Tables.TERM;
 import static jooq.generated.Tables.TERM_TOPIC; 
 MIT-JOOQ-ENDE */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.apache.log4j.Logger;

import cc.topicexplorer.commands.TableFillCommand;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * @author angefangen von Mattes weiterverarbeitet von Gert Kommaersetzung, Pfadangabe eingefügt, Tabellenname mit Jooq
 *         verknüpft
 * 
 */
public class TopicFill extends TableFillCommand {

	private static final Logger logger = Logger.getLogger(TopicFill.class);

	@Override
	public void fillTable() {
		try {
			this.prepareMetaDataAndFillTable();
		} catch (SQLException sqlEx) {
			logger.error("Table " + "TOPIC" + " could not be updated properly.");
			throw new RuntimeException(sqlEx);
		}
	}

	private void prepareMetaDataAndFillTable() throws SQLException {
		String name;

//		@formatter:off
		/** MIT-JOOQ-START 
		Statement stmt = database.getCreateJooq().getConnection()
				.createStatement();

		
		ResultSet rsNames;
		for (int i = 0; i < Integer.parseInt(this.properties
				.getProperty("malletNumTopics")); i++) {

			String sql = "SELECT CONCAT((select k." + TERM.TERM_NAME.getName()
					+ " from " + TERM.getName() + " k, (SELECT "
					+ TERM_TOPIC.TOPIC_ID.getName() + ", "
					+ TERM_TOPIC.TERM_ID.getName() + " FROM `"
					+ TERM_TOPIC.getName() + "` WHERE "
					+ TERM_TOPIC.TOPIC_ID.getName() + "=" + i + " order by "
					+ TERM_TOPIC.PR_TERM_GIVEN_TOPIC.getName()
					+ " desc limit 0,1) x where k." + TERM.TERM_ID.getName()
					+ "=x." + TERM_TOPIC.TERM_ID.getName()
					+ "),', ',(select k." + TERM.TERM_NAME.getName() + " from "
					+ TERM.getName() + " k, (SELECT "
					+ TERM_TOPIC.TOPIC_ID.getName() + ", "
					+ TERM_TOPIC.TERM_ID.getName() + " FROM `"
					+ TERM_TOPIC.getName() + "` WHERE "
					+ TERM_TOPIC.TOPIC_ID.getName() + "=" + i + " order by "
					+ TERM_TOPIC.PR_TERM_GIVEN_TOPIC.getName()
					+ " desc limit 1,1) x where k." + TERM.TERM_ID.getName()
					+ "=x." + TERM_TOPIC.TERM_ID.getName()
					+ "),', ',(select k." + TERM.TERM_NAME.getName() + " from "
					+ TERM.getName() + " k, (SELECT "
					+ TERM_TOPIC.TOPIC_ID.getName() + ", "
					+ TERM_TOPIC.TERM_ID.getName() + " FROM `"
					+ TERM_TOPIC.getName() + "` WHERE "
					+ TERM_TOPIC.TOPIC_ID.getName() + "=" + i + " order by "
					+ TERM_TOPIC.PR_TERM_GIVEN_TOPIC.getName()
					+ " desc limit 2,1) x where k." + TERM.TERM_ID.getName()
					+ "=x." + TERM_TOPIC.TERM_ID.getName() + "))";
 
			rsNames = database.executeQuery(sql);

			if (rsNames.next()) {
				name = rsNames.getString(1);

				stmt.addBatch(" UPDATE " + TOPIC.getName() + " set "
						+ TOPIC.TEXT$TOPIC_LABEL.getName() + " = '"
						+ name + "' " + " WHERE "
						+ TOPIC.TOPIC_ID.getName() + " = " + i + "; ");
			} else {
				logger.fatal("Error fetching name data");
				System.exit(0);
			}
		}

		logger.info("Starting batch execution TopicMetaData update themen.");
		stmt.executeBatch();
MIT-JOOQ-ENDE */
//		@formatter:on
		/** OHNE_JOOQ-START */
		Statement stmt = database.getConnection().createStatement();
		ResultSet rsNames;
		for (int i = 0; i < Integer.parseInt(this.properties.getProperty("malletNumTopics")); i++) {

//			@formatter:off
			String sql = "SELECT CONCAT((select k." + "TERM_NAME"
					+ " from " + "TERM" + " k, (SELECT "
					+ "TERM_TOPIC.TOPIC_ID" + ", "
					+ "TERM_TOPIC.TERM_ID" + " FROM `"
					+ "TERM_TOPIC" + "` WHERE "
					+ "TERM_TOPIC.TOPIC_ID" + "=" + i + " order by "
					+ "TERM_TOPIC.PR_TERM_GIVEN_TOPIC"
					+ " desc limit 0,1) x where k." + "TERM_ID"
					+ "=x." + "TERM_ID"
					+ "),', ',(select k." + "TERM_NAME" + " from "
					+ "TERM" + " k, (SELECT "
					+ "TERM_TOPIC.TOPIC_ID" + ", "
					+ "TERM_TOPIC.TERM_ID" + " FROM `"
					+ "TERM_TOPIC" + "` WHERE "
					+ "TERM_TOPIC.TOPIC_ID" + "=" + i + " order by "
					+ "TERM_TOPIC.PR_TERM_GIVEN_TOPIC"
					+ " desc limit 1,1) x where k." + "TERM_ID"
					+ "=x." + "TERM_ID"
					+ "),', ',(select k." + "TERM_NAME" + " from "
					+ "TERM" + " k, (SELECT "
					+ "TERM_TOPIC.TOPIC_ID" + ", "
					+ "TERM_TOPIC.TERM_ID" + " FROM `"
					+ "TERM_TOPIC" + "` WHERE "
					+ "TERM_TOPIC.TOPIC_ID" + "=" + i + " order by "
					+ "TERM_TOPIC.PR_TERM_GIVEN_TOPIC"
					+ " desc limit 2,1) x where k." + "TERM_ID"
					+ "=x." + "TERM_ID" + "))";
// 			@formatter:on

			rsNames = database.executeQuery(sql);

			Preconditions.checkState(rsNames.next(),
					"Error fetching name data. rsNames should have had yet another tuple.");
			name = rsNames.getString(1);

//			@formatter:off
			stmt.addBatch(" UPDATE " + "TOPIC" + " set "
					+ "TOPIC.TEXT$TOPIC_LABEL" + " = '"
					+ name + "' " + " WHERE "
					+ "TOPIC.TOPIC_ID" + " = " + i + "; ");
//			@formatter:on
		}

		logger.info("Starting batch execution TopicMetaData update themen.");
		stmt.executeBatch();

		/** OHNE_JOOQ-ENDE */

	}

	@Override
	public void setTableName() {
		/**
		 * MIT-JOOQ-START this.tableName = TOPIC.getName(); MIT-JOOQ-ENDE
		 */
	}

	@Override
	public Set<String> getAfterDependencies() {
		return Sets.newHashSet();
	}

	@Override
	public Set<String> getBeforeDependencies() {
		return Sets.newHashSet("Text_TopicCreate", "TopicFill", "TermTopicFill", "TermFill");
	}

	@Override
	public Set<String> getOptionalAfterDependencies() {
		return Sets.newHashSet();
	}

	@Override
	public Set<String> getOptionalBeforeDependencies() {
		return Sets.newHashSet();
	}

}