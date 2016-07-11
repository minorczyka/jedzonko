package dao

import com.google.inject.{Inject, Singleton}
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class VotingDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def insertVoting(voting: VotingRow) : Future[VotingRow] = {
    db.run((Tables.Voting returning Tables.Voting) += voting)
  }

  def insertVotingPlaces(voting: VotingRow, placesIds: Seq[Int]) : Future[Unit] = {
    val q = Tables.VotingToPlace ++= placesIds.map(p => VotingToPlaceRow(0, voting.id, p))
    db.run(q).map(_ => ())
  }

  def findUserVotings(userId: Int) : Future[Seq[(GroupRow, VotingRow)]] = {
    val q = for {
      (((u, ug), g), v) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Voting on (_._2.id === _.groupId)
      if (u.id === userId)
    } yield(g, v)
    db.run(q.result).map(x => x)
  }

  def findUserVotingsWithAuthors(userId: Int) : Future[Seq[(GroupRow, VotingRow, UserRow)]] = {
    val q = for {
      ((((u, ug), g), v), a) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Voting on (_._2.id === _.groupId) join Tables.User on (_._2.authorId === _.id)
      if (u.id === userId)
    } yield(g, v, a)
    db.run(q.result).map(x => x)
  }

  def find(votingId: Int) : Future[Option[VotingRow]] = {
    val q = Tables.Voting.filter(_.id === votingId)
    db.run(q.result.headOption).map(v => v)
  }

  def findVotingPlaces(votingId: Int) : Future[Seq[PlaceRow]] = {
    val q = for {
      ((v, vp), p) <- Tables.Voting join Tables.VotingToPlace on (_.id === _.votingId) join Tables.Place on (_._2.placeId === _.id)
      if (v.id === votingId)
    } yield(p)
    db.run(q.result).map(p => p)
  }

  def updateUserVotes(votingId: Int, userId: Int, votes: Seq[Int]) : Future[Unit] = {
    val actions = (for {
      _ <- Tables.UserToVoting.filter(v => v.userId === userId && v.voteId === votingId).delete
      _ <- Tables.UserToVoting ++= votes.map(placeId => UserToVotingRow(0, userId, votingId, placeId))
    } yield()).transactionally
    db.run(actions).map(_ => ())
  }

  def findVotes(votingId: Int) : Future[Map[PlaceRow, Seq[UserRow]]] = {
    val q = for {
      ((u, v), p) <- Tables.User join Tables.UserToVoting on (_.id === _.userId) join Tables.Place on (_._2.placeId === _.id)
      if (v.voteId === votingId)
    } yield(p, u)
    db.run(q.result).map(result => result.groupBy(x => x._1).mapValues(seq => seq.map(t => t._2)))
  }

  def updateVoting(votingRow: VotingRow) : Future[VotingRow] = {
    val q = Tables.Voting.filter(_.id === votingRow.id)
    db.run(q.update(votingRow)).map(_ => votingRow)
  }

  def updateVotingPlaces(voting: VotingRow, placesIds: Seq[Int]) : Future[Unit] = {
    val actions = (for {
      _ <- Tables.VotingToPlace.filter(_.votingId === voting.id).delete
      _ <- Tables.VotingToPlace ++= placesIds.map(p => VotingToPlaceRow(0, voting.id, p))
      _ <- Tables.UserToVoting.filter(uv => uv.voteId === voting.id && !(uv.placeId inSetBind placesIds)).delete
    } yield()).transactionally
    db.run(actions)
  }

  def deleteVoting(votingId: Int) : Future[Unit] = {
    val q = Tables.Voting.filter(_.id === votingId)
    db.run(q.delete).map(_ => ())
  }

  def allVotings : Future[Seq[VotingRow]] = {
    db.run(Tables.Voting.result).map(x => x)
  }
}

object VotingDao {
  val votingStarted : Short = 0
  val votingFinished : Short = 1
}
