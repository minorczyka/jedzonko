import slick.codegen.SourceCodeGenerator
import slick.driver.JdbcProfile
import slick.model.Model
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Await}

class CustomizedCodeGenerator(model: Model) extends SourceCodeGenerator(model) {
  val models = new mutable.MutableList[String]

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    super.packageCode(profile, pkg, container, parentType) + "\n" + outsideCode
  }

  def outsideCode = s"${indent(models.mkString("\n"))}"

  override def Table = new Table(_) {
    override def EntityType = new EntityTypeDef {
      override def docWithCode: String = {
        models += super.docWithCode.toString + "\n"
        ""
      }
    }
  }
}

object CustomizedCodeGenerator {

  def run(slickDriver: String, jdbcDriver: String, url: String, outputDir: String, pkg: String, user: Option[String], password: Option[String]): Unit = {
    val driver: JdbcProfile =
      Class.forName(slickDriver + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = driver.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user.getOrElse(null), password = password.getOrElse(null), keepAliveConnection = true)
    try {
      val m = Await.result(db.run(driver.createModel(None, false)(ExecutionContext.global).withPinnedSession), Duration.Inf)
      new CustomizedCodeGenerator(m).writeToFile(slickDriver,outputDir,pkg)
    } finally db.close
  }

  def main(args: Array[String]): Unit = {
    args.toList match {
      case slickDriver :: jdbcDriver :: url :: outputDir :: pkg :: Nil =>
        run(slickDriver, jdbcDriver, url, outputDir, pkg, None, None)
      case slickDriver :: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: Nil =>
        run(slickDriver, jdbcDriver, url, outputDir, pkg, Some(user), Some(password))
      case _ => {
        System.exit(1)
      }
    }
  }
}

val slickDriver = "slick.driver.PostgresDriver"
val jdbcDriver = "org.postgresql.Driver"
val url = "jdbc:postgresql://192.168.1.101:5432/jedzenie"
val outputFolder = "E:\\BitBucket\\System_Zamawiania_Jedzenia\\system-zamawiania-jedzenia\\app"
val pkg = "models"
val user = "postgres"
val password = "qwe123"

CustomizedCodeGenerator.main(
  Array(slickDriver, jdbcDriver, url, outputFolder, pkg, user, password)
)