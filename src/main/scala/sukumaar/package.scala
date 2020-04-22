package object sukumaar {
  val PARENT_DIR="./data"
  val DATABASE_NAME:String = "DB"
  val DATABASE_DIR:String = s"$PARENT_DIR/$DATABASE_NAME"
  val DATABASE_URL:String = s"jdbc:h2:$DATABASE_DIR"
}
