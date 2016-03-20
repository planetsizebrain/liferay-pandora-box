import com.liferay.portal.kernel.util.InfrastructureUtil
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.command.InvocationContext
import org.crsh.command.ScriptException
import org.crsh.text.ui.UIBuilder
import org.crsh.util.Utils

import javax.naming.NoInitialContextException
import java.sql.*

@Usage("Liferay DB connection")
class db implements Completer {

	@Usage("open a connection from JNDI bound datasource")
	@Command
	public String open() {
		if (connection != null) {
			throw new ScriptException("Already connected");
		}

		try {
			connection = InfrastructureUtil.dataSource.connection

			return "Connected to datasource\n"
		} catch (NoInitialContextException e) {
			throw new ScriptException("No initial context found", e)
		} finally {
			Utils.close(ctx);
		}
	}

	@Usage("execute a SQL statement")
	@Command
	public String execute(
			@Usage("The statement")
			@Argument(unquote = false)
					List<String> statement) {

		if (connection == null) {
			throw new ScriptException("You are not connected to database, please call jdbc open [JNDI DS]");
		} else {
			StringBuilder sb = new StringBuilder();
			statement.each { sb << " " << it };
			String sql = sb.toString().trim();
			if (sql.startsWith('"') && sql.endsWith('"') || sql.startsWith("'") && sql.endsWith("'"))
				sql = sql.substring(1, sql.length() - 1)

			Statement stmt = connection.createStatement();
			try {
				stmt.execute(sql)
				ResultSet resultSet = stmt.getResultSet();
				if (resultSet != null) {
					resultSet.close();
				}
				return "Statement executed successfully\n";
			}
			finally {
				Utils.close(stmt);
			}
		}
	}

	@Usage("select SQL statement")
	@Command
	public void select(
			InvocationContext<Map> context,
			@Usage("The statement")
			@Argument(unquote = false)
					List<String> statement) {
		if (connection == null) {
			throw new ScriptException("You are not connected to database, please call jdbc open [JNDI DS]");
		} else {
			StringBuilder sb = new StringBuilder("select ");
			statement.each { sb << " " << it };
			String sql = sb.toString().trim();
			if (sql.startsWith('"') && sql.endsWith('"') || sql.startsWith("'") && sql.endsWith("'"))
				sql = sql.substring(1, sql.length() - 1)
			Statement stmt = connection.createStatement();
			try {
				stmt.execute(sql)
				ResultSet resultSet = stmt.getResultSet();
				try {
					if (resultSet != null) {
						ResultSetMetaData metaData = resultSet.getMetaData();
						int columnCount = resultSet.getMetaData().getColumnCount()
						while (resultSet.next()) {
							LinkedHashMap row = new LinkedHashMap();
							(1..columnCount).each { row[metaData.getColumnName(it)] = resultSet.getString(it) }
							context.provide(row)
						}
						out << "Query executed successfully\n";
					}
				}
				catch (IOException e) {
					e.printStackTrace()
				}
				finally {
					Utils.close(resultSet)
				}
			}
			finally {
				Utils.close(stmt);
			}
		}
	}

	@Usage("show the database properties")
	@Command
	public void props(InvocationContext<Map> context) {
		if (connection == null) {
			throw new ScriptException("Not connected");
		}
		DatabaseMetaData md = connection.getMetaData();
		md.properties.each { key, value ->
			try {
				context.provide([NAME: key, VALUE: value] as LinkedHashMap)
			}
			catch (IOException e) {
				e.printStackTrace()
			};
		}
	}

	@Usage("describe the tables")
	@Command
	public Object tables() {
		if (connection == null) {
			throw new ScriptException("Not connected");
		}
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		def ui = new UIBuilder();
		try {
			ui.table(columns: [1, 1, 1]) {
				header(bold: true, fg: black, bg: white) {
					label("NAME");
					label("CAT");
					label("TYPE");
				}
				while (rs.next()) {
					row {
						label(rs.getString("TABLE_NAME"))
						label(rs.getString("TABLE_SCHEM"))
						label(rs.getString("TABLE_TYPE"))
					}
				}
			}
		}
		finally {
			Utils.close(rs)
		}
		return ui;
	}

	@Usage("describe the tables")
	@Command
	public Object table(@Argument @Usage("the table names") List<String> tableNames) {
		if (connection == null) {
			throw new ScriptException("Not connected");
		}
		DatabaseMetaData md = connection.getMetaData();
		def ui = new UIBuilder();
		ui.table(columns: [2, 2, 1, 1], border: dashed) {
			header(bold: true, fg: black, bg: white) {
				label("COLUMN")
				label("TYPE")
				label("SIZE")
				label("NULLABLE")
			}
			tableNames.each {
				// Save it here because it seems to go away for some scoping reason in the header
				def res = "" + it;
				ResultSet rs = md.getColumns(null, null, it, null);
				header(fg: black, bg: white) {
					label(res)
				}
				try {
					while (rs.next()) {
						row {
							label(rs.getString("COLUMN_NAME"))
							label("${rs.getString('TYPE_NAME')} (${rs.getString('DATA_TYPE')})")
							label(rs.getString("COLUMN_SIZE"))
							label(rs.getString("IS_NULLABLE"))
						}
					}
				} finally {
					Utils.close(rs)
				}
			}
		}
		return ui;
	}

	@Usage("describe the database")
	@Command
	public void info(InvocationContext<Map> context) {
		if (connection == null) {
			throw new ScriptException("Not connected");
		}
		// columns : 1,2,2,1,1
		DatabaseMetaData md = connection.getMetaData();

		//
		try {
			context.provide([
					TYPE   : "Product",
					NAME   : "${md.databaseProductName}",
					VERSION: "${md.databaseProductVersion}",
					MAJOR  : "${md.databaseMajorVersion}",
					MINOR  : "${md.databaseMinorVersion}"
			] as LinkedHashMap)
		}
		catch (IOException e1) {
			e1.printStackTrace()
		};

		//
		try {
			context.provide([
					TYPE   : "Product",
					NAME   : "${md.driverName}",
					VERSION: "${md.driverVersion}",
					MAJOR  : "${md.driverMajorVersion}",
					MINOR  : "${md.driverMinorVersion}"
			] as LinkedHashMap)
		}
		catch (IOException e) {
			e.printStackTrace()
		};
	}

	@Usage("close the current connection")
	@Command
	public String close() {
		if (connection == null) {
			throw new ScriptException("Not connected");
		} else {
			connection.close();
			connection = null;
			return "Connection closed\n"
		}
	}

	Completion complete(ParameterDescriptor parameter, String prefix) {
		return c.complete(parameter, prefix);
	}

}