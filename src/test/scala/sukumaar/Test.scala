package sukumaar

import java.io.File
import java.sql.{Connection, DriverManager, Statement}

import org.h2.engine.{ConnectionInfo, Database => H2Database}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.reflect.io.Directory

/**
 * Note: Database class from org.h2.engine has been aliased to H2Database
 * while importing to make it more readable
 *
 * some info: https://medium.com/@harittweets/how-to-connect-to-h2-database-during-development-testing-using-spring-boot-44bbb287570
 */
class Test extends AnyFunSuite with BeforeAndAfterAll {

  val databaseInstance: H2Database = createDatabaseInstance()
  val con: Connection = DriverManager.getConnection(DATABASE_URL)
  val stm: Statement = con.createStatement

  override def afterAll() {
    jdbcDisconnect()
    databaseInstance.shutdownImmediately()
    clearDatabaseDataDir()
  }

  test("embedded H2 database example") {
    var count = 0
    val rs = stm.executeQuery("SELECT 1+1")
    try {
      if (rs.next) {
        count = rs.getInt(1)
      }
    }
    catch {
      case ex: Exception => ex.printStackTrace()

    } finally {
      if (rs != null) rs.close()
    }

    assert(count == 2)
  }

  test("embedded H2 database create table") {
    /**
     * By default table & column names will get capitalized in H2 DB if no quotes are provided,
     * Relevant setting is DATABASE_TO_UPPER inside [[org.h2.engine.DbSettings]] databaseToUpper()
     * https://github.com/h2database/h2database/issues/1739
     * https://github.com/h2database/h2database/blob/6b033540387fad0bb8fe9de428eb8d1c72b2c09e/h2/src/main/org/h2/engine/DbSettings.java#L58-L66
     **/
    var isTableCreated = false
    val sql: String =
      """create table TEST1(ID INT PRIMARY KEY,NAME VARCHAR(500));"""
    try {
      stm.execute(sql)
      val rs = stm.executeQuery(
        """
          |select count(*) as count_of_table
          |from information_schema.tables
          |where table_type='TABLE' and table_name = 'TEST1'""".stripMargin)
      try {
        if (rs.next) {
          isTableCreated = 1 == rs.getInt("count_of_table")
        }
      }
      catch {
        case ex: Exception =>
          println(ex.getMessage)
      } finally {
        if (rs != null) rs.close()
      }
    }
    catch {
      case ex: Exception =>
        println("Unable to create the table")
        println(ex.getMessage)
    }
    assert(isTableCreated)
  }

  test("embedded H2 database create table with insert") {
    var row1Insertion = false
    var row2Insertion = false
    var row3Insertion = false

    try {
      val sql: String =
        """
          |create table test2(ID INT PRIMARY KEY,NAME VARCHAR(500));
          |insert into test2 values (1,'A');
          |insert into test2 values (2,'B');
          |insert into test2 values (3,'C');""".stripMargin
      stm.execute(sql)
      val rs = stm.executeQuery("""select * from test2""")
      try {
        if (rs.next) {
          row1Insertion = (1 == rs.getInt("ID")) && ("A" == rs.getString("NAME"))

          rs.next
          row2Insertion = (2 == rs.getInt("ID")) && ("B" == rs.getString("NAME"))

          rs.next
          row3Insertion = (3 == rs.getInt("ID")) && ("C" == rs.getString("NAME"))
        }
      }
      catch {
        case ex: Exception => ex.printStackTrace()
      } finally {
        if (rs != null) rs.close()
      }
    }
    catch {
      case e: Exception => println(e.getMessage)
    }
    assert(row1Insertion && row2Insertion && row3Insertion, "Data not inserted")
  }

  test("embedded H2 database create table and then drop it") {
    var isTableCreated: Boolean = false
    var isTableDropped: Boolean = false

    try {
      val sql: String =
        """ create table test3(ID INT PRIMARY KEY,
          |NAME VARCHAR(500));""".stripMargin
      stm.execute(sql)
      val rs = stm.executeQuery("""select count(*) as count_of_table from information_schema.tables where table_name = 'TEST3'""")
      try {
        if (rs.next) {
          isTableCreated = (1 == rs.getInt("count_of_table"))
        }
      }
      catch {
        case ex: Exception => println(ex.getMessage)
      } finally {
        if (rs != null) rs.close()
      }

      stm.execute("drop table test3")
      val rs2 = stm.executeQuery("""select count(*) as count_of_table from information_schema.tables where table_name = 'TEST3'""")
      try {
        if (rs2.next) {
          isTableDropped = 0 == rs2.getInt("count_of_table")
        }
      }
      catch {
        case ex: Exception => println(ex.getMessage)
      } finally {
        if (rs2 != null) rs2.close()
      }

    }
    catch {
      case e: Exception => println(e.getMessage)
    }
    assert(isTableCreated)
    assert(isTableDropped)
  }

  private def clearDatabaseDataDir(): Unit = {
    //DeleteDbFiles.execute(DATABASE_DIR, DATABASE_NAME, false) // I don't know  why but this is not working as expected
    val parentDirectory = new File(PARENT_DIR)
    assert(new Directory(parentDirectory).deleteRecursively())
  }

  private def jdbcDisconnect(): Unit = {
    if (con != null) con.close()
    if (stm != null) stm.close()
  }

  private def createDatabaseInstance(): H2Database = {
    val connectionInfo = new ConnectionInfo(DATABASE_NAME)
    connectionInfo.setBaseDir(DATABASE_DIR)
    val databaseInstance = new H2Database(connectionInfo, "")
    databaseInstance.setDeleteFilesOnDisconnect(true)
    databaseInstance
  }

}