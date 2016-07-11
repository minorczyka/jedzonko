package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Group.schema, Oauth2Info.schema, Order.schema, OrderMessage.schema, PasswordInfo.schema, Place.schema, PlaceComment.schema, PlayEvolutions.schema, Prepaid.schema, User.schema, UserToGroup.schema, UserToOrder.schema, UserToVoting.schema, Voting.schema, VotingToPlace.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching GroupRow objects using plain SQL queries */
  implicit def GetResultGroupRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[GroupRow] = GR{
    prs => import prs._
    GroupRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table group. Objects of this class serve as prototypes for rows in queries. */
  class Group(_tableTag: Tag) extends Table[GroupRow](_tableTag, "group") {
    def * = (id, ownerId, name, password, creationDate) <> (GroupRow.tupled, GroupRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(ownerId), Rep.Some(name), Rep.Some(password), Rep.Some(creationDate)).shaped.<>({r=>import r._; _1.map(_=> GroupRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owner_id SqlType(int4) */
    val ownerId: Rep[Int] = column[Int]("owner_id")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column password SqlType(text) */
    val password: Rep[String] = column[String]("password")
    /** Database column creation_date SqlType(timestamp) */
    val creationDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("creation_date")

    /** Foreign key referencing User (database name group_owner_id_fk) */
    lazy val userFk = foreignKey("group_owner_id_fk", ownerId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Group */
  lazy val Group = new TableQuery(tag => new Group(tag))


  /** GetResult implicit for fetching Oauth2InfoRow objects using plain SQL queries */
  implicit def GetResultOauth2InfoRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Int]]): GR[Oauth2InfoRow] = GR{
    prs => import prs._
    Oauth2InfoRow.tupled((<<[Int], <<[String], <<?[String], <<?[Int], <<?[String], <<[Int]))
  }
  /** Table description of table oauth2_info. Objects of this class serve as prototypes for rows in queries. */
  class Oauth2Info(_tableTag: Tag) extends Table[Oauth2InfoRow](_tableTag, "oauth2_info") {
    def * = (id, accessToken, tokenType, expiresIn, refreshToken, userId) <> (Oauth2InfoRow.tupled, Oauth2InfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(accessToken), tokenType, expiresIn, refreshToken, Rep.Some(userId)).shaped.<>({r=>import r._; _1.map(_=> Oauth2InfoRow.tupled((_1.get, _2.get, _3, _4, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column access_token SqlType(text) */
    val accessToken: Rep[String] = column[String]("access_token")
    /** Database column token_type SqlType(text), Default(None) */
    val tokenType: Rep[Option[String]] = column[Option[String]]("token_type", O.Default(None))
    /** Database column expires_in SqlType(int4), Default(None) */
    val expiresIn: Rep[Option[Int]] = column[Option[Int]]("expires_in", O.Default(None))
    /** Database column refresh_token SqlType(text), Default(None) */
    val refreshToken: Rep[Option[String]] = column[Option[String]]("refresh_token", O.Default(None))
    /** Database column user_id SqlType(int4) */
    val userId: Rep[Int] = column[Int]("user_id")

    /** Foreign key referencing User (database name oauth2_info_user_id_fk) */
    lazy val userFk = foreignKey("oauth2_info_user_id_fk", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Oauth2Info */
  lazy val Oauth2Info = new TableQuery(tag => new Oauth2Info(tag))


  /** GetResult implicit for fetching OrderRow objects using plain SQL queries */
  implicit def GetResultOrderRow(implicit e0: GR[Int], e1: GR[Short], e2: GR[java.sql.Timestamp], e3: GR[Option[java.sql.Timestamp]], e4: GR[Option[Float]]): GR[OrderRow] = GR{
    prs => import prs._
    OrderRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Short], <<[java.sql.Timestamp], <<?[java.sql.Timestamp], <<?[Float], <<?[Float]))
  }
  /** Table description of table order. Objects of this class serve as prototypes for rows in queries. */
  class Order(_tableTag: Tag) extends Table[OrderRow](_tableTag, "order") {
    def * = (id, groupId, authorId, placeId, status, creationDate, orderEnd, discount, additionalCost) <> (OrderRow.tupled, OrderRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(groupId), Rep.Some(authorId), Rep.Some(placeId), Rep.Some(status), Rep.Some(creationDate), orderEnd, discount, additionalCost).shaped.<>({r=>import r._; _1.map(_=> OrderRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(int4) */
    val groupId: Rep[Int] = column[Int]("group_id")
    /** Database column author_id SqlType(int4) */
    val authorId: Rep[Int] = column[Int]("author_id")
    /** Database column place_id SqlType(int4) */
    val placeId: Rep[Int] = column[Int]("place_id")
    /** Database column status SqlType(int2) */
    val status: Rep[Short] = column[Short]("status")
    /** Database column creation_date SqlType(timestamp) */
    val creationDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("creation_date")
    /** Database column order_end SqlType(timestamp), Default(None) */
    val orderEnd: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("order_end", O.Default(None))
    /** Database column discount SqlType(float4), Default(None) */
    val discount: Rep[Option[Float]] = column[Option[Float]]("discount", O.Default(None))
    /** Database column additional_cost SqlType(float4), Default(None) */
    val additionalCost: Rep[Option[Float]] = column[Option[Float]]("additional_cost", O.Default(None))

    /** Foreign key referencing Group (database name order_group_id_fk) */
    lazy val groupFk = foreignKey("order_group_id_fk", groupId, Group)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Place (database name order_place_id_fk) */
    lazy val placeFk = foreignKey("order_place_id_fk", placeId, Place)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name order_author_id_fk) */
    lazy val userFk = foreignKey("order_author_id_fk", authorId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Order */
  lazy val Order = new TableQuery(tag => new Order(tag))


  /** GetResult implicit for fetching OrderMessageRow objects using plain SQL queries */
  implicit def GetResultOrderMessageRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[OrderMessageRow] = GR{
    prs => import prs._
    OrderMessageRow.tupled((<<[Int], <<[Int], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table order_message. Objects of this class serve as prototypes for rows in queries. */
  class OrderMessage(_tableTag: Tag) extends Table[OrderMessageRow](_tableTag, "order_message") {
    def * = (id, orderId, message, creationDate) <> (OrderMessageRow.tupled, OrderMessageRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(orderId), Rep.Some(message), Rep.Some(creationDate)).shaped.<>({r=>import r._; _1.map(_=> OrderMessageRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column order_id SqlType(int4) */
    val orderId: Rep[Int] = column[Int]("order_id")
    /** Database column message SqlType(text) */
    val message: Rep[String] = column[String]("message")
    /** Database column creation_date SqlType(timestamp) */
    val creationDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("creation_date")

    /** Foreign key referencing Order (database name order_message_order_id_fk) */
    lazy val orderFk = foreignKey("order_message_order_id_fk", orderId, Order)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table OrderMessage */
  lazy val OrderMessage = new TableQuery(tag => new OrderMessage(tag))


  /** GetResult implicit for fetching PasswordInfoRow objects using plain SQL queries */
  implicit def GetResultPasswordInfoRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]]): GR[PasswordInfoRow] = GR{
    prs => import prs._
    PasswordInfoRow.tupled((<<[Int], <<[String], <<[String], <<?[String], <<?[String], <<[Int]))
  }
  /** Table description of table password_info. Objects of this class serve as prototypes for rows in queries. */
  class PasswordInfo(_tableTag: Tag) extends Table[PasswordInfoRow](_tableTag, "password_info") {
    def * = (id, hasher, password, salt, resetToken, userId) <> (PasswordInfoRow.tupled, PasswordInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hasher), Rep.Some(password), salt, resetToken, Rep.Some(userId)).shaped.<>({r=>import r._; _1.map(_=> PasswordInfoRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column hasher SqlType(text) */
    val hasher: Rep[String] = column[String]("hasher")
    /** Database column password SqlType(text) */
    val password: Rep[String] = column[String]("password")
    /** Database column salt SqlType(text), Default(None) */
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Default(None))
    /** Database column reset_token SqlType(text), Default(None) */
    val resetToken: Rep[Option[String]] = column[Option[String]]("reset_token", O.Default(None))
    /** Database column user_id SqlType(int4) */
    val userId: Rep[Int] = column[Int]("user_id")

    /** Foreign key referencing User (database name password_info_user_id_fk) */
    lazy val userFk = foreignKey("password_info_user_id_fk", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table PasswordInfo */
  lazy val PasswordInfo = new TableQuery(tag => new PasswordInfo(tag))


  /** GetResult implicit for fetching PlaceRow objects using plain SQL queries */
  implicit def GetResultPlaceRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Float]], e4: GR[Boolean]): GR[PlaceRow] = GR{
    prs => import prs._
    PlaceRow.tupled((<<[Int], <<[Int], <<[Int], <<[String], <<?[String], <<?[Float], <<?[Float], <<[Boolean]))
  }
  /** Table description of table place. Objects of this class serve as prototypes for rows in queries. */
  class Place(_tableTag: Tag) extends Table[PlaceRow](_tableTag, "place") {
    def * = (id, groupId, authorId, name, url, minimumOrder, deliveryCost, isDeleted) <> (PlaceRow.tupled, PlaceRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(groupId), Rep.Some(authorId), Rep.Some(name), url, minimumOrder, deliveryCost, Rep.Some(isDeleted)).shaped.<>({r=>import r._; _1.map(_=> PlaceRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(int4) */
    val groupId: Rep[Int] = column[Int]("group_id")
    /** Database column author_id SqlType(int4) */
    val authorId: Rep[Int] = column[Int]("author_id")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column url SqlType(text), Default(None) */
    val url: Rep[Option[String]] = column[Option[String]]("url", O.Default(None))
    /** Database column minimum_order SqlType(float4), Default(None) */
    val minimumOrder: Rep[Option[Float]] = column[Option[Float]]("minimum_order", O.Default(None))
    /** Database column delivery_cost SqlType(float4), Default(None) */
    val deliveryCost: Rep[Option[Float]] = column[Option[Float]]("delivery_cost", O.Default(None))
    /** Database column is_deleted SqlType(bool), Default(false) */
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))

    /** Foreign key referencing Group (database name place_group_id_fk) */
    lazy val groupFk = foreignKey("place_group_id_fk", groupId, Group)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name place_author_id_fk) */
    lazy val userFk = foreignKey("place_author_id_fk", authorId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Place */
  lazy val Place = new TableQuery(tag => new Place(tag))


  /** GetResult implicit for fetching PlaceCommentRow objects using plain SQL queries */
  implicit def GetResultPlaceCommentRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[PlaceCommentRow] = GR{
    prs => import prs._
    PlaceCommentRow.tupled((<<[Int], <<[Int], <<[Int], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table place_comment. Objects of this class serve as prototypes for rows in queries. */
  class PlaceComment(_tableTag: Tag) extends Table[PlaceCommentRow](_tableTag, "place_comment") {
    def * = (id, authorId, placeId, comment, creationDate) <> (PlaceCommentRow.tupled, PlaceCommentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(authorId), Rep.Some(placeId), Rep.Some(comment), Rep.Some(creationDate)).shaped.<>({r=>import r._; _1.map(_=> PlaceCommentRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column author_id SqlType(int4) */
    val authorId: Rep[Int] = column[Int]("author_id")
    /** Database column place_id SqlType(int4) */
    val placeId: Rep[Int] = column[Int]("place_id")
    /** Database column comment SqlType(text) */
    val comment: Rep[String] = column[String]("comment")
    /** Database column creation_date SqlType(timestamp) */
    val creationDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("creation_date")

    /** Foreign key referencing Place (database name place_comment_place_fk) */
    lazy val placeFk = foreignKey("place_comment_place_fk", placeId, Place)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name place_comment_author_fk) */
    lazy val userFk = foreignKey("place_comment_author_fk", authorId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table PlaceComment */
  lazy val PlaceComment = new TableQuery(tag => new PlaceComment(tag))


  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
    def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column hash SqlType(varchar), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255,varying=true))
    /** Database column applied_at SqlType(timestamp) */
    val appliedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("applied_at")
    /** Database column apply_script SqlType(text), Default(None) */
    val applyScript: Rep[Option[String]] = column[Option[String]]("apply_script", O.Default(None))
    /** Database column revert_script SqlType(text), Default(None) */
    val revertScript: Rep[Option[String]] = column[Option[String]]("revert_script", O.Default(None))
    /** Database column state SqlType(varchar), Length(255,true), Default(None) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255,varying=true), O.Default(None))
    /** Database column last_problem SqlType(text), Default(None) */
    val lastProblem: Rep[Option[String]] = column[Option[String]]("last_problem", O.Default(None))
  }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))


  /** GetResult implicit for fetching PrepaidRow objects using plain SQL queries */
  implicit def GetResultPrepaidRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[Float], e3: GR[java.sql.Timestamp], e4: GR[Short]): GR[PrepaidRow] = GR{
    prs => import prs._
    PrepaidRow.tupled((<<[Int], <<[Int], <<[Int], <<?[Int], <<[Float], <<[java.sql.Timestamp], <<[Short]))
  }
  /** Table description of table prepaid. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Prepaid(_tableTag: Tag) extends Table[PrepaidRow](_tableTag, "prepaid") {
    def * = (id, receiverId, payerId, orderId, amount, transactionDate, `type`) <> (PrepaidRow.tupled, PrepaidRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(receiverId), Rep.Some(payerId), orderId, Rep.Some(amount), Rep.Some(transactionDate), Rep.Some(`type`)).shaped.<>({r=>import r._; _1.map(_=> PrepaidRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column receiver_id SqlType(int4) */
    val receiverId: Rep[Int] = column[Int]("receiver_id")
    /** Database column payer_id SqlType(int4) */
    val payerId: Rep[Int] = column[Int]("payer_id")
    /** Database column order_id SqlType(int4), Default(None) */
    val orderId: Rep[Option[Int]] = column[Option[Int]]("order_id", O.Default(None))
    /** Database column amount SqlType(float4) */
    val amount: Rep[Float] = column[Float]("amount")
    /** Database column transaction_date SqlType(timestamp) */
    val transactionDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("transaction_date")
    /** Database column type SqlType(int2)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Short] = column[Short]("type")

    /** Foreign key referencing User (database name prepaid_payer_fk) */
    lazy val userFk1 = foreignKey("prepaid_payer_fk", payerId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing User (database name prepaid_receiver_fk) */
    lazy val userFk2 = foreignKey("prepaid_receiver_fk", receiverId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing UserToOrder (database name prepaid_order_fk) */
    lazy val userToOrderFk = foreignKey("prepaid_order_fk", orderId, UserToOrder)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.SetNull)
  }
  /** Collection-like TableQuery object for table Prepaid */
  lazy val Prepaid = new TableQuery(tag => new Prepaid(tag))


  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Short]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[Short]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "user") {
    def * = (id, providerId, providerKey, firstName, lastName, fullName, email, avatarUrl, privileges) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(providerId), Rep.Some(providerKey), firstName, lastName, fullName, email, avatarUrl, Rep.Some(privileges)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column provider_id SqlType(text) */
    val providerId: Rep[String] = column[String]("provider_id")
    /** Database column provider_key SqlType(text) */
    val providerKey: Rep[String] = column[String]("provider_key")
    /** Database column first_name SqlType(text), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Default(None))
    /** Database column last_name SqlType(text), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Default(None))
    /** Database column full_name SqlType(text), Default(None) */
    val fullName: Rep[Option[String]] = column[Option[String]]("full_name", O.Default(None))
    /** Database column email SqlType(text), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    /** Database column avatar_url SqlType(text), Default(None) */
    val avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url", O.Default(None))
    /** Database column privileges SqlType(int2) */
    val privileges: Rep[Short] = column[Short]("privileges")
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))


  /** GetResult implicit for fetching UserToGroupRow objects using plain SQL queries */
  implicit def GetResultUserToGroupRow(implicit e0: GR[Int]): GR[UserToGroupRow] = GR{
    prs => import prs._
    UserToGroupRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table user_to_group. Objects of this class serve as prototypes for rows in queries. */
  class UserToGroup(_tableTag: Tag) extends Table[UserToGroupRow](_tableTag, "user_to_group") {
    def * = (id, userId, groupId) <> (UserToGroupRow.tupled, UserToGroupRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(groupId)).shaped.<>({r=>import r._; _1.map(_=> UserToGroupRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(int4) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column group_id SqlType(int4) */
    val groupId: Rep[Int] = column[Int]("group_id")

    /** Foreign key referencing Group (database name user_to_group_group_id) */
    lazy val groupFk = foreignKey("user_to_group_group_id", groupId, Group)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_to_group_user_id_fk) */
    lazy val userFk = foreignKey("user_to_group_user_id_fk", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table UserToGroup */
  lazy val UserToGroup = new TableQuery(tag => new UserToGroup(tag))


  /** GetResult implicit for fetching UserToOrderRow objects using plain SQL queries */
  implicit def GetResultUserToOrderRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Float]], e4: GR[Short]): GR[UserToOrderRow] = GR{
    prs => import prs._
    UserToOrderRow.tupled((<<[Int], <<[Int], <<[Int], <<[String], <<?[String], <<?[Float], <<[Short]))
  }
  /** Table description of table user_to_order. Objects of this class serve as prototypes for rows in queries. */
  class UserToOrder(_tableTag: Tag) extends Table[UserToOrderRow](_tableTag, "user_to_order") {
    def * = (id, orderId, userId, subject, additionalInfo, cost, status) <> (UserToOrderRow.tupled, UserToOrderRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(orderId), Rep.Some(userId), Rep.Some(subject), additionalInfo, cost, Rep.Some(status)).shaped.<>({r=>import r._; _1.map(_=> UserToOrderRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column order_id SqlType(int4) */
    val orderId: Rep[Int] = column[Int]("order_id")
    /** Database column user_id SqlType(int4) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column subject SqlType(text) */
    val subject: Rep[String] = column[String]("subject")
    /** Database column additional_info SqlType(text), Default(None) */
    val additionalInfo: Rep[Option[String]] = column[Option[String]]("additional_info", O.Default(None))
    /** Database column cost SqlType(float4), Default(None) */
    val cost: Rep[Option[Float]] = column[Option[Float]]("cost", O.Default(None))
    /** Database column status SqlType(int2) */
    val status: Rep[Short] = column[Short]("status")

    /** Foreign key referencing Order (database name user_to_order_order_id_fk) */
    lazy val orderFk = foreignKey("user_to_order_order_id_fk", orderId, Order)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_to_order_user_id_fk) */
    lazy val userFk = foreignKey("user_to_order_user_id_fk", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table UserToOrder */
  lazy val UserToOrder = new TableQuery(tag => new UserToOrder(tag))


  /** GetResult implicit for fetching UserToVotingRow objects using plain SQL queries */
  implicit def GetResultUserToVotingRow(implicit e0: GR[Int]): GR[UserToVotingRow] = GR{
    prs => import prs._
    UserToVotingRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table user_to_voting. Objects of this class serve as prototypes for rows in queries. */
  class UserToVoting(_tableTag: Tag) extends Table[UserToVotingRow](_tableTag, "user_to_voting") {
    def * = (id, userId, voteId, placeId) <> (UserToVotingRow.tupled, UserToVotingRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(voteId), Rep.Some(placeId)).shaped.<>({r=>import r._; _1.map(_=> UserToVotingRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id SqlType(int4) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column vote_id SqlType(int4) */
    val voteId: Rep[Int] = column[Int]("vote_id")
    /** Database column place_id SqlType(int4) */
    val placeId: Rep[Int] = column[Int]("place_id")

    /** Foreign key referencing Place (database name user_to_voting_place_id_fk) */
    lazy val placeFk = foreignKey("user_to_voting_place_id_fk", placeId, Place)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_to_voting_user_id_fk) */
    lazy val userFk = foreignKey("user_to_voting_user_id_fk", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
    /** Foreign key referencing Voting (database name user_to_voting_vote_id_fk) */
    lazy val votingFk = foreignKey("user_to_voting_vote_id_fk", voteId, Voting)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table UserToVoting */
  lazy val UserToVoting = new TableQuery(tag => new UserToVoting(tag))


  /** GetResult implicit for fetching VotingRow objects using plain SQL queries */
  implicit def GetResultVotingRow(implicit e0: GR[Int], e1: GR[Short], e2: GR[java.sql.Timestamp], e3: GR[Option[java.sql.Timestamp]]): GR[VotingRow] = GR{
    prs => import prs._
    VotingRow.tupled((<<[Int], <<[Int], <<[Int], <<[Short], <<[java.sql.Timestamp], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table voting. Objects of this class serve as prototypes for rows in queries. */
  class Voting(_tableTag: Tag) extends Table[VotingRow](_tableTag, "voting") {
    def * = (id, groupId, authorId, status, creationDate, votingEnd, orderEnd) <> (VotingRow.tupled, VotingRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(groupId), Rep.Some(authorId), Rep.Some(status), Rep.Some(creationDate), votingEnd, orderEnd).shaped.<>({r=>import r._; _1.map(_=> VotingRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(int4) */
    val groupId: Rep[Int] = column[Int]("group_id")
    /** Database column author_id SqlType(int4) */
    val authorId: Rep[Int] = column[Int]("author_id")
    /** Database column status SqlType(int2) */
    val status: Rep[Short] = column[Short]("status")
    /** Database column creation_date SqlType(timestamp) */
    val creationDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("creation_date")
    /** Database column voting_end SqlType(timestamp), Default(None) */
    val votingEnd: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("voting_end", O.Default(None))
    /** Database column order_end SqlType(timestamp), Default(None) */
    val orderEnd: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("order_end", O.Default(None))

    /** Foreign key referencing Group (database name voting_group_id_fk) */
    lazy val groupFk = foreignKey("voting_group_id_fk", groupId, Group)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name voting_author_id_fk) */
    lazy val userFk = foreignKey("voting_author_id_fk", authorId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Voting */
  lazy val Voting = new TableQuery(tag => new Voting(tag))


  /** GetResult implicit for fetching VotingToPlaceRow objects using plain SQL queries */
  implicit def GetResultVotingToPlaceRow(implicit e0: GR[Int]): GR[VotingToPlaceRow] = GR{
    prs => import prs._
    VotingToPlaceRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table voting_to_place. Objects of this class serve as prototypes for rows in queries. */
  class VotingToPlace(_tableTag: Tag) extends Table[VotingToPlaceRow](_tableTag, "voting_to_place") {
    def * = (id, votingId, placeId) <> (VotingToPlaceRow.tupled, VotingToPlaceRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(votingId), Rep.Some(placeId)).shaped.<>({r=>import r._; _1.map(_=> VotingToPlaceRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column voting_id SqlType(int4) */
    val votingId: Rep[Int] = column[Int]("voting_id")
    /** Database column place_id SqlType(int4) */
    val placeId: Rep[Int] = column[Int]("place_id")

    /** Foreign key referencing Place (database name voting_to_place_place_id_fk) */
    lazy val placeFk = foreignKey("voting_to_place_place_id_fk", placeId, Place)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Voting (database name voting_to_place_voting_id_fk) */
    lazy val votingFk = foreignKey("voting_to_place_voting_id_fk", votingId, Voting)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table VotingToPlace */
  lazy val VotingToPlace = new TableQuery(tag => new VotingToPlace(tag))
}
/** Entity class storing rows of table Group
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param ownerId Database column owner_id SqlType(int4)
   *  @param name Database column name SqlType(text)
   *  @param password Database column password SqlType(text)
   *  @param creationDate Database column creation_date SqlType(timestamp) */
  case class GroupRow(id: Int, ownerId: Int, name: String, password: String, creationDate: java.sql.Timestamp)

  /** Entity class storing rows of table Oauth2Info
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param accessToken Database column access_token SqlType(text)
   *  @param tokenType Database column token_type SqlType(text), Default(None)
   *  @param expiresIn Database column expires_in SqlType(int4), Default(None)
   *  @param refreshToken Database column refresh_token SqlType(text), Default(None)
   *  @param userId Database column user_id SqlType(int4) */
  case class Oauth2InfoRow(id: Int, accessToken: String, tokenType: Option[String] = None, expiresIn: Option[Int] = None, refreshToken: Option[String] = None, userId: Int)

  /** Entity class storing rows of table Order
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param groupId Database column group_id SqlType(int4)
   *  @param authorId Database column author_id SqlType(int4)
   *  @param placeId Database column place_id SqlType(int4)
   *  @param status Database column status SqlType(int2)
   *  @param creationDate Database column creation_date SqlType(timestamp)
   *  @param orderEnd Database column order_end SqlType(timestamp), Default(None)
   *  @param discount Database column discount SqlType(float4), Default(None)
   *  @param additionalCost Database column additional_cost SqlType(float4), Default(None) */
  case class OrderRow(id: Int, groupId: Int, authorId: Int, placeId: Int, status: Short, creationDate: java.sql.Timestamp, orderEnd: Option[java.sql.Timestamp] = None, discount: Option[Float] = None, additionalCost: Option[Float] = None)

  /** Entity class storing rows of table OrderMessage
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param orderId Database column order_id SqlType(int4)
   *  @param message Database column message SqlType(text)
   *  @param creationDate Database column creation_date SqlType(timestamp) */
  case class OrderMessageRow(id: Int, orderId: Int, message: String, creationDate: java.sql.Timestamp)

  /** Entity class storing rows of table PasswordInfo
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param hasher Database column hasher SqlType(text)
   *  @param password Database column password SqlType(text)
   *  @param salt Database column salt SqlType(text), Default(None)
   *  @param resetToken Database column reset_token SqlType(text), Default(None)
   *  @param userId Database column user_id SqlType(int4) */
  case class PasswordInfoRow(id: Int, hasher: String, password: String, salt: Option[String] = None, resetToken: Option[String] = None, userId: Int)

  /** Entity class storing rows of table Place
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param groupId Database column group_id SqlType(int4)
   *  @param authorId Database column author_id SqlType(int4)
   *  @param name Database column name SqlType(text)
   *  @param url Database column url SqlType(text), Default(None)
   *  @param minimumOrder Database column minimum_order SqlType(float4), Default(None)
   *  @param deliveryCost Database column delivery_cost SqlType(float4), Default(None)
   *  @param isDeleted Database column is_deleted SqlType(bool), Default(false) */
  case class PlaceRow(id: Int, groupId: Int, authorId: Int, name: String, url: Option[String] = None, minimumOrder: Option[Float] = None, deliveryCost: Option[Float] = None, isDeleted: Boolean = false)

  /** Entity class storing rows of table PlaceComment
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param authorId Database column author_id SqlType(int4)
   *  @param placeId Database column place_id SqlType(int4)
   *  @param comment Database column comment SqlType(text)
   *  @param creationDate Database column creation_date SqlType(timestamp) */
  case class PlaceCommentRow(id: Int, authorId: Int, placeId: Int, comment: String, creationDate: java.sql.Timestamp)

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param hash Database column hash SqlType(varchar), Length(255,true)
   *  @param appliedAt Database column applied_at SqlType(timestamp)
   *  @param applyScript Database column apply_script SqlType(text), Default(None)
   *  @param revertScript Database column revert_script SqlType(text), Default(None)
   *  @param state Database column state SqlType(varchar), Length(255,true), Default(None)
   *  @param lastProblem Database column last_problem SqlType(text), Default(None) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.sql.Timestamp, applyScript: Option[String] = None, revertScript: Option[String] = None, state: Option[String] = None, lastProblem: Option[String] = None)

  /** Entity class storing rows of table Prepaid
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param receiverId Database column receiver_id SqlType(int4)
   *  @param payerId Database column payer_id SqlType(int4)
   *  @param orderId Database column order_id SqlType(int4), Default(None)
   *  @param amount Database column amount SqlType(float4)
   *  @param transactionDate Database column transaction_date SqlType(timestamp)
   *  @param `type` Database column type SqlType(int2) */
  case class PrepaidRow(id: Int, receiverId: Int, payerId: Int, orderId: Option[Int] = None, amount: Float, transactionDate: java.sql.Timestamp, `type`: Short)

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param providerId Database column provider_id SqlType(text)
   *  @param providerKey Database column provider_key SqlType(text)
   *  @param firstName Database column first_name SqlType(text), Default(None)
   *  @param lastName Database column last_name SqlType(text), Default(None)
   *  @param fullName Database column full_name SqlType(text), Default(None)
   *  @param email Database column email SqlType(text), Default(None)
   *  @param avatarUrl Database column avatar_url SqlType(text), Default(None)
   *  @param privileges Database column privileges SqlType(int2) */
  case class UserRow(id: Int, providerId: String, providerKey: String, firstName: Option[String] = None, lastName: Option[String] = None, fullName: Option[String] = None, email: Option[String] = None, avatarUrl: Option[String] = None, privileges: Short)

  /** Entity class storing rows of table UserToGroup
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(int4)
   *  @param groupId Database column group_id SqlType(int4) */
  case class UserToGroupRow(id: Int, userId: Int, groupId: Int)

  /** Entity class storing rows of table UserToOrder
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param orderId Database column order_id SqlType(int4)
   *  @param userId Database column user_id SqlType(int4)
   *  @param subject Database column subject SqlType(text)
   *  @param additionalInfo Database column additional_info SqlType(text), Default(None)
   *  @param cost Database column cost SqlType(float4), Default(None)
   *  @param status Database column status SqlType(int2) */
  case class UserToOrderRow(id: Int, orderId: Int, userId: Int, subject: String, additionalInfo: Option[String] = None, cost: Option[Float] = None, status: Short)

  /** Entity class storing rows of table UserToVoting
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param userId Database column user_id SqlType(int4)
   *  @param voteId Database column vote_id SqlType(int4)
   *  @param placeId Database column place_id SqlType(int4) */
  case class UserToVotingRow(id: Int, userId: Int, voteId: Int, placeId: Int)

  /** Entity class storing rows of table Voting
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param groupId Database column group_id SqlType(int4)
   *  @param authorId Database column author_id SqlType(int4)
   *  @param status Database column status SqlType(int2)
   *  @param creationDate Database column creation_date SqlType(timestamp)
   *  @param votingEnd Database column voting_end SqlType(timestamp), Default(None)
   *  @param orderEnd Database column order_end SqlType(timestamp), Default(None) */
  case class VotingRow(id: Int, groupId: Int, authorId: Int, status: Short, creationDate: java.sql.Timestamp, votingEnd: Option[java.sql.Timestamp] = None, orderEnd: Option[java.sql.Timestamp] = None)

  /** Entity class storing rows of table VotingToPlace
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param votingId Database column voting_id SqlType(int4)
   *  @param placeId Database column place_id SqlType(int4) */
  case class VotingToPlaceRow(id: Int, votingId: Int, placeId: Int)
