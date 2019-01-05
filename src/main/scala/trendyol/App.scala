package trendyol

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.FileSystem.WriteMode
/**
 * @author ${akocabiyik}
 */
object App {
  
  def main(args : Array[String]) {

    import org.apache.flink.streaming.api.scala._

    val params: ParameterTool = ParameterTool.fromArgs(args)
    val env = ExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)

    //val resourcePath = getClass().getResource("/case.csv").toString

    val eventDataset = env.readCsvFile[(Int, String, Int)](
      "case.csv",
      fieldDelimiter = "|",
      ignoreFirstLine = true,
      includedFields = Array(1, 2, 3)
    )

    val dirOutputFiles = sys.env.getOrElse("OUTPUT_FILES_DIR", "/")
    //val dirOutputFiles = "resultFiles"

    val UniqueProductView = eventDataset.filter { x => x._2 == "view" }
      .map { x => (x._1, 1) }
      .groupBy(0)
      .sum(1)

    UniqueProductView.writeAsCsv(s"${dirOutputFiles}/uniqueProductView", "\n", "|", WriteMode.OVERWRITE).setParallelism(1)

    val UniqueEventCounts = eventDataset
      .map { x => (x._2, 1) }
      .groupBy(0)
      .sum(1)

    UniqueEventCounts.writeAsCsv(s"${dirOutputFiles}/uniqueEventCounts", "\n", "|", WriteMode.OVERWRITE).setParallelism(1)

    val topFiveUsers = eventDataset
      .distinct(1, 2)
      .map { x => (x._3, 1) }
      .groupBy(0)
      .sum(1)
      .filter { x => x._2 > 3 }
      .map { x => x._1 }
      .first(5)

    topFiveUsers.writeAsText(s"${dirOutputFiles}/topFiveUsers", WriteMode.OVERWRITE).setParallelism(1)

    val allEventsOfUser = eventDataset.filter { x => x._3 == 47 }
      .map(x => (x._2, 1))
      .groupBy(0)
      .sum(1)

    allEventsOfUser.writeAsCsv(s"${dirOutputFiles}/allEventsOfUser47", "\n", "|", WriteMode.OVERWRITE).setParallelism(1)

    val productViewOfUser = eventDataset.filter { x => x._3 == 47 }
      .filter { x => x._2 == "view" }
      .map { x => x._1 }

    productViewOfUser.writeAsText(s"${dirOutputFiles}/productViewOfUser47", WriteMode.OVERWRITE).setParallelism(1)

    env.execute("Analyze user activities")
  }

}
